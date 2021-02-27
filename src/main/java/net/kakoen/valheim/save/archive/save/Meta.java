package net.kakoen.valheim.save.archive.save;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meta {
	
	private long modified;
	private int worldVersion;
	private double netTime;
	
}
