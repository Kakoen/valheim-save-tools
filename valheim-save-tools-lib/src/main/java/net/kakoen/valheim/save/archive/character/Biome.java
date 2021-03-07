package net.kakoen.valheim.save.archive.character;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Biome {
	
	public static final Map<Integer, String> BIOMES_BY_ID = Map.of(
			1, "Meadows",
			2, "Swamp",
			4, "Mountain",
			8, "BlackForest",
			16, "Plains",
			32, "AshLands",
			64, "DeepNorth",
			256, "Ocean",
			512, "MistLands"
	);
	
	private int id;
	private String name;
	
}
