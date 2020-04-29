package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.util.Scanner;

import ca.nrc.ui.commandline.SubCommand;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactory;

public abstract class ConsoleCommand extends SubCommand {

	public static final String OPT_DATA_FILE = "data-file";
	public static final String OPT_INPUT_FILE = "input-file";
	public static final String OPT_CORPUS_DIR = "corpus-dir";
	public static final String OPT_CORPUS_NAME = "corpus-name";
	public static final String OPT_COMP_FILE = "comp-file";
	public static final String OPT_GS_FILE = "gs-file";

	public static final String OPT_MORPHEMES = "morphemes";
	public static final String OPT_MORPHEME = "morpheme";
	public static final String OPT_WORD = "word";
	public static final String OPT_MAX_WORDS = "max-words";
	public static final String OPT_WORDS_ONLY = "words-only";
	public static final String OPT_MAX_NGRAMS = "max-ngrams";
	public static final String OPT_MIN_NGRAM_LEN = "min-ngram-len";

	public static final String OPT_FROM_SCRATCH = "from-scratch";
	public static final String OPT_REDO_FAILED = "redo-failed";
	public static final String OPT_CONTENT = "content";
	public static final String OPT_FONT = "font";
	public static final String OPT_SOM = "stats-over-morphemes";
	public static final String OPT_DICT_FILE = "dict-file";
	public static final String OPT_MAX_CORR = "max-corr";
	public static final String OPT_ED_ALGO = "edit-dist";
	public static final String OPT_EXTENDED_ANALYSIS = "extended-analysis";
	public static final String OPT_EXCLUDE = "exclude";
	public static final String OPT_PIPELINE_MODE = "pipeline-mode";

	public ConsoleCommand(String name) {
		super(name);
	}
	
	protected String getCompilationFile() {
		return getCompilationFile(true);
	}
	protected String getCompilationFile(boolean failIfAbsent) {
		String tFile = getOptionValue(ConsoleCommand.OPT_COMP_FILE, failIfAbsent);
		if (tFile != null && !tFile.endsWith("json")) {
			tFile = tFile + ".json";
		}
		return tFile;
	}

	protected File getDataFile() {
		String fileStr = getOptionValue(ConsoleCommand.OPT_DATA_FILE);
		return new File(fileStr);
	}
	
	protected String getDictFile() {
		return getDictFile(true);
	}
	protected String getDictFile(boolean failIfAbsent) {
		String dFile = getOptionValue(ConsoleCommand.OPT_DICT_FILE, true);
		return dFile;
	}

	protected String getCorpusDir() {
		String dir = getOptionValue(ConsoleCommand.OPT_CORPUS_DIR, true);
		return dir;
	}

	protected String getCorpusName() {
		return getCorpusName(false);
	}
	protected String getCorpusName(boolean failIfAbsent) {
		String corpusName = getOptionValue(ConsoleCommand.OPT_CORPUS_NAME, failIfAbsent);
		return corpusName;		
	}
	
	protected String[] getMorphemes() {
		return getMorphemes(true);
	}
	
	protected String[] getMorphemes(boolean failIfAbsent) {
		String morphStr = getOptionValue(ConsoleCommand.OPT_MORPHEMES, failIfAbsent);
		String[] morphSeq = null;
		if (morphStr != null) {
			morphSeq = morphStr.split("\\s+");
		}
		
		return morphSeq;		
	}
	
	protected String getWord() {
		return getWord(true);
	}

	protected boolean getWordsOnlyOpt() {
		return hasOption(OPT_WORDS_ONLY);
	}

	protected Long getMaxWords() {
		String maxStr = getOptionValue(ConsoleCommand.OPT_MAX_WORDS, false);
		Long max = Long.MAX_VALUE;
		if (maxStr != null) {
			try {
				max = Long.parseLong(maxStr);
			} catch (Exception e) {
				usageBadOption(ConsoleCommand.OPT_MAX_WORDS, 
						"Value must be an integer");
			}
		}
		return max;
	}
	
	protected Long getMaxNgrams() {
		String maxStr = getOptionValue(ConsoleCommand.OPT_MAX_NGRAMS, false);
		Long max = null;
		if (maxStr != null) {
			try {
				max = Long.parseLong(maxStr);
			} catch (Exception e) {
				usageBadOption(ConsoleCommand.OPT_MAX_WORDS, 
						"Value must be an integer");
			}
		}
		return max;
	}

	protected Integer getMinNgramLen() {
		String minStr = getOptionValue(ConsoleCommand.OPT_MIN_NGRAM_LEN, false);
		Integer min = null;
		if (minStr != null) {
			try {
				min = Integer.parseInt(minStr);
			} catch (Exception e) {
				usageBadOption(ConsoleCommand.OPT_MAX_WORDS, 
						"Value must be an integer");
			}
		}
		return min;
	}

	protected String getWord(boolean failIfAbsent) {
		String wordStr = getOptionValue(ConsoleCommand.OPT_WORD, failIfAbsent);
		return wordStr;		
	}
	
	protected String getExcludePattern() {
		String pattern = getOptionValue(ConsoleCommand.OPT_EXCLUDE, false);
		return pattern;		
	}
	
	protected boolean getExtendedAnalysis() {
		boolean option = hasOption(ConsoleCommand.OPT_EXTENDED_ANALYSIS);
		return option;
	}
	
	protected boolean inPipelineMode() {
		return hasOption(ConsoleCommand.OPT_PIPELINE_MODE);
	}
	
	protected String getMorpheme() {
		return getMorpheme(true);
	}
	protected String getMorpheme(boolean failIfAbsent) {
		String morpheme = getOptionValue(ConsoleCommand.OPT_MORPHEME, failIfAbsent);
		return morpheme;		
	}
	
	protected String getContent() {
		return getContent(true);
	}
	protected String getContent(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_CONTENT, failIfAbsent);
	}
	
	protected String getFont() {
		return getContent(true);
	}
	protected String getFont(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_FONT, failIfAbsent);
	}
	
	protected String getInputFile() {
		return getInputFile(true);
	}
	protected String getInputFile(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_INPUT_FILE, failIfAbsent);
	}
	
	protected String getGoldStandardFile() {
		return getGoldStandardFile(true);
	}
	protected String getGoldStandardFile(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_GS_FILE, failIfAbsent);
	}
	
	protected String getStatsOverMorphemes() {
		return getStatsOverMorphemes(false);
	}
	protected String getStatsOverMorphemes(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_SOM, failIfAbsent);
	}
	
	protected String getMaxCorr() {
		return getMaxCorr(false);
	}
	protected String getMaxCorr(boolean failIfAbsent) {
		return getOptionValue(ConsoleCommand.OPT_MAX_CORR, failIfAbsent);
	}
	
	protected EditDistanceCalculatorFactory.DistanceMethod getEditDistanceAlgorithm() {
		return getEditDistanceAlgorithm(false);
	}
	
	protected EditDistanceCalculatorFactory.DistanceMethod getEditDistanceAlgorithm(boolean failIfAbsent) {
		String algName = getOptionValue(ConsoleCommand.OPT_ED_ALGO, failIfAbsent);
		EditDistanceCalculatorFactory.DistanceMethod alg = null;
		if (algName != null)
			alg = EditDistanceCalculatorFactory.DistanceMethod.valueOf(algName.toUpperCase());
		
		return alg;
	}
	
	protected static String prompt(String mess) {
		mess = "\n" + mess + " ('q' to quit).";
		System.out.print(mess+"\n> ");
		Scanner input = new Scanner(System.in);
        String resp = input.nextLine();
        if (resp.matches("^\\s*q\\s*$")) {
        	resp = null;
        }
		return resp;
	}
	

}
