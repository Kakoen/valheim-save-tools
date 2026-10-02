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

/**
 * Player inventory (Inventory.Save / Inventory.Load).
 *
 * Versions (Version.Item enum): 101 Quality, 102 Variant, 103 CrafterID,
 * 104 CustomData, 105 WorldLevel, 106 PickedUp, 107 AbandonedDN (old format
 * + cheated bool), 108 Smaller (compact item format, uint16 item count),
 * 109 ChunksNCheats (Valheim 1.0, compact format + per-item cheated flags).
 *
 * The version read from the file is preserved when saving.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Inventory {

	private static final int MAX_SUPPORTED_INVENTORY_VERSION = 109;

	/** First version using the compact item format (Version.Item.Smaller). */
	public static final int V_SMALLER = 108;

	private int version;
	private List<InventoryItem> inventoryItems;

	public Inventory(ZPackage zPackage, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		version = zPackage.readInt32();
		if (version > MAX_SUPPORTED_INVENTORY_VERSION) {
			if (hints.isFailOnUnsupportedVersion()) {
				throw new ValheimArchiveUnsupportedVersionException(Inventory.class, "inventory", version, MAX_SUPPORTED_INVENTORY_VERSION);
			}
			log.warn("Inventory version {} encountered, last tested version is {}", version, MAX_SUPPORTED_INVENTORY_VERSION);
		}
		int count = version >= V_SMALLER ? zPackage.readUShort() : zPackage.readInt32();
		inventoryItems = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			inventoryItems.add(new InventoryItem(zPackage, version));
		}
	}

	public void save(ZPackage writer) {
		writer.writeInt32(version);
		if (version >= V_SMALLER) {
			writer.writeUShort(inventoryItems.size());
		} else {
			writer.writeInt32(inventoryItems.size());
		}
		inventoryItems.forEach(inventoryItem -> inventoryItem.save(writer, version));
	}
}
