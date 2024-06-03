package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.hints.ValheimSaveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

@Slf4j
public class ValheimSaveArchiveTest {
	
	@Test
	public void valheimSaveArchive_shouldCorrectlyLoadSave_forVersion32() throws IOException, ValheimArchiveUnsupportedVersionException {
		ValheimSaveArchive valheimSaveArchive = new ValheimSaveArchive(new File("src/test/resources/TestWorld32.db"), ValheimSaveReaderHints.builder().build());
		Assertions.assertEquals(32, valheimSaveArchive.getMeta().getWorldVersion());
	}

	@Test
	public void valheimSaveArchive_shouldCorrectlyLoadSave_forVersion34() throws IOException, ValheimArchiveUnsupportedVersionException {
		ValheimSaveArchive valheimSaveArchive = new ValheimSaveArchive(new File("src/test/resources/TestWorld34.db"), ValheimSaveReaderHints.builder().build());
		Assertions.assertEquals(34, valheimSaveArchive.getMeta().getWorldVersion());
	}
	
	@Test
	public void valheimSaveArchive_shouldCorrectlySave() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/TestWorld34.db");
		ValheimSaveArchive valheimSaveArchive = new ValheimSaveArchive(inFile, ValheimSaveReaderHints.builder().build());
		File outFile = File.createTempFile("out", ".db");
		valheimSaveArchive.save(outFile);
		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}
	
}
