package net.kakoen.valheim.cli.processor;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;

@Slf4j
public class AddGlobalKeyProcessor implements ValheimArchiveProcessor {
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
	
	@Override
	public boolean isEnabled(SaveToolsCLIOptions options) {
		return options.getAddGlobalKeys() != null;
	}
	
	@Override
	public void process(ValheimArchive archive, SaveToolsCLIOptions options) {
		ValheimSaveArchive valheimSaveArchive = (ValheimSaveArchive)archive;
		if(valheimSaveArchive.getZones() == null || valheimSaveArchive.getZones().getGlobalKeys() == null) {
			log.info("Global keys not present in archive");
			return;
		}
		for (String globalKeyToAdd : options.getAddGlobalKeys()) {
			if (valheimSaveArchive.getZones().getGlobalKeys().add(globalKeyToAdd)) {
				log.info("Global key {} added", globalKeyToAdd);
			}
			else {
				log.info("Global key {} was already present", globalKeyToAdd);
			}
		}
	}
}
