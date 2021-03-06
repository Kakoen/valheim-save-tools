package net.kakoen.valheim.save.processor;

import net.kakoen.valheim.save.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;

public interface ValheimArchiveProcessor {
	
	boolean isEnabled(SaveToolsCLIOptions options);
	void process(ValheimArchive archive, SaveToolsCLIOptions options);
	ValheimArchiveType getType();
	
}
