package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

@Slf4j
public class ValheimSaveMetadataTest {
	
	@Test
	public void valheimSaveMetadata_shouldCorrectlyLoadMetadata() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/TestWorldKakoen.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		Assertions.assertEquals(32, metadata.getWorldVersion());
		Assertions.assertEquals(2, metadata.getWorldGenVersion());
		Assertions.assertEquals("TestWorldKakoen", metadata.getName());
		Assertions.assertEquals("q6GhJN6FwT", metadata.getSeedName());
		Assertions.assertEquals(517038747, metadata.getSeed());
		Assertions.assertEquals(Set.of(), metadata.getStartingGlobalKeys());
	}
	
	@Test
	public void valheimSaveMetadata_shouldCorrectlySaveMetadata() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/TestWorldKakoen.fwl");
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(inFile, new ValheimArchiveReaderHints());
		File outFile = File.createTempFile("out", ".fwl");
		metadata.save(outFile);
		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}
}