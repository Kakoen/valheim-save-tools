package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrefabLocation {

	private String name;
	private Vector3 position;
	private boolean generated;
	
	public PrefabLocation(ZPackage reader) {
		name = reader.readString();
		position = reader.readVector3();
		generated = reader.readBool();
	}
	
	public void save(ZPackage writer) {
		writer.writeString(name);
		writer.writeVector3(position);
		writer.writeBool(generated);
	}
}
