package net.kakoen.valheim.cli.processor;

import java.util.LinkedHashSet;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;

@Slf4j
public class RemoveGlobalKeyProcessor implements ValheimArchiveProcessor {
	@Override
	public boolean isEnabled(SaveToolsCLIOptions options) {
		return options.getRemoveGlobalKeys() != null;
	}
	
	@Override
	public void process(ValheimArchive archive, SaveToolsCLIOptions options) {
		ValheimSaveArchive valheimSaveArchive = (ValheimSaveArchive)archive;
		if(valheimSaveArchive.getZones() == null || valheimSaveArchive.getZones().getGlobalKeys() == null) {
			log.info("Global keys not present in archive");
			return;
		}
		for (String removeGlobalKey : options.getRemoveGlobalKeys()) {
			if(removeGlobalKey.equals("all")) {
				log.info("Removed all global keys");
				valheimSaveArchive.getZones().setGlobalKeys(new LinkedHashSet<>());
			}
			else if (valheimSaveArchive.getZones().getGlobalKeys().remove(removeGlobalKey)) {
				log.info("Global key {} removed", removeGlobalKey);
			}
			else {
				log.info("Global key {} was not present", removeGlobalKey);
			}
		}
	}
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
}
