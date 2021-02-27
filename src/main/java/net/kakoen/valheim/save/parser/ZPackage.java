package net.kakoen.valheim.save.parser;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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
		long position = getPosition();
		try {
			return reader.apply(this);
		} finally {
			if(getPosition() != position + count) {
				log.debug("Object was not fully read at {}, {} bytes remain", position, (position + count) - getPosition());
			}
			setPosition((int)(position + count));
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
		int result = 0;
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
		throw new IllegalStateException("Unable to read char");
	}
	
	public String readString() {
		int stringLength = readStringLength();
		String result = new String(readBytes(stringLength));
		return result;
	}
	
	public int readStringLength() {
		int length = buffer.get();
		int length2 = 0;
		int length3 = 0;
		int length4 = 0;
		int length5 = 0;
		if(length < 0) {
			length = length & 0x7F;
			length2 = buffer.get();
			if(length2 < 0) {
				length2 = length2 & 0x7F;
				length3 = buffer.get();
				if(length3 < 0) {
					length3 = length3 & 0x7F;
					length4 = buffer.get();
					if(length4 < 0) {
						length4 = length4 & 0x7F;
						length5 = buffer.get();
						if(length5 < 0) throw new RuntimeException("Limit of readString reached");
					}
				}
			}
		}
		return (length5 << 28) + (length4 << 21) + (length3 << 14) + (length2 << 7) + length;
	}
	
	@Override
	public void close() throws IOException {
		if(aFile != null) {
			aFile.close();
		}
	}
}
