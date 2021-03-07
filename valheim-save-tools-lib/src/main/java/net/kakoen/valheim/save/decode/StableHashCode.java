package net.kakoen.valheim.save.decode;

public class StableHashCode {
	
	public static int getStableHashCode(String str) {
		int num1 = 5381;
		int num2 = num1;
		for(int index = 0; index < str.length() && str.charAt(index) != 0; index += 2) {
			num1 = (num1 << 5) + num1 ^ (int)str.charAt(index);
			if(index != str.length() - 1 && str.charAt(index + 1) != 0) {
				num2 = (num2 << 5) + num2 ^ (int) str.charAt(index + 1);
			} else {
				break;
			}
		}
		return num1 + num2 * 1566083941;
	}
}
