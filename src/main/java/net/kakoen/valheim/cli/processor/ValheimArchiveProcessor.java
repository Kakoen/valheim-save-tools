package net.kakoen.valheim.cli.processor;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;

public interface ValheimArchiveProcessor {
	
	/**
	 * Returns on which archive type the processor can be executed.
	 */
	ValheimArchiveType getType();
	
	/**
	 * Returns whether the processor can be applied based on the given options. There's no need to
	 * do an archive type check here.
	 */
	boolean isEnabled(SaveToolsCLIOptions options);
	
	/**
	 * Processes the given archive. At this point you can be certain that the type of the archive and the
	 * enabled state have been checked.
	 */
	void process(ValheimArchive archive, SaveToolsCLIOptions options);
	
}
