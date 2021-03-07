package net.kakoen.valheim.cli.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.save.PrefabLocation;
import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.archive.save.Zones;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;

@Slf4j
public class ResetWorldProcessor implements ValheimArchiveProcessor {
	
	public boolean isEnabled(SaveToolsCLIOptions options) {
		return options.isResetWorld();
	}
	
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
	
	@Override
	public void process(ValheimArchive valheimArchive, SaveToolsCLIOptions options) {
		ValheimSaveArchive archive = (ValheimSaveArchive)valheimArchive;
		
		Zones zones = archive.getZones();
		
		log.info("Resetting world...");
		
		int deadZdosCount = archive.getDeadZdos().size();
		archive.getDeadZdos().clear();
		log.info("Removed {} dead zdos", deadZdosCount);
		
		Set<Vector2i> sectorsWithPlayerBuiltStructures = new HashSet<>();
		List<Zdo> playerBuiltStructures = archive.getZdoList().stream()
				.filter(ResetWorldProcessor::isPlayerBuilt)
				.peek(zdo -> {
					sectorsWithPlayerBuiltStructures.add(zdo.getSector());
				})
				.collect(Collectors.toList());
		log.info("Found {} sectors with {} player built structures", sectorsWithPlayerBuiltStructures.size(), playerBuiltStructures.size());
		
		int zonesBefore = zones.getGeneratedZones().size();
		zones.setGeneratedZones(zones.getGeneratedZones().stream()
				.filter(sectorsWithPlayerBuiltStructures::contains)
				.collect(Collectors.toSet()));
		int zonesAfter = zones.getGeneratedZones().size();
		log.info("Removed {} generated zones (before {}, after {})", (zonesBefore - zonesAfter), zonesBefore, zonesAfter);
		
		int zdosBefore = archive.getZdoList().size();
		archive.setZdoList(archive.getZdoList().stream()
				.filter(zdo -> sectorsWithPlayerBuiltStructures.contains(zdo.getSector()))
				.collect(Collectors.toList()));
		int zdosAfter = archive.getZdoList().size();
		log.info("Removed {} game objects (before {}, after {})", (zdosBefore - zdosAfter), zdosBefore, zdosAfter);
		
		int locationsBefore = (int)(zones.getPrefabLocations().stream().filter(PrefabLocation::isGenerated).count());
		zones.getPrefabLocations().forEach(location -> {
			if(!sectorsWithPlayerBuiltStructures.contains(getSector(location.getPosition()))) {
				location.setGenerated(false);
			}
		});
		int locationsAfter = (int)(zones.getPrefabLocations().stream().filter(PrefabLocation::isGenerated).count());
		log.info("Removed {} generated locations (before {}, after {})", (locationsBefore - locationsAfter), locationsBefore, locationsAfter);
		
	}
	
	public static boolean isPlayerBuilt(Zdo zdo) {
		if(zdo.getLongsByName() != null && zdo.getLongsByName().containsKey("creator")) {
			return true;
		}
		if(zdo.getLongs() != null && zdo.getLongs().containsKey(StableHashCode.getStableHashCode("creator"))) {
			return true;
		}
		return false;
	}
	
	public static Vector2i getSector(Vector3 position) {
		return new Vector2i((int)Math.floor((position.getX() + 32) / 64), (int)Math.floor((position.getZ() + 32) / 64));
	}
}
