package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;

@Slf4j
public class ValheimSaveArchiveTest {
	
	@Test
	public void valheimSave_shouldCorrectlyLoadSave() throws IOException {
		ValheimSaveArchive valheimSave = new ValheimSaveArchive(new File("src/test/resources/DM20022026.db"), ValheimSaveReaderHints.builder().build());
		Assertions.assertEquals(26, valheimSave.getMeta().getWorldVersion());
	}
	
}
