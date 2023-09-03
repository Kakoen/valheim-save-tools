package net.kakoen.valheim.cli.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.cli.SaveToolsCLIOptions;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.decode.ReverseHashcodeLookup;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector2s;

@Slf4j
public class CleanStructuresProcessor implements ValheimArchiveProcessor {
	
	//TODO Exclude certain "structures" from cleaning:
	// * Structures that have been built less than x days ago
	// * Ships?
	
	private final static Set<Integer> PREFABS_EXCLUDED_FROM_COUNT = Set.of(
			StableHashCode.getStableHashCode("raise"),
			StableHashCode.getStableHashCode("cultivate"),
			StableHashCode.getStableHashCode("digg"),
			StableHashCode.getStableHashCode("paved_road"),
			StableHashCode.getStableHashCode("mud_road"),
			StableHashCode.getStableHashCode("path"),
			StableHashCode.getStableHashCode("replant") //player built foliage?
	);
	
	/**
	 * Count before a structure is considered a more permanent base
	 */
	public static final int DEFAULT_STRUCTURES_THRESHOLD = 25;
	
	@Override
	public ValheimArchiveType getType() {
		return ValheimArchiveType.DB;
	}
	
	@Override
	public boolean isEnabled(SaveToolsCLIOptions options) {
		return options.isCleanStructures();
	}
	
	@Override
	public void process(ValheimArchive archive, SaveToolsCLIOptions options) {
		ValheimSaveArchive valheimSaveArchive = (ValheimSaveArchive)archive;
		int structuresThreshold = Optional.ofNullable(options.getCleanStructuresThreshold()).orElse(DEFAULT_STRUCTURES_THRESHOLD);
		log.info("Structure count threshold: {}", structuresThreshold);
		
		long zdosBefore = valheimSaveArchive.getZdoList().size();
		
		Map<Vector2s, Set<Zdo>> playerBuiltStructuresBySector = new HashMap<>();
		valheimSaveArchive.getZdoList().stream()
				.filter(WorldProcessorUtils::isPlayerBuilt)
				.forEach(zdo -> playerBuiltStructuresBySector.compute(zdo.getSector(), (k, v) -> {
					if(v == null) v = new HashSet<>();
					v.add(zdo);
					return v;
				}));
		
		Map<Vector2s, Set<Zdo>> playerBuiltStructuresCountedBySector = new HashMap<>();
		playerBuiltStructuresBySector.forEach((sector, structures) -> {
			playerBuiltStructuresCountedBySector.put(sector,
					structures.stream()
							.filter(CleanStructuresProcessor::countAsPlayerBuilt)
							.collect(Collectors.toSet()));
		});
		
		log.info("{} chunks with player built structures found", playerBuiltStructuresBySector.size());
		
		Set<Vector2s> chunksToKeep = new HashSet<>();
		Set<Vector2s> chunksToClear = new HashSet<>();
		
		playerBuiltStructuresBySector.forEach((sector, structures) -> {
			int count = playerBuiltStructuresCountedBySector.get(sector).size();
			if(count < structuresThreshold) {
				//Check neighbouring sectors, could be that the structure is on the edge of a chunk
				count = getNeighbouringSectors(sector, true).stream()
						.map(neighbour -> {
							Set<Zdo> structuresInSector = playerBuiltStructuresCountedBySector.get(neighbour);
							return structuresInSector == null ? 0 : structuresInSector.size();
						})
						.reduce(0, Integer::sum);
			}
			
			if(count >= structuresThreshold) {
				chunksToKeep.add(sector);
			} else {
				chunksToClear.add(sector);
			}
		});
		
		log.info("Rough pass, chunks to keep: {}, chunks to clear: {}", chunksToKeep.size(), chunksToClear.size());
		
		//Keep adding neighbouring zones with structures, for example, roads?
		int pass = 0;
		int lastSize;
		do {
			lastSize = chunksToKeep.size();
			playerBuiltStructuresBySector.forEach((sector, structures) -> {
				if(!chunksToClear.contains(sector)) {
					return;
				}
				getNeighbouringSectors(sector, false).forEach(e -> {
					if(chunksToKeep.contains(e)) {
						chunksToKeep.add(sector);
						chunksToClear.remove(sector);
					}
				});
			});
			if(chunksToKeep.size() > lastSize) {
				log.info("Keep neighbours pass {}: Chunks to keep: {}, chunks to clear: {}", ++pass, chunksToKeep.size(), chunksToClear.size());
			}
		} while(chunksToKeep.size() > lastSize);
		
		//Keep ships
		playerBuiltStructuresBySector.forEach((sector, structures) -> {
			List<Zdo> shipPresent = structures.stream().filter(WorldProcessorUtils::isShip).collect(Collectors.toList());
			if(shipPresent.size() > 0) {
				chunksToKeep.add(sector);
				chunksToClear.remove(sector);
			}
		});
		
		if(options.isVerbose()) {
			log.info("Chunks to keep: {}", chunksToKeep);
			log.info("Chunks to clear: {}", chunksToClear);
		} else {
			log.info("Ships pass: Chunks to keep: {}, chunks to clear: {}", chunksToKeep.size(), chunksToClear.size());
		}
		
		Map<String, Integer> countByType = new HashMap<>();
		valheimSaveArchive.setZdoList(valheimSaveArchive.getZdoList().stream()
				.filter(zdo -> {
					boolean keep = chunksToKeep.contains(zdo.getSector()) || !WorldProcessorUtils.isPlayerBuilt(zdo);
					if(!keep && options.isVerbose()) {
						countByType.compute(
								Optional.ofNullable(ReverseHashcodeLookup.lookup(zdo.getPrefab()))
								.orElse(Integer.toString(zdo.getPrefab())), (k, v) -> v == null ? 1 : v + 1);
					}
					return keep;
				})
				.collect(Collectors.toList()));
		if(options.isVerbose()) {
			log.info("Cleaning {}", countByType);
		}
		
		long zdosAfter = valheimSaveArchive.getZdoList().size();
		
		log.info("Cleared {} player built structures in {} chunks", zdosBefore - zdosAfter, chunksToClear.size());
		
	}
	
	private Set<Vector2s> getNeighbouringSectors(Vector2s sector, boolean includeThisOne) {
		Set<Vector2s> neighbours = new HashSet<>();
		for(short x = -1; x <= 1; x++) {
			for(short y = -1; y <= 1; y++) {
				if(x == 0 && y == 0 && !includeThisOne) {
					continue;
				}
				neighbours.add(new Vector2s((short)(sector.getX() + x), (short)(sector.getY() + y)));
			}
		}
		return neighbours;
	}
	
	public static boolean countAsPlayerBuilt(Zdo zdo) {
		if (!WorldProcessorUtils.isPlayerBuilt(zdo)) {
			return false;
		}
		
		return !PREFABS_EXCLUDED_FROM_COUNT.contains(zdo.getPrefab());
	}
}