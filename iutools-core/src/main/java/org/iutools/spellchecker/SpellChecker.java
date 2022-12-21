package org.iutools.spellchecker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.datastructure.CloseableIteratorChain;
import org.iutools.morph.*;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.script.TransCoderException;
import org.iutools.text.IUWord;
import org.iutools.text.NonIUWord;
import org.iutools.text.Word;
import org.iutools.text.WordException;
import org.iutools.text.segmentation.Token;
import org.iutools.utilities.StopWatch;
import ca.nrc.debug.Debug;
import org.iutools.corpus.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.config.ConfigException;
import org.iutools.datastructure.trie.StringSegmenterException;
import org.iutools.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.json.PrettyPrinter;
import org.iutools.NumericExpression;
import org.iutools.edit_distance.EditDistanceCalculator;
import org.iutools.edit_distance.EditDistanceCalculatorException;
import org.iutools.edit_distance.EditDistanceCalculatorFactory;
import org.iutools.edit_distance.EditDistanceCalculatorFactoryException;
import org.iutools.text.ngrams.NgramCompiler;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.config.IUConfig;
import org.iutools.script.Orthography;
import org.iutools.script.Syllabics;
import org.iutools.script.TransCoder;
import static org.iutools.script.TransCoder.Script;
import org.iutools.utilbin.AnalyzeNumberExpressions;


/**
 * Spell Checker for Inuktitut
 *
 * It uses a combination of two techniques.
 *
 * Shallow character-level analysis
 *   Uses simple rules to look for sequences of characters that can never appear
 *   in a valid Inuktitut word.
 *
 *   Some of the rules are able to correct bad sequences, while others can only
 *   identify bad sequences without correcting them.
 *
 * Deep morphological analysis
 *   Check that the word does not violate any morpheme composition rules. More
 *   precisely, we check that:
 *
 *   - The word is composed of a sequence of valid morphemes that are allowed to
 *     follow each other
 *   - The written form of each morpheme takes into account the morpheme that
 *     precede and follow it.
 *
 */
public class SpellChecker {
	
	public int MAX_SEQ_LEN = 3;

	public int MIN_NGRAM_LEN = 3;
	public int MAX_NGRAM_LEN = 4;


	public int MAX_CANDIDATES = 2000;
	public int DEFAULT_CORRECTIONS = 5;
	
	/** Maximum msecs allowed for decomposing a word during 
	 *  spell checker
	 */
	private long MAX_DECOMP_MSECS = 5*1000;

	/**
	 * We can perform spell checking at different level. As we increase level,
	 * we catch more types of mistakes, but we also get slower.
	 *
	 *   Level 1: Only performs a SHALLOW CHARACTER-LEVEL check where we look for
	 *   sequences of characters that can never appear in a valid Inuktitut word.
	 *
	 *   Level 2: On top of the checks in Level 1, we also perform a DEEP MORPHOLOGICAL
	 *   check where we ensure that the word does not violate any morpheme
	 *   composition rules. More precisely, we check that:
	 *
	 *     - word is composed of a sequence of valid morphemes that are allowed to
	 *       follow each other
	 *     - the written form of each morpheme takes into account the morpheme that
	 *       precede and follow it.
	 *
	 *  Level 3: On top of the SHALLOW CHARACTER-LEVEL and DEEP MORPHOLOGICAL checks done
	 *  in Level 2, we look for a "partial correction" which identifies the
	 *  longest valid sequences at the start and end of the word. Validity of
	 *  these longest sequences is evaluated through a DEEP-like analysis that takes
	 *  into account the morpheme composition rules.
	 *
	 */
	int checkLevel = 2;

	protected String esIndexNameRoot = null;

	protected CompiledCorpus explicitlyCorrectWords = null;

	// TODO-June2020: Can we get rid of this and use the explicitlyCorrectWords
	//   CompiledCorpus_ES instance instead?
	/** 
	 * Words that are NOT numeric expressions and were EXPLICLITLY as being 
	 * correct
	 */
	protected Set<String> explicitlyCorrect_NonNumeric = new HashSet<String>();

	// TODO-June2020: Can we get rid of this and use the explicitlyCorrectWords
	//   CompiledCorpus_ES instance instead?
	/** 
	 * Words that ARE numeric expressions and were EXPLICLITLY as being 
	 * correct
	 */
	protected Set<String> explicitlyCorrect_Numeric = new HashSet<String>();

	public transient EditDistanceCalculator editDistanceCalculator;
	public transient boolean verbose = false;
	
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
		init_SpellChecker((String)null, (Boolean)null);
	}

	public SpellChecker(String corpusName, boolean mustBeRegistered)
		throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker(corpusName, mustBeRegistered);
	}

	public SpellChecker(String corpusName) throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker(corpusName, (Boolean)null);
	}

	public void setDictionaryFromCorpus() throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = new CompiledCorpusRegistry().getCorpus();
			__processCorpus();
		} catch (CompiledCorpusRegistryException | CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}
	}

	private void init_SpellChecker(
		String _corpusName, Boolean mustBeRegistered) throws SpellCheckerException {

		if (mustBeRegistered == null) {
			mustBeRegistered = true;
		}
		if (_corpusName == null) {
			_corpusName = CompiledCorpusRegistry.defaultCorpusName;
		}

		esIndexNameRoot = _corpusName;
		if (!mustBeRegistered) {
			try {
//				corpus = new CompiledCorpus_ES(_corpusName);
//				corpus = new CompiledCorpusRegistry().getCorpus(_corpusName);
				corpus = new CompiledCorpusRegistry().makeCorpus(_corpusName);
			} catch (CompiledCorpusException | CompiledCorpusRegistryException e) {
				throw new SpellCheckerException(e);
			}
		} else {
			try {
				corpus = new CompiledCorpusRegistry().getCorpus(_corpusName);
			} catch (CompiledCorpusRegistryException | CompiledCorpusException e) {
				throw new SpellCheckerException(
						"No registered corpus by the name of "+_corpusName, e);
			}
		}
		try {
			explicitlyCorrectWords =
//					new CompiledCorpus_ES(
//							explicitlyCorrectWordsIndexName());
				new CompiledCorpusRegistry().makeCorpus(explicitlyCorrectWordsIndexName());
		} catch (CompiledCorpusException | CompiledCorpusRegistryException e) {
			throw new SpellCheckerException(e);
		}

		try {
			editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
			segmenter = new StringSegmenter_IUMorpheme();
			setDictionaryFromCorpus(corpus);
		} catch (FileNotFoundException | SpellCheckerException | ConfigException e) {
			throw new SpellCheckerException(e);
		}

		return;
	}

	/**
	 * Set the maximum number of seconds that can be spent trying
	 * to decompose a word into morphemes.
	 */
	public SpellChecker setMaxDecompSecs(int maxSecs) {
		MAX_DECOMP_MSECS = maxSecs * 1000;
		return this;
	}

	public SpellChecker setCheckLevel(int _checkLevel) throws SpellCheckerException {
		if (_checkLevel < 1 && _checkLevel > 3) {
			throw new SpellCheckerException("Spell checking level should be between 0 and 3 (was: "+_checkLevel+")");
		}
		this.checkLevel = _checkLevel;
		return this;
	}

	public boolean deepCheckEnabled() {
		return checkLevel > 1;
	}

	public boolean partialCorrectionEnabled() {
		return checkLevel > 2;
	}

	public void setDictionaryFromCorpus(CompiledCorpus _corpus) throws SpellCheckerException, ConfigException, FileNotFoundException {
		this.corpus = _corpus;
		__processCorpus();
	}

	public void setDictionaryFromCorpus(String _corpusName) throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = new CompiledCorpusRegistry().getCorpus(_corpusName);
			setDictionaryFromCorpus(corpus);
		} catch (CompiledCorpusException | CompiledCorpusRegistryException e) {
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
		try {
			return correctWord(new IUWord(word),(Integer)null);
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
	}

	public SpellingCorrection correctWord(String word, Integer maxCorrections) throws SpellCheckerException {
		SpellingCorrection correction = null;
		try {
			Word wordObject = Word.build(word);
			if (wordObject instanceof NonIUWord) {
				// This is not an IU word. Wrap it in a "blank" correction.
				correction = new SpellingCorrection(word);
			} else {
				correction = correctWord(new IUWord(word), maxCorrections);
			}
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
		return correction;
	}

	public SpellingCorrection correctWord(IUWord word) throws SpellCheckerException {
		return correctWord(word, (Integer)null);
	}

	public SpellingCorrection correctWord(IUWord word, Integer maxCorrections) throws SpellCheckerException {
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.correctWord");
		Logger excLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.correctWord.exc");

		SpellingCorrection corr = new SpellingCorrection(word.word());
		try {
			if (maxCorrections == null || maxCorrections == -1) {
				maxCorrections = DEFAULT_CORRECTIONS;
			}

			long start = StopWatch.nowMSecs();

			SpellDebug.trace("SpellChecker.correctWord",
				"Invoked on word=" + word.inRoman(),
				word.word(), null);

			boolean wordIsSyllabic = Syllabics.allInuktitut(word.inSyll());

			corr.wasMispelled = isMispelled(word);
			tLogger.trace("wasMispelled= " + corr.wasMispelled);

			SpellDebug.trace("SpellChecker.correctWord",
				"corr.wasMispelled=" + corr.wasMispelled,
				word.word(), null);

			if (corr.wasMispelled) {
				tLogger.trace("Before correctWord_Shallow, corr.shallowFix="+corr.shallowFix);
				boolean completelyFixed = correctWord_Shallow(corr);
				tLogger.trace("After correctWord_Shallow, corr.shallowFix="+corr.shallowFix);


				// Only carry out a deeper morphological check if:

				// - the shallow, surface check did not completely fix the problem
				//   AND
				// - deep checking is enabled
				//
				if (!completelyFixed && deepCheckEnabled()) {
					// The "shallow" correction rules have not resolved all
					// problems the word. So keep going.
					if (partialCorrectionEnabled()) {
						SpellDebug.trace("SpellChecker.correctWord",
							"Computing longest correct head and tail of the word",
							word.word(), null);
						computeCorrectPortions(corr);
					}

					correctWord_Deep(word, corr, maxCorrections);
				}
			}

			ensureCandidatesAreInScript(corr, word.origScript());

			long elapsed = StopWatch.elapsedMsecsSince(start);
			tLogger.trace("word=" + word + " took " + elapsed + "msecs");

		} catch (Exception e) {
			excLogger.trace("word="+word+" raised exception e="+e+"\nCall stack was:\n"+ Debug.printCallStack(e));
			if (e instanceof SpellCheckerException) {
				throw e;
			} else {
				throw new SpellCheckerException(e);
			}
		}

		return corr;
	}

	private void correctWord_Deep(IUWord word, SpellingCorrection corr, int maxCorrections) throws SpellCheckerException {
		// set ngramStats and suite of words for candidates according to
		// type of word (normal word or numeric expression)
		String[] numericTermParts = splitNumericExpression(word.inRoman());
		boolean wordIsNumericTerm = numericTermParts != null;

		SpellDebug.trace("SpellChecker.correctWord",
			"wordIsNumericTerm=" + wordIsNumericTerm,
			word.word(), null);

		SpellDebug.trace("SpellChecker.correctWord_Deep",
			"Computing 1st pass candidates",
			word.word(), null);

		List<ScoredSpelling> candidates =
			candidatesWithSimilarNgrams(word.inRoman(), wordIsNumericTerm);

		SpellDebug.trace("SpellChecker.correctWord_Deep",
			"Number of 1st pass candidates=" + (candidates.size()),
			word.word(), null);

		SpellDebug.containsDuplicates(
			"SpellChecker.correctWord_Deep",
			"1st pass candidates", word.word(), candidates);

		SpellDebug.trace("SpellChecker.correctWord_Deep",
			"Computing candidates similariy using " + editDistanceCalculator.getClass(),
			word.word(), null);

		List<ScoredSpelling> scoredSpellings =
			computeCandidateDistances(word.inRoman(), candidates);

		SpellDebug.containsDuplicates(
			"SpellChecker.correctWord_Deep",
			"candidates with similarities", word.word(), scoredSpellings);

		SpellDebug.containsCorrection(
			"SpellChecker.correctWord_Deep",
			"UNSORTED scored spellings",
			word.word(), scoredSpellings);

		List<ScoredSpelling> sortedSpellings =
			sortCandidatesByOverallScore(scoredSpellings);

		SpellDebug.containsDuplicates(
			"SpellChecker.correctWord_Deep",
			"SORTED scored spellings", word.word(), sortedSpellings);

		SpellDebug.containsCorrection(
			"SpellChecker.correctWord_Deep",
			"SORTED scored spellings",
			word.word(), scoredSpellings);

		if (wordIsNumericTerm) {
			for (int ic = 0; ic < scoredSpellings.size(); ic++) {
				ScoredSpelling scoredCandidate = sortedSpellings.get(ic);
				scoredCandidate.spelling =
				scoredCandidate.spelling.replaceAll("\\d+-*", numericTermParts[0]);
			}
		}

		sortedSpellings = selectTopCandidates(maxCorrections, sortedSpellings);

		SpellDebug.containsDuplicates(
			"TOP PORTION of SORTED scored spellings",
			"SORTED scored spellings", word.word(), sortedSpellings);

		SpellDebug.containsCorrection(
			"SpellChecker.correctWord",
			"TOP PORTION of SORTED scored spellings",
			word.word(), scoredSpellings);

		if (SpellDebug.traceIsActive("SpellChecker.correctWord", word.word())) {
			SpellDebug.trace("SpellChecker.correctWord",
				"TOP PORTION of SORTED scored spellings is:\n" +
				PrettyPrinter.print(scoredSpellings),
				word.word(), null);
		}

		corr.setPossibleSpellings(sortedSpellings);
	}

	private boolean correctWord_Shallow(
		SpellingCorrection corr) throws SpellCheckerException {
		Boolean wasRepaired = false;
		String shallowFix = new CorrectionRulesSet().fixWord(corr.orig);
		if (!shallowFix.equals(corr.orig)) {
			corr.shallowFix = shallowFix;

			// Check if the absolute mistake corrections have fixed all problems
			// with the word
			if (!corr.shallowFixHighlightsFaultyChars() &&
				!isMispelled(corr.shallowFix)) {
				wasRepaired = true;
			}
		}

		return wasRepaired;
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

	protected void computeCorrectPortions(SpellingCorrection corr) throws SpellCheckerException {
		computeCorrectLead(corr);
		computeCorrectTail(corr);
		return;
	}

	private void computeCorrectLead(SpellingCorrection corr) throws SpellCheckerException {
		final int MAX_WORDS_TO_TRY = 5;

		String longestCorrectLead = null;
		String correctionSoFar = corr.bestSuggestionSoFar(false);
		for (int endPos = correctionSoFar.length()-1; endPos > 3; endPos--) {
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
			String lead = correctionSoFar.substring(0, endPos-1);
			try (CloseableIterator<String> iterWords =
				wordsContainingNgram("^"+lead)) {
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
			} catch (Exception e) {
				throw new SpellCheckerException(e);
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
				new MorphologicalAnalyzer_R2L()
						.setTimeout(MAX_DECOMP_MSECS)
						.activateTimeout()
						.decomposeWord(word);
		} catch(TimeoutException e) {
			answer = false;
		} catch(MorphologicalAnalyzerException e) {
			throw new SpellCheckerException(e);
		}

		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = null;
				try {
					morphemes = aDecomp.surfaceForms();
				} catch (DecompositionException decompositionExcepion) {
					throw new SpellCheckerException(decompositionExcepion);
				}

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
	

	private void computeCorrectTail(SpellingCorrection corr) throws SpellCheckerException {
		
		final int MAX_WORDS_TO_TRY = 5;
		
		String longestCorrectTail = null;
		String correctionSoFar = corr.bestSuggestionSoFar(false);
		for (int startPos = 0; startPos < correctionSoFar.length()-2; startPos++) {
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
			String tail = correctionSoFar.substring(startPos);
			try (CloseableIterator<String> iterWords = wordsContainingNgram(tail+"$")) {
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
			} catch (Exception e) {
				throw new SpellCheckerException(e);
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
				new MorphologicalAnalyzer_R2L()
						.setTimeout(MAX_DECOMP_MSECS)
						.activateTimeout()
						.decomposeWord(word);
		} catch(TimeoutException e) {
			answer = false;
		} catch(MorphologicalAnalyzerException e) {
			throw new SpellCheckerException(e);
		}
		
		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = null;
				try {
					morphemes = aDecomp.surfaceForms();
				} catch (DecompositionException decompositionExcepion) {
					throw new SpellCheckerException(decompositionExcepion);
				}

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
	 * Ensure that the candidates are in the same script as the input word
	 */
	private void ensureCandidatesAreInScript(SpellingCorrection corr, Script script) throws SpellCheckerException {
		for (int ic = 0; ic < corr.deepFixes.size(); ic++) {
			ScoredSpelling candidate = corr.deepFixes.get(ic);
			try {
				candidate.spelling = TransCoder.ensureScript(script, candidate.spelling);
			} catch (TransCoderException e) {
				throw new SpellCheckerException(e);
			}
		}

		return;
	}

	public Boolean isMispelled(String wordStr) throws SpellCheckerException {
		boolean answer = false;
		try {
			Word word = Word.build(wordStr);
			answer = isMispelled(word);
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
		return answer;
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
	public Boolean isMispelled(Word word) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.isMispelled");

		logger.trace("invoked with: word="+word);

		SpellDebug.trace("SpellChecker.isMispelled", "Checking correctness of word", word.word(), null);

		Boolean wordIsMispelled = false;
		// We assume the word is correctly spelled, unless it's a word in Inuktitut
		if (word instanceof IUWord) {
			IUWord iuWord = (IUWord)word;
			// First, carry out a rapid, SHALLOW CHARACTER-level analysis.
			//
			wordIsMispelled = isMispelled_ShallowAnalysis(iuWord);

			// If deep check is active, and the SHALLOW check hasn't been able
			// to established whether or not the word is mis-spelled, then carry out a
			// DEEP MORPHOLOGICAL analysis.
			//
			if (deepCheckEnabled() && wordIsMispelled == null) {
				wordIsMispelled = isMispelled_DeepAnalysis(iuWord);
			}

			if (wordIsMispelled == null) {
				wordIsMispelled = false;
			}

			logger.trace("Exiting");

			SpellDebug.trace("SpellChecker.isMispelled",
			"Returning wordIsMispelled=" + wordIsMispelled, iuWord.inRoman(), null);
		}

		return wordIsMispelled;
	}

	private Boolean isMispelled_DeepAnalysis(IUWord word) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.isMispelled_DeepAnalysis");

		Boolean wordIsMispelled = null;
		if (corpus != null) {
			try {
				WordInfo wInfo = corpus.info4word(word.inRoman());
				if (wInfo != null && wInfo.totalDecompositions > 0) {
					wordIsMispelled = false;
					logger.trace("Corpus contains some decompositions for this word");
				}
			} catch (CompiledCorpusException e) {
				logger.trace(
				"[word=" + word + "]: throws exception e=" + e.getMessage() +
				"\ncorpus index name was: " + corpus.getIndexName());
				throw new SpellCheckerException(e);
			}
		}

		if (wordIsMispelled == null) {
			try {
				String[] segments = segmenter.segment(word.inRoman());
				logger.trace("word submitted to IMA: " + word);
				if (segments == null || segments.length == 0) {
					wordIsMispelled = true;
				}
			} catch (TimeoutException e) {
				wordIsMispelled = true;
			} catch (StringSegmenterException e) {
				throw new SpellCheckerException(e);
			}
			logger.trace("word submitted to IMA - mispelled: " + wordIsMispelled);
		}

		return wordIsMispelled;
	}

	/**
	 * Check if the word is mis-spelled using a SHALLOW, CHARACTER-LEVEL
	 * analysis.
	 */
	private Boolean isMispelled_ShallowAnalysis(IUWord word) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.isMispelled_ShallowAnalysis");

		Boolean wordIsMispelled = null;

		// If the word is all numbers, then it is NOT mis-spelled
		if (word.inRoman().matches("^[0-9]+$")) {
			logger.trace("word is all digits");
			wordIsMispelled = false;
		}

		// First, check if word is in the dictionary of explicitly correct
		// words.
		//
		if (wordIsMispelled == null &&
			(isExplicitlyCorrect(word.inRoman()) || isExplicitlyCorrect(word.inSyll()))) {
			logger.trace("word is was explicity tagged as being correct");
			wordIsMispelled = false;
		}

		if (wordIsMispelled == null && latinSingleInuktitutCharacters.contains(word.inRoman())) {
			logger.trace("single inuktitut character");
			wordIsMispelled = false;
		}

		// Next, check if the word contains more than two consecutive consonants
		if (wordIsMispelled == null && wordContainsMoreThanTwoConsecutiveConsonants(word.inRoman())) {
			logger.trace("more than 2 consecutive consonants in the word");
			wordIsMispelled = true;
		}

		// Next, see if any of the "shallow" correction rules apply
		//
		// Note: We feed the SYLLABIC version of the word since some of the
		//   correction rules may already have been applied when we
		//   transcoded from ROMAN to SYLLABIC (in which case, it would
		//   appear like the original SYLLABIC word was correctly spelled when
		//   in fact it was not).
		if (wordIsMispelled == null && fitsSomeCorrectionRules(word.inSyll())) {
			logger.trace("some correction rules apply for this word");
			wordIsMispelled = true;
		}

		String[] numericTermParts = null;
		if (wordIsMispelled == null && (numericTermParts = splitNumericExpression(word.inRoman())) != null) {
			logger.trace("numeric expression: " + word.inRoman() + " (" + numericTermParts[1] + ")");
			boolean pseudoWordWithSuffixAnalysesWithSuccess = assessEndingWithIMA(numericTermParts[1]);
			wordIsMispelled = !pseudoWordWithSuffixAnalysesWithSuccess;
			logger.trace("numeric expression - wordIsMispelled: " + wordIsMispelled);
		}

		// If the 'word' is made up of just punctuaation, then it is NOT
		// mis-spelled.
		if (wordIsMispelled == null && wordIsPunctuation(word.inRoman())) {
			logger.trace("word is punctuation");
			wordIsMispelled = false;
		}

		return wordIsMispelled;
	}

	private boolean fitsSomeCorrectionRules(String word) throws SpellCheckerException {
		boolean answer = new CorrectionRulesSet().someRulesApply(word);
		return answer;
	}

	public boolean isExplicitlyCorrect(String word) throws SpellCheckerException {
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.isExplicitlyCorrect");
		tLogger.trace("[word="+word+"] entered");
		boolean answer = false;
		try {
			if (explicitlyCorrectWords != null) {
				answer = explicitlyCorrectWords.containsWord(word);
			}
		} catch (CompiledCorpusException e) {
			tLogger.trace("[word="+word+"] !!! RAISES EXCEPTION e="+e.getCause());
			throw new SpellCheckerException(e);
		}

		tLogger.trace("[word="+word+"] exited");
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
		Logger logger = LogManager.getLogger("SpellChecker.wordContainsMoreThanTwoConsecutiveConsonants");
		boolean result = false;
		String wordInSimplifiedOrthography = Orthography.simplifiedOrthography(word, false);
		logger.trace("wordInSimplifiedOrthography= "+wordInSimplifiedOrthography+" ("+word+")");
		Pattern p = Pattern.compile("[gjklmnprstvN]{3,}");
		Matcher mp = p.matcher(wordInSimplifiedOrthography);
		if (mp.find()) {
			logger.trace("match= "+mp.group());
			result = true;
		} 
		
		return result;
	}


	private List<ScoredSpelling> sortCandidatesByOverallScore(
		List<ScoredSpelling> spellings) {

		SpellDebug.traceTrackedCandidates("SpellChecker.sortCandidatesByOverallScore",
			spellings);

		Collections.sort(spellings,
			(ScoredSpelling p1, ScoredSpelling p2) -> {
				return ScoredSpelling.compareSpellings(p1, p2);
			}
		);

		return spellings;
	}

	protected List<ScoredSpelling> computeCandidateDistances(
		String badWord, List<ScoredSpelling> candidates) throws SpellCheckerException {
		SpellDebug.trace("SpellChecker.computeCandidateSimilarities",
				"Invoked on word="+badWord+
					", editDistanceCalculator=\n"+editDistanceCalculator.getClass()+
					PrettyPrinter.print(editDistanceCalculator),
				badWord, null);
		List<ScoredSpelling> scoredCandidates = new ArrayList<ScoredSpelling>();

		for (ScoredSpelling aCandidate: candidates) {
			double editDistance =
				computeCandidateEditDistance(badWord, aCandidate.spelling);
			aCandidate.editDist = editDistance;
		}
		
		return candidates;
	}

	private double computeCandidateEditDistance(String badWord, String candidate) throws SpellCheckerException {

		SpellDebug.trace("SpellChecker.computeCandidateNgramSimilarity",
			"Invoked, editDistanceCalculator="+editDistanceCalculator.getClass(),
			badWord, candidate);
		
		double distance;
		try {
			distance = editDistanceCalculator.distance(badWord, candidate);
		} catch (EditDistanceCalculatorException e) {
			throw new SpellCheckerException(e);
		}

		SpellDebug.trace("SpellChecker.computeCandidateNgramSimilarity",
				"returning distance="+distance,
				badWord, candidate);

		return distance;
	}

	public List<ScoredSpelling> candidatesWithSimilarNgrams(String badWord,
	   boolean wordIsNumericTerm) throws SpellCheckerException {

		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.candidatesWithSimilarNgrams");
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
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.computeNgramIDFs");

		long start = StopWatch.nowMSecs();

		Pair<String,Double> ngramFreqs[] = new Pair[ngrams.length];

		for (int i=0; i<ngrams.length; i++) {
			Long ngramFreq = ngramFrequency(ngrams[i]);
			tLogger.trace("for ngram="+ngrams[i]+", ngramFreq="+ngramFreq);
			ngramFreqs[i] = Pair.of(ngrams[i],1.0*ngramFreq);
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
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.ngramFrequency");
		long freq = 0;
		try {
			freq = corpus.totalWordsWithCharNgram(
				ngram, CompiledCorpus_ES.SearchOption.EXCL_MISSPELLED);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		tLogger.trace("for ngram="+ngram+"; returning freq="+freq);
		return freq;
	}

	private List<ScoredSpelling> candidatesWithBestNGramsMatch(
		Pair<String, Double>[] idf)
		throws SpellCheckerException {

		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.candidatesWithBestNGramsMatch");

		long start = StopWatch.nowMSecs();
		tLogger.trace("Started");

		Set<ScoredSpelling> candidateSpellingsSet = new HashSet<ScoredSpelling>();
		for (int i=0; i<idf.length; i++) {

			String ngram = idf[i].getLeft();
			Double ngramIDF = idf[i].getRight();

			Set<ScoredSpelling> candidateSpellingsWithNgram =
					new HashSet<ScoredSpelling>();

			tLogger.trace("adding candidates that contain ngram=" + ngram + " (ngramIDF=" + ngramIDF + ")");

			try (CloseableIterator<WordInfo> iterWinfoCandaWithNgram =
				winfosContainingNgram(
					ngram, CompiledCorpus_ES.SearchOption.EXCL_MISSPELLED)) {
				while (iterWinfoCandaWithNgram.hasNext()) {
					WordInfo winfo = iterWinfoCandaWithNgram.next();
					ScoredSpelling candidate = new ScoredSpelling(winfo.word);
					candidate.frequency = winfo.frequency;
					candidateSpellingsWithNgram.add(candidate);
				}
			} catch (Exception e) {
				throw new SpellCheckerException(e);
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
		Logger tLogger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.sortCandidatesByNgramSimilarity");
		tLogger.trace("invoked");

		List<ScoredSpelling> candidates = new ArrayList<ScoredSpelling>();
		candidates.addAll(initialCands);
		if (onlyNumericTerms) {
			candidates = keepOnlyNumericTerms(initialCands);
		}
		
		Map<String,Double> badWordNgramInvFreqHash = new HashMap<String,Double>();
		for (Pair<String,Double> ngramInfo: badWordNgramFreqs) {
			badWordNgramInvFreqHash.put(
				ngramInfo.getLeft(),
				inverseFrequency(ngramInfo.getRight()));
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
			scoreValues.add(Pair.of(candidate,totalScore));
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
	    	if (a.getRight() > b.getRight())
	    		return 1;
	    	else if (a.getRight() < b.getRight())
	    		return -1;
	    	else 
	    		return 0;
	    }
	}

	protected CloseableIterator<String> wordsContainingNgram(
		String seq) throws SpellCheckerException {
		return wordsContainingNgram(seq, new CompiledCorpus_ES.SearchOption[0]);
	}

	protected CloseableIterator<WordInfo> winfosContainingNgram(String seq,
		CompiledCorpus_ES.SearchOption... options) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.winfosContainingNgram");
		logger.trace("invoked with seq="+seq);
		long start = StopWatch.nowMSecs();

		CloseableIteratorChain<WordInfo> winfosIter = null;
		try {
			CloseableIterator<WordInfo> winfosIter1 =
				corpus.winfosContainingNgram(seq, options);

			// When looking in the corpus of explicitly correct words, don't worry
			// about making sure that it's correctly spelled according to the
			// analyzer
			//
			options = ArrayUtils.removeElement(
				options, CompiledCorpus_ES.SearchOption.EXCL_MISSPELLED);

			CloseableIterator<WordInfo> winfosIter2 =
				explicitlyCorrectWords.winfosContainingNgram(seq, options);

			winfosIter = new CloseableIteratorChain<WordInfo>(winfosIter1, winfosIter2);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		long elapsed = StopWatch.elapsedMsecsSince(start);
		logger.trace("seq="+seq+" took "+elapsed+"msecs");
		return winfosIter;
	}

	protected CloseableIterator<String> wordsContainingNgram(String seq,
		CompiledCorpus_ES.SearchOption... options) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.SpellChecker.wordsContainingSequ");

		long start = StopWatch.nowMSecs();

		CloseableIterator<String> wordsIter = null;
		CloseableIterator<String> wordsIter1 = null;
		CloseableIterator<String> wordsIter2 = null;
		try {
			wordsIter1 =
				corpus.wordsContainingNgram(seq, options);

			wordsIter2 =
				explicitlyCorrectWords.wordsContainingNgram(
					seq, CompiledCorpus_ES.SearchOption.EXCL_MISSPELLED);

			wordsIter = new CloseableIteratorChain<String>(wordsIter1, wordsIter2);
		} catch (CompiledCorpusException e) {
			throw new SpellCheckerException(e);
		}

		long elapsed = StopWatch.elapsedMsecsSince(start);
		logger.trace("seq="+seq+" took "+elapsed+"msecs");
		return (CloseableIterator<String>) wordsIter;
	}

	// For some reason, jMeter load testing for the SpellChecker
	//   causes some ConcurrentModificationException.
	//   Hoping to solve that problem by making the correctText be synchronized.
	//
	public synchronized List<SpellingCorrection> correctText(String text) throws SpellCheckerException {
		return correctText(text, null);
	}

	public List<SpellingCorrection> correctText(String text, Integer nCorrections) throws SpellCheckerException {
		Logger tLogger = LogManager.getLogger("SpellChecker.correctText");
		List<SpellingCorrection> corrections = new ArrayList<SpellingCorrection>();
		
		IUTokenizer iutokenizer = new IUTokenizer();
		iutokenizer.tokenize(text);
		List<Token> tokens = iutokenizer.getAllTokens();
		
		if (tLogger.isTraceEnabled()) tLogger.trace("tokens= "+PrettyPrinter.print(tokens));
		
		for (Token aToken: tokens) {
			String tokString = aToken.text;
			Boolean isDelimiter = !aToken.isWord; // IUTokenizer returns TRUE for words and FALSE for non-words.
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
		Logger logger = LogManager.getLogger("SpellChecker.assessEndingWithIMA");
		boolean accepted = false;
		MorphologicalAnalyzer_R2L morphAnalyzer = segmenter.getAnalyzer();
		for (int i=0; i<makeUpWords.length; i++) {
			accepted = false;
			String term = makeUpWords[i]+ending;
			logger.trace("term= "+term);
			Decomposition[] decs = null;
			try {
				decs = morphAnalyzer.decomposeWord(term);
			} catch (TimeoutException | MorphologicalAnalyzerException e) {
			}
			logger.trace("decs: "+(decs==null?"null":decs.length));
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
