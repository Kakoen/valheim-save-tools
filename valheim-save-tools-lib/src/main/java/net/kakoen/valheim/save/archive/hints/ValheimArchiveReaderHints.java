package net.kakoen.valheim.save.archive.hints;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class ValheimArchiveReaderHints {
	/**
	 * When the version of the save is higher than the last supported version, throw
	 * {@link net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException}
	 */
	private boolean failOnUnsupportedVersion;
}
