package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class ValheimSaveMetadataTest {
	
	@Test
	public void valheimSave_shouldCorrectlyLoadMetadata() throws IOException {
		ValheimSaveMetadata metadata = new ValheimSaveMetadata(new File("src/test/resources/DM20022026.fwl"));
		Assertions.assertEquals(26, metadata.getWorldVersion());
		Assertions.assertEquals(1, metadata.getWorldGenVersion());
		Assertions.assertEquals("DM20022026", metadata.getName());
		Assertions.assertEquals("wMW3mSL2S0", metadata.getSeedName());
		Assertions.assertEquals(1707192617, metadata.getSeed());
		
	}
	
}
