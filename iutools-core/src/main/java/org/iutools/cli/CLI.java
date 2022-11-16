package org.iutools.cli;

import ca.nrc.string.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.WebConcordancer;
import org.apache.commons.cli.Option;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.MainCommand;
import ca.nrc.ui.commandline.SubCommand;;

/**
 * Command Line Interface for iutools
 */
public class CLI {
	
	protected static MainCommand defineMainCommand() throws CommandLineException {
		MainCommand mainCmd = new MainCommand("Command line console for iutools.");

		Option optForce = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_FORCE)
			.desc("Force operation without prompting user for confirmation.")
			.build();

		Option optInputDir = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_INPUT_DIR)
			    .desc("Input directory to be processed.")
			    .hasArg()
			    .argName("INPUT_DIR")
			    .build();

		Option optOutputDir = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_OUTPUT_DIR)
				.desc("Directory where outputs will be written.")
				.hasArg()
				.argName("OUTPUT_DIR")
				.build();

		Option optCompFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_CORPUS_SAVE_PATH)
			    .desc("Path where to save the compiled corpus")
			    .hasArg()
			    .argName("CORPUS_SAVE_PATH")
			    .build();

		Option optDataFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_DATA_FILE)
				.desc("Path of the data file to be read or written.")
				.hasArg()
				.argName("DATA_FILE")
				.build();

		Option optLogFile = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_LOG_FILE)
			.desc("Path of the log file to be analyzed.")
			.hasArg()
			.argName("LOG_FILE")
			.build();

		Option optLogReport = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_LOG_REPORT_TYPE)
			.desc("Type of report to be produced.")
			.hasArg()
			.argName("LOG_REPORT")
			.build();


		Option optFileRegexp = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_FILE_REGEXP)
				.desc("Only files whose names match that regexp will be processed.")
				.hasArg()
				.argName("REGEXP")
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

		Option optURL = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_URL)
			.desc("URL of the page to be processed")
			.hasArg()
			.argName("URL")
			.build();

		Option optTopics = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_TOPICS)
			.desc("Comma separated list of topics (ex: 'government,medical')")
			.hasArg()
			.argName("TOPICS")
			.build();


		Option optLangs = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_LANGS)
			.desc("Comma separated list of language codes (ex: 'en,fr,iu').")
			.hasArg()
			.argName("LANGUAGES")
			.build();

		Option optAlignSentences = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_SENTENCES_ALIGN)
			.desc("Set to false if you want to download the parallel pages without aligning their sentences.")
			.build();

//		WebConcordancer.AlignOptions[] blah = WebConcordancer.AlignOptions.values();
//		StringUtils.join(WebConcordancer.AlignOptions.values(), ",");

		Option optAlignerOptions = Option.builder(null)
			.longOpt(ConsoleCommand.OPT_ALIGNER_OPTIONS)
			.desc("Comma separated list of aligner options (valid options: " +
				StringUtils.join(WebConcordancer.AlignOptions.values(), ",")+").")
			.hasArg()
			.argName("ALIGNER_OPTS")
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

			Option optOutputFile = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_OUTPUT_FILE)
			    .desc("The full path of the output file.")
			    .hasArg()
			    .argName("OUTPUT_FILE")
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

		Option optComment = Option.builder(null)
				.longOpt(ConsoleCommand.OPT_COMMENT)
			    .desc("Comment describing the action taken by the command.")
			    .hasArg()
			    .argName("COMMENT")
			    .build();
		
		// --- COMMANDS

		// Compile a corpus and save it to file
		SubCommand compileCorpus = 
				new CmdCompileCorpus("compile_corpus")
				.addOption(optInputDir)
				.addOption(optOutputDir)
				.addOption(optCompFile)
				.addOption(optFromScratch)
				.addOption(optRedoFailed)
				;
		mainCmd.addSubCommand(compileCorpus);

		// Recompute the decomposition of certain words in
		// a compiled Corpus
		SubCommand recomputeDecomps =
				new CmdRecompileDecomps("recompute_decomps")
				.addOption(optInputFile)
				.addOption(optComment)
				;
		mainCmd.addSubCommand(recomputeDecomps);


		// Dump a corpus to file
		SubCommand dumpCorpus =
				new CmdDumpCorpus("dump_corpus")
						.addOption(optCorpusName)
						.addOption(optDataFile)
						.addOption(optWordsOnly)
				;
		mainCmd.addSubCommand(dumpCorpus);

		// Load a corpus into ElasticSearch
		SubCommand esLoadCorpus =
			new CmdEsLoadCorpus("load_corpus")
				.addOption(optCorpusName)
				.addOption(optForce)
				.addOption(optVerbosity)
			;
		mainCmd.addSubCommand(esLoadCorpus);

		// Load a translation memory into ElasticSearch
		SubCommand loadTranslationMemory =
			new CmdLoadTranslationMemory("load_translation_memory")
				.addOption(optInputDir)
				.addOption(optVerbosity)
				.addOption(optFileRegexp)
			;
		mainCmd.addSubCommand(loadTranslationMemory);

		// Describe a corpus
		SubCommand describeCorpus =
				new CmdDescribeCorpus("describe_corpus")
				.addOption(optCompFile)
				;
		mainCmd.addSubCommand(describeCorpus);

		
		// Decompose an Inuktut word
		SubCommand segmentIU = 
				new CmdSegmentIU("segment_iu")
				.addOption(optWord)
				.addOption(optExtendedAnalysis)
				.addOption(optTimeoutSecs)
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
				new CmdRelatedWords("related_words")
				.addOption(optCorpusName)
				.addOption(optWord)
				;
		mainCmd.addSubCommand(expandIUQuery);
		
		// Convert a Inuktitut segmentation into Trie-compatible segments
		SubCommand convertIU = 
				new CmdConvertIUSegments("convert_iu_segments")
				.addOption(optWord)
				;
		mainCmd.addSubCommand(convertIU);

		SubCommand lookForMorpheme = 
				new CmdLookForMorpheme("look_for_morpheme")
				.addOption(optCorpusName)
				.addOption(optMorpheme)
				;
		mainCmd.addSubCommand(lookForMorpheme);

		// Show information about a word found in a given corpus
		SubCommand wordInfo =
				new CmdWordInfo("word_info")
					.addOption(optCorpusName)
				;
		mainCmd.addSubCommand(wordInfo);

				// Analyse failures of the morphological analyser
		SubCommand morphFailureAnalysis =
				new CmdMorphFailureAnalysis("morph_failure_analysis")
				.addOption(optMaxWords)
				.addOption(optExclude)
				.addOption(optMinNgramLen)
				;
		mainCmd.addSubCommand(morphFailureAnalysis);

		SubCommand align =
			new CmdAlignPages("align")
				.addOption(optURL)
				.addOption(optLangs)
				.addOption(optAlignSentences)
				.addOption(optAlignerOptions)
				.addOption(optPipelineMode)
			;
		mainCmd.addSubCommand(align);

		SubCommand tmx2tmjson =
			new Cmd_tmx2tmjson("tmx2tmjson")
				.addOption(optInputDir)
				.addOption(optOutputFile)
				.addOption(optFileRegexp)
				.addOption(optTopics)
			;
		mainCmd.addSubCommand(tmx2tmjson);

		SubCommand portage2tmjson =
			new Cmd_portage2tmjson("portage2tmjson")
				.addOption(optInputDir)
				.addOption(optOutputFile)
				.addOption(optTopics)
			;
		mainCmd.addSubCommand(portage2tmjson);

		SubCommand tmjon2portage =
			new Cmd_tmjson2iu_en_portage("tmjson2en_iu_portage")
				.addOption(optInputFile)
				.addOption(optOutputDir)
			;
		mainCmd.addSubCommand(tmjon2portage);

		SubCommand analyzeLog =
			new CmdAnalyzeLog("analyze_log")
			.addOption(optLogFile)
			;
		mainCmd.addSubCommand(analyzeLog);

		return mainCmd;
	}

	public static void main(String[] args) throws Exception {
		Logger logger = LogManager.getLogger("org.iutools.CLI.main");
		if (logger.isTraceEnabled()) {
			logger.trace("CLI invoked with args="+String.join(", ", args));
		}
		MainCommand mainCmd = defineMainCommand();
		mainCmd.run(args);
	}
}
