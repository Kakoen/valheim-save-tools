package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.archive.character.InventoryItem;
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

	@Test
	public void valheim46Character_shouldCorrectlyLoad() throws IOException, ValheimArchiveUnsupportedVersionException {
		ValheimCharacter valheimCharacter = new ValheimCharacter(new File("src/test/resources/Kakoentest46.fch"), new ValheimArchiveReaderHints());
		Assertions.assertEquals(46, valheimCharacter.getVersion());
		Assertions.assertEquals("Kakoentest46", valheimCharacter.getPlayerName());
		Assertions.assertFalse(valheimCharacter.isUsedCheats());
		Assertions.assertNotNull(valheimCharacter.getPlayerStatsSets());
		Assertions.assertEquals(10, valheimCharacter.getPlayerStatsSets().size());
		Assertions.assertEquals(205, valheimCharacter.getPlayerStatsSets().get(0).getStats().length);

		Assertions.assertNotNull(valheimCharacter.getPlayerData());
		Assertions.assertEquals(33, valheimCharacter.getPlayerData().getVersion());
		Assertions.assertEquals(3, valheimCharacter.getPlayerData().getInventory().getInventoryItems().size());

		InventoryItem sword = valheimCharacter.getPlayerData().getInventory().getInventoryItems().get(0);
		Assertions.assertEquals("SwordIron", sword.getName());
		Assertions.assertEquals(3, sword.getQuality());
		Assertions.assertTrue(sword.isEquipped());
		Assertions.assertFalse(sword.isCheated());
		Assertions.assertEquals("Kakoentest", sword.getCrafterName());

		InventoryItem crossbow = valheimCharacter.getPlayerData().getInventory().getInventoryItems().get(2);
		Assertions.assertEquals("CrossbowArbalest", crossbow.getName());
		Assertions.assertEquals(2, crossbow.getQuality());
		Assertions.assertTrue(crossbow.isCheated());
		Assertions.assertEquals(1, crossbow.getCustomData().size());

		// known biomes are strings from playerData version 33 on
		Assertions.assertTrue(valheimCharacter.getPlayerData().getKnownBiomes().contains("Meadows"));
		// build UI blob survived
		Assertions.assertEquals(4, valheimCharacter.getPlayerData().getBuildUi().length);
	}

	@Test
	public void valheim46Character_shouldCorrectlySave() throws IOException, ValheimArchiveUnsupportedVersionException {
		File inFile = new File("src/test/resources/Kakoentest46.fch");
		ValheimCharacter valheimCharacter = new ValheimCharacter(inFile, new ValheimArchiveReaderHints());

		File outFile = File.createTempFile("out46", ".fch");
		valheimCharacter.save(outFile);

		AssertionHelper.assertZPackageEqual(inFile, outFile);
	}

}
