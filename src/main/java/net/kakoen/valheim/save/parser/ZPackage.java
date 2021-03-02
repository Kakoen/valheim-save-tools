package net.kakoen.valheim.save.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.struct.Quaternion;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;

@Slf4j
public class ZPackage implements AutoCloseable {
	
	private final MappedByteBuffer buffer;
	private final FileChannel inChannel;
	private final RandomAccessFile aFile;
	
	public ZPackage(File inputFile) throws IOException {
		aFile = new RandomAccessFile(inputFile,"r");
		inChannel = aFile.getChannel();
		MappedByteBuffer mappedByteBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
		mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		this.buffer = mappedByteBuffer;
	}
	
	public int readInt32() {
		return buffer.getInt();
	}
	
	public short readShort() {
		return buffer.getShort();
	}
	
	public double readDouble() {
		return buffer.getDouble();
	}
	
	public long readLong() {
		return buffer.getLong();
	}
	
	public long readUInt() {
		return buffer.getInt();
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
	
	public boolean readBool() {
		return buffer.get() > 0;
	}
	
	public byte readByte() {
		return buffer.get();
	}
	
	public Vector2i readVector2i() {
		return new Vector2i(readInt32(), readInt32());
	}
	
	public float readSingle() {
		return buffer.getFloat();
	}
	
	public Vector3 readVector3() {
		return new Vector3(readSingle(), readSingle(), readSingle());
	}
	
	public Quaternion readQuaternion() {
		return new Quaternion(readSingle(), readSingle(), readSingle(), readSingle());
	}
	
	public int readChar() {
		int startPosition = getPosition();
		int first = buffer.get();
		int second = 0;
		int third = 0;
		int fourth = 0;
		if((first & 0x80) == 0) {
			return first;
		}
		if((first & 0xE0) == 0xC0) { //110xxxxx 10xxxxxx
			first = first & 0x1F;
			second = buffer.get() & 0x3F;
			return (first << 6) | second;
		}
		if((first & 0xF0) == 0xC0) { //1110xxxx 10xxxxxx 10xxxxxx
			first = first & 0x0F;
			second = buffer.get() & 0x3F;
			third = buffer.get() & 0x3F;
			return (first << 12) | (second << 6) | third;
		}
		if((first & 0xF0) == 0xF0) { //11110xxx 10xxxxxx 10xxxxxx
			first = first & 0x07;
			second = buffer.get() & 0x3F;
			third = buffer.get() & 0x3F;
			fourth = buffer.get() & 0x3F;
			return (first << 18) | (second << 12) | (third << 6) | fourth;
		}
		throw new IllegalStateException("Unable to read char at position " + startPosition + ", not a valid UTF-8 character");
	}
	
	public String readString() {
		int startPosition = getPosition();
		int stringLength = readStringLength();
		if(getPosition() + stringLength > buffer.limit()) {
			throw new IllegalStateException("Reading string at " + startPosition + " with length " + stringLength + " would exceed the end of the file");
		}
		return new String(readBytes(stringLength));
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
	
	@Override
	public void close() throws IOException {
		if(getPosition() < buffer.limit()) {
			log.warn("File not fully read, {} bytes remain", buffer.limit() - getPosition());
		}
		aFile.close();
	}
	
	public byte[] readByteArray() {
		return readBytes(readInt32());
	}
	
	public Set<String> readStringSet() {
		int count = readInt32();
		Set<String> result = new LinkedHashSet<>();
		for(int i = 0; i < count; i++) {
			result.add(readString());
		}
		return result;
	}
}
