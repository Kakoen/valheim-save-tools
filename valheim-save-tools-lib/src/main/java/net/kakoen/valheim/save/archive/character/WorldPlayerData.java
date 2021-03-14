package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WorldPlayerData {
	
	private boolean haveCustomSpawnPoint;
	private Vector3 spawnPoint;
	private boolean hasLogoutPoint;
	private Vector3 logoutPoint;
	private boolean hasDeathPoint;
	private Vector3 deathPoint;
	private Vector3 homePoint;
	private MinimapData mapData;
	
	public WorldPlayerData(ZPackage zPackage, int version, ValheimArchiveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
		haveCustomSpawnPoint = zPackage.readBool();
		spawnPoint = zPackage.readVector3();
		hasLogoutPoint = zPackage.readBool();
		logoutPoint = zPackage.readVector3();
		if(version >= 30) {
			hasDeathPoint = zPackage.readBool();
			deathPoint = zPackage.readVector3();
		}
		homePoint = zPackage.readVector3();
		if(version >= 29 && zPackage.readBool()) {
			mapData = zPackage.readLengthPrefixedObject(reader -> new MinimapData(reader, hints));
		}
	}
	
	public void save(ZPackage writer) {
		writer.writeBool(haveCustomSpawnPoint);
		writer.writeVector3(spawnPoint);
		writer.writeBool(hasLogoutPoint);
		writer.writeVector3(logoutPoint);
		writer.writeBool(hasDeathPoint);
		writer.writeVector3(deathPoint);
		writer.writeVector3(homePoint);
		writer.writeBool(mapData != null);
		if(mapData != null) {
			writer.writeLengthPrefixedObject(mapData::save);
		}
	}
}
