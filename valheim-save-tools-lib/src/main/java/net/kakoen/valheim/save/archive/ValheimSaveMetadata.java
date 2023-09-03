package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.fwl save metadata files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimSaveMetadata implements ValheimArchive {
	
	private static final int MAX_SUPPORTED_METADATA_VERSION = ValheimSaveArchive.MAX_SUPPORTED_WORLD_VERSION;
	
	private int worldVersion;
	private String name;
	private String seedName;
	private int seed;
	private long uid;
	private boolean needsDB;
	private int worldGenVersion;
	private Set<String> startingGlobalKeys = new LinkedHashSet<>();
	
	public ValheimSaveMetadata(File file, ValheimArchiveReaderHints hints) throws IOException, ValheimArchiveUnsupportedVersionException {
		try(ZPackage zPackage = new ZPackage(file)) {
			zPackage.readLengthPrefixedObject((ZPackage reader) -> {
				worldVersion = reader.readInt32();
				if(worldVersion > MAX_SUPPORTED_METADATA_VERSION) {
					if(hints.isFailOnUnsupportedVersion()) {
						throw new ValheimArchiveUnsupportedVersionException(ValheimSaveMetadata.class, "metadata", worldVersion, MAX_SUPPORTED_METADATA_VERSION);
					}
					log.warn("Metadata version {} encountered, last tested version was {}", worldVersion, MAX_SUPPORTED_METADATA_VERSION);
				}
				name = reader.readString();
				seedName = reader.readString();
				seed = reader.readInt32();
				uid = reader.readLong();
				worldGenVersion = reader.readInt32();
				needsDB = worldVersion >= 30 && reader.readBool();
				startingGlobalKeys = reader.readStringSet();

				return ValheimSaveMetadata.this;
			});
		}
	}
	
	@Override
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeLengthPrefixedObject((ZPackage writer) -> {
				writer.writeInt32(MAX_SUPPORTED_METADATA_VERSION);
				writer.writeString(name);
				writer.writeString(seedName);
				writer.writeInt32(seed);
				writer.writeLong(uid);
				writer.writeInt32(worldGenVersion);
				writer.writeBool(needsDB);
				writer.writeStringSet(startingGlobalKeys);
			});
			zPackage.writeTo(file);
		}
	}
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.FWL;
	}
	
}
