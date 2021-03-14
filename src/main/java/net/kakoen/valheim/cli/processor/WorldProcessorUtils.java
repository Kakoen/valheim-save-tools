package net.kakoen.valheim.cli.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;

@Slf4j
public class WorldProcessorUtils {
	
	private final static int DIGG_STABLE_HASHCODE = StableHashCode.getStableHashCode("digg");
	
	public static boolean isPlayerBuilt(Zdo zdo) {
		return
				zdoHasProperty(zdo, "creator") ||
				zdo.getPrefab() == DIGG_STABLE_HASHCODE;
	}
	
	public static Vector2i getSector(Vector3 position) {
		return new Vector2i((int)Math.floor((position.getX() + 32) / 64), (int)Math.floor((position.getZ() + 32) / 64));
	}
	
	public static boolean isBossStone(Zdo zdo) {
		for(String bossStone : Set.of("BossStone_TheElder", "BossStone_Bonemass", "BossStone_Eikthyr", "BossStone_Yagluth", "BossStone_DragonQueen")) {
			if(zdo.getPrefab() == StableHashCode.getStableHashCode(bossStone)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isShip(Zdo zdo) {
		return zdo.getPrefab() == StableHashCode.getStableHashCode("Raft")
				|| zdo.getPrefab() == StableHashCode.getStableHashCode("VikingShip")
				|| zdo.getPrefab() == StableHashCode.getStableHashCode("Karve")
				|| zdoHasProperty(zdo, "rudder");
	}
	
	public static boolean zdoHasProperty(Zdo zdo, String name) {
		return zdoHasTypedProperty(zdo, name, Zdo::getFloats, Zdo::getFloatsByName)
				|| zdoHasTypedProperty(zdo, name, Zdo::getLongs, Zdo::getLongsByName)
				|| zdoHasTypedProperty(zdo, name, Zdo::getInts, Zdo::getIntsByName)
				|| zdoHasTypedProperty(zdo, name, Zdo::getQuats, Zdo::getQuatsByName)
				|| zdoHasTypedProperty(zdo, name, Zdo::getStrings, Zdo::getStringsByName)
				|| zdoHasTypedProperty(zdo, name, Zdo::getVector3s, Zdo::getVector3sByName);
	}
	
	private static boolean zdoHasTypedProperty(Zdo zdo, String name, Function<Zdo, Map<Integer, ?>> byHash, Function<Zdo, Map<String, ?>> byName) {
		if(byName.apply(zdo) != null && byName.apply(zdo).containsKey(name)) {
			return true;
		}
		if(byHash.apply(zdo) != null && byHash.apply(zdo).containsKey(StableHashCode.getStableHashCode(name))) {
			return true;
		}
		return false;
	}
	
	/**
	 * For debugging purposes
	 */
	public static void logStructuresInSector(List<Zdo> zdoList, int x, int y) {
		Map<String, Integer> countByType = new HashMap<>();
		zdoList.stream()
				.filter(zdo -> zdo.getSector().equals(new Vector2i(x, y)))
				.forEach(zdo -> {
					String prefabName = zdo.getPrefabName() != null ? zdo.getPrefabName() : zdo.getPrefab() + "";
					countByType.compute(prefabName, (k, v) -> {
						return v == null ? 1 : v + 1;
					});
				});
		
		log.info("Zdos in sector {}, {}: {}", x, y, countByType);
	}
}
