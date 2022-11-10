package org.iutools.morphrelatives;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.file.ResourceGetter;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertRuntime;
import ca.nrc.testing.TestDirs;
import ca.nrc.ui.commandline.UserIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.morph.r2l.DecompositionState;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;

import org.iutools.utilities.StopWatch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

public class MorphRelativesFinderEvaluator {

	private UserIO userIO = new UserIO();
	private RelatedWordsExperiment experiment = null;

	public Integer stopAfterNWords = null;
	public CSVParser csvParser = null;
	MorphRelativesFinder relsFinder = null;
	protected Boolean onlyMeasureSpeed = false;

	/*
	 * 0. Mot original (fréquence Google du mot en syllabique),
	 * 1. Mot original en syllabique,
	 * 2. Traduction,
	 * 3. "Reformulations trouvées",
	 * 4. "Meilleures Reformulations",
	 * 5. "Syllabique des refromulations avec #hits Google",
	 * 6. "Traduction des meilleures Reformulations",
	 * 7. Notes
	 */
	
	public MorphRelativesFinderEvaluator() throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(null, null, null);
	}
	
	public MorphRelativesFinderEvaluator(String csvGoldStandardFilePath)
		throws MorphRelativesFinderException {
		File goldStandardFile = new File(csvGoldStandardFilePath);
		init__MorphRelativesFinderEvaluator(null, goldStandardFile, (Boolean)null);
	}

	public MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile) throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(finder, csvGoldStandardFile, (Boolean)null);
	}

	public MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile, Boolean onlyMeasureSpeed) throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(finder, csvGoldStandardFile, onlyMeasureSpeed);
	}

	public void init__MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile, Boolean _speedTestOnly) throws MorphRelativesFinderException {
		if (_speedTestOnly != null) {
			this.onlyMeasureSpeed = _speedTestOnly;
		}
		if (finder == null) {
			finder = new MorphRelativesFinder();
		}
		setRelsFinder(finder);
		try {
			setGoldStandard(csvGoldStandardFile);
		} catch (IOException e) {
			throw new MorphRelativesFinderException(e);
		}
	}

	public void setStopAfterNWords(Integer _stopAfterNWords) {
		this.stopAfterNWords = _stopAfterNWords;
	}

	public void setVerbose(boolean value) {
		if (value) {
			userIO.setVerbosity(UserIO.Verbosity.Level0);
		} else {
			userIO.setVerbosity(UserIO.Verbosity.Levelnull);
		}
	}
	
	public void setGoldStandard(File csvGoldStandardFile) throws IOException {
		if (csvGoldStandardFile != null) {
			BufferedReader reader = Files.newBufferedReader(Paths.get(csvGoldStandardFile.getAbsolutePath()));
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
		}
	}
	
	public void setGoldStandard(String[] csvGoldStandardLines) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(String.join("\n", csvGoldStandardLines)));
		CSVParser blah;
		CSVFormat blobl;
		csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
	}
	
	public void run(RelatedWordsExperiment _exp, TestInfo testInfo)
		throws MorphRelativesFinderException, IOException {

		this.experiment = _exp;
		TestDirs testDirs = new TestDirs(testInfo);
		Map<String,String[]> goldStandard = readGoldStandard();

		Map<String,WordOutcome> expOutcomes = null;
		if (!onlyMeasureSpeed) {
			expOutcomes = readExpectedOutcomes(testInfo);
		}

		long startMSecs = StopWatch.nowMSecs();
		Map<String,WordOutcome> actualOutcomes =
			generateActualOutcomes(goldStandard);
		writeActualOutcomes(actualOutcomes, testDirs);
		long elapsedMSecs = StopWatch.elapsedMsecsSince(startMSecs);

		double secsPerWord =
			1.0 * elapsedMSecs / (1000*actualOutcomes.keySet().size());

		writeListOfAffectedWords(expOutcomes, actualOutcomes, goldStandard,
			testInfo);

		compareActualAndExpectedOutcomes(expOutcomes, actualOutcomes, secsPerWord,
			goldStandard, testInfo);
	}

	private void writeListOfAffectedWords(
		Map<String, WordOutcome> expOutcomes,
		Map<String, WordOutcome> actualOutcomes,
		Map<String, String[]> goldStandard, TestInfo testInfo) throws IOException {

		Logger tLogger = LogManager.getLogger("org.iutools.morphrelatives.MorphRelativesFinderEvaluator.writeListOfAffectedWords");
		if (!onlyMeasureSpeed) {
			Set<String> affectedWords = new HashSet<String>();
			for (Map<?, ?> outcomes :
			new Map<?, ?>[]{expOutcomes, actualOutcomes}) {
				for (Object key : outcomes.keySet()) {
					String word = (String) key;
					WordOutcome wordOutcome = (WordOutcome) outcomes.get(key);
					affectedWords.add(word);
					Collections.addAll(affectedWords, wordOutcome.relsProduced);
				}
			}

			for (String word : goldStandard.keySet()) {
				affectedWords.add(word);
				Collections.addAll(affectedWords, goldStandard.get(word));
			}

			Path wordsFile = affectWordsFile(testInfo);
			tLogger.trace("Writing list of affected words to file: " + wordsFile);
			FileWriter writer = null;
			try {
				writer = new FileWriter(wordsFile.toFile());
				List<String> sortedWords = new ArrayList<String>();
				sortedWords.addAll(affectedWords);
				Collections.sort(sortedWords);
				for (String word : sortedWords) {
					writer.write(word + "\n");
				}
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}

		return;
	}

	private Path affectWordsFile(TestInfo testInfo) throws IOException {
		Path wordsFile =
			new TestDirs(testInfo)
			.outputsFile("affectedWords.txt");

		return wordsFile;
	}

	private void compareActualAndExpectedOutcomes(
		Map<String, WordOutcome> expOutcomes,
		Map<String, WordOutcome> actualOutcomes,
		double gotSecsPerWord, Map<String, String[]> goldStandard,
		TestInfo testInfo)
		throws MorphRelativesFinderException, IOException {

		if (!onlyMeasureSpeed) {
			String diffText = "";

			diffText += comparePrecision(expOutcomes, actualOutcomes, goldStandard);
			diffText += compareRecall(expOutcomes, actualOutcomes, goldStandard);
			diffText += compareSecsPerWord(gotSecsPerWord, testInfo);

			if (!diffText.isEmpty()) {
				diffText = howToAddressDifferences(testInfo) + diffText;
				Assertions.fail(diffText);
			}
		}
	}

	private String compareSecsPerWord(double gotSecsPerWord, TestInfo testInfo) throws IOException {
		String errMess = "";
		try {
			AssertRuntime.runtimeHasNotChanged(
				gotSecsPerWord, 0.3, "Secs per word", testInfo);
		} catch (AssertionError e) {
			errMess += e.getMessage();
		}
		return errMess;
	}

	private String howToAddressDifferences(TestInfo testInfo) throws IOException {
		TestDirs testDirs = new TestDirs(testInfo);
		String testMethod = testInfo.getTestMethod().get().getName();

		String expOutcomesFile =
			"iutools/iutools-core/src/test/resources/org/iutools/relatedwords/"+
			testMethod+"/expOutcomes.json";

		String hints =
			"\n\n"+
			"Some of the performance metrics haves changed significantly.                  \n"+
			"More details will be provided below.\n"+
			"\n"+
			"If you have recently made changes to the MorphologicalAnalyser, it could be \n"+
			"that the test is using old morphological decompositions that are still present \n"+
			"the precompiled corpus. If you suspect this, then you can \"patch\" the \n"+
			"compiled corpus by issueing the following commands:\n"+
			"\n"+
			"   iutools_cli recompute_decomps \\\n"+
			"     --input-file \""+affectWordsFile(testInfo)+"\"\n"+
			"\n"+
			"   iutools_cli dump_corpus\n"+
			"\n"+
			"Once you have done this, you should probably commmit and push the iutools-data \n"+
			"project.\n"+
			"\n"+
			"Note also that this should only be considered a temporary solution and that you \n"+
			"should eventually recompute the decompositions of ALL words contained in the \n"+
			"corpus by issueing the command:\n"+
			"\n"+
			"   iutools_cli compile_corpus ???\n"+
			"\n"+
			"On the other hand, if the changes that have happened for some of the words \n"+
			"correspond to the \"new normal\", then you can change the expectations for those \n"+
			"words in the file:\n"+
			"\n"+
			"   "+expOutcomesFile+"\n"+
			"\n"+
			"You can also change the expectations of all words by copying the file:\n"+
			"\n"+
			"   "+actualOutcomesFiles(testDirs).toString()+"\n"+
			"\n"+
			"to file:\n"+
			"\n"+
			"   "+expOutcomesFile+"\n"+
			"\n"
		;
		File hFile = hintsFile(testDirs).toFile();
		FileWriter writer = null;
		try {
			writer = new FileWriter(hFile);
			writer.write(hints);
		} finally {
			if (writer != null) {writer.close();}
		}

		String linkToHintsFile =
			"\n\n"+
			"Some of the performance metrics haves changed significantly.                  \n"+
			"For hints on possible ways to address this failure, see file:\n"+
			"\n"+
			"   "+hFile.toString()+"\n\n"
		;

		return linkToHintsFile;
	}

	private Path hintsFile(TestDirs testDirs) throws IOException {
		Path file = testDirs.outputsFile("hints.txt");
		return file;
	}

	private String comparePrecision(Map<String, WordOutcome> expOutcomes,
		Map<String, WordOutcome> actualOutcomes, Map<String, String[]> goldStandard) {

		String precDiff = "";
		Double expPrecision = computePrecision(expOutcomes, goldStandard);
		Double actualPrecision = computePrecision(actualOutcomes, goldStandard);
		Double tolerance = experiment.precRecTolerance * expPrecision;
		try {
			AssertNumber.performanceHasNotChanged(
				"Precision",
				actualPrecision, expPrecision,
				tolerance);
		} catch (AssertionError e) {
			precDiff += e.getMessage()+"\n\n";
		}

		if (!precDiff.isEmpty()) {
			// Precision has changed significantly. Print the words for
			// which precision has changed
			precDiff += "Below is a list of the words for which Precision has changed.\n\n";
			int diffNum = 0;
			for (String word: outcomeWords(actualOutcomes)) {
				String[] wordGS = goldStandard.get(word);
				WordOutcome expOutcome = expOutcomes.get(word);
				WordOutcome gotOutcome = actualOutcomes.get(word);
				double expPrec = expOutcome.precision(wordGS);
				double gotPrec = gotOutcome.precision(wordGS);
				if (expPrec != gotPrec) {
					diffNum++;
					precDiff += "Diff #"+diffNum+": word="+word+"\n";
					precDiff += "  Used to be:\n"+
						expOutcome.fitnessToGoldStandard(wordGS, "    ")+"\n";
					precDiff += "  Now is:\n"+
						gotOutcome.fitnessToGoldStandard(wordGS, "    ")+"\n";
				}
			}
		}

		return precDiff;
	}

	private Double computePrecision(Map<String, WordOutcome> wordOutcomes,
		Map<String,String[]> goldStandard) {
		return computePrecision(wordOutcomes, goldStandard, (Set<String>)null);
	}

	private Double computePrecision(Map<String, WordOutcome> wordOutcomes,
		Map<String,String[]> goldStandard, Set<String> onlyForWords) {

		int totalRelsProduced = 0;
		int totalGoodRels = 0;
		if (onlyForWords == null) {
			onlyForWords = wordOutcomes.keySet();
		}
		for (String word: onlyForWords) {
			String[] wordGS = goldStandard.get(word);
			WordOutcome outcome = wordOutcomes.get(word);
			totalGoodRels += outcome.correctRelatives(wordGS).size();
			totalRelsProduced += outcome.relsProduced.length;
		}

		double precision = 0.0;
		if (totalRelsProduced > 0) {
			precision = 1.0 * totalGoodRels / totalRelsProduced;
		}

		return precision;
	}

	private String compareRecall(Map<String, WordOutcome> expOutcomes,
		Map<String, WordOutcome> actualOutcomes,
		Map<String, String[]> goldStandard) throws MorphRelativesFinderException {

		Logger logger = LogManager.getLogger("org.iutools.morphrelatives.MorphRelativesFinderEvaluator");
		String recallDiff = "";
		Double expRecall = computeRecall(expOutcomes, goldStandard, actualOutcomes.keySet());
		Double actualRecall = computeRecall(actualOutcomes, goldStandard);
		Double tolerance = experiment.precRecTolerance * expRecall;
		try {
			AssertNumber.performanceHasNotChanged(
				"Recall",
				actualRecall, expRecall,
				tolerance);
		} catch (AssertionError e) {
			recallDiff += e.getMessage()+"\n\n";
		}

		if (!recallDiff.isEmpty()) {
			// Recall has changed significantly. Print the words for
			// which precision has changed
			recallDiff += "Below is a list of the words for which Recall has changed.\n\n";
			int diffNum = 0;
			for (String word: outcomeWords(actualOutcomes)) {
				String[] wordGS = goldStandard.get(word);
				WordOutcome expOutcome = expOutcomes.get(word);
				WordOutcome gotOutcome = actualOutcomes.get(word);
				expRecall = expOutcome.recall(wordGS);
				actualRecall = gotOutcome.recall(wordGS);
				logger.trace("For word: "+word+"expRecall-actualRecall="+(expRecall-actualRecall));
				if (expRecall != actualRecall) {
					diffNum++;
					recallDiff += "Diff #"+diffNum+": word="+word+"\n";
					recallDiff += "  Used to be:\n"+
						expOutcome.fitnessToGoldStandard(wordGS, "    ")+"\n";
					recallDiff += "  Now is:\n"+
						gotOutcome.fitnessToGoldStandard(wordGS, "    ")+"\n";
				}
			}
		}

		return recallDiff;
	}

	private Double computeRecall(Map<String, WordOutcome> wordOutcomes,
		Map<String,String[]> goldStandard) throws MorphRelativesFinderException {
		return computeRecall(wordOutcomes, goldStandard, (Set<String>)null);
	}

	private Double computeRecall(Map<String, WordOutcome> wordOutcomes,
		Map<String,String[]> goldStandard, Set<String> onlyForWords)
		throws MorphRelativesFinderException {

		int totalGoodRels = 0;
		int totalGSRels = 0;
		if (onlyForWords == null) {
			onlyForWords = wordOutcomes.keySet();
		}
		for (String word: onlyForWords) {
			String[] wordGS = goldStandard.get(word);
			if (wordGS == null) {
				throw new MorphRelativesFinderException(
					"No gold standard entry for word: "+word);
			}
			totalGSRels += wordGS.length;
			WordOutcome outcome = wordOutcomes.get(word);
			totalGoodRels += outcome.correctRelatives(wordGS).size();
		}

		double recall = 0.0;
		if (totalGSRels > 0) {
			recall = 1.0 * totalGoodRels / totalGSRels;
		}

		return recall;
	}

	private void writeActualOutcomes(Map<String, WordOutcome> actualOutcomes,
		TestDirs testDirs) throws IOException {
		Path actualOutcomesFile = actualOutcomesFiles(testDirs);
		FileWriter writer = null;
		try {
			writer = new FileWriter(actualOutcomesFile.toFile());
			writer.write("bodyEndMarker=BLANK_LINE\n");
			writer.write("class="+WordOutcome.class.getName()+"\n\n");
			for (String word: outcomeWords(actualOutcomes)) {
				String outcomeJson = PrettyPrinter.print(actualOutcomes.get(word));
				writer.write(outcomeJson+"\n\n");
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private List<String> outcomeWords(Map<String, WordOutcome> outcomes) {
		List<String> words = new ArrayList<String>();
		words.addAll(outcomes.keySet());
		Collections.sort(words);

		return words;
	}

	private Path actualOutcomesFiles(TestDirs testDirs) throws IOException {
		Path filePath = testDirs.outputsFile("actualOutcomes.json");
		return filePath;
	}


	private Map<String, WordOutcome> generateActualOutcomes(
		Map<String, String[]> goldStandard) throws MorphRelativesFinderException {

		Map<String, WordOutcome> outcomes = new HashMap<String, WordOutcome>();
		List<String> words = sortGSWords(goldStandard);
		for (String word: words) {
			MorphologicalRelative[] expansions = relsFinder.findRelatives(word);
			WordOutcome wordOutcome = new WordOutcome(word, expansions);
			outcomes.put(word, wordOutcome);
		}

		int nWords = outcomes.keySet().size();
		Double precision = computePrecision(outcomes, goldStandard);
		Double recall = computeRecall(outcomes, goldStandard);
		System.out.println(
			"Words evaluated: "+nWords+"\n"+
			"Performance:\n"+
			"  Precision : "+precision+"\n"+
			"  Recall    : "+recall+"\n"+
			"\n");

		return outcomes;
	}

	private List<String> sortGSWords(Map<String, String[]> goldStandard) {
		List<String> gsWords = new ArrayList<String>();
		gsWords.addAll(goldStandard.keySet());
		Collections.sort(gsWords);

		return gsWords;
	}

	private Map<String, WordOutcome> readExpectedOutcomes(TestInfo testInfo)
		throws IOException {
		Map<String,WordOutcome> expOutcomes = new HashMap<String, WordOutcome>();
		if (!onlyMeasureSpeed) {

			Path expOutcomesFile = expOutcomesFile(testInfo);
			ObjectStreamReader oReader =
			new ObjectStreamReader(expOutcomesFile.toFile());
			while (true) {
				try {
					WordOutcome anOutcome = (WordOutcome) oReader.readObject();
					if (anOutcome == null) {
						break;
					}
					expOutcomes.put(anOutcome.word, anOutcome);
				} catch (ClassNotFoundException | ObjectStreamReaderException e) {
					throw new IOException(e);
				}
			}
		}

		return expOutcomes;
	}


	private Path expOutcomesFile(TestInfo testInfo) throws IOException {

		String testMethod = testInfo.getTestMethod().get().getName();
		Path filePath = null;
		if (!onlyMeasureSpeed) {
			filePath = Paths.get(ResourceGetter.getResourcePath(
				"org/iutools/relatedwords/" + testMethod+"/expOutcomes.json"));
		}

		return filePath;
	}

	private Map<String, String[]> readGoldStandard()
		throws MorphRelativesFinderException {

		Map<String, String[]> goldStandard = new HashMap<String, String[]>();

		echo("Reading gold standard");
		echo(1);
		{
			Pattern patMotFreq = Pattern.compile("^(.+) \\((\\d+)\\).*$");

			ArrayList<String> listAllGsAlternatives = new ArrayList<String>();

			boolean stop = false;

			int wordCount = 0;
			for (CSVRecord csvRecord : csvParser) {
				if (stop) break;

				// Accessing Values by Column Index
				String motLatinEtFreq = csvRecord.get(0);
				Matcher m = patMotFreq.matcher(motLatinEtFreq);
				String mot = null;
				long freqMotGoogle = -1;
				if (m.matches()) {
					mot = m.group(1);
					freqMotGoogle = Long.parseUnsignedLong(m.group(2));
				} else {
					continue;
				}

				wordCount++;
				if (stopAfterNWords != null &&
				wordCount > stopAfterNWords) {
					break;
				}

				if (experiment.focusOnWord != null && !mot.equals(experiment.focusOnWord)) {
					continue;
				}
				echo("\nReading Gold Standard for word #" + wordCount + ": " + mot + " " + "(" + freqMotGoogle + ")");
				if (mot == null) {continue;}

				//String decMot = String.join(" ",compiledCorpus.getSegmenter().segment(mot))+" \\";
				echo("    Gold Standard reformulations (frequencies in compiled corpus):");
				String[] gsalternatives = (mot + "; " + csvRecord.get(4)).split(";\\s*");
				String[] gsalternativesMorphemes = new String[gsalternatives.length];
				List<String> listgsalternatives = Arrays.asList(gsalternatives);
				listAllGsAlternatives.addAll(listgsalternatives);

				for (int igs = 0; igs < gsalternatives.length; igs++) {
					String gsalternative = gsalternatives[igs];
					long freqGSAlternativeInCorpus = wordFrequency(gsalternative);
					echo("        " + gsalternative + " : " + freqGSAlternativeInCorpus);
					String altDecomp = null;
					try {
						altDecomp = String.join(
							" ",
							relsFinder.compiledCorpus.topDecomposition(gsalternative));
						altDecomp =  DecompositionState.formatDecompStr(altDecomp);
						gsalternativesMorphemes[igs] = altDecomp;
					} catch (Exception e) {
						altDecomp = "";
					}

					goldStandard.put(mot, gsalternatives);

				}
				List<String> listgsalternativesmorphemes = Arrays.asList(gsalternativesMorphemes);
			}
		}
		echo(-1);

		return goldStandard;
	}


	private long wordFrequency(String word)
			throws MorphRelativesFinderException {

		long freqDansCorpus = 0;
		try {
			WordInfo wInfo = relsFinder.compiledCorpus.info4word(word);
			if (wInfo != null) {
				freqDansCorpus = wInfo.frequency;
			}
		} catch (CompiledCorpusException e) {
			throw new MorphRelativesFinderException(e);
		}

		return freqDansCorpus;
	}

	public void setRelsFinder(MorphRelativesFinder finder) {
		this.relsFinder = finder;
	}

	protected void echo(String mess) {
		userIO.echo(mess);
	}

	protected void echo(int level) {
		userIO.echo(level);
	}
}