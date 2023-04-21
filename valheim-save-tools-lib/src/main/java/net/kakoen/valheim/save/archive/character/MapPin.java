package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class MapPin {

	private String name;
	private Vector3 position;
	private PinType pinType;
	private boolean checked;
	private long ownerId;
	
	public MapPin(ZPackage zPackage, int mapVersion) {
		name = zPackage.readString();
		position = zPackage.readVector3();
		pinType = new PinType(zPackage.readInt32());
		checked = (mapVersion >= 3) && zPackage.readBool();
		ownerId = mapVersion >= 6 ? zPackage.readLong() : 0L;
	}
	
	public void save(ZPackage writer) {
		writer.writeString(name);
		writer.writeVector3(position);
		writer.writeInt32(pinType.getId());
		writer.writeBool(checked);
		writer.writeLong(ownerId);
	}
}
