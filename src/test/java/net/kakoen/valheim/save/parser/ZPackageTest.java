package net.kakoen.valheim.save.parser;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ZPackageTest {
	
	@Test
	public void zpackage_shouldReadValuesCorrectly() throws IOException {
		ZPackage zPackage = new ZPackage(new File("src/test/resources/out.bin"));
		Assertions.assertEquals("1", zPackage.readString());
		Assertions.assertEquals("123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals(10, zPackage.readChar());
		Assertions.assertEquals(280, zPackage.readChar());
		Assertions.assertEquals(10, zPackage.readUInt());
		Assertions.assertEquals(0xFFFFFFFFL, zPackage.readUInt());
	}
	
	@Test
	public void zPackage_shouldWriteValuesCorrectly() throws IOException {
		ZPackage zPackage = new ZPackage();
		zPackage.writeString("1");
		zPackage.writeString("123456789012345678901234567890");
		zPackage.writeString("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		zPackage.writeString("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
		zPackage.writeChar(10);
		zPackage.writeChar(280);
		zPackage.writeUInt(10);
		zPackage.writeUInt(0xFFFFFFFFL);
		byte[] test = zPackage.getBufferAsBytes();
		
		ZPackage zPackageFromFile = new ZPackage(new File("src/test/resources/out.bin"));
		byte[] expected = zPackageFromFile.getBufferAsBytes();
		
		Assertions.assertArrayEquals(expected, test);
		
	}
	
}
