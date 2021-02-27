package net.kakoen.valheim.save.parser;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.parser.ZPackage;

public class ZPackageTest {
	
	@Test
	public void zpackage_shouldReadStringsAndCharsCorrectly() throws IOException {
		ZPackage zPackage = new ZPackage(new File("src/test/resources/out.bin"));
		Assertions.assertEquals("1", zPackage.readString());
		Assertions.assertEquals("123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals("1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals("12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", zPackage.readString());
		Assertions.assertEquals(10, zPackage.readChar());
		Assertions.assertEquals(280, zPackage.readChar());
	}
	
}
