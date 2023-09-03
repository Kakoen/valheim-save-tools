package net.kakoen.valheim.cli.processor;

import java.util.Collection;
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
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector2s;
import net.kakoen.valheim.save.struct.ZdoId;

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
		
		Set<Vector2s> keepSectors = new HashSet<>();
		keepSectors.addAll(getSectorsWithPlayerBuiltStructures(archive.getZdoList()));
		keepSectors.addAll(getSectorsWithBossStones(archive.getZdoList()));
		
		int zonesBefore = zones.getGeneratedZones().size();
		zones.setGeneratedZones(zones.getGeneratedZones().stream()
				.filter(keepSectors::contains)
				.collect(Collectors.toSet()));
		int zonesAfter = zones.getGeneratedZones().size();
		log.info("Removed {} generated zones (before {}, after {})", (zonesBefore - zonesAfter), zonesBefore, zonesAfter);
		
		int zdosBefore = archive.getZdoList().size();
		archive.setZdoList(archive.getZdoList().stream()
				.filter(zdo -> keepSectors.contains(zdo.getSector()))
				.collect(Collectors.toList()));
		int zdosAfter = archive.getZdoList().size();
		log.info("Removed {} game objects (before {}, after {})", (zdosBefore - zdosAfter), zdosBefore, zdosAfter);
		
		int locationsBefore = (int)(zones.getPrefabLocations().stream().filter(PrefabLocation::isGenerated).count());
		zones.getPrefabLocations().forEach(location -> {
			if(!keepSectors.contains(WorldProcessorUtils.getSector(location.getPosition()))) {
				location.setGenerated(false);
			}
		});
		int locationsAfter = (int)(zones.getPrefabLocations().stream().filter(PrefabLocation::isGenerated).count());
		log.info("Removed {} generated locations (before {}, after {})", (locationsBefore - locationsAfter), locationsBefore, locationsAfter);
		
//		int deadZdosBefore = archive.getDeadZdos().size();
//		Set<Long> zdoIds = archive.getZdoList().stream().map(Zdo::getUid).map(ZdoId::getId).collect(Collectors.toSet());
//		archive.setDeadZdos(archive.getDeadZdos().stream()
//				.filter(deadZdo -> zdoIds.contains(deadZdo.getUid().getId()))
//				.collect(Collectors.toList()));
//		int deadZdosAfter = archive.getDeadZdos().size();
//		log.info("Removed {} dead zdos (before {}, after {})", (deadZdosBefore - deadZdosAfter), deadZdosBefore, deadZdosAfter);
	}
	
	private Collection<? extends Vector2s> getSectorsWithBossStones(List<Zdo> zdoList) {
		Set<Vector2s> sectorsWithBossStones = new HashSet<>();
		zdoList.stream()
				.filter(WorldProcessorUtils::isBossStone)
				.forEach(zdo -> sectorsWithBossStones.add(zdo.getSector()));
		return sectorsWithBossStones;
	}
	
	private Collection<Vector2s> getSectorsWithPlayerBuiltStructures(List<Zdo> zdoList) {
		Set<Vector2s> sectorsWithPlayerBuiltStructures = new HashSet<>();
		List<Zdo> playerBuiltStructures = zdoList.stream()
				.filter(WorldProcessorUtils::isPlayerBuilt)
				.peek(zdo -> {
					sectorsWithPlayerBuiltStructures.add(zdo.getSector());
				})
				.collect(Collectors.toList());
		log.info("Found {} sectors with {} player built structures", sectorsWithPlayerBuiltStructures.size(), playerBuiltStructures.size());
		return sectorsWithPlayerBuiltStructures;
	}
	
}
