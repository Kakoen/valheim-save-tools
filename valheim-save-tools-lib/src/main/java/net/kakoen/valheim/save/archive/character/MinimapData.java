package net.kakoen.valheim.save.archive.character;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;

/**
 * Minimap data (Minimap.GetMapData / SetMapData).
 *
 * Map versions (Version.Map enum): 2 Pins, 3 PinsChecked, 4 VisibleOnMap,
 * 5 NewExplore, 6 PinsOwnerID, 7 Compressed, 8 PinsAuthor (Valheim 1.0).
 *
 * The version read from the file is preserved when saving.
 *
 * The raw serialized bytes are kept and re-emitted verbatim when the parsed
 * content is unchanged, so that files pass through byte-identically even when
 * the game's gzip stream was produced by a different deflate implementation
 * than this JVM's.
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinimapData {

	private static final int MAX_SUPPORTED_MAP_VERSION = 8;

	private int version;
	private int textureSize;
	private byte[] explored;
	private byte[] exploredOthers;
	private List<MapPin> pins;
	private boolean visibleToOthers;
	@JsonIgnore
	private byte[] rawData;

	public MinimapData(ZPackage zPackage, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		version = zPackage.readInt32();
		if (version > MAX_SUPPORTED_MAP_VERSION) {
			if (hints.isFailOnUnsupportedVersion()) {
				throw new ValheimArchiveUnsupportedVersionException(MinimapData.class, "minimap", version, MAX_SUPPORTED_MAP_VERSION);
			}
			log.warn("Map version {} encountered, but latest tested map version was {}", version, MAX_SUPPORTED_MAP_VERSION);
		}

		ZPackage minimapPackage = version >= 7 ? zPackage.readCompressedPackage() : zPackage;

		textureSize = minimapPackage.readInt32();
		explored = minimapPackage.readBytes(textureSize * textureSize);

		if (version >= 5) {
			exploredOthers = minimapPackage.readBytes(textureSize * textureSize);
		}

		if (version >= 2) {
			int pinsCount = minimapPackage.readInt32();
			pins = new ArrayList<>();
			for (int i = 0; i < pinsCount; i++) {
				pins.add(new MapPin(minimapPackage, version));
			}
		}

		if (version >= 4) {
			visibleToOthers = minimapPackage.readBool();
		}
	}

	/**
	 * Serializes this map data. When the parsed content re-serializes to the
	 * same (decompressed) content it was read from, the original raw bytes are
	 * returned instead, so unmodified files survive a read-write round trip
	 * byte-identically even when the game's gzip stream was produced by a
	 * different compressor than this JVM's.
	 */
	public byte[] serializePreservingRaw() {
		ZPackage contentPackage = new ZPackage();
		writeMapContent(contentPackage);
		byte[] content = contentPackage.getBufferAsBytes();
		if (rawData != null && rawData.length >= 4) {
			byte[] rawContent = rawContent();
			if (rawContent != null && java.util.Arrays.equals(rawContent, content)) {
				return rawData;
			}
		}
		ZPackage zPackage = new ZPackage();
		save(zPackage);
		return zPackage.getBufferAsBytes();
	}

	private byte[] rawContent() {
		try {
			if (version >= 7) {
				// rawData = [i32 mapVersion][i32 compressedLength][gzip blob]
				int compressedLength = java.nio.ByteBuffer.wrap(rawData, 4, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
				try (java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(new java.io.ByteArrayInputStream(rawData, 8, compressedLength))) {
					return gis.readAllBytes();
				}
			}
			return java.util.Arrays.copyOfRange(rawData, 4, rawData.length);
		} catch (Exception e) {
			return null;
		}
	}

	public void save(ZPackage zPackage) {
		zPackage.writeInt32(version);

		if (version >= 7) {
			zPackage.writeCompressedPackage(minimapPackage -> writeMapContent(minimapPackage));
		} else {
			writeMapContent(zPackage);
		}
	}

	private void writeMapContent(ZPackage minimapPackage) {
		minimapPackage.writeInt32(textureSize);
		minimapPackage.writeBytes(explored);

		if (version >= 5) {
			minimapPackage.writeBytes(exploredOthers);
		}

		if (version >= 2) {
			minimapPackage.writeInt32(pins.size());
			pins.forEach(pin -> pin.save(minimapPackage, version));
		}

		if (version >= 4) {
			minimapPackage.writeBool(visibleToOthers);
		}
	}
}
