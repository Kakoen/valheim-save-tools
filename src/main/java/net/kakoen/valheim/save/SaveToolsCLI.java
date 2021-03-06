package net.kakoen.valheim.save;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimCharacter;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.ValheimSaveMetadata;
import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;

@Slf4j
public class SaveToolsCLI {
	
	public static void main(String[] args) {
		SaveToolsCLIOptions cliOptions = new SaveToolsCLIOptions();
		try {
			cliOptions.parseOptions(args);
		}
		catch (ParseException e) {
			log.error(e.getMessage());
			cliOptions.printHelp();
			System.exit(1);
		}
		
		File inputFile = new File(cliOptions.getInputFileName());
		ValheimArchive inputArchive = readValheimArchive(inputFile, cliOptions);
		log.info("Archive type: " + inputArchive.getType());
		
		if(inputArchive.getType() == ValheimArchiveType.DB) {
			if(cliOptions.getListGlobalKeys()) {
				listGlobalKeys((ValheimSaveArchive) inputArchive);
			}
			if(cliOptions.getRemoveGlobalKeys() != null) {
				removeGlobalKeys((ValheimSaveArchive) inputArchive, cliOptions.getRemoveGlobalKeys());
			}
		}
		
		if(cliOptions.getOutputFileName() != null) {
			saveArchive(inputArchive, new File(cliOptions.getOutputFileName()));
		}
	}
	
	private static void listGlobalKeys(ValheimSaveArchive valheimSaveArchive) {
		if(valheimSaveArchive.getZones() == null || valheimSaveArchive.getZones().getGlobalKeys() == null) {
			log.info("Global keys not present in archive");
			return;
		}
		log.info("Global keys: {}", String.join(", ", valheimSaveArchive.getZones().getGlobalKeys()));
	}
	
	private static void removeGlobalKeys(ValheimSaveArchive valheimSaveArchive, String[] removeGlobalKeys) {
		if(valheimSaveArchive.getZones() == null || valheimSaveArchive.getZones().getGlobalKeys() == null) {
			log.info("Global keys not present in archive");
			return;
		}
		for (String removeGlobalKey : removeGlobalKeys) {
			if (valheimSaveArchive.getZones().getGlobalKeys().remove(removeGlobalKey)) {
				log.info("Global key {} removed", removeGlobalKey);
			}
			else {
				log.info("Global key {} was not present", removeGlobalKey);
			}
		}
	}
	
	private static void saveArchive(ValheimArchive valheimArchive, File outputFile) {
		ValheimArchiveType outputFileType = ValheimArchiveType.fromFileName(outputFile.getName());
		if(outputFileType == null) {
			log.error("Failed to determine archive type of output file {}", outputFile.getAbsolutePath());
			log.error("Make sure the file name ends with .fch, .db, .fwl or .json");
			System.exit(1);
		}
		try {
			log.info("Saving {} to {}", outputFileType, outputFile.getAbsolutePath());
			switch (outputFileType) {
				case FWL:
				case DB:
				case FCH:
					if(valheimArchive.getType() != outputFileType) {
						log.error("Make sure the input file type and output file type (extensions) are compatible");
						System.exit(1);
					}
					valheimArchive.save(outputFile);
					break;
				case JSON:
					writeJson(valheimArchive, outputFile);
					break;
			}
		} catch(IOException e) {
			log.error("Failed to write output file {}", outputFile.getAbsolutePath(), e);
			System.exit(1);
		}
	}
	
	private static ValheimArchive readValheimArchive(File inputFile, SaveToolsCLIOptions cliOptions) {
		log.info("Reading from {}", inputFile.getAbsolutePath());
		ValheimArchiveType inputFileType = ValheimArchiveType.fromFileName(inputFile.getName());
		if(inputFileType == null) {
			log.error("Unable to determine type of input file {}", inputFile.getAbsolutePath());
			log.error("Make sure the file name ends with .fch, .db, .fwl or .json");
			System.exit(1);
		}
		try {
			switch (inputFileType) {
				case FWL:
					return new ValheimSaveMetadata(inputFile);
				case DB:
					return new ValheimSaveArchive(inputFile, ValheimSaveReaderHints.builder().resolveNames(!cliOptions.isSkipResolveNames()).build());
				case FCH:
					return new ValheimCharacter(inputFile);
				case JSON:
					return readJson(inputFile, ValheimArchive.class);
			}
		} catch(IOException e) {
			log.error("Failed to read input file {}", inputFile.getAbsolutePath(), e);
			System.exit(1);
		}
		return null;
	}
	
	private static <T> T readJson(File inputFile, Class<T> clazz) throws IOException {
		return new ObjectMapper().readerFor(clazz).readValue(inputFile);
	}
	
	private static <T> void writeJson(T objectToWrite, File outputFile) throws IOException {
		try {
			new ObjectMapper()
					.writer()
					.withDefaultPrettyPrinter()
					.writeValue(outputFile, objectToWrite);
		} catch(Exception e) {
			log.error("Failed to write JSON file {}", outputFile, e);
		}
	}
	
}
