package net.kakoen.valheim.save.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.kakoen.valheim.save.decode.StableHashCode;

public class StableHashCodeTest {

	@Test
	public void stableHashCode_shouldReturnCorrectCode_forRandomString() {
		Assertions.assertEquals(-957960349, StableHashCode.getStableHashCode("wefijfewijfjewi"));
	}
	
	@Test
	public void stableHashCode_shouldReturnCorrectCode_forOwnerName() {
		Assertions.assertEquals(1227488406, StableHashCode.getStableHashCode("ownerName"));
	}

}
