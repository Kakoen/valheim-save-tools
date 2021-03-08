package net.kakoen.valheim.save.archive.character;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( value = "name" )
public class PinType {
	
	public static final Map<Integer, String> PIN_TYPES_BY_ID = Map.ofEntries(
			Map.entry(0, "Icon0"),
			Map.entry(1, "Icon1"),
			Map.entry(2, "Icon2"),
			Map.entry(3, "Icon3"),
			Map.entry(4, "Death"),
			Map.entry(5, "Bed"),
			Map.entry(6, "Icon4"),
			Map.entry(7, "Shout"),
			Map.entry(8, "None"),
			Map.entry(9, "Boss"),
			Map.entry(10, "Player"),
			Map.entry(11, "RandomEvent"),
			Map.entry(12, "Ping"),
			Map.entry(13, "EventArea")
	);
	
	private int id;
	
	@JsonGetter
	public String getName() {
		return PIN_TYPES_BY_ID.get(id);
	}
	
}