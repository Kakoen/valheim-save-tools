package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
	
	private int version;
	private List<InventoryItem> inventoryItems;
	
	public Inventory(ZPackage zPackage) {
		version = zPackage.readInt32();
		int count = zPackage.readInt32();
		inventoryItems = new ArrayList<>();
		for(int i = 0; i < count; i++) {
			inventoryItems.add(new InventoryItem(zPackage, version));
		}
	}
}
