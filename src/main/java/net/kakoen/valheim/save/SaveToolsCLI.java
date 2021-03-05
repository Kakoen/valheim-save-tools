package net.kakoen.valheim.save;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kakoen.valheim.save.archive.ValheimCharacter;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.ValheimSaveMetadata;
import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;

@Slf4j
public class SaveToolsCLI {
	
	public static void main(String[] args) {
		
		if(args.length < 2) {
			showUsage();
			return;
		}
		
		File inputFile = new File(args[0]);
		log.info("Input file: {}", inputFile);
		
		if(!inputFile.isFile()) {
			log.error("Input file {} does not exist!", inputFile.getAbsolutePath());
			System.exit(1);
		}
		
		File outputFile = new File(args[1]);
		log.info("Output file: {}", outputFile);
		
		if(inputFile.getName().endsWith(".fwl")) {
			try {
				writeJson(new ValheimSaveMetadata(inputFile), outputFile);
			} catch(IOException e) {
				log.error("Failed to read metadata file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
			return;
		}
		
		if(inputFile.getName().endsWith(".db")) {
			try {
				writeJson(new ValheimSaveArchive(inputFile, ValheimSaveReaderHints.builder().resolveNames(true).build()), outputFile);
			} catch(IOException e) {
				log.error("Failed to read save file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
			return;
		}
		
		if(inputFile.getName().endsWith(".fch")) {
			try {
				writeJson(new ValheimCharacter(inputFile), outputFile);
			} catch(IOException e) {
				log.error("Failed to read character file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
			return;
		}
		
		if(inputFile.getName().endsWith(".json")) {
			try {
				if(outputFile.getName().endsWith(".db")) {
					ValheimSaveArchive valheimSaveArchive = readJson(inputFile, ValheimSaveArchive.class);
					valheimSaveArchive.save(outputFile);
					return;
				}
				if(outputFile.getName().endsWith(".fch")) {
					ValheimCharacter valheimCharacter = readJson(inputFile, ValheimCharacter.class);
					valheimCharacter.save(outputFile);
				}
				if(outputFile.getName().endsWith(".fwl")) {
					ValheimSaveMetadata valheimSaveMetadata = readJson(inputFile, ValheimSaveMetadata.class);
					valheimSaveMetadata.save(outputFile);
					return;
				}
			}
			catch (IOException e) {
				log.error("Failed to parse JSON file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
		}
		
		showUsage();
	}
	
	private static <T> T readJson(File inputFile, Class<T> clazz) throws IOException {
		return new ObjectMapper().readerFor(clazz).readValue(inputFile);
	}
	
	private static void showUsage() {
		log.info("Usage:");
		log.info("java -jar valheim-save-tools.jar <infile> <outfile>");
		log.info("");
		log.info("Extract game data to JSON file:");
		log.info("<infile>: Can be a world save (*.db), world metadata (*.fwl) or character (*.fch) file");
		log.info("<outfile>: Output json file");
		log.info("");
		log.info("Read JSON and convert back to game data file:");
		log.info("<infile>: Input JSON file (needs to end with .json)");
		log.info("<outfile>: Output world save (*.db), world metadata (*.fwl) or character (*.fch) file");
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
