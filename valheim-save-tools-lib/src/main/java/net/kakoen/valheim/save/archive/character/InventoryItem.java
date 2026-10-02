package net.kakoen.valheim.save.archive.character;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.decode.ReverseHashcodeLookup;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector2i;

/**
 * A single inventory item.
 *
 * Inventory version &lt;= 107 uses the legacy verbose format (item name as string).
 * Version &gt;= 108 (Version.Item.Smaller / ChunksNCheats, Valheim 1.0) uses a
 * compact format: durability as int32 (value * 100), grid position and world
 * level as bytes, a flag byte indicating which optional fields are present, and
 * the item identity as the stable hash of its prefab name. Version 109 (and the
 * abandoned 107 Deep North branch) append a cheat-flags byte whose lowest bit
 * marks the item as cheated.
 */
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
	private Map<String, String> customData;
	private int worldLevel;
	private boolean pickedUp;
	private boolean cheated;
	/** Stable hash of the prefab name; item identity in the compact format. 0 = unknown. */
	private int prefabHash;

	// flag bits of the compact format
	private static final int FLAG_PICKED_UP = 1;
	private static final int FLAG_EQUIPPED = 2;
	private static final int FLAG_QUALITY = 4;
	private static final int FLAG_STACK = 8;
	private static final int FLAG_VARIANT = 16;
	private static final int FLAG_CRAFTER = 32;
	private static final int FLAG_PREFAB_HASH = 64;
	private static final int FLAG_CUSTOM_DATA = 128;

	public InventoryItem(ZPackage zPackage, int version) {
		if (version >= Inventory.V_SMALLER) {
			readCompact(zPackage, version);
		} else {
			readLegacy(zPackage, version);
		}
	}

	private void readLegacy(ZPackage zPackage, int version) {
		name = zPackage.readString();
		stack = zPackage.readInt32();
		durability = zPackage.readSingle();
		pos = zPackage.readVector2i();
		equipped = zPackage.readBool();
		quality = version >= 101 ? zPackage.readInt32() : 1;
		variant = version >= 102 ? zPackage.readInt32() : 0;
		crafterId = version >= 103 ? zPackage.readLong() : 0;
		crafterName = version >= 103 ? zPackage.readString() : "";
		customData = version >= 104 ? zPackage.readMap() : new LinkedHashMap<>();
		worldLevel = version >= 105 ? zPackage.readInt32() : 0;
		pickedUp = version >= 106 && zPackage.readBool();
		cheated = version == 107 && zPackage.readBool();
		if (!name.isEmpty()) {
			prefabHash = StableHashCode.getStableHashCode(name);
		}
	}

	private void readCompact(ZPackage zPackage, int version) {
		durability = zPackage.readInt32() * 0.01f;
		pos = new Vector2i((int) zPackage.readByte(), (int) zPackage.readByte());
		worldLevel = zPackage.readByte();
		int flags = zPackage.readByte();
		pickedUp = (flags & FLAG_PICKED_UP) != 0;
		equipped = (flags & FLAG_EQUIPPED) != 0;
		quality = (flags & FLAG_QUALITY) != 0 ? zPackage.readUShort() : 1;
		stack = (flags & FLAG_STACK) != 0 ? zPackage.readUShort() : 1;
		variant = (flags & FLAG_VARIANT) != 0 ? zPackage.readInt32() : 0;
		crafterId = (flags & FLAG_CRAFTER) != 0 ? zPackage.readLong() : 0;
		crafterName = (flags & FLAG_CRAFTER) != 0 ? zPackage.readString() : "";
		prefabHash = (flags & FLAG_PREFAB_HASH) != 0 ? zPackage.readInt32() : 0;
		customData = new LinkedHashMap<>();
		if ((flags & FLAG_CUSTOM_DATA) != 0) {
			// inventory items always use the >= 33 two-byte capable encoding
			int count = zPackage.readNumItems(33);
			for (int i = 0; i < count; i++) {
				customData.put(zPackage.readString(), zPackage.readString());
			}
		}
		// Version.Item.ChunksNCheats (109) or AbandonedDN (107)
		cheated = (version >= 109 || version == 107) && (zPackage.readByte() & 1) != 0;
		if (prefabHash != 0) {
			String resolved = ReverseHashcodeLookup.lookup(prefabHash);
			name = resolved != null ? resolved : "";
		} else {
			name = "";
		}
	}

	public void save(ZPackage writer, int version) {
		if (version >= Inventory.V_SMALLER) {
			writeCompact(writer, version);
		} else {
			writeLegacy(writer, version);
		}
	}

	private void writeLegacy(ZPackage writer, int version) {
		writer.writeString(name);
		writer.writeInt32(stack);
		writer.writeSingle(durability);
		writer.writeVector2i(pos);
		writer.writeBool(equipped);
		if (version >= 101) {
			writer.writeInt32(quality);
		}
		if (version >= 102) {
			writer.writeInt32(variant);
		}
		if (version >= 103) {
			writer.writeLong(crafterId);
			writer.writeString(crafterName);
		}
		if (version >= 104) {
			writer.writeMap(customData);
		}
		if (version >= 105) {
			writer.writeInt32(worldLevel);
		}
		if (version >= 106) {
			writer.writeBool(pickedUp);
		}
		if (version == 107) {
			writer.writeBool(cheated);
		}
	}

	private void writeCompact(ZPackage writer, int version) {
		if (prefabHash == 0 && name != null && !name.isEmpty()) {
			prefabHash = StableHashCode.getStableHashCode(name);
		}
		writer.writeInt32(Math.round(durability * 100f));
		writer.writeByte((byte) pos.getX());
		writer.writeByte((byte) pos.getY());
		writer.writeByte((byte) worldLevel);
		int flags = 0;
		flags |= pickedUp ? FLAG_PICKED_UP : 0;
		flags |= equipped ? FLAG_EQUIPPED : 0;
		flags |= quality != 1 ? FLAG_QUALITY : 0;
		flags |= stack != 1 ? FLAG_STACK : 0;
		flags |= variant != 0 ? FLAG_VARIANT : 0;
		flags |= crafterId != 0 ? FLAG_CRAFTER : 0;
		flags |= prefabHash != 0 ? FLAG_PREFAB_HASH : 0;
		flags |= customData != null && !customData.isEmpty() ? FLAG_CUSTOM_DATA : 0;
		writer.writeByte((byte) flags);
		if ((flags & FLAG_QUALITY) != 0) {
			writer.writeUShort(quality);
		}
		if ((flags & FLAG_STACK) != 0) {
			writer.writeUShort(stack);
		}
		if ((flags & FLAG_VARIANT) != 0) {
			writer.writeInt32(variant);
		}
		if ((flags & FLAG_CRAFTER) != 0) {
			writer.writeLong(crafterId);
			writer.writeString(crafterName);
		}
		if ((flags & FLAG_PREFAB_HASH) != 0) {
			writer.writeInt32(prefabHash);
		}
		if ((flags & FLAG_CUSTOM_DATA) != 0) {
			writer.writeNumItems(customData.size());
			customData.forEach((key, value) -> {
				writer.writeString(key);
				writer.writeString(value);
			});
		}
		if (version >= 109 || version == 107) {
			writer.writeByte((byte) (cheated ? 1 : 0));
		}
	}
}
