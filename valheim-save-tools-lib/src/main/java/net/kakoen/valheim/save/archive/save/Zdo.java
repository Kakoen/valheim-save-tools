package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kakoen.valheim.save.archive.hints.ValheimSaveReaderHints;
import net.kakoen.valheim.save.decode.ReverseHashcodeLookup;
import net.kakoen.valheim.save.decode.StableHashCode;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;
import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Quaternion;
import net.kakoen.valheim.save.struct.Vector2s;
import net.kakoen.valheim.save.struct.Vector3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class Zdo {
    private boolean persistent;
    private byte type;
    private boolean distant;

    private int prefab;
    private String prefabName;

    private Vector2s sector;
    private Vector3 position;
    private Vector3 rotation;

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

    private Map<Integer, byte[]> byteArrays;
    private Map<String, byte[]> byteArraysByName;

    private boolean saveConnections;
    private boolean withRotation;
    private byte connectionType;
    private int connectionHash;

    private static final int FLAG_WRITE_CONNECTIONS = 1;
    private static final int FLAG_WRITE_FLOATS = 2;
    private static final int FLAG_WRITE_VECTOR3S = 4;
    private static final int FLAG_WRITE_QUATS = 8;
    private static final int FLAG_WRITE_INTS = 16;
    private static final int FLAG_WRITE_LONGS = 32;
    private static final int FLAG_WRITE_STRINGS = 64;
    private static final int FLAG_WRITE_BYTEARRAYS = 128;
    private static final int FLAG_WRITE_PERSISTENT = 256;
    private static final int FLAG_WRITE_DISTANT = 512;
    private static final int FLAG_WRITE_TYPE = 1024 | 2048;
    private static final int FLAG_WRITE_ROTATION = 4096;

    /**
     * @param reader     The package that's being read
     * @param worldVersion The world version of the save
     * @param hints        Hints that affect processing the zdo
     * @return
     */
    public Zdo(ZPackage reader, int worldVersion, ValheimSaveReaderHints hints) throws ValheimArchiveUnsupportedVersionException {
        int flags = reader.readUShort();

        persistent = (flags & 256) != 0;
        distant = (flags & 512) != 0;
        type = (byte) ((flags >> 10) & 3);
        sector = reader.readVector2s();
        position = reader.readVector3();
        prefab = reader.readInt32();
        if (hints.isResolveNames()) {
            prefabName = ReverseHashcodeLookup.lookup(this.prefab);
        }

        withRotation = (flags & FLAG_WRITE_ROTATION) != 0;
        if (withRotation) {
            rotation = reader.readVector3();
        }

        if ((flags & 255) == 0) {
            return;
        }

        saveConnections = flagSet(flags, FLAG_WRITE_CONNECTIONS);
        if (saveConnections) {
            connectionType = reader.readByte();
            connectionHash = reader.readInt32();
        }

        if (flagSet(flags, FLAG_WRITE_FLOATS)) {
            int floatCount = reader.readNumItems(worldVersion);
            if (floatCount > 0) {
                floats = new LinkedHashMap<>();
                floatsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < floatCount; i++) {
                    readValue(reader, ZPackage::readSingle, floatsByName, floats, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_VECTOR3S)) {
            int numVector3 = reader.readNumItems(worldVersion);
            if (numVector3 > 0) {
                vector3s = new LinkedHashMap<>();
                vector3sByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < numVector3; i++) {
                    readValue(reader, ZPackage::readVector3, vector3sByName, vector3s, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_QUATS)) {
            int quatCount = reader.readNumItems(worldVersion);
            if (quatCount > 0) {
                quats = new LinkedHashMap<>();
                quatsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < quatCount; i++) {
                    readValue(reader, ZPackage::readQuaternion, quatsByName, quats, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_INTS)) {
            int intCount = reader.readNumItems(worldVersion);
            if (intCount > 0) {
                ints = new LinkedHashMap<>();
                intsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < intCount; i++) {
                    readValue(reader, ZPackage::readInt32, intsByName, ints, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_LONGS)) {
            int longCount = reader.readNumItems(worldVersion);
            if (longCount > 0) {
                longs = new LinkedHashMap<>();
                longsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < longCount; i++) {
                    readValue(reader, ZPackage::readLong, longsByName, longs, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_STRINGS)) {
            int stringCount = reader.readNumItems(worldVersion);
            if (stringCount > 0) {
                strings = new LinkedHashMap<>();
                stringsByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < stringCount; i++) {
                    readValue(reader, ZPackage::readString, stringsByName, strings, hints.isResolveNames());
                }
            }
        }

        if (flagSet(flags, FLAG_WRITE_BYTEARRAYS)) {
            int byteArrayCount = reader.readNumItems(worldVersion);
            if (byteArrayCount > 0) {
                byteArrays = new LinkedHashMap<>();
                byteArraysByName = hints.isResolveNames() ? new LinkedHashMap<>() : null;
                for (int i = 0; i < byteArrayCount; i++) {
                    readValue(reader, ZPackage::readLengthPrefixedByteArray, byteArraysByName, byteArrays, hints.isResolveNames());
                }
            }
        }
    }

    private <T> void readValue(ZPackage zPackage, Function<ZPackage, T> valueReader, Map<String, T> byName, Map<Integer, T> byHash, boolean resolveNames) {
        int hashcode = zPackage.readInt32();
        T value = valueReader.apply(zPackage);
        String key = resolveNames ? ReverseHashcodeLookup.lookup(hashcode) : null;
        if (key != null) {
            byName.put(key, value);
        } else {
            byHash.put(hashcode, value);
        }
    }

    public void save(ZPackage writer) {

        int flags =
                (saveConnections ? FLAG_WRITE_CONNECTIONS : 0)
                        | (getDataTypeFlag(floats, floatsByName, FLAG_WRITE_FLOATS))
                        | (getDataTypeFlag(vector3s, vector3sByName, FLAG_WRITE_VECTOR3S))
                        | (getDataTypeFlag(quats, quatsByName, FLAG_WRITE_QUATS))
                        | (getDataTypeFlag(ints, intsByName, FLAG_WRITE_INTS))
                        | (getDataTypeFlag(longs, longsByName, FLAG_WRITE_LONGS))
                        | (getDataTypeFlag(strings, stringsByName, FLAG_WRITE_STRINGS))
                        | (getDataTypeFlag(byteArrays, byteArraysByName, FLAG_WRITE_BYTEARRAYS))
                        | (persistent ? FLAG_WRITE_PERSISTENT : 0)
                        | (distant ? FLAG_WRITE_DISTANT : 0)
                        | ((((int) type) << 10))
                        | (withRotation ? FLAG_WRITE_ROTATION : 0);

        writer.writeUShort(flags);
        writer.writeVector2s(sector);
        writer.writeVector3(position);
        writer.writeInt32(prefab);

        if (withRotation) {
            writer.writeVector3(rotation);
        }

        if ((flags & 255) == 0) {
            return;
        }

        if (saveConnections) {
            writer.writeByte(connectionType);
            writer.writeInt32(connectionHash);
        }

        if (flagSet(flags, FLAG_WRITE_FLOATS))
            writeProperties(writer, floats, floatsByName, writer::writeSingle);
        if (flagSet(flags, FLAG_WRITE_VECTOR3S))
            writeProperties(writer, vector3s, vector3sByName, writer::writeVector3);
        if (flagSet(flags, FLAG_WRITE_QUATS))
            writeProperties(writer, quats, quatsByName, writer::writeQuaternion);
        if (flagSet(flags, FLAG_WRITE_INTS))
            writeProperties(writer, ints, intsByName, writer::writeInt32);
        if (flagSet(flags, FLAG_WRITE_LONGS))
            writeProperties(writer, longs, longsByName, writer::writeLong);
        if (flagSet(flags, FLAG_WRITE_STRINGS))
            writeProperties(writer, strings, stringsByName, writer::writeString);
        if (flagSet(flags, FLAG_WRITE_BYTEARRAYS))
            writeProperties(writer, byteArrays, byteArraysByName, writer::writeLengthPrefixedByteArray);
    }

    private int getDataTypeFlag(Map<Integer, ?> rawMap, Map<String, ?> byNameMap, int flag) {
        if ((rawMap == null || rawMap.isEmpty()) && (byNameMap == null || byNameMap.isEmpty())) {
            return 0;
        }
        return flag;
    }

    private boolean flagSet(int flags, int flag) {
        return (flags & flag) != 0;
    }

    private <T> void writeProperties(ZPackage writer, Map<Integer, T> valuesByHash, Map<String, T> valuesByName, Consumer<T> writeFunction) {
        int size = ((valuesByHash != null) ? valuesByHash.size() : 0)
                + ((valuesByName != null) ? valuesByName.size() : 0);

        writer.writeNumItems(size);
        if (valuesByHash != null) {
            valuesByHash.forEach((k, v) -> {
                writer.writeInt32(k);
                writeFunction.accept(v);
            });
        }
        if (valuesByName != null) {
            valuesByName.forEach((k, v) -> {
                writer.writeInt32(StableHashCode.getStableHashCode(k));
                writeFunction.accept(v);
            });
        }
    }
}

