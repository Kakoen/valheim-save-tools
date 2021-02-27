package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.struct.ZdoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadZdo {

	private ZdoId uid;
	private Long timestamp;

}
