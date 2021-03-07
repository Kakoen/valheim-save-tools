package net.kakoen.valheim.cli.processor;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;

@Slf4j
public class ListGlobalKeysProcessor implements ValheimArchiveProcessor {
	@Override
	public boolean isEnabled(SaveToolsCLIOptions options) {
		return options.isListGlobalKeys();
	}
	
	@Override
	public void process(ValheimArchive archive, SaveToolsCLIOptions options) {
		ValheimSaveArchive valheimSaveArchive = (ValheimSaveArchive)archive;
		if(valheimSaveArchive.getZones() == null || valheimSaveArchive.getZones().getGlobalKeys() == null) {
			log.info("Global keys not present in archive");
			return;
		}
		log.info("Global keys: {}", String.join(", ", valheimSaveArchive.getZones().getGlobalKeys()));
	}
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
}
