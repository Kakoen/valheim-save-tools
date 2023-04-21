package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Inventory {
	
	private static final int MAX_SUPPORTED_INVENTORY_VERSION = 104;
	
	private int version;
	private List<InventoryItem> inventoryItems;
	
	public Inventory(ZPackage zPackage, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		version = zPackage.readInt32();
		if(version > MAX_SUPPORTED_INVENTORY_VERSION) {
			if(hints.isFailOnUnsupportedVersion()) {
				throw new ValheimArchiveUnsupportedVersionException(Inventory.class, "inventory", version, MAX_SUPPORTED_INVENTORY_VERSION);
			}
			log.warn("Inventory version {} encountered, last tested version is {}", version, MAX_SUPPORTED_INVENTORY_VERSION);
		}
		int count = zPackage.readInt32();
		inventoryItems = new ArrayList<>();
		for(int i = 0; i < count; i++) {
			inventoryItems.add(new InventoryItem(zPackage, version));
		}
	}
	
	public void save(ZPackage writer) {
		writer.writeInt32(MAX_SUPPORTED_INVENTORY_VERSION);
		writer.writeInt32(inventoryItems.size());
		inventoryItems.forEach(inventoryItem -> {
			inventoryItem.save(writer);
		});
	}
}
