package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

public class ValheimCharacterTest {
	
	@Test
	public void valheimCharacter_shouldCorrectlyLoad() throws IOException, ValheimArchiveUnsupportedVersionException {
		ValheimCharacter valheimCharacter = new ValheimCharacter(new File("src/test/resources/Kakoentest.fch"), new ValheimArchiveReaderHints());
		Assertions.assertEquals(37, valheimCharacter.getVersion());
		Assertions.assertEquals("Kakoentest", valheimCharacter.getPlayerName());
	}
	
	@Test
	public void valheimCharacter_shouldCorrectlySave() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/Kakoentest.fch");
		ValheimCharacter valheimCharacter = new ValheimCharacter(inFile, new ValheimArchiveReaderHints());
		
		File outFile = File.createTempFile("out", ".fch");
		valheimCharacter.save(outFile);

		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}

}
