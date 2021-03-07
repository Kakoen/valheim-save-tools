package net.kakoen.valheim.cli.processor;

import net.kakoen.valheim.save.archive.save.Zdo;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;

public class WorldProcessorUtils {
	
	private final static int CREATOR_STABLE_HASHCODE = StableHashCode.getStableHashCode("creator");
	
	public static boolean isPlayerBuilt(Zdo zdo) {
		if(zdo.getLongsByName() != null && zdo.getLongsByName().containsKey("creator")) {
			return true;
		}
		if(zdo.getLongs() != null && zdo.getLongs().containsKey(CREATOR_STABLE_HASHCODE)) {
			return true;
		}
		return false;
	}
	
	public static Vector2i getSector(Vector3 position) {
		return new Vector2i((int)Math.floor((position.getX() + 32) / 64), (int)Math.floor((position.getZ() + 32) / 64));
	}
}
