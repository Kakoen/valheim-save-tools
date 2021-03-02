package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;
import net.kakoen.valheim.save.struct.ZdoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PlayerData {
	
	private static final int TESTED_PLAYERDATA_VERSION = 24;
	private static final int TESTED_SKILLS_VERSION = 2;
	
	private int version;
	private float maxHealth;
	private float health;
	private float stamina;
	private boolean firstSpawn;
	private float timeSinceDeath;
	private String guardianPower;
	private float guardianPowerCooldown;
	private ZdoId zdoId;
	private Inventory inventory;
	private Set<String> knownRecipes;
	private Set<String> knownMaterials;
	private Set<String> shownTutorials;
	private Set<String> uniques;
	private Set<String> trophies;
	private Map<Integer, String> knownBiomes;
	private Map<String, Integer> knownStations;
	private Map<String, String> knownTexts;
	private String beardItem;
	private String hairItem;
	private Vector3 skinColor;
	private Vector3 hairColor;
	private int modelIndex;
	private List<Food> foods;
	
	private int skillsVersion;
	private Map<Integer, Skill> skills;
	
	public PlayerData(ZPackage zPackage) {
		version = zPackage.readInt32();
		if(version > TESTED_PLAYERDATA_VERSION) {
			log.warn("Player data version {} encountered, last tested version is {}", version, TESTED_PLAYERDATA_VERSION);
		}
		if(version >= 7) {
			maxHealth = zPackage.readSingle();
		}
		health = zPackage.readSingle();
		if(version >= 10) {
			stamina = zPackage.readSingle();
		}
		if(version >= 8) {
			firstSpawn = zPackage.readBool();
		}
		if(version >= 20) {
			timeSinceDeath = zPackage.readSingle();
		}
		if(version >= 23) {
			guardianPower = zPackage.readString();
		}
		if(version >= 24) {
			guardianPowerCooldown = zPackage.readSingle();
		}
		if(version == 2) {
			this.zdoId = new ZdoId(zPackage);
		}
		this.inventory = new Inventory(zPackage);
		
		knownRecipes = zPackage.readStringSet();
		
		// Known stations
		int knownStationsCount = zPackage.readInt32();
		knownStations = new LinkedHashMap<>();
		for(int i = 0; i < knownStationsCount; i++) {
			knownStations.put(zPackage.readString(), zPackage.readInt32());
		}
		
		knownMaterials = zPackage.readStringSet();
		shownTutorials = zPackage.readStringSet();
		uniques = zPackage.readStringSet();
		trophies = zPackage.readStringSet();
		
		// Known biomes
		int knownBiomesCount = zPackage.readInt32();
		knownBiomes = new LinkedHashMap<>();
		for(int i = 0; i < knownBiomesCount; i++) {
			int biomeId = zPackage.readInt32();
			String biomeName = Biome.BIOMES_BY_ID.get(biomeId);
			if(biomeName == null) {
				biomeName = Integer.toString(biomeId);
				log.warn("Encountered unknown Biome {}", biomeId);
			}
			knownBiomes.put(biomeId, biomeName);
		}
		
		int knownTextsCount = zPackage.readInt32();
		knownTexts = new LinkedHashMap<>();
		for(int i = 0; i < knownTextsCount; i++) {
			knownTexts.put(zPackage.readString(), zPackage.readString());
		}
		
		beardItem = zPackage.readString();
		hairItem = zPackage.readString();
		skinColor = zPackage.readVector3();
		hairColor = zPackage.readVector3();
		modelIndex = zPackage.readInt32();
		
		int foodsCount = zPackage.readInt32();
		foods = new ArrayList<>();
		for(int i = 0; i < foodsCount; i++) {
			foods.add(new Food(zPackage.readString(), zPackage.readSingle(), zPackage.readSingle()));
		}
		
		readSkills(zPackage);
	
	}
	
	private void readSkills(ZPackage zPackage) {
		skillsVersion = zPackage.readInt32();
		if(skillsVersion > TESTED_SKILLS_VERSION) {
			log.warn("Skills version is {}, last tested version is {}", version, TESTED_SKILLS_VERSION);
		}
		
		int skillCount = zPackage.readInt32();
		skills = new LinkedHashMap<>();
		for(int i = 0; i < skillCount; i++) {
			skills.put(zPackage.readInt32(), new Skill(zPackage.readSingle(), zPackage.readSingle()));
		}
	}
}

