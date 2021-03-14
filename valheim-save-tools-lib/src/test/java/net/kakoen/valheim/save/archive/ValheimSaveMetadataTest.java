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
		File inFile = new File("src/test/resources/DM20022026.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		Assertions.assertEquals(26, metadata.getWorldVersion());
		Assertions.assertEquals(1, metadata.getWorldGenVersion());
		Assertions.assertEquals("DM20022026", metadata.getName());
		Assertions.assertEquals("wMW3mSL2S0", metadata.getSeedName());
		Assertions.assertEquals(1707192617, metadata.getSeed());
	}
	
	@Test
	public void valheimSaveMetadata_shouldCorrectlySaveMetadata() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/DM20022026.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		File outFile = File.createTempFile("out", ".fwl");
		metadata.save(outFile);
		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}
}