package net.kakoen.valheim.save.archive.save;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;
import net.kakoen.valheim.save.struct.ZdoId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadZdo {

	private ZdoId uid;
	private Long timestamp;
	
	public DeadZdo(ZPackage reader) {
		uid = new ZdoId(reader);
		timestamp = reader.readLong();
	}
	
	public void save(ZPackage writer) {
		uid.save(writer);
		writer.writeLong(timestamp);
	}
}
