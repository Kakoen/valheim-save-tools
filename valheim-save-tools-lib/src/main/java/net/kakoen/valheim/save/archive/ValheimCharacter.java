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
import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.fch character files
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimCharacter implements ValheimArchive {
	
	private static final int MAX_SUPPORTED_CHARACTER_VERSION = 37;
	
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
	
	public ValheimCharacter(File file, ValheimArchiveReaderHints hints) throws IOException, ValheimArchiveUnsupportedVersionException {
		try (ZPackage zPackage = new ZPackage(file)) {
			zPackage.readLengthPrefixedObject(reader -> {
				version = reader.readInt32();
				if(version > MAX_SUPPORTED_CHARACTER_VERSION) {
					if(hints.isFailOnUnsupportedVersion()) {
						throw new ValheimArchiveUnsupportedVersionException(ValheimCharacter.class, "character", version, MAX_SUPPORTED_CHARACTER_VERSION);
					}
					log.warn("Character version {} encountered, last tested version was {}", version, MAX_SUPPORTED_CHARACTER_VERSION);
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
					WorldPlayerData worldPlayerData = new WorldPlayerData(reader, version, hints);
					worlds.put(key, worldPlayerData);
				}
				playerName = reader.readString();
				playerID = reader.readLong();
				startSeed = reader.readString();
				if(reader.readBool()) {
					playerData = reader.readLengthPrefixedObject(zPackageReader -> new PlayerData(zPackageReader, hints));
				}
				return ValheimCharacter.this;
			});
			
			zPackage.readLengthPrefixedByteArray(); //hash
		}
	}
	
	@Override
	public void save(File file) throws IOException {
		try(ZPackage zPackage = new ZPackage()) {
			zPackage.writeLengthPrefixedHashedObject(writer -> {
				writer.writeInt32(MAX_SUPPORTED_CHARACTER_VERSION);
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
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.FCH;
	}
	
}
