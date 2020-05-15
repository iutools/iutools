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
			    .argName("CORPUS_COMPILATION_FILE")
			    .build();

		Option optDataFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_DATA_FILE)
				.desc("Path of the data file to be read or written.")
				.hasArg()
				.argName("DATA_FILE")
				.build();

		Option optCorpusName = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_CORPUS_NAME)
			    .desc("Name of the corpus to be processed or used.")
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

		Option optMaxWords = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MAX_WORDS)
			    .desc("Maximum number of words to be considered.")
			    .hasArg()
			    .argName("MAX_WORDS")
			    .build();

		Option optMaxNgrams = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MAX_NGRAMS)
			    .desc("Maximum number of ngrams to be considered.")
			    .hasArg()
			    .argName("MAX_NGRAMS")
			    .build();

		Option optMinNgramLen = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MIN_NGRAM_LEN)
			    .desc("Minimum length of ngrams to be considered.")
			    .hasArg()
			    .argName("MIN_NGRAM_LEN")
			    .build();

		Option optWordsOnly = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_WORDS_ONLY)
				.desc("If provided, only process words")
				.build();

		Option optExtendedAnalysis = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_LENIENT_DECOMPS)
			    .desc("Tells the morphological analyzer to extend the analysis by adding a consonant after a final vowel.")
			    .argName("EXTENDED_ANALYSIS")
			    .build();
		
		Option optTimeoutSecs = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_TIMEOUT_SECS)
			    .desc("Max number of seconds that a command is allowed to run before timing out.")
			    .hasArg()			    
			    .argName("TIMEOUT_SECS")
			    .required(false)
			    .build();

		Option optPipelineMode = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_PIPELINE)
			    .desc(
					"If present, run the command in pipeline mode.\n"+
					"The command will then read inputs from STDIN and produce outputs on STDOUT.\n"+
					"For each line of STDIN, it will produce EXACTLY one line on STDOUT (even if an\n"+
					"excepion was raised.")
			    .build();

		Option optFromScratch = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_FROM_SCRATCH)
			    .desc("Tells the compiler to start from scratch.")
			    .argName("FROM_SCRATCH")
			    .required(false)
			    .build();
		
		Option optRedoFailed = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_REDO_FAILED)
			    .desc("Reprocess words that previously failed morphological analysis.")
			    .argName("REDO_FAILED")
			    .required(false)
			    .build();
		
		Option optContent = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_CONTENT)
			    .desc("A string of inuktitut words.")
			    .hasArg()
			    .argName("CONTENT")
			    .build();

		Option optFont = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_FONT)
			    .desc("Name of legacy font.")
			    .hasArg()
			    .argName("FONT")
			    .build();

		Option optInputFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_INPUT_FILE)
			    .desc("The full path of a file containing inuktitut words.")
			    .hasArg()
			    .argName("INPUT_FILE")
			    .build();

		Option optDictFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_DICT_FILE)
			    .desc("The full path of a file containing the compiled dictionary for the spell checker.")
			    .hasArg()
			    .argName("DICT_FILE")
			    .build();

		Option optGoldStandardFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_GS_FILE)
			    .desc("The full path of the Gold Standard file for evaluating the query expander.")
			    .hasArg()
			    .argName("GS_FILE")
			    .build();

		Option optVerbosity = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_VERBOSITY)
			    .desc("Level of verbosity of the output.")
			    .hasArg()
			    .argName("VERBOSITY")
			    .build();

		Option optStatsOverMorphemes = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_SOM)
			    .desc("Compute stats over morphemes instead of words.")
			    .argName("STATS_OVER_MORPHEMES")
			    .build();

		Option optMaxCorr = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MAX_CORR)
			    .desc("Maximum number of corrections")
			    .hasArg()
			    .argName("MAX_CORR")
			    .build();

		Option optEditDistAlgo = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_ED_ALGO)
			    .desc("Edit distance algorithm")
			    .hasArg()
			    .argName("EDIT_DIST")
			    .build();

		Option optMorpheme = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_MORPHEME)
			    .desc("An inuktitut morpheme.")
			    .hasArg()
			    .argName("MORPHEME")
			    .build();

		Option optExclude = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_EXCLUDE)
			    .desc("Exclude some data from processing.")
			    .hasArg()
			    .argName("EXCLUDE_PATTERN")
			    .build();

		
		// --- COMMANDS

		// Compile a corpus and save it to file
		SubCommand compileCorpus = 
				new CmdCompileCorpus("compile_corpus")
				.addOption(optCorpusDir)				
				.addOption(optCompFile)
				.addOption(optFromScratch)
				.addOption(optRedoFailed)
				;
		mainCmd.addSubCommand(compileCorpus);

		// Dump a corpus to file
		SubCommand dumpCorpus =
				new CmdDumpCorpus("dump_corpus")
						.addOption(optCorpusName)
						.addOption(optDataFile)
						.addOption(optWordsOnly)
				;
		mainCmd.addSubCommand(dumpCorpus);

		// Describe a corpus
//		SubCommand describeCorpus = 
//				new CmdDescribeCorpus("describe_corpus")
//				.addOption(optCompFile)	
//				;
//		mainCmd.addSubCommand(describeCorpus);
		
		// Search a trie for a sequence of morphemes
		SubCommand searchTrie = 
				new CmdSearchTrie("search_trie")
				.addOption(optCompFile)	
				.addOption(optMorphemes)
				.addOption(optWord)
				.addOption(optPipelineMode)
				.addOption(optTimeoutSecs)
				;
		mainCmd.addSubCommand(searchTrie);
		
		
		// Decompose an Inuktut word
		SubCommand segmentIU = 
				new CmdSegmentIU("segment_iu")
				.addOption(optWord)
				.addOption(optExtendedAnalysis)
				;
		mainCmd.addSubCommand(segmentIU);
		
				
		// Check spelling of some Inuktut text
		SubCommand checkSpelling = 
				new CmdCheckSpelling("check_spelling")
				.addOption(optCompFile)	
				.addOption(optCorpusName)
				.addOption(optMaxCorr)
				.addOption(optEditDistAlgo)
				;
		mainCmd.addSubCommand(checkSpelling);
		
				
		// Return the gist of inuktitut words
		SubCommand gist = 
				new CmdGist("gist")
				.addOption(optContent)
				.addOption(optInputFile)
				.addOption(optVerbosity)
				;
		mainCmd.addSubCommand(gist);
		
				
		// Output transliterated inuktitut from Legacy to Unicode
		SubCommand translit = 
				new CmdTranslit("transliterate")
				.addOption(optContent)
				.addOption(optInputFile)
				.addOption(optFont)
				;
		mainCmd.addSubCommand(translit);
		
				
		// Find words related to an inuktitut query word
		SubCommand expandIUQuery = 
				new CmdExpandQuery("expand_query")
				.addOption(optCompFile)	
				.addOption(optWord)
				;
		mainCmd.addSubCommand(expandIUQuery);
		
				
		// Find words related to an inuktitut query word
		SubCommand evaluateQueryExpansion = 
				new CmdEvaluateQueryExpansion("evaluate_query_expansion")
				.addOption(optCompFile)	
				.addOption(optGoldStandardFile)
				.addOption(optStatsOverMorphemes)
				;
		mainCmd.addSubCommand(evaluateQueryExpansion);
		
				
		// Convert a Inuktitut segmentation into Trie-compatible segments
		SubCommand convertIU = 
				new CmdConvertIUSegments("convert_iu_segments")
				.addOption(optWord)
				;
		mainCmd.addSubCommand(convertIU);

		SubCommand lookForMorpheme = 
				new CmdLookForMorpheme("look_for_morpheme")
				.addOption(optCompFile)
				.addOption(optMorpheme)
				;
		mainCmd.addSubCommand(lookForMorpheme);
		
		// Analyse failures of the morphological analyser
		SubCommand morphFailureAnalysis = 
				new CmdMorphFailureAnalysis("morph_failure_analysis")
				.addOption(optMaxWords)
				.addOption(optExclude)
				.addOption(optMinNgramLen)
				;
		mainCmd.addSubCommand(morphFailureAnalysis);
				
				
		return mainCmd;
	}

	public static void main(String[] args) throws Exception {
		MainCommand mainCmd = defineMainCommand();
		mainCmd.run(args);
	}
}
