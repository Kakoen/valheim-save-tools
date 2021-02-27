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
public class ValheimSaveMetadata {
	
	private int worldVersion;
	private String name;
	private String seedName;
	private int seed;
	private long uid;
	private int worldGenVersion;
	
	public ValheimSaveMetadata(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage(file)) {
			int size = zPackage.readInt32();
			worldVersion = zPackage.readInt32();
			name = zPackage.readString();
			seedName = zPackage.readString();
			seed = zPackage.readInt32();
			uid = zPackage.readLong();
			worldGenVersion = zPackage.readInt32();
		}
	}

}
