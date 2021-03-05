package net.kakoen.valheim.save.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import net.kakoen.valheim.save.parser.ZPackage;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZdoId {
	
	private long userId;
	private long id;
	
	public ZdoId(ZPackage zPackage) {
		userId = zPackage.readLong();
		id = zPackage.readUInt();
	}
	
	public void save(ZPackage writer) {
		writer.writeLong(userId);
		writer.writeUInt(id);
	}
}
