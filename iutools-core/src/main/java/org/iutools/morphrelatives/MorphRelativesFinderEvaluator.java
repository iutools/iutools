package org.iutools.morphrelatives;

import java.io.*;
import java.nio.charset.Charset;
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
import ca.nrc.testing.TestDirs;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.io.FileUtils;
import org.iutools.morph.Decomposition;
import org.iutools.utilities.StopWatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.junit.Assert;

import ca.nrc.debug.Debug;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

public class MorphRelativesFinderEvaluator {

	private UserIO userIO = new UserIO();
	private PerformanceExpectations expectations = null;

	public List<Object[]> wordOutcomes = new ArrayList<Object[]>();


	public Integer stopAfterNWords = null;
	
	public CSVParser csvParser = null;
	public boolean computeStatsOverSurfaceForms = true;
	public float precision = -1;
	public float recall = -1;
	public float fmeasure = -1;

	int nbTotalCases = 0;
	protected long elapsedTime = -1;

	MorphRelativesFinder relsFinder = null;
	private String focusOnWord;

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
		init__MorphRelativesFinderEvaluator(null, null);
	}
	
	public MorphRelativesFinderEvaluator(String csvGoldStandardFilePath)
		throws MorphRelativesFinderException {
		File goldStandardFile = new File(csvGoldStandardFilePath);
		init__MorphRelativesFinderEvaluator(null, goldStandardFile);
	}

	public MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile) throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(finder, csvGoldStandardFile);
	}

	public void init__MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile) throws MorphRelativesFinderException {
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

	public void setFocusOnWord(String _word) {
		this.focusOnWord = _word;
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
	
	public void setOptionComputeStatsOverSurfaceForms(boolean value) {
		computeStatsOverSurfaceForms = value;
	}
	
	public void run() {

		long startTime = StopWatch.nowMSecs();

		Logger logger = Logger.getLogger("QueryExpanderEvaluator");
		
        try {
            Pattern patMotFreq = Pattern.compile("^(.+) \\((\\d+)\\).*$");
            
            nbTotalCases = 0;
            int nbTotalGoldStandardAlternatives = 0;
            int nbTotalExpansionsFromCorpus = 0;
            int nbTotalExpansionsNotInGSAlternatives = 0;
				int nbTotalCasesWithNoExpansion = 0;
            int nbTotalCasesCouldNotBeDecomposed = 0;
            
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
                    if ( m.matches() ) {
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

                    if (focusOnWord != null && !mot.equals(focusOnWord)) {
                    	continue;
                    }
                    echo("\nReading Gold Standard for word #"+wordCount+": "+mot+" "+"("+freqMotGoogle+")");
                    if (mot==null) continue;
                    
                    //String decMot = String.join(" ",compiledCorpus.getSegmenter().segment(mot))+" \\";
                    nbTotalCases++;
						echo("    Gold Standard reformulations (frequencies in compiled corpus):");
                    String[] gsalternatives = (mot+"; "+csvRecord.get(4)).split(";\\s*");
                    String[] gsalternativesMorphemes = new String[gsalternatives.length];
                    List<String> listgsalternatives = Arrays.asList(gsalternatives);
                    nbTotalGoldStandardAlternatives += gsalternatives.length;
                    listAllGsAlternatives.addAll(listgsalternatives);
                    
                    for (int igs=0; igs<gsalternatives.length; igs++) {	
                    	String gsalternative = gsalternatives[igs];
                    	long freqGSAlternativeInCorpus = freqDansCorpus(gsalternative);
						   echo("        "+gsalternative+" : "+freqGSAlternativeInCorpus);
                    	String altDecomp = null;
                    	try {
                    		altDecomp = String.join(" ", relsFinder.compiledCorpus.decomposeWord(gsalternative));
							altDecomp =
								Decomposition.formatDecompStr(altDecomp);
                        	gsalternativesMorphemes[igs] = altDecomp;
                    	} catch (Exception e) {
                    		altDecomp = "";
                    	}
                    }
                    List<String> listgsalternativesmorphemes = Arrays.asList(gsalternativesMorphemes);

                    echo("    Morphological Relatives found (frequencies in compiled corpus):");
                    try {
                    	MorphologicalRelative[] expansions = relsFinder.findRelatives(mot);
                    	if ( expansions != null ) {
                        	logger.debug(mot+" - expansions: "+expansions.length);
                        	removeTailingBackslashFromDecomps(expansions);
                    		ArrayList<String> listexpansionsmorphemes = new ArrayList<String>();
                    		ArrayList<String> listexpansions = new ArrayList<String>();
                    		nbTotalExpansionsFromCorpus += expansions.length;
                    		if (expansions.length==0) {
                        		nbTotalCasesWithNoExpansion++;
										echo("        0 expansion");
                    		}
                    		Arrays.sort(expansions, (MorphologicalRelative a, MorphologicalRelative b) ->
                    			{
                    				if (a.getFrequency() == b.getFrequency())
                    					return 0;
                    				else
                    					return a.getFrequency() < b.getFrequency()? 1 : -1;
                    			});

                    		int currWordGoodExpansions = 0;
                    		for (MorphologicalRelative expansion : expansions) {
                    			long freqExpansion =expansion.getFrequency();
                    			boolean expansionInGSalternatives = true;
                    			String expansionMorphemes = String.join(" ", expansion.getMorphemes());
                    			expansionMorphemes = Decomposition.formatDecompStr(expansionMorphemes);
                    			if (computeStatsOverSurfaceForms) {
                    				String relative = expansion.getWord();
                    				if ( !listgsalternatives.contains(expansion.getWord()) ) {
                    					logger.debug("****** word: "+expansion.getWord()+" not in gsalternatives "+gsalternatives.toString());
                    					nbTotalExpansionsNotInGSAlternatives++;
                    					expansionInGSalternatives = false;
											echo("  '"+relative+"' WAS NOT in GS alternatives");
                    				} else {
											currWordGoodExpansions++;
											echo("  '"+relative+"' was in GS alternatives");
										}
                    			} else {
                    				if ( !listgsalternativesmorphemes.contains(expansionMorphemes) ) {
                    					nbTotalExpansionsNotInGSAlternatives++;
                    					expansionInGSalternatives = false;
                    				} else {
											currWordGoodExpansions++;
										}
                    			}
									echo("        "+expansion.getWord()+" : "+freqExpansion+(expansionInGSalternatives? " ***":""));
                    			listexpansionsmorphemes.add(expansionMorphemes);
                    			listexpansions.add(expansion.getWord());
                    		}

								wordOutcomes.add(
									new Object[] {
										mot, currWordGoodExpansions,
										expansions.length,gsalternatives.length});

                    		if (computeStatsOverSurfaceForms) {
                    			for (String gsalternative : gsalternatives)
                    				if ( !listexpansions.contains(gsalternative) )
										;
                    		} else {
                    			for (String gsalternativeMorphemes : gsalternativesMorphemes)
                    				if ( !listexpansionsmorphemes.contains(gsalternativeMorphemes) )
										;
                    		}
                    	} else {
                    		logger.debug(mot+" - expansions null");
							nbTotalCasesWithNoExpansion++;
                    		nbTotalCasesCouldNotBeDecomposed++;
								echo("        the word could not be decomposed.");
                    	}
                    } catch(Exception e) {
                    	echo(
                    		"Error getting the expansions for word: "+mot+"\n"+
                    		Debug.printCallStack(e));
                    }
            }
            csvParser.close();
				elapsedTime = StopWatch.elapsedMsecsSince(startTime);

            for (int igsa=0; igsa<listAllGsAlternatives.size(); igsa++) {
					echo((igsa+1)+". "+listAllGsAlternatives.get(igsa));
            }
            
	            echo("\nTotal number of evaluated words: "+nbTotalCases);
	            echo("Total number of alternatives in Gold Standard: "+nbTotalGoldStandardAlternatives);
	            echo("Total number of expansions found in corpus: "+nbTotalExpansionsFromCorpus);
					echo(String.format("Average time per evaluated word: %.2f secs", secsPerCase()));
	            echo("\nTotal number of cases with no expansion found in corpus: "+nbTotalCasesWithNoExpansion+", of which");
	            echo("Total number of cases that could not be decomposed: "+nbTotalCasesCouldNotBeDecomposed);
	            
	            echo("\n\tNo expansion: either the word could not be decomposed"+
	            					"\n\tor the decomposition process timed out,"+
	            					"\n\tor the corpus contains nothing with the word's root.");
	            
	            echo("\nTotal number of corpus expansions not in GS alternatives: "+nbTotalExpansionsNotInGSAlternatives);            
            int nbTotalGoodExpansions = nbTotalExpansionsFromCorpus - nbTotalExpansionsNotInGSAlternatives;
			  	echo("\tTotal number of GOOD corpus expansion: "+nbTotalGoodExpansions);
            
            precision = (float)nbTotalGoodExpansions / (float)nbTotalExpansionsFromCorpus;
            recall = (float)nbTotalGoodExpansions / (float)nbTotalGoldStandardAlternatives;
            fmeasure = 2 * precision * recall / (precision + recall);

			  	echo("Precision = "+precision);
            echo("Recall = "+recall);
            echo("F-measure = "+fmeasure);
            
        } catch(Exception e) {
        	echo("Exception raised: "+e.getClass()+"\n"+e.getMessage());
        	if (csvParser != null)
				try {
					csvParser.close();
				} catch (IOException e1) {
					echo(e1.getMessage());
				}
        }

        echo("\n\n== Word outcomes:\n");
        echo("Total words: "+wordOutcomes.size());
        for (Object[] outcome: wordOutcomes) {
        		String word = (String)outcome[0];
        		Integer goodRels = (Integer)outcome[1];
        		Integer relsProduced = (Integer)outcome[2];
        		Integer gsRels = (Integer)outcome[3];
        		echo("\t\t\tdefineOutcome(\""+word+"\", "+
					goodRels+", "+relsProduced+", "+gsRels+");");
		  }

        if (focusOnWord != null) {
        	Assert.fail("TEST WAS RUN ON A SINGLE WORD!\nRemember to set focusOnWord = null to run the test on all words");
        }
	}

	public void runNew(PerformanceExpectations _exp, TestInfo testInfo)
		throws MorphRelativesFinderException, IOException {
		this.expectations = _exp;
		TestDirs testDirs = new TestDirs(testInfo);
		Map<String,String[]> goldStandard = readGoldStandard();

		Map<String,WordOutcome> actualOutcomes =
			generateActualOutcomes(goldStandard);
		writeActualOutcomes(actualOutcomes, testDirs);

		Map<String,WordOutcome> expOutcomes = readExpectedOutcomes(testInfo);

		writeListOfAffectedWords(expOutcomes, actualOutcomes, goldStandard,
			testInfo);

		compareActualAndExpectedOutcomes(expOutcomes, actualOutcomes,
			goldStandard, testInfo);
	}

	private void writeListOfAffectedWords(
		Map<String, WordOutcome> expOutcomes,
		Map<String, WordOutcome> actualOutcomes,
		Map<String, String[]> goldStandard, TestInfo testInfo) throws IOException {

		Set<String> affectedWords = new HashSet<String>();
		for (Map<?, ?> outcomes:
			new Map<?,?>[] {expOutcomes, actualOutcomes}) {
			for (Object key: outcomes.keySet()) {
				String word = (String)key;
				WordOutcome wordOutcome = (WordOutcome)outcomes.get(key);
				affectedWords.add(word);
				Collections.addAll(affectedWords, wordOutcome.relsProduced);
			}
		}

		for (String word: goldStandard.keySet()) {
			affectedWords.add(word);
			Collections.addAll(affectedWords, goldStandard.get(word));
		}

		Path wordsFile = affectWordsFile(testInfo);
		FileWriter writer = null;
		try {
			writer = new FileWriter(wordsFile.toFile());
			List<String> sortedWords = new ArrayList<String>();
			sortedWords.addAll(affectedWords);
			Collections.sort(sortedWords);
			for (String word: sortedWords) {
				writer.write(word+"\n");
			}
		} finally {
			if (writer != null) {
				writer.close();
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
		Map<String, String[]> goldStandard, TestInfo testInfo)
		throws MorphRelativesFinderException, IOException {

		String diffText = "";

		diffText += comparePrecision(expOutcomes, actualOutcomes, goldStandard);
		diffText += compareRecall(expOutcomes, actualOutcomes, goldStandard);

		if (!diffText.isEmpty()) {
			diffText = howToAddressDifferences(testInfo) + diffText;
			Assertions.fail(diffText);
		}
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
		Double tolerance = expectations.precRecTolerance * expPrecision;
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
		int totalRelsProduced = 0;
		int totalGoodRels = 0;
		for (String word: wordOutcomes.keySet()) {
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

		String recallDiff = "";
		Double expRecall = computeRecall(expOutcomes, goldStandard);
		Double actualRecall = computeRecall(actualOutcomes, goldStandard);
		Double tolerance = expectations.precRecTolerance * expRecall;
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
		int totalGoodRels = 0;
		int totalGSRels = 0;
		for (String word: wordOutcomes.keySet()) {
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

		Path expOutcomesFile = expOutcomesFile(testInfo);
		ObjectStreamReader oReader =
			new ObjectStreamReader(expOutcomesFile.toFile());
		while (true) {
			try {
				WordOutcome anOutcome = (WordOutcome)oReader.readObject();
				if (anOutcome == null) { break; }
				expOutcomes.put(anOutcome.word, anOutcome);
			} catch (ClassNotFoundException | ObjectStreamReaderException e) {
				throw new IOException(e);
			}
		}

		return expOutcomes;
	}

	private Path expOutcomesFile(TestInfo testInfo) throws IOException {
		String testMethod = testInfo.getTestMethod().get().getName();
		Path filePath = Paths.get(ResourceGetter.getResourcePath(
		"org/iutools/relatedwords/" + testMethod+"/expOutcomes.json"));

		return filePath;
	}

	private Map<String, String[]> readGoldStandard()
		throws MorphRelativesFinderException {

		Map<String, String[]> goldStandard = new HashMap<String, String[]>();

		echo("Reading gold standard");
		echo(1);
		{
			Pattern patMotFreq = Pattern.compile("^(.+) \\((\\d+)\\).*$");

			nbTotalCases = 0;

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

				if (focusOnWord != null && !mot.equals(focusOnWord)) {
					continue;
				}
				echo("\nReading Gold Standard for word #" + wordCount + ": " + mot + " " + "(" + freqMotGoogle + ")");
				if (mot == null) {continue;}

				//String decMot = String.join(" ",compiledCorpus.getSegmenter().segment(mot))+" \\";
				nbTotalCases++;
				echo("    Gold Standard reformulations (frequencies in compiled corpus):");
				String[] gsalternatives = (mot + "; " + csvRecord.get(4)).split(";\\s*");
				String[] gsalternativesMorphemes = new String[gsalternatives.length];
				List<String> listgsalternatives = Arrays.asList(gsalternatives);
				listAllGsAlternatives.addAll(listgsalternatives);

				for (int igs = 0; igs < gsalternatives.length; igs++) {
					String gsalternative = gsalternatives[igs];
					long freqGSAlternativeInCorpus = freqDansCorpus(gsalternative);
					echo("        " + gsalternative + " : " + freqGSAlternativeInCorpus);
					String altDecomp = null;
					try {
						altDecomp = String.join(" ", relsFinder.compiledCorpus.decomposeWord(gsalternative));
						altDecomp =
						Decomposition.formatDecompStr(altDecomp);
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


	private long freqDansCorpus(String reformulation) 
			throws MorphRelativesFinderException {

		long freqDansCorpus = 0;
		try {
			WordInfo wInfo = relsFinder.compiledCorpus.info4word(reformulation);
			if (wInfo != null) {
				freqDansCorpus = wInfo.frequency;
			}
		} catch (CompiledCorpusException e) {
			throw new MorphRelativesFinderException(e);
		}
	
		return freqDansCorpus;
	}
	
	private void removeTailingBackslashFromDecomps(
		MorphologicalRelative[] morphologicalRelatives) {
		MorphologicalRelative[] morphRelativesTrimmed =
			new MorphologicalRelative[morphologicalRelatives.length];
		for (int ii=0; ii < morphologicalRelatives.length; ii++) {
			MorphologicalRelative aRelative = morphologicalRelatives[ii];
			String[] aDecomp = aRelative.getMorphemes();
			if (aDecomp != null && aDecomp.length > 0 &&
				aDecomp[aDecomp.length-1].equals("\\")) {
				aDecomp = Arrays.copyOfRange(aDecomp, 0, aDecomp.length-1);
				aRelative.setMorphemes(aDecomp);
			}
		}
	}

	public double secsPerCase() {
		double mSecs = 1.0 * elapsedTime / nbTotalCases;
		double secs = mSecs / 1000;
		return secs;
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