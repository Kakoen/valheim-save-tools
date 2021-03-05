package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import net.kakoen.valheim.save.parser.ZPackage;

public class AssertionHelper {
	
	public static void assertZPackageEqual(File inFile, File outFile) throws IOException {
		try(ZPackage zPackageIn = new ZPackage(inFile);
			ZPackage zPackageOut = new ZPackage(outFile)) {
			
			Assertions.assertArrayEquals(zPackageIn.getBufferAsBytes(), zPackageOut.getBufferAsBytes());
		}
	}
}
