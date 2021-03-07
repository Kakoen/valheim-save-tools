package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldPlayerData {
	
	private boolean haveCustomSpawnPoint;
	private Vector3 spawnPoint;
	private boolean hasLogoutPoint;
	private Vector3 logoutPoint;
	private boolean hasDeathPoint;
	private Vector3 deathPoint;
	private Vector3 homePoint;
	private byte[] mapData;
	
	public WorldPlayerData(ZPackage zPackage, int version) {
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
			mapData = zPackage.readLengthPrefixedByteArray();
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
		if(mapData != null) {
			writer.writeBool(true);
			writer.writeLengthPrefixedByteArray(mapData);
		}
	}
}
