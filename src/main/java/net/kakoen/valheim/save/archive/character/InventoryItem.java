package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector2i;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {
	
	private String name;
	private int stack;
	private float durability;
	private Vector2i pos;
	private boolean equipped;
	private int quality;
	private int variant;
	private long crafterId;
	private String crafterName;
	
	public InventoryItem(ZPackage zPackage, int version) {
		name = zPackage.readString();
		stack = zPackage.readInt32();
		durability = zPackage.readSingle();
		pos = zPackage.readVector2i();
		equipped = zPackage.readBool();
		quality = version >= 101 ? zPackage.readInt32() : 1;
		variant = version >= 102 ? zPackage.readInt32() : 0;
		crafterId = version >= 103 ? zPackage.readLong() : 0;
		crafterName = version >= 103 ? zPackage.readString() : "";
	}
	
}
