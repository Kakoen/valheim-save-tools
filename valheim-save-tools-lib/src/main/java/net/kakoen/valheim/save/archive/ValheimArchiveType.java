package net.kakoen.valheim.save.archive;

import lombok.Getter;

public enum ValheimArchiveType {
	FCH("fch"),
	DB("db"),
	FWL("fwl"),
	JSON("json");
	
	@Getter
	private final String extension;
	
	ValheimArchiveType(String extension) {
		this.extension = extension;
	}
	
	public static ValheimArchiveType fromFileName(String fileName) {
		String[] parts = fileName.split("\\.");
		String extension = parts[parts.length - 1];
		for(ValheimArchiveType type : ValheimArchiveType.values()) {
			if(extension.equalsIgnoreCase(type.getExtension())) {
				return type;
			}
		}
		return null;
	}
}
