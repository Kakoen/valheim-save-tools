package net.kakoen.valheim.save.archive.hints;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder
public class ValheimSaveReaderHints extends ValheimArchiveReaderHints {
	/**
	 * Try to resolve prefab names and property keys into their readable names using a reverse StableHashCode lookup
	 */
	private boolean resolveNames;
}
