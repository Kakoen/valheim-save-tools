package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Player statistics block as written by PlayerProfile.SavePlayerToDisk for
 * character data version 44 (AbandonedDN) and 46+ (DeepNorth / Valheim 1.0).
 *
 * Layout per stat set:
 *   numStatTypes floats, then string->float dictionaries:
 *   knownWorlds, knownWorldKeys, knownCommands,
 *   int32 enemyGroupCount followed by that many enemyStats dictionaries,
 *   itemPickupStats, itemCraftStats, pickableStats, foodEatenStats, piecesPlacedStats
 */
@Data
@NoArgsConstructor
public class PlayerStats {

	private float[] stats;
	private Map<String, Float> knownWorlds = new LinkedHashMap<>();
	private Map<String, Float> knownWorldKeys = new LinkedHashMap<>();
	private Map<String, Float> knownCommands = new LinkedHashMap<>();
	private List<Map<String, Float>> enemyStats = new ArrayList<>();
	private Map<String, Float> itemPickupStats = new LinkedHashMap<>();
	private Map<String, Float> itemCraftStats = new LinkedHashMap<>();
	private Map<String, Float> pickableStats = new LinkedHashMap<>();
	private Map<String, Float> foodEatenStats = new LinkedHashMap<>();
	private Map<String, Float> piecesPlacedStats = new LinkedHashMap<>();

	public PlayerStats(ZPackage reader, int numStatTypes) {
		stats = new float[numStatTypes];
		for (int i = 0; i < numStatTypes; i++) {
			stats[i] = reader.readSingle();
		}
		knownWorlds = readStringFloatMap(reader);
		knownWorldKeys = readStringFloatMap(reader);
		knownCommands = readStringFloatMap(reader);
		int enemyGroupCount = reader.readInt32();
		for (int i = 0; i < enemyGroupCount; i++) {
			enemyStats.add(readStringFloatMap(reader));
		}
		itemPickupStats = readStringFloatMap(reader);
		itemCraftStats = readStringFloatMap(reader);
		pickableStats = readStringFloatMap(reader);
		foodEatenStats = readStringFloatMap(reader);
		piecesPlacedStats = readStringFloatMap(reader);
	}

	public void save(ZPackage writer) {
		for (float stat : stats) {
			writer.writeSingle(stat);
		}
		writeStringFloatMap(writer, knownWorlds);
		writeStringFloatMap(writer, knownWorldKeys);
		writeStringFloatMap(writer, knownCommands);
		writer.writeInt32(enemyStats.size());
		for (Map<String, Float> enemyGroup : enemyStats) {
			writeStringFloatMap(writer, enemyGroup);
		}
		writeStringFloatMap(writer, itemPickupStats);
		writeStringFloatMap(writer, itemCraftStats);
		writeStringFloatMap(writer, pickableStats);
		writeStringFloatMap(writer, foodEatenStats);
		writeStringFloatMap(writer, piecesPlacedStats);
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
}
