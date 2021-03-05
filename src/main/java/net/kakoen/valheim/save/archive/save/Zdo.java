package net.kakoen.valheim.save.archive.save;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;
import net.kakoen.valheim.save.decode.ReverseHashcodeLookup;
import net.kakoen.valheim.save.decode.StableHashCode;
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
	public Zdo(ZPackage zPackage, int worldVersion, ValheimSaveReaderHints hints) {
		this.uid = new ZdoId(zPackage);
		zPackage.readLengthPrefixedObject(reader -> {
			this.ownerRevision = reader.readUInt();
			this.dataRevision = reader.readUInt();
			this.persistent = reader.readBool();
			this.owner = reader.readLong();
			this.timeCreated = reader.readLong();
			this.pgwVersion = reader.readInt32();
			if(worldVersion >= 16 && worldVersion < 24) {
				reader.readInt32();
			}
			if(worldVersion >= 23) {
				this.type = reader.readByte();
			}
			if(worldVersion >= 22) {
				this.distant = reader.readBool();
			}
			if(worldVersion >= 17) {
				this.prefab = reader.readInt32();
				if(hints.isResolveNames()) {
					prefabName = Optional.ofNullable(ReverseHashcodeLookup.lookup(this.prefab)).orElse(null);
				}
			}
			this.sector = reader.readVector2i();
			this.position = reader.readVector3();
			this.rotation = reader.readQuaternion();
			
			int floatCount = reader.readChar();
			if(floatCount > 0) {
				floats = new LinkedHashMap<>();
				floatsByName = new LinkedHashMap<>();
				for(int i = 0; i < floatCount; i++) {
					readValue(reader, ZPackage::readSingle, floatsByName, floats, hints.isResolveNames());
				}
			}
			
			int numVector3 = reader.readChar();
			if(numVector3 > 0) {
				vector3s = new LinkedHashMap<>();
				vector3sByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
				for(int i = 0; i < numVector3; i++) {
					readValue(reader, ZPackage::readVector3, vector3sByName, vector3s, hints.isResolveNames());
				}
			}
			
			int quatCount = reader.readChar();
			if(quatCount > 0) {
				quats = new LinkedHashMap<>();
				quatsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
				for(int i = 0; i < quatCount; i++) {
					readValue(reader, ZPackage::readQuaternion, quatsByName, quats, hints.isResolveNames());
				}
			}
			
			int intCount = reader.readChar();
			if(intCount > 0) {
				ints = new LinkedHashMap<>();
				intsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
				for(int i = 0; i < intCount; i++) {
					readValue(reader, ZPackage::readInt32, intsByName, ints, hints.isResolveNames());
				}
			}
			
			int longCount = reader.readChar();
			if(longCount > 0) {
				longs = new LinkedHashMap<>();
				longsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
				for(int i = 0; i < longCount; i++) {
					readValue(reader, ZPackage::readLong, longsByName, longs, hints.isResolveNames());
				}
			}
			
			int stringCount = reader.readChar();
			if(stringCount > 0) {
				strings = new LinkedHashMap<>();
				stringsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
				for(int i = 0; i < stringCount; i++) {
					readValue(reader, ZPackage::readString, stringsByName, strings, hints.isResolveNames());
				}
			}
			
			return Zdo.this;
		});
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
	
	public void save(ZPackage zPackage) {
		uid.save(zPackage);
		zPackage.writeLengthPrefixedObject(writer -> {
			writer.writeUInt(ownerRevision);
			writer.writeUInt(dataRevision);
			writer.writeBool(persistent);
			writer.writeLong(owner);
			writer.writeLong(timeCreated);
			writer.writeInt32(pgwVersion);
			writer.writeByte(type);
			writer.writeBool(distant);
			writer.writeInt32(prefab);
			writer.writeVector2i(sector);
			writer.writeVector3(position);
			writer.writeQuaternion(rotation);
			
			writeProperties(writer, floats, floatsByName, writer::writeSingle);
			writeProperties(writer, vector3s, vector3sByName, writer::writeVector3);
			writeProperties(writer, quats, quatsByName, writer::writeQuaternion);
			writeProperties(writer, ints, intsByName, writer::writeInt32);
			writeProperties(writer, longs, longsByName, writer::writeLong);
			writeProperties(writer, strings, stringsByName, writer::writeString);
		});
	}
	
	private <T> void writeProperties(ZPackage writer, Map<Integer, T> valuesByHash, Map<String, T> valuesByName, Consumer<T> writeFunction) {
		int size = ((valuesByHash != null) ? valuesByHash.size() : 0)
				 + ((valuesByName != null) ? valuesByName.size() : 0);
		
		writer.writeChar(size);
		if(valuesByHash != null) {
			valuesByHash.forEach((k, v) -> {
				writer.writeInt32(k);
				writeFunction.accept(v);
			});
		}
		if(valuesByName != null) {
			valuesByName.forEach((k, v) -> {
				writer.writeInt32(StableHashCode.getStableHashCode(k));
				writeFunction.accept(v);
			});
		}
	}
}

