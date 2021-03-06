package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.fwl save metadata files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimSaveMetadata implements ValheimArchive {
	
	private int worldVersion;
	private String name;
	private String seedName;
	private int seed;
	private long uid;
	private int worldGenVersion;
	
	public ValheimSaveMetadata(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage(file)) {
			zPackage.readLengthPrefixedObject((ZPackage reader) -> {
				worldVersion = reader.readInt32();
				name = reader.readString();
				seedName = reader.readString();
				seed = reader.readInt32();
				uid = reader.readLong();
				worldGenVersion = reader.readInt32();
				return ValheimSaveMetadata.this;
			});
		}
	}
	
	@Override
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeLengthPrefixedObject((ZPackage writer) -> {
				writer.writeInt32(worldVersion);
				writer.writeString(name);
				writer.writeString(seedName);
				writer.writeInt32(seed);
				writer.writeLong(uid);
				writer.writeInt32(worldGenVersion);
			});
			zPackage.writeTo(file);
		}
	}
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.FWL;
	}
	
}
