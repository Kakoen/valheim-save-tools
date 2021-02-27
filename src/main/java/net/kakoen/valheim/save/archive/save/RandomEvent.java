package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.struct.Vector3;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RandomEvent {
	
	private float eventTimer;
	private String name;
	private float num;
	private Vector3 position;
	
}
