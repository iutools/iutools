package ca.pirurvik.iutools.spellchecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.pirurvik.iutools.corpus.*;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.NumericExpression;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculator;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorException;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactory;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactoryException;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;
import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzerException;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilbin.AnalyzeNumberExpressions;


public class SpellChecker {
	
	public int MAX_SEQ_LEN = 3;

	public int MIN_NGRAM_LEN = 3;
	public int MAX_NGRAM_LEN = 4;


	public int MAX_CANDIDATES = 2000;
	public int DEFAULT_CORRECTIONS = 5;
	
	/** Maximum msecs allowed for decomposing a word during 
	 *  spell checker
	 */
	private final long MAX_DECOMP_MSECS = 5*1000;

	protected String esIndexNameRoot = null;

	protected CompiledCorpus explicitlyCorrectWords = null;

	// TODO-June2020: Can we get rid of this and use the explicitlyCorrectWords
	//   CompiledCorpus instance instead?
	/** 
	 * Words that are NOT numeric expressions and were EXPLICLITLY as being 
	 * correct
	 */
	protected Set<String> explicitlyCorrect_NonNumeric = new HashSet<String>();

	// TODO-June2020: Can we get rid of this and use the explicitlyCorrectWords
	//   CompiledCorpus instance instead?	
	/** 
	 * Words that ARE numeric expressions and were EXPLICLITLY as being 
	 * correct
	 */
	protected Set<String> explicitlyCorrect_Numeric = new HashSet<String>();

	/** If true, partial corrections are enabled. That measns the spell checker
	 *  will identify the longest leading and tailing strings that seem 
	 *  correctly spelled.*/
	private boolean partialCorrectionEnabled = false;
		public SpellChecker setPartialCorrectionEnabled(boolean flag) {
			partialCorrectionEnabled = flag;
			return this;
		}
		public SpellChecker enablePartialCorrections() {
			partialCorrectionEnabled = true;
			return this;
		}
		public SpellChecker disablePartialCorrections() {
			partialCorrectionEnabled = false;
			return this;
		}
	
	public transient EditDistanceCalculator editDistanceCalculator;
	public transient boolean verbose = true;
	
	public CompiledCorpus corpus = null;
	
	private static StringSegmenter_IUMorpheme segmenter = null;
	private transient String[] makeUpWords = new String[] {"sivu","sia"};
	private static ArrayList<String> latinSingleInuktitutCharacters = new ArrayList<String>();
	static {
		for (int i=0; i<Syllabics.syllabicsToRomanICI.length; i++) {
			latinSingleInuktitutCharacters.add(Syllabics.syllabicsToRomanICI[i][1]);
		};
	}

	public SpellChecker() throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker_ES((String)null, (Boolean)null);
	}

	public SpellChecker(String corpusName, boolean mustBeRegistered)
		throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker_ES(corpusName, mustBeRegistered);
	}

	public SpellChecker(String corpusName) throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker_ES(corpusName, (Boolean)null);
	}

	public void setDictionaryFromCorpus() throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = CompiledCorpusRegistry.getCorpusWithName();
			__processCorpus();
		} catch (CompiledCorpusRegistryException e) {
			throw new SpellCheckerException(e);
		}
	}

	private void init_SpellChecker_ES(
		String _corpusName, Boolean mustBeRegistered) throws SpellCheckerException {

		if (mustBeRegistered == null) {
			mustBeRegistered = true;
		}
		if (_corpusName == null) {
			_corpusName = CompiledCorpusRegistry.defaultCorpusName;
		}

		esIndexNameRoot = _corpusName;
		if (!mustBeRegistered) {
			corpus = new CompiledCorpus(_corpusName);
		} else {
			try {
				corpus = CompiledCorpusRegistry.getCorpusWithName(_corpusName);
			} catch (CompiledCorpusRegistryException e) {
				throw new SpellCheckerException(
						"No registered corpus by the name of "+_corpusName, e);
			}
		}
		explicitlyCorrectWords =
				new CompiledCorpus(
						explicitlyCorrectWordsIndexName());

		try {
			editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
			segmenter = new StringSegmenter_IUMorpheme();
			setDictionaryFromCorpus(corpus);
		} catch (StringSegmenterException | FileNotFoundException | SpellCheckerException | ConfigException e) {
			throw new SpellCheckerException(e);
		}

		return;
	}


	public void setDictionaryFromCorpus(CompiledCorpus _corpus) throws SpellCheckerException, ConfigException, FileNotFoundException {
		this.corpus = _corpus;
		__processCorpus();
	}

	public void setDictionaryFromCorpus(String _corpusName) throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = CompiledCorpusRegistry.getCorpusWithName(_corpusName);
			setDictionaryFromCorpus(corpus);
		} catch (CompiledCorpusRegistryException e) {
			throw new SpellCheckerException(e);
		}
	}

	public void setDictionaryFromCorpus(File compiledCorpusFile) throws SpellCheckerException {
		try {
			CompiledCorpus corpus = RW_CompiledCorpus.read(compiledCorpusFile);
			setDictionaryFromCorpus(corpus);
		} catch (Exception e) {
			throw new SpellCheckerException(
					"Could not create the compiled corpus from file: " + compiledCorpusFile.toString(), e);
		}

		return;
	}
	
	protected void __processCorpus() throws ConfigException, FileNotFoundException {
		// Ideally, these should be compiled along with allWords and ngramsStats during corpus compilation
		String dataPath = IUConfig.getIUDataPath();
		FileReader fr = new FileReader(dataPath+"/data/numericTermsCorpus.json");
		AnalyzeNumberExpressions numberExpressionsAnalysis = new Gson().fromJson(fr, AnalyzeNumberExpressions.class);

		return;
	}

	public void setEditDistanceAlgorithm(EditDistanceCalculatorFactory.DistanceMethod name) throws ClassNotFoundException, EditDistanceCalculatorFactoryException {
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator(name);
	}
	
	public void setVerbose(boolean value) {
		verbose = value;
	}
	
	public void addExplicitlyCorrectWord(String word) throws SpellCheckerException {
		try {
			explicitlyCorrectWords.addWordOccurence(word);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		String[] numericTermParts = null;
		boolean wordIsNumericTerm = (numericTermParts = splitNumericExpression(word)) != null;
		if (wordIsNumericTerm) {
			explicitlyCorrect_Numeric.add(word);
		} else {
			explicitlyCorrect_NonNumeric.add(word);
		}
	}
		
	public void deleteExplicitlyCorrectWord(String word) throws SpellCheckerException {
		try {
			explicitlyCorrectWords.deleteWord(word);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}
	}

	public void saveToFile(File checkerFile) throws IOException {
		FileWriter saveFile = new FileWriter(checkerFile);
		Gson gson = new Gson();
		gson.toJson(this, saveFile);
		saveFile.flush();
		saveFile.close();
	}

	private int getMaxSeqLen() {
		return this.MAX_SEQ_LEN;
	}

	public SpellingCorrection correctWord(String word) throws SpellCheckerException {
		return correctWord(word,-1);
	}

	public SpellingCorrection correctWord(String word, int maxCorrections) throws SpellCheckerException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.correctWord");
		long start = StopWatch.nowMSecs();

		SpellDebug.trace("SpellChecker.correctWord",
				"Invoked on word="+word,
				word, null);
		
		boolean wordIsSyllabic = Syllabics.allInuktitut(word);
		
		String wordInLatin = word;
		if (wordIsSyllabic) {
			wordInLatin = TransCoder.unicodeToRoman(word);
		}
		
		SpellingCorrection corr = new SpellingCorrection(word);
		corr.wasMispelled = isMispelled(wordInLatin);		
		tLogger.debug("wasMispelled= "+corr.wasMispelled);

		SpellDebug.trace("SpellChecker.correctWord",
				"corr.wasMispelled="+corr.wasMispelled,
				word, null);
		
		if (corr.wasMispelled) {
			// set ngramStats and suite of words for candidates according to type of word (normal word or numeric expression)
			String[] numericTermParts = splitNumericExpression(wordInLatin);
			boolean wordIsNumericTerm = numericTermParts != null;

			SpellDebug.trace("SpellChecker.correctWord",
				"wordIsNumericTerm="+wordIsNumericTerm,
				word, null);

			if (partialCorrectionEnabled) {
				SpellDebug.trace("SpellChecker.correctWord",
						"Computing longest correct head and tail of the word",
						word, null);
				computeCorrectPortions(wordInLatin, corr);
			}

			SpellDebug.trace("SpellChecker.correctWord",
				"Computing 1st pass candidates",
				word, null);

			List<ScoredSpelling> candidates =
				candidatesWithSimilarNgrams(wordInLatin, wordIsNumericTerm);

			SpellDebug.trace("SpellChecker.correctWord",
			"Number of 1st pass candidates="+(candidates.size()),
			word, null);

			SpellDebug.containsDuplicates("SpellChecker.correctWord",
				"1st pass candidates", word, candidates);

			SpellDebug.containsCorrection(
				"SpellChecker.correctWord",
				"First pass candidates",
				word, "nunavut", candidates);

			SpellDebug.trace("SpellChecker.correctWord",
				"Computing candidates similariy using "+editDistanceCalculator.getClass(),
				word, null);

			List<ScoredSpelling> scoredSpellings =
				computeCandidateSimilarities(wordInLatin, candidates);

			SpellDebug.containsDuplicates(
				"SpellChecker.correctWord",
				"candidates with similarities", word, scoredSpellings);

			SpellDebug.containsCorrection(
					"SpellChecker.correctWord",
					"UNSORTED scored spellings",
					word, scoredSpellings);

			List<ScoredSpelling> sortedSpellings =
				sortCandidatesByOverallScore(scoredSpellings);

			SpellDebug.containsDuplicates(
				"SpellChecker.correctWord",
				"SORTED scored spellings", word, sortedSpellings);

			SpellDebug.containsCorrection(
				"SpellChecker.correctWord",
				"SORTED scored spellings",
				word, scoredSpellings);

			if (wordIsNumericTerm) {
				for (int ic=0; ic<scoredSpellings.size(); ic++) {
					ScoredSpelling scoredCandidate = sortedSpellings.get(ic);
					scoredCandidate.spelling = 
						scoredCandidate.spelling.replaceAll("\\d+-*",numericTermParts[0]);
				}
			}

			sortedSpellings = selectTopCandidates(maxCorrections, sortedSpellings);

			SpellDebug.containsDuplicates(
				"TOP PORTION of SORTED scored spellings",
				"SORTED scored spellings", word, sortedSpellings);

			SpellDebug.containsCorrection(
				"SpellChecker.correctWord",
				"TOP PORTION of SORTED scored spellings",
				word, scoredSpellings);

			if (SpellDebug.traceIsActive("SpellChecker.correctWord", word)) {
				SpellDebug.trace("SpellChecker.correctWord",
						"TOP PORTION of SORTED scored spellings is:\n"+
							PrettyPrinter.print(scoredSpellings),
						word, null);
			}
			
	 		corr.setPossibleSpellings(sortedSpellings);
		}
		
		if (wordIsSyllabic) {
			transcodeCandidatesToSyllabic(corr);
		}

		long elapsed = StopWatch.elapsedMsecsSince(start);
		tLogger.trace("word="+word+" took "+elapsed+"msecs");
	
 		return corr;
	}

	private List<ScoredSpelling> selectTopCandidates(int maxCorrections,
		List<ScoredSpelling> sortedCandidates) throws SpellCheckerException {
		List<ScoredSpelling> topCandidates = sortedCandidates;
		if (maxCorrections != -1) {
			topCandidates = new ArrayList<ScoredSpelling>();
			Iterator<ScoredSpelling> iterCand = sortedCandidates.iterator();
			while (iterCand.hasNext() && topCandidates.size() < maxCorrections) {
				ScoredSpelling candidate = iterCand.next();
				// Make sure all the retained candidates are correctly spelled
				//
				try {
					if (!isMispelled(candidate.spelling)) {
						topCandidates.add(candidate);
					}
				} catch (SpellCheckerException e) {
					throw new SpellCheckerException(e);
				}
			}
		}

		return topCandidates;
	}

	protected void computeCorrectPortions(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
		computeCorrectLead(badWordRoman, corr);
		computeCorrectTail(badWordRoman, corr);
	}

	private void computeCorrectLead(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
		
		final int MAX_WORDS_TO_TRY = 5;

		String longestCorrectLead = null;
		for (int endPos=badWordRoman.length()-1; endPos > 3; endPos--) {
			//
			// Loop through all the leading strings L of the bad word, starting 
			// the complete bad word and removing one tailing character at a time, 
			// until we find a L that satifies the following conditions:
			// 
			// - There is a correctly spelled word W that also starts with L
			// - L does not span across phonemes. In other words,
			//   the last character L corresponds to the end of a 
			//   morpheme in W.
			//
			String lead = badWordRoman.substring(0, endPos-1);
			Iterator<String> iterWords =
				wordsContainingNgram("^"+lead);
			boolean wordWasFoundForLead = false;
			
			int wordCount = 0;
			while (iterWords.hasNext()) {
				String aWord = iterWords.next();
				wordCount++;
				if (wordCount > 5) {
					break;
				}
				if (leadRespectsMorphemeBoundaries(lead, aWord)) {
					// Found a word with the right characteristics
					wordWasFoundForLead = true;					
					longestCorrectLead = lead;
					break;
				}
			}
			
			if (wordWasFoundForLead) {
				break;
			}			
		}
		corr.setCorrectLead(longestCorrectLead);
	}
	
	protected boolean leadRespectsMorphemeBoundaries(String lead, String word) 
			throws SpellCheckerException {

		Boolean answer = null;
		
		Decomposition[] decomps = null;
		try {
			 decomps = 
				new MorphologicalAnalyzer()
						.setTimeout(MAX_DECOMP_MSECS)
						.activateTimeout()
						.decomposeWord(word);
		} catch(TimeoutException e) {
			answer = false;
		} catch(MorphologicalAnalyzerException | LinguisticDataException e) {
			throw new SpellCheckerException(e);
		}
		
		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = aDecomp.morphemeSurfaceForms();

				String morphLead = "";
				for (String morph: morphemes) {
					morphLead += morph;
					if (morphLead.equals(lead)) {
						answer = true;
						break;
					}
				}
				if (answer != null) { break; }
			}
		}
		
		if (answer == null) { answer = false; }
		
		return answer.booleanValue();
	}
	

	private void computeCorrectTail(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
		
		final int MAX_WORDS_TO_TRY = 5;
		
		String longestCorrectTail = null;
		for (int startPos=0; startPos < badWordRoman.length()-2; startPos++) {
			//
			// Loop through all the tailing strings L of the bad word, starting 
			// the complete bad word and removing one leading character at a time, 
			// until we find a L that satifies the following conditions:
			// 
			// - There is a correctly spelled word W that also starts with L
			// - L does not span across phonemes. In other words,
			//   the last character L corresponds to the end of a 
			//   morpheme in W.
			//
			String tail = badWordRoman.substring(startPos);
			Iterator<String> iterWords = wordsContainingNgram(tail+"$");
			boolean wordWasFoundForTail = false;
			int wordCount = 0;
			while (iterWords.hasNext()) {
				String aWord = iterWords.next();
				wordCount++;
				if (wordCount > 5) {
					break;
				}
				if (tailRespectsMorphemeBoundaries(tail, aWord)) {
					// Found a word with the right characteristics
					wordWasFoundForTail = true;					
					longestCorrectTail = tail;
					break;
				}
			}
			
			if (wordWasFoundForTail) {
				break;
			}			
		}
		corr.setCorrectTail(longestCorrectTail);
	}
	
	public boolean tailRespectsMorphemeBoundaries(String tail, String word) 
			throws SpellCheckerException {
		
		Boolean answer = null;
		
		Decomposition[] decomps = null;
		try {
			 decomps = 
				new MorphologicalAnalyzer()
						.setTimeout(MAX_DECOMP_MSECS)
						.activateTimeout()
						.decomposeWord(word);
		} catch(TimeoutException e) {
			answer = false;
		} catch(MorphologicalAnalyzerException | LinguisticDataException e) {
			throw new SpellCheckerException(e);
		}
		
		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = aDecomp.morphemeSurfaceForms();

				String morphTail = "";
				for (int ii=morphemes.size()-1; ii > 0; ii--) {
					String morph = morphemes.get(ii);
					morphTail = morph + morphTail;
					if (morphTail.equals(tail)) {
						answer = true;
						break;
					}
				}
				if (answer != null) { break; }
			}
		}
		
		if (answer == null) { answer = false; }
		
		return answer.booleanValue();
	}
		
	
	/**
	 * Transcode a list of scored candidate spellings to 
	 * syllabic.
	 */
	private void transcodeCandidatesToSyllabic(SpellingCorrection corr) {
		for (int ic=0; ic < corr.scoredCandidates.size(); ic++) {
			ScoredSpelling candidate = corr.scoredCandidates.get(ic);
			candidate.spelling = TransCoder.romanToUnicode(candidate.spelling);
		}
		
		return;
	}


	/*
	 * A term is considered ok if:
	 *   - it is recorded as successfully decomposed by the IMA during the compilation of the Hansard corpus, or
	 *   - it consists of digits only, or
	 *   - it consists of only 1 syllabic character (latin equivalent of)
	 *   - it is not recorded as not decomposed by the IMA during the compilation of the Hansard corpus, or
	 *   - it is a punctuation mark
	 *   
	 * A word is considered mispelled if:
	 *   - it is recorded as UNsuccessfully decomposed by the IMA during the compilation of the Nunavut corpus, or
	 *   - it cannot be decomposed by the IMA (if never encountered in the Hansard corpus)
	 */
	protected Boolean isMispelled(String word) throws SpellCheckerException {
		Logger logger = Logger.getLogger("SpellChecker.isMispelled");
		logger.debug("word: "+word);

		Boolean wordIsMispelled = null;
		if (wordIsMispelled == null) {

			if (isExplicitlyCorrect(word)) {
				logger.debug("word is was explicity tagged as being correct");
				wordIsMispelled = false;
			}

			if (wordIsMispelled == null && corpus != null) {
				try {
					WordInfo wInfo = corpus.info4word(word);
					if (wInfo != null && wInfo.totalDecompositions > 0) {
						wordIsMispelled = false;
						logger.debug("Corpus contains some decompositions for this word");
					}
				} catch (CompiledCorpusException e) {
					throw new SpellCheckerException(e);
				}
			}

			if (wordIsMispelled == null && word.matches("^[0-9]+$")) {
				logger.debug("word is all digits");
				wordIsMispelled = false;
			}

			if (wordIsMispelled == null && latinSingleInuktitutCharacters.contains(word)) {
				logger.debug("single inuktitut character");
				wordIsMispelled = false;
			}

			if (wordIsMispelled == null && wordContainsMoreThanTwoConsecutiveConsonants(word)) {
				logger.debug("more than 2 consecutive consonants in the word");
				wordIsMispelled = true;
			}

			String[] numericTermParts = null;
			if (wordIsMispelled == null && (numericTermParts = splitNumericExpression(word)) != null) {
				logger.debug("numeric expression: " + word + " (" + numericTermParts[1] + ")");
				boolean pseudoWordWithSuffixAnalysesWithSuccess = assessEndingWithIMA(numericTermParts[1]);
				wordIsMispelled = !pseudoWordWithSuffixAnalysesWithSuccess;
				logger.debug("numeric expression - wordIsMispelled: " + wordIsMispelled);
			}

			if (wordIsMispelled == null && wordIsPunctuation(word)) {
				logger.debug("word is punctuation");
				wordIsMispelled = false;
			}

			if (wordIsMispelled == null) {
				try {
					String[] segments = segmenter.segment(word);
					logger.debug("word submitted to IMA: " + word);
					if (segments == null || segments.length == 0) {
						wordIsMispelled = true;
					}
				} catch (TimeoutException e) {
					wordIsMispelled = true;
				} catch (StringSegmenterException | LinguisticDataException e) {
					throw new SpellCheckerException(e);
				}
				logger.debug("word submitted to IMA - mispelled: " + wordIsMispelled);
			}

			if (wordIsMispelled == null) {
				wordIsMispelled = false;
			}
		}
		
		return wordIsMispelled;
	}

	private boolean isExplicitlyCorrect(String word) throws SpellCheckerException {
		boolean answer = false;
		try {
			if (explicitlyCorrectWords != null) {
				answer = explicitlyCorrectWords.containsWord(word);
			}
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}
		return answer;
	}
	
	protected boolean wordIsPunctuation(String word) {
		Pattern p = Pattern.compile("(\\p{Punct}|[\u2013\u2212])+");
		Matcher mp = p.matcher(word);
		return mp.matches();
	}


	/**
	 * Check if a word is a numeric expression of the form
	 * 
	 *    DDDDD suffix1 suffix2 etc...
	 *    
	 * If so, split it into parts:
	 * 
	 *    - Numeric part
	 *    - Suffixes
	 *    
	 * Otherwise, return null
	 * 
	 * @param word
	 * @return
	 */
	protected String[] splitNumericExpression(String word) {
		NumericExpression numericExpression = NumericExpression.tokenIsNumberWithSuffix(word);
		if (numericExpression != null)
			return new String[] { numericExpression.numericFrontPart+numericExpression.separator, numericExpression.morphemicEndPart };
		else
			return null;
	}

	protected boolean wordContainsMoreThanTwoConsecutiveConsonants(String word) {
		Logger logger = Logger.getLogger("SpellChecker.wordContainsMoreThanTwoConsecutiveConsonants");
		boolean result = false;
		String wordInSimplifiedOrthography = Orthography.simplifiedOrthography(word, false);
		logger.debug("wordInSimplifiedOrthography= "+wordInSimplifiedOrthography+" ("+word+")");
		Pattern p = Pattern.compile("[gjklmnprstvN]{3,}");
		Matcher mp = p.matcher(wordInSimplifiedOrthography);
		if (mp.find()) {
			logger.debug("match= "+mp.group());
			result = true;
		} 
		
		return result;
	}


	private List<ScoredSpelling> sortCandidatesByOverallScore(
		List<ScoredSpelling> spellings) {

		Collections.sort(spellings,
			(ScoredSpelling p1, ScoredSpelling p2) -> {
				int score = p1.ngramSim.compareTo(p2.ngramSim);
				if (score == 0) {
					// Use frequency to break ties in the value of ngram
					// similarity
					score = p1.frequency.compareTo(p2.frequency);
				}
				return score;
			}
		);

		return spellings;
	}

	protected List<ScoredSpelling> computeCandidateSimilarities(
		String badWord, List<ScoredSpelling> candidates) throws SpellCheckerException {
		SpellDebug.trace("SpellChecker.computeCandidateSimilarities",
				"Invoked on word="+badWord+
					", editDistanceCalculator=\n"+editDistanceCalculator.getClass()+
					PrettyPrinter.print(editDistanceCalculator),
				badWord, null);
		List<ScoredSpelling> scoredCandidates = new ArrayList<ScoredSpelling>();

		for (ScoredSpelling aCandidate: candidates) {
			double ngramSimilarity =
				computeCandidateNgramSimilarity(badWord, aCandidate.spelling);
			aCandidate.ngramSim = ngramSimilarity;
		}
		
		return candidates;
	}

	private double computeCandidateNgramSimilarity(String badWord, String candidate) throws SpellCheckerException {

		SpellDebug.trace("SpellChecker.computeCandidateSimilarity",
			"Invoked, editDistanceCalculator="+editDistanceCalculator.getClass(),
			badWord, candidate);
		
		double distance;
		try {
			distance = editDistanceCalculator.distance(badWord, candidate);
		} catch (EditDistanceCalculatorException e) {
			throw new SpellCheckerException(e);
		}

		SpellDebug.trace("SpellChecker.computeCandidateSimilarity",
				"returning distance="+distance,
				badWord, candidate);

		return distance;
	}

	public List<ScoredSpelling> candidatesWithSimilarNgrams(String badWord,
	   boolean wordIsNumericTerm) throws SpellCheckerException {

		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.candidatesWithSimilarNgrams");
		tLogger.trace("Starting");

		long start = StopWatch.nowMSecs();

		// 1. compile ngrams of badWord
		// 2. compile IDF of each ngram
		// 3. add words for top IDFs to the set of candidates until the number 
		//    of candidates exceeds the maximum
		// 4. compute scores for each word and order words highest score first
		//    compute edit distance, etc.
		
		tLogger.trace("Computing the most significant NGrams for the mis-spelled words");
		
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMin(MIN_NGRAM_LEN).setMax(MAX_NGRAM_LEN);
		ngramCompiler.includeExtremities(true);
		
		// Step 1: compile ngrams for the bad word
		//
		String[] badWordNgrams = ngrams4word(badWord, ngramCompiler);
		
		// Step 2: compile Inverse Document Frequency (IDF) of each 
		// ngram and sort them from highest to lowest IDF 
		//
		//    IDF(word) = 1 / (#words with this ngram + 1)
		//
		Pair<String,Double>[] ngramFreqs =
				computeNgramFrequencies(badWordNgrams);

		SpellDebug.containsNgramsToTrace(
		"SpellChecker.firstPassCandidates_TFIDF",
		"Most significant ngrams of the misspelled word",
			(String)null, (String)null, ngramFreqs);

		SpellDebug.traceNgrams("SpellChecker.firstPassCandidates_TFIDF",
			"Most significant ngrams of the misspelled word",
			(String)null, (String)null, ngramFreqs);

		
		// Step 3: Find words that most closely match the ngrams of the bad 
		// (up to a maximum of MAX_CANDIDATES
		//

		tLogger.trace("Finding candidates whose NGrams best matches those of the misspelled word");

		List<ScoredSpelling> candidates =
			candidatesWithBestNGramsMatch(ngramFreqs);

		tLogger.trace("Scoring candidates in terms of similarity to the mis-spelled word");
		
		// Step 4: compute scores for each word and sort them from highest to
		//   lowest score.
		//
		List<ScoredSpelling> sortedCandidates =
			sortCandidatesByNgramSimilarity(wordIsNumericTerm, candidates,
				badWordNgrams, ngramFreqs, ngramCompiler);

		SpellDebug.containsCorrection("SpellChecker.firstPassCandidates_TFIDF",
				"Returned list sortedCandidates", badWord,
				sortedCandidates);

		tLogger.trace("Returning candidates list of size="+sortedCandidates.size());

		long elapsed = StopWatch.elapsedMsecsSince(start);
		tLogger.trace("word="+badWord+" took "+elapsed+"msecs");

		return sortedCandidates;
	}

	private Pair<String,Double>[] computeNgramFrequencies(String[] ngrams) throws SpellCheckerException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.computeNgramIDFs");

		long start = StopWatch.nowMSecs();

		Pair<String,Double> ngramFreqs[] = new Pair[ngrams.length];

		for (int i=0; i<ngrams.length; i++) {
			Long ngramFreq = ngramFrequency(ngrams[i]);
			tLogger.trace("for ngram="+ngrams[i]+", ngramFreq="+ngramFreq);
			ngramFreqs[i] = new Pair<String,Double>(ngrams[i],1.0*ngramFreq);
		}
		IDFComparator dcomparator = new IDFComparator();
		Arrays.sort(ngramFreqs,dcomparator);

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning idf="+PrettyPrinter.print(ngramFreqs));
			tLogger.trace("completed in "+StopWatch.elapsedMsecsSince(start)+"msecs");
		}
		return ngramFreqs;
	}

	public long ngramFrequency(String ngram) throws SpellCheckerException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.ngramFrequency");
		long freq = 0;
		try {
			freq = corpus.totalWordsWithCharNgram(
					ngram, CompiledCorpus.SearchOption.EXCL_MISSPELLED);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		tLogger.trace("for ngram="+ngram+"; returning freq="+freq);
		return freq;
	}

	private List<ScoredSpelling> candidatesWithBestNGramsMatch(
		Pair<String, Double>[] idf)
		throws SpellCheckerException {

		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.candidatesWithBestNGramsMatch");

		long start = StopWatch.nowMSecs();
		tLogger.trace("Started");

		Set<ScoredSpelling> candidateSpellingsSet = new HashSet<ScoredSpelling>();
		for (int i=0; i<idf.length; i++) {

			String ngram = idf[i].getFirst();
			Double ngramIDF = idf[i].getSecond();

			Set<ScoredSpelling> candidateSpellingsWithNgram =
					new HashSet<ScoredSpelling>();

			tLogger.trace("adding candidates that contain ngram=" + ngram + " (ngramIDF=" + ngramIDF + ")");

			Iterator<WordInfo> iterWinfoCandaWithNgram =
				winfosContainingNgram(
					ngram, CompiledCorpus.SearchOption.EXCL_MISSPELLED);
			while (iterWinfoCandaWithNgram.hasNext()) {
				WordInfo winfo = iterWinfoCandaWithNgram.next();
				ScoredSpelling candidate = new ScoredSpelling(winfo.word);
				candidate.frequency = winfo.frequency;
				candidateSpellingsWithNgram.add(candidate);
			}

			candidateSpellingsSet.addAll(candidateSpellingsWithNgram);

			SpellDebug.containsCorrection(
					"SpellChecker.candidatesWithBestNGramsMatch",
					"After adding words containing ngram=" + ngram,
					null, candidateSpellingsSet);

			tLogger.trace("DONE adding candidates that contain ngram=" + ngram + "; total added = " + candidateSpellingsSet.size());

			if (candidateSpellingsSet.size() > MAX_CANDIDATES) {
				break;
			}
		}

		SpellDebug.containsDuplicates(
			"SpellChecker.candidatesWithBestNGramsMatch",
			"Candidates with best ngram match",
			null, candidateSpellingsSet);

		long elapsed = 0;
		elapsed = StopWatch.elapsedMsecsSince(start);

		tLogger.trace("Completed in "+elapsed+"msecs");

		List<ScoredSpelling> candidateSpellingsList =
			new ArrayList<ScoredSpelling>();
		candidateSpellingsList.addAll(candidateSpellingsSet);
		
		return candidateSpellingsList;
	}

	private List<ScoredSpelling> sortCandidatesByNgramSimilarity(
		boolean onlyNumericTerms, List<ScoredSpelling> initialCands,
		String[] badWordNGrams, Pair<String, Double>[] badWordNgramFreqs,
		NgramCompiler ngramCompiler) {

		Map<String,Long> wordFreqs = new HashMap<String,Long>();
		Set<String> initialWords = new HashSet<String>();
		for (ScoredSpelling aSpelling: initialCands) {
			initialWords.add(aSpelling.spelling);
			wordFreqs.put(aSpelling.spelling, aSpelling.frequency);
		}
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.sortCandidatesByNgramSimilarity");
		tLogger.trace("invoked");

		List<ScoredSpelling> candidates = new ArrayList<ScoredSpelling>();
		candidates.addAll(initialCands);
		if (onlyNumericTerms) {
			candidates = keepOnlyNumericTerms(initialCands);
		}
		
		Map<String,Double> badWordNgramInvFreqHash = new HashMap<String,Double>();
		for (Pair<String,Double> ngramInfo: badWordNgramFreqs) {
			badWordNgramInvFreqHash.put(
				ngramInfo.getFirst(),
				inverseFrequency(ngramInfo.getSecond()));
		}
		
		Set<String> ngramsOfBadWord_Set = new HashSet<String>();
		for (String ngram: badWordNGrams) {
			ngramsOfBadWord_Set.add(ngram);
		}
		
		List<Pair<String,Double>> scoreValues = new ArrayList<Pair<String,Double>>();
		Iterator<ScoredSpelling> it = candidates.iterator();
		while (it.hasNext()) {
			ScoredSpelling candSpelling = it.next();
			String candidate = candSpelling.spelling;
			Set<String> ngramsOfCandidate = ngramCompiler.compile(candidate);
			Set<String> all = new HashSet<String>();
			for (String ngram: badWordNGrams) {
				all.add(ngram);
			}
			all.addAll(ngramsOfCandidate);
			double totalScore = 0;
			Iterator<String> itall = all.iterator();
			while (itall.hasNext()) {
				String el = itall.next();
				if (ngramsOfBadWord_Set.contains(el) && ngramsOfCandidate.contains(el)) {
					Double score = badWordNgramInvFreqHash.get(el);
					if (score != null) {
						totalScore += score;
					}
				}
			}
			candSpelling.ngramSim = totalScore;
			scoreValues.add(new Pair<String,Double>(candidate,totalScore));
		}

		Collections.sort(candidates,
			(ScoredSpelling p1, ScoredSpelling p2) -> {
				int score = p2.ngramSim.compareTo(p1.ngramSim);
				if (score == 0) {
					score = p1.spelling.compareTo(p2.spelling);
				}
				return score;
			});

		tLogger.trace("finished");

		return candidates;
	}
	

	private List<ScoredSpelling> keepOnlyNumericTerms(List<ScoredSpelling> initialCands) {
		List<ScoredSpelling> filteredCands = new ArrayList<ScoredSpelling>();
		for (ScoredSpelling aCand: initialCands) {
			String[] numericParts = splitNumericExpression(aCand.spelling);
			if (numericParts != null) {
				filteredCands.add(aCand);
			}
		}
		return filteredCands;
	}
	
	private String[] ngrams4word(String word, 
			NgramCompiler ngramCompiler) {
		Set<String>ngramsOfBadWord = ngramCompiler.compile(word);
		String[] ngrams = ngramsOfBadWord.toArray(new String[] {});
		
		if (SpellDebug.traceIsActive("SpellChecker.ngrams4word", word)) {
			SpellDebug.trace("SpellChecker.ngrams4word", 
					"word ngrams=['"+String.join("', '", ngrams)+"']", 
					word, null);
		}
		
		return ngrams;
	}

	public boolean knowsWord(String word) throws SpellCheckerException {
		WordInfo winfo = null;
		try {
			winfo = corpus.info4word(word);
			if (winfo == null) {
				winfo = explicitlyCorrectWords.info4word(word);
			}
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		boolean answer = (winfo != null);

		return answer;
	}

	public String corpusIndexName() {
		String name = corpus.getIndexName();
		return name;
	}

	protected String explicitlyCorrectWordsIndexName() {
		return esIndexNameRoot+"_EXPLICLTY_CORRECT";
	}

	public class IDFComparator implements Comparator<Pair<String,Double>> {
	    @Override
	    public int compare(Pair<String,Double> a, Pair<String,Double> b) {
	    	if (a.getSecond() > b.getSecond())
	    		return 1;
	    	else if (a.getSecond() < b.getSecond())
	    		return -1;
	    	else 
	    		return 0;
	    }
	}

	protected Iterator<String> wordsContainingNgram(
		String seq) throws SpellCheckerException {
		return wordsContainingNgram(seq, new CompiledCorpus.SearchOption[0]);
	}

	protected Iterator<WordInfo> winfosContainingNgram(String seq,
		CompiledCorpus.SearchOption... options) throws SpellCheckerException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.wordsContainingSequ");

		long start = StopWatch.nowMSecs();

		Iterator<WordInfo> winfosIter = null;
		try {
			Iterator<WordInfo> winfosIter1 =
				corpus.winfosContainingNgram(
					seq, options);

			Iterator<WordInfo> winfosIter2 =
				explicitlyCorrectWords.winfosContainingNgram(
					seq, CompiledCorpus.SearchOption.EXCL_MISSPELLED);

			winfosIter =
				new IteratorChain<WordInfo>(winfosIter1, winfosIter2);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		long elapsed = StopWatch.elapsedMsecsSince(start);
		logger.trace("seq="+seq+" took "+elapsed+"msecs");
		return winfosIter;
	}

	protected Iterator<String> wordsContainingNgram(String seq,
		CompiledCorpus.SearchOption... options) throws SpellCheckerException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.spellchecker.SpellChecker.wordsContainingSequ");

		long start = StopWatch.nowMSecs();

		Iterator<String> wordsIter = null;
		try {
			Iterator<String> wordsIter1 =
				corpus.wordsContainingNgram(
					seq, options);

			Iterator<String> wordsIter2 =
				explicitlyCorrectWords.wordsContainingNgram(
					seq, CompiledCorpus.SearchOption.EXCL_MISSPELLED);

			wordsIter = new IteratorChain<String>(wordsIter1, wordsIter2);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		long elapsed = StopWatch.elapsedMsecsSince(start);
		logger.trace("seq="+seq+" took "+elapsed+"msecs");
		return wordsIter;
	}

	public List<SpellingCorrection> correctText(String text) throws SpellCheckerException {
		return correctText(text, null);
	}

	public List<SpellingCorrection> correctText(String text, Integer nCorrections) throws SpellCheckerException {
		Logger tLogger = Logger.getLogger("SpellChecker.correctText");
		if (nCorrections == null) nCorrections = DEFAULT_CORRECTIONS;
		List<SpellingCorrection> corrections = new ArrayList<SpellingCorrection>();
		
		IUTokenizer iutokenizer = new IUTokenizer();
		iutokenizer.tokenize(text);
		List<Pair<String,Boolean>> tokens = iutokenizer.getAllTokens();
		
		if (tLogger.isTraceEnabled()) tLogger.trace("tokens= "+PrettyPrinter.print(tokens));
		
		for (Pair<String,Boolean> aToken: tokens) {
			String tokString = aToken.getFirst();
			Boolean isDelimiter = !aToken.getSecond(); // IUTokenizer returns TRUE for words and FALSE for non-words.
			SpellingCorrection correction = null;
			if (isDelimiter) {
				correction = new SpellingCorrection(tokString);
			} else {
				correction = this.correctWord(tokString, nCorrections);
			}
			corrections.add(correction);
			
		}
		
		return corrections;
	}
	
	protected boolean assessEndingWithIMA(String ending) {
		Logger logger = Logger.getLogger("SpellChecker.assessEndingWithIMA");
		boolean accepted = false;
		MorphologicalAnalyzer morphAnalyzer = segmenter.getAnalyzer();
		for (int i=0; i<makeUpWords.length; i++) {
			accepted = false;
			String term = makeUpWords[i]+ending;
			logger.debug("term= "+term);
			Decomposition[] decs = null;
			try {
				decs = morphAnalyzer.decomposeWord(term);
			} catch (TimeoutException | MorphologicalAnalyzerException e) {
			}
			logger.debug("decs: "+(decs==null?"null":decs.length));
			if (decs!=null && decs.length!=0) {
				accepted = true;
				break;
			}
		}
		
		return accepted;
	}

	protected double inverseFrequency(double freq) {
		double iFreq = 1.0 / (freq + 1);
		return iFreq;
	}
}
