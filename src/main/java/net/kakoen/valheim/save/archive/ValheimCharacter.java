package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
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
	private Map<Long, WorldPlayerData> worlds = new LinkedHashMap<>();
	private String playerName;
	private long playerID;
	private String startSeed;
	private PlayerData playerData;
	
	public ValheimCharacter(File file) throws IOException {
		try (ZPackage zPackage = new ZPackage(file)) {
			zPackage.readLengthPrefixedObject(reader -> {
				version = reader.readInt32();
				if(version > TESTED_CHARACTER_VERSION) {
					log.warn("Character version {} encountered, last tested version was {}", version, TESTED_CHARACTER_VERSION);
				}
				if(version >= 28) {
					kills = reader.readInt32();
					deaths = reader.readInt32();
					crafts = reader.readInt32();
					builds = reader.readInt32();
				}
				int numWorlds = reader.readInt32();
				for(int i = 0; i < numWorlds; i++) {
					long key = reader.readLong();
					WorldPlayerData worldPlayerData = new WorldPlayerData(reader, version);
					worlds.put(key, worldPlayerData);
				}
				playerName = reader.readString();
				playerID = reader.readLong();
				startSeed = reader.readString();
				if(reader.readBool()) {
					playerData = reader.readLengthPrefixedObject(PlayerData::new);
				}
				return ValheimCharacter.this;
			});
			
			zPackage.readLengthPrefixedByteArray(); //hash
		}
	}
	
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeLengthPrefixedHashedObject(writer -> {
				writer.writeInt32(version);
				writer.writeInt32(kills);
				writer.writeInt32(deaths);
				writer.writeInt32(crafts);
				writer.writeInt32(builds);
				
				writer.writeInt32(worlds.size());
				worlds.forEach((key, worldPlayerData) -> {
					writer.writeLong(key);
					worldPlayerData.save(writer);
				});
				
				writer.writeString(playerName);
				writer.writeLong(playerID);
				writer.writeString(startSeed);
				
				if(playerData != null) {
					writer.writeBool(true);
					writer.writeLengthPrefixedObject(playerData::save);
				}
			});
			
			zPackage.writeTo(file);
		}
	}

}
