package net.kakoen.valheim.save.archive.character;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Skill {
	private float level;
	private float accumulator;
	
	public Skill(ZPackage reader) {
		level = reader.readSingle();
		accumulator = reader.readSingle();
	}
	
	public void save(ZPackage writer) {
		writer.writeSingle(level);
		writer.writeSingle(accumulator);
	}
}
