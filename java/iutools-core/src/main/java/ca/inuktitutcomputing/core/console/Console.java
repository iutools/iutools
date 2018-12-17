package ca.inuktitutcomputing.core.console;

import org.apache.commons.cli.Option;
import org.apache.log4j.helpers.OptionConverter;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.MainCommand;
import ca.nrc.ui.commandline.SubCommand;;

public class Console {
	
	protected static MainCommand defineMainCommand() throws CommandLineException {
		MainCommand mainCmd = new MainCommand("Command line console for iutools.");

		Option optCorpusDir = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_CORPUS_DIR)
			    .desc("Path of a directory contains files for a corpus to be processed.")
			    .hasArg()
			    .argName("CORPUS_DIR")
			    .build();

		Option optCompFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_COMP_FILE)
			    .desc("Path of json file where the result of the compilation is saved (trie, etc).")
			    .hasArg()
			    .argName("TRIE_FILE")
			    .build();

		Option optCorpusName = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_CORPUS_NAME)
			    .desc("Name of the corpus to be processed.")
			    .hasArg()
			    .argName("CORPUS_NAME")
			    .build();

		Option optMorphemes = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MORPHEMES)
			    .desc("A sequence of iu morphemes (space separated).")
			    .hasArg()
			    .argName("MORPH_SEQUENCE")
			    .build();

		Option optWord = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_WORD)
			    .desc("An inuktitut word.")
			    .hasArg()
			    .argName("WORD")
			    .build();

		Option optFromScratch = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_FROM_SCRATCH)
			    .desc("Tells the compiler to start from scratch.")
			    .argName("FROM_SCRATCH")
			    .required(false)
			    .build();

		// Compile a trie and save it to file
		SubCommand compileTrie = 
				new CmdCompileTrie("compile_trie")
				.addOption(optCorpusDir)				
				.addOption(optCompFile)
				.addOption(optFromScratch)
				;
		mainCmd.addSubCommand(compileTrie);
		
		
		// Create and add the read_trie command
		SubCommand searchTrie = 
				new CmdSearchTrie("search_trie")
				.addOption(optCompFile)	
				.addOption(optMorphemes)
				.addOption(optWord)
				;
		mainCmd.addSubCommand(searchTrie);
		

				
		return mainCmd;
	}

	public static void main(String[] args) throws Exception {
		MainCommand mainCmd = defineMainCommand();
		mainCmd.run(args);
	}
	
//	public ConsoleCommand getConsoleCommand(String commandName) throws CommandLineException {
//		MainCommand mainCmd = defineMainCommand();
//		ConsoleCommand cmd = (ConsoleCommand) mainCmd.getSubCommandWithName(commandName);
//		
//		return cmd;
//	}
//
//	public static void runCommand(String cmdName, String[] args) throws Exception {
//		String[] commandWithArgs = new String[args.length+3];
//		commandWithArgs[0] = cmdName;
//		for (int ii=0; ii < args.length; ii++) {
//			commandWithArgs[ii+1] = args[ii];
//		}
//		commandWithArgs[commandWithArgs.length-2] = "-verbosity";
//		commandWithArgs[commandWithArgs.length-1] = "null";
//		
//		Console.main(commandWithArgs);
//		
//		Thread.sleep(1*1000);
//	}	
}
