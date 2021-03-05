package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomEvent {
	
	private float eventTimer;
	private String name;
	private float num;
	private Vector3 position;
	
	public RandomEvent(ZPackage reader) {
		eventTimer = reader.readSingle();
		name = reader.readString();
		num = reader.readSingle();
		position = reader.readVector3();
	}
	
	public void save(ZPackage writer) {
		writer.writeSingle(eventTimer);
		writer.writeString(name);
		writer.writeSingle(num);
		writer.writeVector3(position);
	}
}
