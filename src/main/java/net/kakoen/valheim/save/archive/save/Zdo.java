package net.kakoen.valheim.save.archive.save;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;
import net.kakoen.valheim.save.decode.ReverseHashcodeLookup;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Quaternion;
import net.kakoen.valheim.save.struct.Vector2i;
import net.kakoen.valheim.save.struct.Vector3;
import net.kakoen.valheim.save.struct.ZdoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Zdo {
	private ZdoId uid;
	private long ownerRevision;
	private long dataRevision;
	private boolean persistent;
	private long owner;
	private long timeCreated;
	private int pgwVersion;
	private byte type;
	private boolean distant;
	private int prefab;
	private String prefabName;
	private Vector2i sector;
	private Vector3 position;
	private Quaternion rotation;
	
	private Map<Integer, Float> floats;
	private Map<String, Float> floatsByName;
	
	private Map<Integer, Vector3> vector3s;
	private Map<String, Vector3> vector3sByName;
	
	private Map<Integer, Quaternion> quats;
	private Map<String, Quaternion> quatsByName;
	
	private Map<Integer, Integer> ints;
	private Map<String, Integer> intsByName;
	
	private Map<Integer, Long> longs;
	private Map<String, Long> longsByName;
	
	private Map<Integer, String> strings;
	private Map<String, String> stringsByName;
	
	
	public Zdo(ZdoId zdoId) {
		this.uid = zdoId;
	}
	
	/**
	 *
	 * @param zPackage The package that's being read
	 * @param worldVersion The world version of the save
	 * @param hints Hints that affect processing the zdo
	 * @return
	 */
	public Zdo(ZdoId uid, ZPackage zPackage, int worldVersion, ValheimSaveReaderHints hints) {
		this.uid = uid;
		this.ownerRevision = zPackage.readUInt();
		this.dataRevision = zPackage.readUInt();
		this.persistent = zPackage.readBool();
		this.owner = zPackage.readLong();
		this.timeCreated = zPackage.readLong();
		this.pgwVersion = zPackage.readInt32();
		if(worldVersion >= 16 && worldVersion < 24) {
			zPackage.readInt32();
		}
		if(worldVersion >= 23) {
			this.type = zPackage.readByte();
		}
		if(worldVersion >= 22) {
			this.distant = zPackage.readBool();
		}
		if(worldVersion >= 17) {
			this.prefab = zPackage.readInt32();
			if(hints.isResolveNames()) {
				prefabName = Optional.ofNullable(ReverseHashcodeLookup.lookup(this.prefab)).orElse(null);
			}
		}
		this.sector = zPackage.readVector2i();
		this.position = zPackage.readVector3();
		this.rotation = zPackage.readQuaternion();
		
		int floatCount = zPackage.readChar();
		if(floatCount > 0) {
			floats = new LinkedHashMap<>();
			floatsByName = new LinkedHashMap<>();
			for(int i = 0; i < floatCount; i++) {
				readValue(zPackage, ZPackage::readSingle, floatsByName, floats, hints.isResolveNames());
			}
		}
		
		int numVector3 = zPackage.readChar();
		if(numVector3 > 0) {
			vector3s = new LinkedHashMap<>();
			vector3sByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
			for(int i = 0; i < numVector3; i++) {
				readValue(zPackage, ZPackage::readVector3, vector3sByName, vector3s, hints.isResolveNames());
			}
		}
		
		int quatCount = zPackage.readChar();
		if(quatCount > 0) {
			quats = new LinkedHashMap<>();
			quatsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
			for(int i = 0; i < quatCount; i++) {
				readValue(zPackage, ZPackage::readQuaternion, quatsByName, quats, hints.isResolveNames());
			}
		}
		
		int intCount = zPackage.readChar();
		if(intCount > 0) {
			ints = new LinkedHashMap<>();
			intsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
			for(int i = 0; i < intCount; i++) {
				readValue(zPackage, ZPackage::readInt32, intsByName, ints, hints.isResolveNames());
			}
		}
		
		int longCount = zPackage.readChar();
		if(longCount > 0) {
			longs = new LinkedHashMap<>();
			longsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
			for(int i = 0; i < longCount; i++) {
				readValue(zPackage, ZPackage::readLong, longsByName, longs, hints.isResolveNames());
			}
		}
		
		int stringCount = zPackage.readChar();
		if(stringCount > 0) {
			strings = new LinkedHashMap<>();
			stringsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
			for(int i = 0; i < stringCount; i++) {
				readValue(zPackage, ZPackage::readString, stringsByName, strings, hints.isResolveNames());
			}
		}
	}
	
	private <T> void readValue(ZPackage zPackage, Function<ZPackage, T> valueReader, Map<String, T> byName, Map<Integer, T> byHash, boolean resolveNames) {
		int hashcode = zPackage.readInt32();
		T value = valueReader.apply(zPackage);
		String key = resolveNames ? ReverseHashcodeLookup.lookup(hashcode) : null;
		if(key != null) {
			byName.put(key, value);
		} else {
			byHash.put(hashcode, value);
		}
	}
	
}
