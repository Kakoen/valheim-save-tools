package net.kakoen.valheim.save.archive.save;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
	
	private Set<Vector2i> generatedZones = new LinkedHashSet<>();
	private Set<String> globalKeys = new LinkedHashSet<>();
	private boolean locationsGenerated = false;
	private List<PrefabLocation> prefabLocations = new ArrayList<>();
	private int pgwVersion;
	private int locationVersion;
	
	public void load(ZPackage reader, int version) {
		int generatedZonesCount = reader.readInt32();
		for(int i = 0; i < generatedZonesCount; i++) {
			generatedZones.add(reader.readVector2i());
		}
		pgwVersion = reader.readInt32();
		locationVersion = version >= 21 ? reader.readInt32() : 0;
		
		if(version >= 14) {
			int globalKeyCount = reader.readInt32();
			for(int i = 0; i < globalKeyCount; i++) {
				globalKeys.add(reader.readString());
			}
		}
		
		locationsGenerated = reader.readBool();
		
		int prefabLocationsCount = reader.readInt32();
		for(int i = 0; i < prefabLocationsCount; i++) {
			prefabLocations.add(new PrefabLocation(reader));
		}
		
		log.info("Loaded {} locations", prefabLocations.size());
	}
	
	public void save(ZPackage writer) {
		writer.writeInt32(generatedZones.size());
		generatedZones.forEach(writer::writeVector2i);
		
		writer.writeInt32(pgwVersion);
		writer.writeInt32(locationVersion);
		
		writer.writeInt32(globalKeys.size());
		globalKeys.forEach(writer::writeString);
		
		writer.writeBool(locationsGenerated);
		
		writer.writeInt32(prefabLocations.size());
		prefabLocations.forEach(prefabLocation -> prefabLocation.save(writer));
	}
}
