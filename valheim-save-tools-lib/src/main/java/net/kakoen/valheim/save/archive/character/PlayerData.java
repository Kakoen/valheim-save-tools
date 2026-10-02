package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

/**
 * The player data blob embedded in *.fch files (Player.Save / Player.Load).
 *
 * Versions (Version.PlayerData enum): layout follows the game's gates.
 * Verified against the decompiled Valheim 1.0 assembly and real character
 * files: the inventory follows the guardian power cooldown directly; no
 * position/rotation/spawn-point block is stored in player data (that data
 * lives in the per-world WorldPlayerData of the character file).
 * Version 33 (ChunkedNorth, Valheim 1.0) appends the build-UI byte array
 * and stores known biomes as strings.
 *
 * The version read from the file is preserved when saving.
 */
@Data
@NoArgsConstructor
@Slf4j
public class PlayerData {

	private static final int MAX_SUPPORTED_PLAYERDATA_VERSION = 33;

	// Version.PlayerData constants
	private static final int PD_UNIQUES = 6;
	private static final int PD_MAX_HEALTH = 7;
	private static final int PD_FIRST_SPAWN = 8;
	private static final int PD_TROPHIES = 9;
	private static final int PD_MAX_STAMINA = 10;
	private static final int PD_PLAYER_MODEL = 11;
	private static final int PD_FOOD = 12;
	private static final int PD_BURN_RATE = 13;
	private static final int PD_FOOD2 = 14;
	private static final int PD_STATIONS = 15;
	private static final int PD_FOOD_STAMINA = 16;
	private static final int PD_SKILLS = 17;
	private static final int PD_KNOWN_BIOMES = 18;
	private static final int PD_REMOVE_TUTORIALS = 19;
	private static final int PD_TIME_SINCE_DEATH = 20;
	private static final int PD_RE_ADD_TUTORIALS = 21;
	private static final int PD_KNOWN_TEXTS = 22;
	private static final int PD_GUARDIAN_POWER = 23;
	private static final int PD_GUARDIAN_POWER_COOLDOWN = 24;
	private static final int PD_FOOD_TIME = 25;
	private static final int PD_EITR_STAMINA = 26;
	private static final int PD_MOVED_FIRST_SPAWN = 28;
	private static final int PD_ABANDONED_DN = 31;
	private static final int PD_CHUNKED_NORTH = 33;

	private int version;
	private float maxHealth;
	private float baseHP;
	private float maxStamina;
	private boolean firstSpawn;
	private float timeSinceDeath;
	private String guardianPower;
	private float guardianPowerCooldown;
	private Inventory inventory;
	private Set<String> knownRecipes = new LinkedHashSet<>();
	private Set<String> knownItems = new LinkedHashSet<>();
	private Map<String, Integer> knownStations = new LinkedHashMap<>();
	private Set<String> knownMaterials = new LinkedHashSet<>();
	private Set<String> shownTutorials = new LinkedHashSet<>();
	private Set<String> uniques = new LinkedHashSet<>();
	private Set<String> trophies = new LinkedHashSet<>();
	private Set<String> knownBiomes = new LinkedHashSet<>();
	private List<Integer> legacyKnownBiomes;
	private Map<String, String> knownTexts = new LinkedHashMap<>();
	private String beardItem;
	private String hairItem;
	private Vector3 skinColor;
	private Vector3 hairColor;
	private int modelIndex;
	private List<Food> foods = new ArrayList<>();
	private Map<Integer, Skill> skills = new LinkedHashMap<>();
	private Map<String, String> customData = new LinkedHashMap<>();
	private float stamina;
	private float maxEitr;
	private float eitr;
	private byte[] buildUi;

	private boolean hasBiomeNames() {
		return version >= PD_CHUNKED_NORTH || version == PD_ABANDONED_DN;
	}

	public PlayerData(ZPackage zPackage, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		version = zPackage.readInt32();
		if (version > MAX_SUPPORTED_PLAYERDATA_VERSION) {
			if (hints.isFailOnUnsupportedVersion()) {
				throw new ValheimArchiveUnsupportedVersionException(PlayerData.class, "playerData", version, MAX_SUPPORTED_PLAYERDATA_VERSION);
			}
			log.warn("PlayerData version {} encountered, last tested version is {}", version, MAX_SUPPORTED_PLAYERDATA_VERSION);
		}
		if (version >= PD_MAX_HEALTH) {
			maxHealth = zPackage.readSingle();
		}
		baseHP = zPackage.readSingle();
		if (version >= PD_MAX_STAMINA) {
			maxStamina = zPackage.readSingle();
		}
		if (version >= PD_FIRST_SPAWN && version < PD_MOVED_FIRST_SPAWN) {
			firstSpawn = zPackage.readBool();
		}
		if (version >= PD_TIME_SINCE_DEATH) {
			timeSinceDeath = zPackage.readSingle();
		}
		if (version >= PD_GUARDIAN_POWER) {
			guardianPower = zPackage.readString();
		}
		if (version >= PD_GUARDIAN_POWER_COOLDOWN) {
			guardianPowerCooldown = zPackage.readSingle();
		}
		if (version == PD_FIRST_SPAWN) {
			// two legacy int32s present only in version 8
			zPackage.readInt32();
			zPackage.readInt32();
		}
		inventory = new Inventory(zPackage, hints);
		knownRecipes = zPackage.readStringSet();
		if (version < PD_STATIONS) {
			knownItems = zPackage.readStringSet();
		} else {
			int stationCount = zPackage.readInt32();
			for (int i = 0; i < stationCount; i++) {
				knownStations.put(zPackage.readString(), zPackage.readInt32());
			}
		}
		knownMaterials = zPackage.readStringSet();
		if (version < PD_REMOVE_TUTORIALS || version >= PD_RE_ADD_TUTORIALS) {
			shownTutorials = zPackage.readStringSet();
		}
		if (version >= PD_UNIQUES) {
			uniques = zPackage.readStringSet();
		}
		if (version >= PD_TROPHIES) {
			trophies = zPackage.readStringSet();
		}
		if (hasBiomeNames()) {
			knownBiomes = zPackage.readStringSet();
		} else if (version >= PD_KNOWN_BIOMES) {
			int biomeCount = zPackage.readInt32();
			legacyKnownBiomes = new ArrayList<>();
			for (int i = 0; i < biomeCount; i++) {
				legacyKnownBiomes.add(zPackage.readInt32());
			}
		}
		if (version >= PD_KNOWN_TEXTS) {
			knownTexts = zPackage.readMap();
		}
		if (version >= 4) {
			beardItem = zPackage.readString();
			hairItem = zPackage.readString();
		}
		if (version >= 5) {
			skinColor = zPackage.readVector3();
			hairColor = zPackage.readVector3();
		}
		if (version >= PD_PLAYER_MODEL) {
			modelIndex = zPackage.readInt32();
		}
		if (version >= PD_FOOD) {
			int foodCount = zPackage.readInt32();
			for (int i = 0; i < foodCount; i++) {
				Food food = new Food();
				food.setName(zPackage.readString());
				if (version >= PD_FOOD2) {
					if (version >= PD_FOOD_TIME) {
						food.setTime(zPackage.readSingle());
					} else {
						food.setHealth(zPackage.readSingle());
						if (version >= PD_FOOD_STAMINA) {
							food.setStamina(zPackage.readSingle());
						}
					}
				} else {
					food.setHealth(zPackage.readSingle());
					food.setStamina(zPackage.readSingle());
					zPackage.readSingle();
					zPackage.readSingle();
					zPackage.readSingle();
					zPackage.readSingle();
					if (version >= PD_BURN_RATE) {
						zPackage.readSingle();
					}
				}
				foods.add(food);
			}
		}
		if (version >= PD_SKILLS) {
			int skillsVersion = zPackage.readInt32();
			int skillCount = zPackage.readInt32();
			for (int i = 0; i < skillCount; i++) {
				int skillType = zPackage.readInt32();
				Skill skill = new Skill();
				skill.setLevel(zPackage.readSingle());
				skill.setAccumulator(skillsVersion >= 2 ? zPackage.readSingle() : 0f);
				skills.put(skillType, skill);
			}
		}
		if (version >= PD_EITR_STAMINA) {
			customData = zPackage.readMap();
			stamina = zPackage.readSingle();
			maxEitr = zPackage.readSingle();
			eitr = zPackage.readSingle();
		}
		if (version >= PD_CHUNKED_NORTH || version == PD_ABANDONED_DN) {
			buildUi = zPackage.readLengthPrefixedByteArray();
		}
	}

	public void save(ZPackage writer) {
		writer.writeInt32(version);
		if (version >= PD_MAX_HEALTH) {
			writer.writeSingle(maxHealth);
		}
		writer.writeSingle(baseHP);
		if (version >= PD_MAX_STAMINA) {
			writer.writeSingle(maxStamina);
		}
		if (version >= PD_FIRST_SPAWN && version < PD_MOVED_FIRST_SPAWN) {
			writer.writeBool(firstSpawn);
		}
		if (version >= PD_TIME_SINCE_DEATH) {
			writer.writeSingle(timeSinceDeath);
		}
		if (version >= PD_GUARDIAN_POWER) {
			writer.writeString(guardianPower);
		}
		if (version >= PD_GUARDIAN_POWER_COOLDOWN) {
			writer.writeSingle(guardianPowerCooldown);
		}
		if (version == PD_FIRST_SPAWN) {
			writer.writeInt32(0);
			writer.writeInt32(0);
		}
		inventory.save(writer);
		writer.writeStringSet(knownRecipes);
		if (version < PD_STATIONS) {
			writer.writeStringSet(knownItems);
		} else {
			writer.writeInt32(knownStations.size());
			knownStations.forEach((name, level) -> {
				writer.writeString(name);
				writer.writeInt32(level);
			});
		}
		writer.writeStringSet(knownMaterials);
		if (version < PD_REMOVE_TUTORIALS || version >= PD_RE_ADD_TUTORIALS) {
			writer.writeStringSet(shownTutorials);
		}
		if (version >= PD_UNIQUES) {
			writer.writeStringSet(uniques);
		}
		if (version >= PD_TROPHIES) {
			writer.writeStringSet(trophies);
		}
		if (hasBiomeNames()) {
			writer.writeStringSet(knownBiomes);
		} else if (version >= PD_KNOWN_BIOMES) {
			writer.writeInt32(legacyKnownBiomes.size());
			for (int biome : legacyKnownBiomes) {
				writer.writeInt32(biome);
			}
		}
		if (version >= PD_KNOWN_TEXTS) {
			writer.writeMap(knownTexts);
		}
		if (version >= 4) {
			writer.writeString(beardItem);
			writer.writeString(hairItem);
		}
		if (version >= 5) {
			writer.writeVector3(skinColor);
			writer.writeVector3(hairColor);
		}
		if (version >= PD_PLAYER_MODEL) {
			writer.writeInt32(modelIndex);
		}
		if (version >= PD_FOOD) {
			writer.writeInt32(foods.size());
			for (Food food : foods) {
				writer.writeString(food.getName());
				if (version >= PD_FOOD2) {
					if (version >= PD_FOOD_TIME) {
						writer.writeSingle(food.getTime());
					} else {
						writer.writeSingle(food.getHealth());
						if (version >= PD_FOOD_STAMINA) {
							writer.writeSingle(food.getStamina());
						}
					}
				} else {
					writer.writeSingle(food.getHealth());
					writer.writeSingle(food.getStamina());
					writer.writeSingle(0f);
					writer.writeSingle(0f);
					writer.writeSingle(0f);
					writer.writeSingle(0f);
					if (version >= PD_BURN_RATE) {
						writer.writeSingle(0f);
					}
				}
			}
		}
		if (version >= PD_SKILLS) {
			writer.writeInt32(2); // skills save version
			writer.writeInt32(skills.size());
			skills.forEach((skillType, skill) -> {
				writer.writeInt32(skillType);
				writer.writeSingle(skill.getLevel());
				writer.writeSingle(skill.getAccumulator());
			});
		}
		if (version >= PD_EITR_STAMINA) {
			writer.writeMap(customData);
			writer.writeSingle(stamina);
			writer.writeSingle(maxEitr);
			writer.writeSingle(eitr);
		}
		if (version >= PD_CHUNKED_NORTH || version == PD_ABANDONED_DN) {
			writer.writeLengthPrefixedByteArray(buildUi);
		}
	}
}
