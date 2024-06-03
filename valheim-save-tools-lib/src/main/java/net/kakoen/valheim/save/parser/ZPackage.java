package net.kakoen.valheim.save.parser;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.struct.Quaternion;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector2s;
import net.kakoen.valheim.save.struct.Vector3;

@Slf4j
public class ZPackage implements AutoCloseable {
	
	private final static int INITIAL_CAPACITY = 4096;
	private final static float EXPAND_FACTOR = 1.5f;
	
	private ByteBuffer buffer;
	
	private RandomAccessFile inFile;
	private FileChannel fileChannel;
	
	public ZPackage(File inputFile) throws IOException {
		inFile = new RandomAccessFile(inputFile,"r");
		fileChannel = inFile.getChannel();
		buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size()).asReadOnlyBuffer();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public ZPackage() {
		buffer = ByteBuffer.allocate(INITIAL_CAPACITY);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	public ZPackage(byte[] contents) {
		buffer = ByteBuffer.wrap(contents).asReadOnlyBuffer();
		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void ensureWritableSpace(int needed) {
		if(buffer.isReadOnly()) {
			throw new IllegalStateException("Buffer is read-only");
		}
		if (buffer.remaining() >= needed) {
			return;
		}
		int newCapacity = (int) (buffer.capacity() * EXPAND_FACTOR);
		while (newCapacity < (buffer.capacity() + needed)) {
			newCapacity *= EXPAND_FACTOR;
		}
		log.debug("Expanding capacity from {} to {}", buffer.capacity(), newCapacity);
		ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
		expanded.order(buffer.order());
		int position = buffer.position();
		buffer.position(0);
		expanded.put(buffer);
		expanded.position(position);
		buffer = expanded;
	}
	
	public int readInt32() {
		return buffer.getInt();
	}
	
	public void writeInt32(int value) {
		ensureWritableSpace(4);
		buffer.putInt(value);
	}
	
	public short readShort() {
		return buffer.getShort();
	}
	
	public void writeShort(short value) {
		ensureWritableSpace(2);
		buffer.putShort(value);
	}
	
	public double readDouble() {
		return buffer.getDouble();
	}
	
	public void writeDouble(double value) {
		ensureWritableSpace(8);
		buffer.putDouble(value);
	}
	
	public long readLong() {
		return buffer.getLong();
	}
	
	public void writeLong(long value) {
		ensureWritableSpace(8);
		buffer.putLong(value);
	}
	
	public long readUInt() {
		// Similar to readUShort, we now read an int and convert it to a long
		// This is because Java does not have an unsigned int type
		return Integer.toUnsignedLong(buffer.getInt());
	}
	
	public void writeUInt(long value) {
		if(value > 0xFFFFFFFFL) {
			throw new IllegalStateException("Unsigned integer " + value + " cannot be larger than " + 0xFFFFFFFFL);
		}
		int toWrite = (int)(value & 0x7FFFFFFF);
		if(value > 0x7FFFFFFFL) {
			toWrite = (int)(toWrite - 0x80000000L);
		}
		writeInt32(toWrite);
	}
	
	public int getPosition() {
		return buffer.position();
	}
	
	public void setPosition(int position) {
		buffer.position(position);
	}
	
	public byte[] readBytes(int count) {
		byte[] ret = new byte[count];
		
		buffer.get(ret);
		
		return ret;
	}
	
	public void writeBytes(byte[] bytes) {
		ensureWritableSpace(bytes.length);
		buffer.put(bytes);
	}
	
	public void writeBytes(byte[] bytes, int start, int count) {
		ensureWritableSpace(count);
		buffer.put(bytes, start, count);
	}
	
	/**
	 * Supplies a method of reading an object with a prefixed size.
	 * Newer version of a Valheim save might add new attributes, and
	 * this makes reading the save a bit more defensive against new
	 * versions that are not supported yet.
	 */
	public <R> R readFixedSizeObject(long count, ZPackageReaderFunction<ZPackage, R> reader) throws ValheimArchiveUnsupportedVersionException {
		int position = getPosition();
		if(position + count > buffer.limit()) {
			throw new IllegalStateException("Failed to read fixed size object at " + position + ", end of object is past end of file");
		}
		try {
			return reader.apply(this);
		} finally {
			if(getPosition() != position + count) {
				log.warn("Object at {} with size {} was not fully read, {} bytes remain", position, count, (position + count) - getPosition());
				log.warn("At " + getFirstStackTraceElementOutsideClass(new Throwable().getStackTrace()));
			}
			if(getPosition() > position + count) {
				log.warn("Fixed size object at {} was read past expected size {}, {} extra bytes were read", position, count, getPosition() - position - count);
				log.warn("At " + getFirstStackTraceElementOutsideClass(new Throwable().getStackTrace()));
			}
			setPosition((int) (position + count));
		}
	}
	
	private StackTraceElement getFirstStackTraceElementOutsideClass(StackTraceElement[] stackTrace) {
		for(int i = 0; i < stackTrace.length; i++) {
			if(!stackTrace[i].getClassName().equals(ZPackage.class.getCanonicalName())) {
				return stackTrace[i];
			}
		}
		return stackTrace[0];
	}
	
	public <R> R readLengthPrefixedObject(ZPackageReaderFunction<ZPackage, R> reader) throws ValheimArchiveUnsupportedVersionException {
		return readFixedSizeObject(readInt32(), reader);
	}
	
	public void writeLengthPrefixedObject(Consumer<ZPackage> writer) {
		ZPackage zPackage = new ZPackage();
		writer.accept(zPackage);
		ensureWritableSpace(4 + zPackage.getPosition());
		writeInt32(zPackage.getPosition());
		zPackage.writeTo(this);
	}
	
	public void writeLengthPrefixedHashedObject(Consumer<ZPackage> writer) {
		ZPackage zPackage = new ZPackage();
		writer.accept(zPackage);
		ensureWritableSpace(4 + zPackage.getPosition());
		writeInt32(zPackage.getPosition());
		byte[] sha512Hash = new byte[64];
		try {
			sha512Hash = MessageDigest.getInstance("SHA-512").digest(zPackage.getBufferAsBytes());
		} catch(NoSuchAlgorithmException e) {
			log.error("Failed to compute SHA-512 hash", e);
		}
		zPackage.writeTo(this);
		writeLengthPrefixedByteArray(sha512Hash);
	}
	
	private void writeTo(ZPackage zPackage) {
		int size = inFile != null ? buffer.capacity() : buffer.position();
		zPackage.writeBytes(buffer.array(), 0, size);
	}
	
	public boolean readBool() {
		return buffer.get() > 0;
	}
	
	public void writeBool(boolean value) {
		writeByte((byte)(value ? 1 : 0));
	}
	
	public byte readByte() {
		return buffer.get();
	}
	
	public void writeByte(byte value) {
		ensureWritableSpace(1);
		buffer.put(value);
	}
	
	public void writeUByte(short value) {
		if(value > 0xFF) {
			throw new IllegalStateException("Unsigned byte " + value + " cannot be larger than " + 0xFF);
		}
		byte toWrite = (byte)(value & 0x7F);
		if(value > 0x7F) {
			toWrite = (byte)(toWrite - 0x80);
		}
		writeByte(toWrite);
	}

	public Vector2s readVector2s() {
		return new Vector2s(readShort(), readShort());
	}

	public void writeVector2s(Vector2s value) {
		writeShort(value.getX());
		writeShort(value.getY());
	}
	
	public Vector2i readVector2i() {
		return new Vector2i(readInt32(), readInt32());
	}
	
	public void writeVector2i(Vector2i value) {
		writeInt32(value.getX());
		writeInt32(value.getY());
	}
	
	public float readSingle() {
		return buffer.getFloat();
	}
	
	public void writeSingle(float value) {
		ensureWritableSpace(4);
		buffer.putFloat(value);
	}
	
	public Vector3 readVector3() {
		return new Vector3(readSingle(), readSingle(), readSingle());
	}
	
	public void writeVector3(Vector3 value) {
		writeSingle(value.getX());
		writeSingle(value.getY());
		writeSingle(value.getZ());
	}
	
	public Quaternion readQuaternion() {
		return new Quaternion(readSingle(), readSingle(), readSingle(), readSingle());
	}
	
	public void writeQuaternion(Quaternion value) {
		writeSingle(value.getX());
		writeSingle(value.getY());
		writeSingle(value.getZ());
		writeSingle(value.getW());
	}
	
	public int readChar() {
		int startPosition = getPosition();
		int first = buffer.get();
		int second = 0;
		int third = 0;
		int fourth = 0;
		if((first & 0x80) == 0) { //0xxxxxxx
			return first;
		}
		if((first & 0xE0) == 0xC0) { //110xxxxx 10xxxxxx
			first = first & 0x1F;
			second = buffer.get() & 0x3F;
			return (first << 6) | second;
		}
		if((first & 0xF0) == 0xE0) { //1110xxxx 10xxxxxx 10xxxxxx
			first = first & 0x0F;
			second = buffer.get() & 0x3F;
			third = buffer.get() & 0x3F;
			return (first << 12) | (second << 6) | third;
		}
		if((first & 0xF0) == 0xF0) { //11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
			first = first & 0x07;
			second = buffer.get() & 0x3F;
			third = buffer.get() & 0x3F;
			fourth = buffer.get() & 0x3F;
			return (first << 18) | (second << 12) | (third << 6) | fourth;
		}
		throw new IllegalStateException("Unable to read char at position " + startPosition + ", not a valid UTF-8 character");
	}
	
	public void writeChar(int value) {
		if(value <= 0x7F) { // 7 bits
			writeUByte((short)value);
			return;
		}
		if(value <= 0x7FF) { // 11 bits
			writeUByte((short)((value >> 6) | 0xC0));
			writeUByte((short)((value & 0x3F) | 0x80));
			return;
		}
		if(value <= 0xFFFF) { // 16 bits
			writeUByte((short)((value >> 12) & 0x0F | 0xE0));
			writeUByte((short)((value >> 6) & 0x3F | 0x80));
			writeUByte((short)((value & 0x3F) | 0x80));
			return;
		}
		if(value <= 0x1FFFFF) { // 21 bits
			writeUByte((short)((value >> 18) & 0x07 | 0xF0));
			writeUByte((short)((value >> 12) & 0x3F | 0x80));
			writeUByte((short)((value >> 6) & 0x3F | 0x80));
			writeUByte((short)((value & 0x3F) | 0x80));
			return;
		}
		throw new IllegalStateException("writeChar: UTF-8 Characters > " + 0x1FFFFF + " are not supported");
	}
	
	public String readString() {
		int startPosition = getPosition();
		int stringLength = readStringLength();
		if(getPosition() + stringLength > buffer.limit()) {
			throw new IllegalStateException("Reading string at " + startPosition + " with length " + stringLength + " would exceed the end of the file");
		}
		return new String(readBytes(stringLength), StandardCharsets.UTF_8);
	}
	
	public void writeString(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		writeStringLength(bytes.length);
		writeBytes(bytes);
	}
	
	public int readStringLength() {
		int startPosition = getPosition();
		Stack<Integer> lengthStack = new Stack<>();
		
		while(true) {
			byte length = buffer.get();
			lengthStack.push(length & 0x7F);
			if(length >= 0) {
				break;
			}
			if(lengthStack.size() > 4) {
				throw new IllegalStateException("String length cannot be read at position " + startPosition);
			}
		}
		
		int result = 0;
		while(lengthStack.size() > 0) {
			result = (result << 7) + lengthStack.pop();
		}
		return result;
	}
	
	public void writeStringLength(int value) {
		if(value == 0) {
			writeUByte((short)0);
			return;
		}
		
		int length = value;
		Stack<Short> encodedLength = new Stack<>();
		
		while(length > 0) {
			short l = (short) (length & 0x7F);
			length = length >> 7;
			if (length > 0) {
				l = (short) (l | 0x80);
			}
			encodedLength.push(l);
		}
		encodedLength.forEach(this::writeUByte);
	}
	
	public byte[] readLengthPrefixedByteArray() {
		return readBytes(readInt32());
	}
	
	public void writeLengthPrefixedByteArray(byte[] bytes) {
		writeInt32(bytes.length);
		writeBytes(bytes);
	}
	
	public Set<String> readStringSet() {
		int count = readInt32();
		Set<String> result = new LinkedHashSet<>();
		for(int i = 0; i < count; i++) {
			result.add(readString());
		}
		return result;
	}
	
	public void writeStringSet(Set<String> strings) {
		writeInt32(strings.size());
		strings.forEach(this::writeString);
	}
	
	public byte[] getBufferAsBytes() {
		int size = inFile != null ? buffer.capacity() : getPosition();
		byte[] dest = new byte[size];
		buffer.position(0);
		buffer.get(dest, 0, size);
		return dest;
	}
	
	@Override
	public void close() throws IOException {
		if(inFile != null) {
			if(getPosition() < buffer.limit()) {
				log.warn("File not fully read, {} bytes remain", buffer.limit() - getPosition());
			}
			inFile.close();
		}
		if(fileChannel != null && fileChannel.isOpen()) {
			fileChannel.close();
		}
		inFile = null;
		fileChannel = null;
		buffer = null;
	}
	
	public void writeTo(File file) throws IOException {
		try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			int size = inFile != null ? buffer.capacity() : buffer.position();
			fileOutputStream.write(buffer.array(), 0, size);
			log.info("Wrote {} bytes to {}", size, file.getAbsolutePath());
		}
	}

	public void writeTo(OutputStream os) throws IOException {
		int size = inFile != null ? buffer.capacity() : buffer.position();
		os.write(buffer.array(), 0, size);
	}

	@SneakyThrows
	public ZPackage readCompressedPackage() {
		byte[] compressedPackageData = readLengthPrefixedByteArray();
		return new ZPackage(new GZIPInputStream(new ByteArrayInputStream(compressedPackageData)).readAllBytes());
    }

	@SneakyThrows
	public void writeCompressedPackage(Consumer<ZPackage> packageBuilder) {
		ZPackage newPackage = new ZPackage();
		packageBuilder.accept(newPackage);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
			newPackage.writeTo(gzos);
		}
		baos.close();
		writeLengthPrefixedByteArray(baos.toByteArray());
	}

	public Map<String, String> readMap() {
		int count = readInt32();
		Map<String, String> result = new LinkedHashMap<>();
		for (int i = 0; i < count; i++) {
			result.put(readString(), readString());
		}
		return result;
	}

	public void writeMap(Map<String, String> map) {
		writeInt32(map.size());
		map.entrySet().forEach(entry -> {
			writeString(entry.getKey());
			writeString(entry.getValue());
		});
	}

    public int readUShort() {
		return Short.toUnsignedInt(readShort());
    }

	public void writeUShort(int value) {
		short toWrite = (short)(value & 0x7FFF);
		if(value > 0x7FFF) {
			toWrite = (short)(toWrite - 0x8000);
		}
		writeShort(toWrite);
	}

	public int readNumItems(int worldVersion) {
		if (worldVersion < 33) {
			return readChar();
		}
		int num = readByte();
		if ((num & 128) != 0) {
			num = ((num & 127) << 8) | readByte();
		}
		return num;
	}

	public void writeNumItems(int numItems) {
		if (numItems < 128) {
			writeByte((byte) numItems);
		} else {
			writeByte((byte) ((numItems >> 8) | 128));
			writeByte((byte) (numItems & 255));
		}
	}
}
