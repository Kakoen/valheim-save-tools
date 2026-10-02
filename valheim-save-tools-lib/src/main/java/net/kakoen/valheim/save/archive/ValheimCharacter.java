package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.character.PlayerData;
import net.kakoen.valheim.save.archive.character.PlayerStats;
import net.kakoen.valheim.save.archive.character.WorldPlayerData;
import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Reads the *.fch character files.
 *
 * Character data versions (from Valheim's Version.Player enum):
 *   28 Stats (kills/deaths/crafts/builds int32s), 29 MapData, 30 DeathPoint,
 *   34 FoodFix, 35 SeparateFog, 36 CompressedMapData, 37 Mistland,
 *   38 Stats2 (float stat array + usedCheats + dateCreated + post-name stat dicts),
 *   39 Ashlands, 40 FirstSpawn, 41 BogWitch, 42 CallToArms (3 extra stat dicts),
 *   43 Celebration, 44 AbandonedDN (full PlayerStats blocks), 45 Chunked,
 *   46 DeepNorth / Valheim 1.0 (full PlayerStats blocks).
 *
 * The version read from the file is preserved when saving, so no accidental
 * downgrades happen.
 */
@Data
@NoArgsConstructor
@Slf4j
public class ValheimCharacter implements ValheimArchive {

	private static final int MAX_SUPPORTED_CHARACTER_VERSION = 46;

	// Version.Player constants
	private static final int V_STATS = 28;
	private static final int V_STATS2 = 38;
	private static final int V_FIRST_SPAWN = 40;
	private static final int V_CALL_TO_ARMS = 42;
	private static final int V_ABANDONED_DN = 44;
	private static final int V_DEEP_NORTH = 46;

	private int version;
	private int kills;
	private int deaths;
	private int crafts;
	private int builds;
	/** v38-43,45: flat float stat array */
	private float[] legacyStats;
	/** v44 and v46+: full per-set player statistics */
	private int numStatTypes;
	private List<PlayerStats> playerStatsSets;
	private boolean firstSpawn;
	private Map<Long, WorldPlayerData> worlds = new LinkedHashMap<>();
	private String playerName;
	private long playerID;
	private String startSeed;
	private boolean usedCheats;
	private long dateCreated;
	/** v38-45 (except 44): stat dictionaries stored after the name */
	private Map<String, Float> knownWorlds;
	private Map<String, Float> knownWorldKeys;
	private Map<String, Float> knownCommands;
	private Map<String, Float> enemyStats;
	private Map<String, Float> itemPickupStats;
	private Map<String, Float> itemCraftStats;
	private PlayerData playerData;

	private boolean hasFullStatBlocks() {
		return version >= V_DEEP_NORTH || version == V_ABANDONED_DN;
	}

	public ValheimCharacter(File file, ValheimArchiveReaderHints hints) throws IOException, ValheimArchiveUnsupportedVersionException {
		try (ZPackage zPackage = new ZPackage(file)) {
			zPackage.readLengthPrefixedObject(reader -> {
				version = reader.readInt32();
				if (version > MAX_SUPPORTED_CHARACTER_VERSION) {
					if (hints.isFailOnUnsupportedVersion()) {
						throw new ValheimArchiveUnsupportedVersionException(ValheimCharacter.class, "character", version, MAX_SUPPORTED_CHARACTER_VERSION);
					}
					log.warn("Character version {} encountered, last tested version was {}", version, MAX_SUPPORTED_CHARACTER_VERSION);
				}
				if (hasFullStatBlocks()) {
					numStatTypes = reader.readInt32();
					int numStatSets = reader.readInt32();
					playerStatsSets = new ArrayList<>();
					for (int i = 0; i < numStatSets; i++) {
						playerStatsSets.add(new PlayerStats(reader, numStatTypes));
					}
				} else if (version >= V_STATS2) {
					int statCount = reader.readInt32();
					legacyStats = new float[statCount];
					for (int i = 0; i < statCount; i++) {
						legacyStats[i] = reader.readSingle();
					}
				} else if (version >= V_STATS) {
					kills = reader.readInt32();
					deaths = reader.readInt32();
					crafts = reader.readInt32();
					builds = reader.readInt32();
				}
				if (version >= V_FIRST_SPAWN) {
					firstSpawn = reader.readBool();
				}
				int numWorlds = reader.readInt32();
				for (int i = 0; i < numWorlds; i++) {
					long key = reader.readLong();
					WorldPlayerData worldPlayerData = new WorldPlayerData(reader, version, hints);
					worlds.put(key, worldPlayerData);
				}
				playerName = reader.readString();
				playerID = reader.readLong();
				startSeed = reader.readString();
				if (version >= V_STATS2) {
					usedCheats = reader.readBool();
					dateCreated = reader.readLong();
					if (!hasFullStatBlocks()) {
						knownWorlds = readStringFloatMap(reader);
						knownWorldKeys = readStringFloatMap(reader);
						knownCommands = readStringFloatMap(reader);
						if (version >= V_CALL_TO_ARMS) {
							enemyStats = readStringFloatMap(reader);
							itemPickupStats = readStringFloatMap(reader);
							itemCraftStats = readStringFloatMap(reader);
						}
					}
				}
				if (reader.readBool()) {
					playerData = reader.readLengthPrefixedObject(zPackageReader -> new PlayerData(zPackageReader, hints));
				}
				return ValheimCharacter.this;
			});

			zPackage.readLengthPrefixedByteArray(); //hash
		}
	}

	@Override
	public void save(File file) throws IOException {
		try (ZPackage zPackage = new ZPackage()) {
			zPackage.writeLengthPrefixedHashedObject(writer -> {
				writer.writeInt32(version);
				if (hasFullStatBlocks()) {
					writer.writeInt32(numStatTypes);
					writer.writeInt32(playerStatsSets.size());
					for (PlayerStats stats : playerStatsSets) {
						stats.save(writer);
					}
				} else if (version >= V_STATS2) {
					writer.writeInt32(legacyStats.length);
					for (float stat : legacyStats) {
						writer.writeSingle(stat);
					}
				} else if (version >= V_STATS) {
					writer.writeInt32(kills);
					writer.writeInt32(deaths);
					writer.writeInt32(crafts);
					writer.writeInt32(builds);
				}
				if (version >= V_FIRST_SPAWN) {
					writer.writeBool(firstSpawn);
				}
				writer.writeInt32(worlds.size());
				worlds.forEach((key, worldPlayerData) -> {
					writer.writeLong(key);
					worldPlayerData.save(writer);
				});
				writer.writeString(playerName);
				writer.writeLong(playerID);
				writer.writeString(startSeed);
				if (version >= V_STATS2) {
					writer.writeBool(usedCheats);
					writer.writeLong(dateCreated);
					if (!hasFullStatBlocks()) {
						writeStringFloatMap(writer, knownWorlds);
						writeStringFloatMap(writer, knownWorldKeys);
						writeStringFloatMap(writer, knownCommands);
						if (version >= V_CALL_TO_ARMS) {
							writeStringFloatMap(writer, enemyStats);
							writeStringFloatMap(writer, itemPickupStats);
							writeStringFloatMap(writer, itemCraftStats);
						}
					}
				}
				if (playerData != null) {
					writer.writeBool(true);
					writer.writeLengthPrefixedObject(playerData::save);
				} else {
					writer.writeBool(false);
				}
			});

			zPackage.writeTo(file);
		}
	}

	private static Map<String, Float> readStringFloatMap(ZPackage reader) {
		int count = reader.readInt32();
		Map<String, Float> map = new LinkedHashMap<>();
		for (int i = 0; i < count; i++) {
			map.put(reader.readString(), reader.readSingle());
		}
		return map;
	}

	private static void writeStringFloatMap(ZPackage writer, Map<String, Float> map) {
		writer.writeInt32(map.size());
		map.forEach((key, value) -> {
			writer.writeString(key);
			writer.writeSingle(value);
		});
	}

	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.FCH;
	}
}
