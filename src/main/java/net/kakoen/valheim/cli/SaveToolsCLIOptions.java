package net.kakoen.valheim.cli;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.kakoen.valheim.cli.processor.CleanStructuresProcessor;

@Slf4j
public class SaveToolsCLIOptions {
	
	private final static Option LIST_GLOBAL_KEY_OPTION = new Option(null, "listGlobalKeys", false, "List global keys (.db only)");
	private final static Option REMOVE_GLOBAL_KEY_OPTION = new Option(null, "removeGlobalKey", true, "Remove a global key, specify 'all' to remove all (.db only)");
	private final static Option ADD_GLOBAL_KEY_OPTION = new Option(null, "addGlobalKey", true, "Adds a global key (.db only)");
	private final static Option SKIP_RESOLVE_NAMES = new Option(null, "skipResolveNames", false, "Do not resolve names of prefabs and property keys (faster for processing, .db only)");
	private final static Option RESET_WORLD = new Option(null, "resetWorld", false, "Regenerates all zones that don't have player-built structures in them (experimental, .db only)");
	private final static Option CLEAN_STRUCTURES = new Option(null, "cleanStructures", false, "Cleans up player built structures (.db only)");
	private final static Option CLEAN_STRUCTURES_THRESHOLD = new Option(null, "cleanStructuresThreshold", true, "Minimum amount of structures to consider as a base (default " + CleanStructuresProcessor.DEFAULT_STRUCTURES_THRESHOLD + ")");
	private final static Option VERBOSE = new Option("v", "verbose", false, "Print debug output");
	private final static Option FAIL_ON_UNSUPPORTED_VERSION = new Option(null, "failOnUnsupportedVersion", false, "Fail when input archive version is newer than known supported");
	
	private CommandLine cmd = null;
	
	public SaveToolsCLIOptions() {
	}
	
	public void parseOptions(String[] args) throws ParseException {
		CommandLineParser commandLineParser = new DefaultParser();
		cmd = commandLineParser.parse(getOptions(), args);
		if(cmd.getArgs().length < 1) {
			throw new ParseException("Expecting at least an input file, and optionally an output file");
		}
	}
	
	public Options getOptions() {
		Options options = new Options();
		options.addOption(SKIP_RESOLVE_NAMES);
		options.addOption(LIST_GLOBAL_KEY_OPTION);
		options.addOption(REMOVE_GLOBAL_KEY_OPTION);
		options.addOption(ADD_GLOBAL_KEY_OPTION);
		options.addOption(RESET_WORLD);
		options.addOption(CLEAN_STRUCTURES);
		options.addOption(CLEAN_STRUCTURES_THRESHOLD);
		CLEAN_STRUCTURES_THRESHOLD.setType(Integer.class);
		options.addOption(VERBOSE);
		options.addOption(FAIL_ON_UNSUPPORTED_VERSION);
		return options;
	}
	
	public void printHelp() {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("java -jar valheim-save-tools.jar <infile> [outfile]",
				"",
				getOptions(),
				"<infile>: Input file of type .fch, .db, .fwl or .json\n<outfile>: Output file of type .fch, .db, .fwl or .json (optional)",
				true);
	}
	
	public String getInputFileName() {
		return cmd.getArgs()[0];
	}
	
	public String getOutputFileName() {
		return cmd.getArgs().length > 1 ? cmd.getArgs()[1] : null;
	}
	
	public String[] getRemoveGlobalKeys() {
		return cmd.getOptionValues(REMOVE_GLOBAL_KEY_OPTION.getLongOpt());
	}
	
	public boolean isSkipResolveNames() {
		return cmd.hasOption(SKIP_RESOLVE_NAMES.getLongOpt());
	}
	
	public boolean isResetWorld() {
		return cmd.hasOption(RESET_WORLD.getLongOpt());
	}
	
	public boolean isListGlobalKeys() {
		return cmd.hasOption(LIST_GLOBAL_KEY_OPTION.getLongOpt());
	}
	
	public boolean isCleanStructures() {
		return cmd.hasOption(CLEAN_STRUCTURES.getLongOpt());
	}
	
	public Integer getCleanStructuresThreshold() {
		try {
			return cmd.hasOption(CLEAN_STRUCTURES_THRESHOLD.getLongOpt())
					? Integer.parseInt(cmd.getOptionValue(CLEAN_STRUCTURES_THRESHOLD.getLongOpt()))
					: null;
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("Structures threshold must be a number");
		}
	}
	
	public String[] getAddGlobalKeys() {
		return cmd.getOptionValues(ADD_GLOBAL_KEY_OPTION.getLongOpt());
	}
	
	public boolean isVerbose() {
		return cmd.hasOption(VERBOSE.getLongOpt());
	}
	
	public boolean isFailOnUnsupportedVersion() {
		return cmd.hasOption(FAIL_ON_UNSUPPORTED_VERSION.getLongOpt());
	}
}
