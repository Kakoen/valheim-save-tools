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

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MinimapData {
	
	private static final int MAX_SUPPORTED_MAP_VERSION = 4;
	
	private int version;
	private int textureSize;
	private byte[] explored;
	private List<MapPin> pins;
	private boolean visibleToOthers;
	
	public MinimapData(ZPackage zPackage, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		version = zPackage.readInt32();
		if(version > MAX_SUPPORTED_MAP_VERSION) {
			if(hints.isFailOnUnsupportedVersion()) {
				throw new ValheimArchiveUnsupportedVersionException(MinimapData.class, "minimap", version, MAX_SUPPORTED_MAP_VERSION);
			}
			log.warn("Map version {} encountered, but latest tested map version was {}", version, MAX_SUPPORTED_MAP_VERSION);
		}
		textureSize = zPackage.readInt32();
		explored = zPackage.readBytes(textureSize * textureSize);
		
		if(version >= 2) {
			int pinsCount = zPackage.readInt32();
			pins = new ArrayList<>();
			for(int i = 0; i < pinsCount; i++) {
				pins.add(new MapPin(zPackage, version));
			}
		}
		
		if(version >= 4) {
			visibleToOthers = zPackage.readBool();
		}
	}
	
	public void save(ZPackage zPackage) {
		zPackage.writeInt32(version);
		zPackage.writeInt32(textureSize);
		zPackage.writeBytes(explored);
		
		zPackage.writeInt32(pins.size());
		pins.forEach(pin -> pin.save(zPackage));
		
		zPackage.writeBool(visibleToOthers);
	}
}
