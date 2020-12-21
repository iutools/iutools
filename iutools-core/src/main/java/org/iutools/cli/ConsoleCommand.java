package org.iutools.cli;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import ca.nrc.ui.commandline.SubCommand;
import org.iutools.edit_distance.EditDistanceCalculatorFactory;
import static org.iutools.concordancer.WebConcordancer.AlignOptions;

public abstract class ConsoleCommand extends SubCommand {

	public static enum Mode {SINGLE_INPUT, INTERACTIVE, PIPELINE}

	public static final String OPT_FORCE = "force";

	public static final String OPT_DATA_FILE = "data-file";
	public static final String OPT_INPUT_FILE = "input-file";
	public static final String OPT_INPUT_DIR = "input-dir";
	public static final String OPT_OUTPUT_DIR = "output-dir";
	public static final String OPT_CORPUS_NAME = "corpus-name";
	public static final String OPT_CORPUS_SAVE_PATH = "corpus-save-path";
	public static final String OPT_GS_FILE = "gs-file";

	public static final String OPT_URL = "url";
	public static final String OPT_LANGS = "langs";
	public static final String OPT_SENTENCES_ALIGN = "align-sentences";
	public static final String OPT_ALIGNER_OPTIONS = "aligner-opts";

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
	public static final String OPT_LENIENT_DECOMPS = "lenient-decomps";
	public static final String OPT_EXCLUDE = "exclude";
	public static final String OPT_PIPELINE = "pipeline";
	public static final String OPT_TIMEOUT_SECS = "timeout-secs";

	public ConsoleCommand(String name) {
		super(name);
	}
	
	protected String getCorpusSavePath() {
		return getCorpusSavePath(true);
	}
	protected String getCorpusSavePath(boolean failIfAbsent) {
		String tFile = getOptionValue(ConsoleCommand.OPT_CORPUS_SAVE_PATH, failIfAbsent);
		return tFile;
	}
	
	protected File getDataFile() {
		return getDataFile(false);
	}
	
	protected File getDataFile(boolean failIfAbsent) {
		String fileStr = 
			getOptionValue(ConsoleCommand.OPT_DATA_FILE, failIfAbsent);
		return new File(fileStr);
	}
	
	protected String getDictFile() {
		return getDictFile(true);
	}
	protected String getDictFile(boolean failIfAbsent) {
		String dFile = getOptionValue(ConsoleCommand.OPT_DICT_FILE, true);
		return dFile;
	}

	protected File getInputDir() {
		return getInputDir(null);
	}	
	
	protected File getInputDir(Boolean failIfAbsent) {
		if (failIfAbsent == null) {
			failIfAbsent = false;
		}
		String dirStr = getOptionValue(ConsoleCommand.OPT_INPUT_DIR, failIfAbsent);
		File dir = null;
		if (dirStr != null) {
			dir = new File(dirStr);
		}
		return dir;
	}

	protected File getOutputDir() {
		return getOutputDir(null);
	}

	protected File getOutputDir(Boolean failIfAbsent) {
		if (failIfAbsent == null) {
			failIfAbsent = true;
		}
		String dirStr = getOptionValue(ConsoleCommand.OPT_OUTPUT_DIR, failIfAbsent);
		File dir = null;
		if (dirStr != null) {
			dir = new File(dirStr);
		}
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

	protected URL getURL() {
		return getURL((Boolean)null);
	}

	protected URL getURL(Boolean failIfAbsent) {
		URL url = null;
		String urlStr = getOptionValue(ConsoleCommand.OPT_URL, failIfAbsent);
		if (urlStr != null) {
			try {
				url = new URL(urlStr);
			} catch (MalformedURLException e) {
				usageBadOption(ConsoleCommand.OPT_URL, "This options must be a valid URL");
			}
		}
		return url;
	}

	protected String[] getLangs() {
		return getLangs((Boolean)null);
	}

	protected String[] getLangs(Boolean failIfAbsent) {
		String[] langs = new String[0];
		String langsStr = getOptionValue(ConsoleCommand.OPT_LANGS, failIfAbsent);
		if (langsStr != null) {
			langs = langsStr.split("\\s*,\\s*");
 		}
		return langs;
	}

	protected AlignOptions[] getAlignOptions() {
		return getAlignOptions(false);
	}

	protected AlignOptions[] getAlignOptions(Boolean failIfAbsent) {
		AlignOptions[] opts = null;
		String optStr = getOptionValue(ConsoleCommand.OPT_ALIGNER_OPTIONS, failIfAbsent);
		if (optStr == null) {
			opts = new AlignOptions[] {
				AlignOptions.MAIN_TEXT, AlignOptions.ALL_TEXT
			};
		} else {
			List<AlignOptions> optsLst = new ArrayList<AlignOptions>();
			String[] optStrings = optStr.split("\\s*,\\s*");
			for (String anOptString: optStrings) {
				try {
					optsLst.add(AlignOptions.valueOf(anOptString));
				} catch (Exception e) {
					usageBadOption(OPT_ALIGNER_OPTIONS,
						"'"+anOptString+"' is not a valid aligner option");
				}
			}
			opts = optsLst.toArray(new AlignOptions[0]);
		}

		return opts;
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
		boolean option = hasOption(ConsoleCommand.OPT_LENIENT_DECOMPS);
		return option;
	}

	protected Mode getMode() {
		return getMode(new String[0]);
	}

	protected Mode getMode(String... singleInputOptions) {
		// Check of an option that provides a single input
		int singleInput = 0;
		for (String option: singleInputOptions) {
			if (hasOption(option)) {
				singleInput = 1;
				break;
			}
		}
		
		// Check if the --interactive or --pipeline-mode options are present
		int interactive = 0;
		if (hasOption(ConsoleCommand.OPT_INTERACTIVE)) {
			interactive = 1;
		}
		int pipeline = 0;
		if (hasOption(ConsoleCommand.OPT_PIPELINE)) {
			pipeline = 1;
		}
		
		if (singleInput + interactive + pipeline > 1) {
			String options = 
				ConsoleCommand.OPT_INTERACTIVE+", "+
				ConsoleCommand.OPT_PIPELINE+", ";
			for (String option: singleInputOptions) {
				options += option+", ";
			}
			usageBadOption(options, "These options are mutually exclusive.");
		}
		
		Mode mode = Mode.SINGLE_INPUT;
		if (pipeline > 0) {
			mode = Mode.PIPELINE;
		} else if (interactive > 0) {
			mode = Mode.INTERACTIVE;
		}
		
		return mode;
	}
	
	protected Long getTimeoutMSecs() {
		String timeoutStr = getOptionValue(ConsoleCommand.OPT_TIMEOUT_SECS, false);
		Long timeoutMSecs = null;
		try {
			if (timeoutStr != null) {
				timeoutMSecs = 1000 * Long.parseLong(timeoutStr);
			}
		} catch (Exception e) {
			usageBadOption(ConsoleCommand.OPT_TIMEOUT_SECS, 
				"Value must be a Long integer");
		}
		return timeoutMSecs;
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
	
	protected void onCommandTimeout(TimeoutException e) {
		echo("Command timed out");
		String mess = e.getMessage();
		if (mess != null) {
			echo("  "+mess);
		}
	}
}
