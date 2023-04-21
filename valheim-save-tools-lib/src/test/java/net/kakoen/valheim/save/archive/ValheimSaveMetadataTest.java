package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

@Slf4j
public class ValheimSaveMetadataTest {
	
	@Test
	public void valheimSaveMetadata_shouldCorrectlyLoadMetadata() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/Test.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		Assertions.assertEquals(29, metadata.getWorldVersion());
		Assertions.assertEquals(2, metadata.getWorldGenVersion());
		Assertions.assertEquals("Test", metadata.getName());
		Assertions.assertEquals("fGSaPHCYaR", metadata.getSeedName());
		Assertions.assertEquals(555290274, metadata.getSeed());
	}
	
	@Test
	public void valheimSaveMetadata_shouldCorrectlySaveMetadata() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/Test.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		File outFile = File.createTempFile("out", ".fwl");
		metadata.save(outFile);
		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}
}