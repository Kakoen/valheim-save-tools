package net.kakoen.valheim.save.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quaternion {
	private float x;
	private float y;
	private float z;
	private float w;
}
