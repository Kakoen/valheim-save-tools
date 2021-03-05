package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.character.PlayerData;
import net.kakoen.valheim.save.archive.character.WorldPlayerData;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.fch character files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimCharacter {
	
	private final static int TESTED_CHARACTER_VERSION = 33;
	
	private int version;
	private int kills;
	private int deaths;
	private int crafts;
	private int builds;
	private Map<Long, WorldPlayerData> worlds = new HashMap<>();
	private String playerName;
	private long playerID;
	private String startSeed;
	private PlayerData playerData;
	
	public ValheimCharacter(File file) throws IOException {
		try (ZPackage zPackage = new ZPackage(file)) {
			zPackage.readInt32();
			version = zPackage.readInt32();
			if(version > TESTED_CHARACTER_VERSION) {
				log.warn("Character version {} encountered, last tested version was {}", version, TESTED_CHARACTER_VERSION);
			}
			if(version >= 28) {
				kills = zPackage.readInt32();
				deaths = zPackage.readInt32();
				crafts = zPackage.readInt32();
				builds = zPackage.readInt32();
			}
			int numWorlds = zPackage.readInt32();
			for(int i = 0; i < numWorlds; i++) {
				long key = zPackage.readLong();
				WorldPlayerData worldPlayerData = new WorldPlayerData(zPackage, version);
				worlds.put(key, worldPlayerData);
			}
			playerName = zPackage.readString();
			playerID = zPackage.readLong();
			startSeed = zPackage.readString();
			if(zPackage.readBool()) {
				playerData = zPackage.readFixedSizeObject(zPackage.readInt32(), PlayerData::new);
			}
			zPackage.readLengthPrefixedByteArray(); //hash
		}
	}

}
