package net.kakoen.valheim.save.archive.save;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector2i;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Zones {
	
	private List<Vector2i> generatedZones = new ArrayList<>();
	private List<String> globalKeys = new ArrayList<>();
	private boolean locationsGenerated = false;
	private List<PrefabLocation> prefabLocations = new ArrayList<>();
	
	public void load(ZPackage reader, int version) {
		int generatedZonesCount = reader.readInt32();
		for(int i = 0; i < generatedZonesCount; i++) {
			generatedZones.add(reader.readVector2i());
		}
		int pgwVersion = reader.readInt32();
		int locationVersion = version >= 21 ? reader.readInt32() : 0;
		
		if(version >= 14) {
			int globalKeyCount = reader.readInt32();
			for(int i = 0; i < globalKeyCount; i++) {
				globalKeys.add(reader.readString());
			}
		}
		
		locationsGenerated = reader.readBool();
		
		int prefabLocationsCount = reader.readInt32();
		for(int i = 0; i < prefabLocationsCount; i++) {
			prefabLocations.add(new PrefabLocation(reader.readString(), reader.readVector3(), reader.readBool()));
		}
		
		log.info("Loaded {} locations", prefabLocations.size());
	}
}
