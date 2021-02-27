package net.kakoen.valheim.save;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.ValheimSaveMetadata;
import net.kakoen.valheim.save.archive.ValheimSaveReaderHints;

@Slf4j
public class SaveParser {
	
	public static void main(String[] args) throws IOException {
		if(args.length < 2) {
			log.info("Usage: java -jar valheim-save-tools.jar <infile> <outfile>");
			log.info("<infile>: Can be a world save (*.db) or metadata file (*.fwl)");
			log.info("<outfile>: Output json file");
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
		
		Object objectToWrite = null;
		if(inputFile.getName().endsWith(".fwl")) {
			try {
				objectToWrite = new ValheimSaveMetadata(inputFile);
			} catch(IOException e) {
				log.error("Failed to read metadata file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
		}
		
		if(inputFile.getName().endsWith(".db")) {
			try {
				objectToWrite = new ValheimSaveArchive(inputFile, ValheimSaveReaderHints.builder().resolveNames(true).build());
			} catch(IOException e) {
				log.error("Failed to read save file {}", inputFile.getAbsolutePath(), e);
				System.exit(1);
			}
		}
		
		if(objectToWrite == null) {
			log.error("Nothing to parse. This could be because your file did not end with .fwl or .db");
			System.exit(1);
		}
		
		try {
			writeJson(objectToWrite, outputFile);
		} catch(IOException e) {
			log.error("Failed to write json file {}", outputFile, e);
		}
	}
	
	private static <T> void writeJson(T objectToWrite, File outputFile) throws IOException {
		new ObjectMapper().writer().withDefaultPrettyPrinter().writeValue(outputFile, objectToWrite);
	}
	
}
