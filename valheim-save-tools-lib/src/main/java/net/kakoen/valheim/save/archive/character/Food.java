package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Food {
	private String name;
	private float health;
	private float stamina;
	
	public Food(ZPackage reader) {
		name = reader.readString();
		health = reader.readSingle();
		stamina = reader.readSingle();
	}
	
	public void save(ZPackage writer) {
		writer.writeString(name);
		writer.writeSingle(health);
		writer.writeSingle(stamina);
	}
}
