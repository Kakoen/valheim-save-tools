package net.kakoen.valheim.save.decode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ReverseHashcodeLookup {
	
	public static ReverseHashcodeLookup INSTANCE;
	
	static {
		try {
			INSTANCE = new ReverseHashcodeLookup();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Map<Integer, String> stringsByStableHashcode = new HashMap<>();
	
	private List<String> readAllLines(InputStream is) {
		return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.toList());
	}
	
	private ReverseHashcodeLookup() throws IOException {
		Set<String> stringList = new HashSet<>();
		
		try (InputStream is = ReverseHashcodeLookup.class.getResourceAsStream("/known_strings.txt")) {
			for (String s : readAllLines(is)) {
				stringList.add(s);
				stringList.add(s.toLowerCase());
			}
		}
		
		for(int i = 0; i < 200; i++) {
			stringList.add("item" + i);
			stringList.add("room" + i);
			stringList.add("room" + i + "_rot");
			stringList.add("room" + i + "_pos");
			stringList.add("slot" + i);
			stringList.add("root" + i);
		}
		
		for(String s : stringList) {
			stringsByStableHashcode.put(StableHashCode.getStableHashCode(s), s);
		}
		
		log.debug("Loaded {} strings for decoding", stringsByStableHashcode.size());
		
	}
	
	/**
	 * Looks up a stable hash code, to see if there's a known string
	 */
	public static String lookup(int stableHashCode) {
		return INSTANCE.getStringsByStableHashcode().get(stableHashCode);
	}
}
