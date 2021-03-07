package net.kakoen.valheim.cli.processor;

import java.util.ArrayList;
import java.util.HashMap;
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

@Slf4j
public class CleanStructuresProcessor implements ValheimArchiveProcessor {
	
	//TODO Exclude certain "structures" from cleaning:
	// * Structures that have been built less than x days ago
	// * Ships?
	
	private final static Set<Integer> PREFABS_EXCLUDED_FROM_COUNT = Set.of(
			StableHashCode.getStableHashCode("raise"),
			StableHashCode.getStableHashCode("cultivate")
	);
	
	public static final int DEFAULT_STRUCTURES_THRESHOLD = 10;
	
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
		
		Map<Vector2i, List<Zdo>> playerBuiltStructuresBySector = new HashMap<>();
		valheimSaveArchive.getZdoList().stream()
				.filter(CleanStructuresProcessor::countAsPlayerBuilt)
				.forEach(zdo -> playerBuiltStructuresBySector.compute(zdo.getSector(), (k, v) -> {
					if(v == null) {
						v = new ArrayList<>();
					}
					v.add(zdo);
					return v;
				}));
		
		log.info("{} chunks with player built structures found", playerBuiltStructuresBySector.size());
		
		List<Vector2i> chunksToClear = new ArrayList<>();
		
		playerBuiltStructuresBySector.forEach((k, v) -> {
			if(v.size() < structuresThreshold) {
				//Check neighbouring sectors, could be that the structure is on the edge of a chunk
				int count = 0;
				for(int x = k.getX(); x <= k.getX() + 1; x++) {
					for(int y = k.getY() - 1; y <= k.getY() + 1; y++) {
						List<Zdo> structuresInSector = playerBuiltStructuresBySector.get(new Vector2i(x, y));
						count += (structuresInSector == null ? 0 : structuresInSector.size());
					}
				}
				if(count < structuresThreshold) {
					//Clear this sector
					if(options.isVerbose()) {
						log.info("Adding chunk {} with {} player built structures ({} including neighbouring chunks) to the cleanup list", k, v.size(), count);
					}
					chunksToClear.add(k);
				}
			}
		});
		
		log.info("Clearing player built structures from {} chunks", chunksToClear.size());
		
		valheimSaveArchive.setZdoList(valheimSaveArchive.getZdoList().stream()
				.filter(zdo -> {
					boolean keep = !WorldProcessorUtils.isPlayerBuilt(zdo) || !chunksToClear.contains(zdo.getSector());
					if(!keep && options.isVerbose()) {
						log.info("Cleaning {} in chunk {}",
								Optional.ofNullable(ReverseHashcodeLookup.lookup(zdo.getPrefab()))
										.orElse(Integer.toString(zdo.getPrefab())),
								zdo.getSector()
						);
					}
					return keep;
				})
				.collect(Collectors.toList()));
		
		long zdosAfter = valheimSaveArchive.getZdoList().size();
		
		log.info("Cleared {} player built structures", zdosBefore - zdosAfter);
		
	}
	
	public static boolean countAsPlayerBuilt(Zdo zdo) {
		if (!WorldProcessorUtils.isPlayerBuilt(zdo)) {
			return false;
		}
		
		return !PREFABS_EXCLUDED_FROM_COUNT.contains(zdo.getPrefab());
	}
}
