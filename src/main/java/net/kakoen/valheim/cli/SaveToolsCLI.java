package net.kakoen.valheim.cli;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.kakoen.valheim.cli.processor.AddGlobalKeyProcessor;
import net.kakoen.valheim.cli.processor.CleanStructuresProcessor;
import net.kakoen.valheim.cli.processor.ListGlobalKeysProcessor;
import net.kakoen.valheim.cli.processor.RemoveGlobalKeyProcessor;
import net.kakoen.valheim.cli.processor.ResetWorldProcessor;
import net.kakoen.valheim.cli.processor.ValheimArchiveProcessor;
import net.kakoen.valheim.save.archive.ValheimArchive;
import net.kakoen.valheim.save.archive.ValheimArchiveType;
import net.kakoen.valheim.save.archive.ValheimCharacter;
import net.kakoen.valheim.save.archive.ValheimSaveArchive;
import net.kakoen.valheim.save.archive.ValheimSaveMetadata;
import net.kakoen.valheim.save.archive.hints.ValheimArchiveReaderHints;
import net.kakoen.valheim.save.archive.hints.ValheimSaveReaderHints;
import net.kakoen.valheim.save.exception.ValheimArchiveUnsupportedVersionException;

@Slf4j
public class SaveToolsCLI {
	
	private final static List<ValheimArchiveProcessor> PROCESSORS = List.of(
			new ListGlobalKeysProcessor(),
			new RemoveGlobalKeyProcessor(),
			new AddGlobalKeyProcessor(),
			new CleanStructuresProcessor(),
			new ResetWorldProcessor()
	);
	
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
		
		for(ValheimArchiveProcessor processor : PROCESSORS) {
			if(processor.getType() == inputArchive.getType() && processor.isEnabled(cliOptions)) {
				log.info("Applying processor " + processor.getClass().getSimpleName());
				processor.process(inputArchive, cliOptions);
			}
		}
		
		if(cliOptions.getOutputFileName() != null) {
			saveArchive(inputArchive, new File(cliOptions.getOutputFileName()));
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
					return new ValheimSaveMetadata(
							inputFile,
							applyGeneralHints(new ValheimArchiveReaderHints(), cliOptions)
					);
				case DB:
					return new ValheimSaveArchive(
							inputFile,
							applyGeneralHints(
								ValheimSaveReaderHints.builder()
										.resolveNames(!cliOptions.isSkipResolveNames())
										.build(),
								cliOptions
							));
				case FCH:
					return new ValheimCharacter(
							inputFile,
							applyGeneralHints(new ValheimArchiveReaderHints(), cliOptions)
					);
				case JSON:
					return readJson(inputFile, ValheimArchive.class);
			}
		} catch(IOException | ValheimArchiveUnsupportedVersionException e) {
			log.error("Failed to read input file {}", inputFile.getAbsolutePath(), e);
			System.exit(1);
		}
		return null;
	}
	
	private static <T extends ValheimArchiveReaderHints> T applyGeneralHints(T hints, SaveToolsCLIOptions cliOptions) {
		hints.setFailOnUnsupportedVersion(cliOptions.isFailOnUnsupportedVersion());
		return hints;
	}
	
	private static <T> T readJson(File inputFile, Class<T> clazz) throws IOException {
		return new ObjectMapper().readerFor(clazz).readValue(inputFile);
	}
	
	private static <T> void writeJson(T objectToWrite, File outputFile) throws IOException, JsonProcessingException {
		new ObjectMapper()
				.writer()
				.withDefaultPrettyPrinter()
				.writeValue(outputFile, objectToWrite);
	}
	
}
