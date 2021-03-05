package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValheimCharacterTest {
	
	@Test
	public void valheimCharacter_shouldCorrectlyLoad() throws IOException {
		ValheimCharacter valheimCharacter = new ValheimCharacter(new File("src/test/resources/Kakoentest.fch"));
		Assertions.assertEquals(33, valheimCharacter.getVersion());
		Assertions.assertEquals("Kakoentest", valheimCharacter.getPlayerName());
	}
	
	@Test
	public void valheimCharacter_shouldCorrectlySave() throws IOException {
		File inFile = new File("src/test/resources/Kakoentest.fch");
		ValheimCharacter valheimCharacter = new ValheimCharacter(inFile);
		
		File outFile = File.createTempFile("out", ".fch");
		valheimCharacter.save(outFile);
		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}

}
