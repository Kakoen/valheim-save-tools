package net.kakoen.valheim.save.archive;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = ValheimCharacter.class, name = "FCH"),
		@JsonSubTypes.Type(value = ValheimSaveArchive.class, name = "DB"),
		@JsonSubTypes.Type(value = ValheimSaveMetadata.class, name = "FWL")
})
public interface ValheimArchive {
	
	void save(File outputFile) throws IOException;
	
	@JsonIgnore
	ValheimArchiveType getType();
	
}
