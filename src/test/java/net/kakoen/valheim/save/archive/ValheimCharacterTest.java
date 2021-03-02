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

}
