package net.kakoen.valheim.save.archive;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
public class ValheimSaveReaderHints {
	/**
	 * Try to resolve prefab names and property keys into their readable names using a reverse StableHashCode lookup
	 */
	private boolean resolveNames;
}
