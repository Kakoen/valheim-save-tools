package net.kakoen.valheim.save.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.struct.Quaternion;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;

@Slf4j
public class ZPackage implements AutoCloseable {
	
	private final static int INITIAL_CAPACITY = 1024;
	private final static float EXPAND_FACTOR = 2f;
	
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
		log.info("Expanding capacity from {} to {}", buffer.capacity(), newCapacity);
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
	
	public void writeShort(float value) {
		ensureWritableSpace(4);
		buffer.putFloat(value);
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
		int value = buffer.getInt();
		if(value < 0) {
			return (value & 0x7FFFFFFF) + 0x80000000L;
		} else {
			return value;
		}
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
	public <R> R readFixedSizeObject(long count, Function<ZPackage, R> reader) {
		int position = getPosition();
		if(position + count > buffer.limit()) {
			throw new IllegalStateException("Failed to read fixed size object at " + position + ", end of object is past end of file");
		}
		try {
			return reader.apply(this);
		} finally {
			if(getPosition() != position + count) {
				log.warn("Object at {} with size {} was not fully read, {} bytes remain", position, count, (position + count) - getPosition());
				log.warn("At " + new Throwable().getStackTrace()[1]);
			}
			if(getPosition() > position + count) {
				log.warn("Fixed size object at {} was read past expected size {}, {} extra bytes were read", position, count, getPosition() - position - count);
				log.warn("At " + new Throwable().getStackTrace()[1]);
			}
			setPosition((int) (position + count));
		}
	}
	
	public <R> R readLengthPrefixedObject(Function<ZPackage, R> reader) {
		return readFixedSizeObject(readInt32(), reader);
	}
	
	public void writeLengthPrefixedObject(Consumer<ZPackage> writer) {
		ZPackage zPackage = new ZPackage();
		writer.accept(zPackage);
		ensureWritableSpace(4 + zPackage.getPosition());
		writeInt32(zPackage.getPosition());
		zPackage.writeTo(this);
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
		return new String(readBytes(stringLength));
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
		int length = value;
		Stack<Short> encodedLength = new Stack<>();
		while(length > 0) {
			short l = (short)(length & 0x7F);
			length = length >> 7;
			if(length > 0) {
				l = (short)(l | 0x80);
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
}
