package net.kakoen.valheim.save;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class SaveToolsCLIOptions {
	
	private final static Option REMOVE_GLOBAL_KEY_OPTION = new Option(null, "removeGlobalKey", true, "Remove a global key, specify 'all' to remove all (.db only)");
	private final static Option LIST_GLOBAL_KEY_OPTION = new Option(null, "listGlobalKeys", false, "List global keys (.db only)");
	private final static Option SKIP_RESOLVE_NAMES = new Option(null, "skipResolveNames", false, "Do not resolve names of prefabs and property keys (faster for processing, .db only)");
	private final static Option RESET_WORLD = new Option(null, "resetWorld", false, "Regenerates all zones that don't have player-built structures in them (experimental, .db only)");
	
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
		options.addOption(RESET_WORLD);
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
}
