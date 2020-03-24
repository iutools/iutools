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
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import ca.nrc.string.diff.DiffCosting;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.NumericExpression;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculator;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorException;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactory;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactoryException;
import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilbin.AnalyzeNumberExpressions;
import ca.inuktitutcomputing.utilities.IUTokenizer;
import ca.inuktitutcomputing.utilities.NgramCompiler;

public class SpellChecker {
	
	public int MAX_SEQ_LEN = 5;
	public int MAX_CANDIDATES = 1000;
	public int DEFAULT_CORRECTIONS = 5;
	
	/** Maximum msecs allowed for decomposing a word during 
	 *  spell checker
	 */
	private final long MAX_DECOMP_MSECS = 5*1000;

	public String allWords = ",,";
	public Map<String,Long> ngramStats = new HashMap<String,Long>();
	
	public String allNormalizedNumericTerms = ",,";
	public Map<String,Long> ngramStatsOfNumericTerms = new HashMap<String,Long>();
	
	public transient Map<String,Long> ngramStatsForCandidates = null;
	
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
	
    public static Cache<String, Set<String>> 
		wordsWithNgramCache = 
			Caffeine.newBuilder().maximumSize(10000)
			.build();
	
	public SpellChecker() throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker(CompiledCorpusRegistry.defaultCorpusName);
	}
	
	public SpellChecker(String corpusName) throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker(corpusName);
	}

	public SpellChecker(File corpusFile) throws StringSegmenterException, SpellCheckerException {
		init_SpellChecker(corpusFile);
	}

	private void init_SpellChecker(Object _corpus) throws SpellCheckerException  {
		try {
			editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
			segmenter = new StringSegmenter_IUMorpheme();
			if (_corpus != null) {
				if (_corpus instanceof String) {
					String corpusName = (String)_corpus;
					if (!corpusName.equals(CompiledCorpusRegistry.emptyCorpusName) ) {
						setDictionaryFromCorpus((String)_corpus);						
					}
				} else if (_corpus instanceof File) {
					setDictionaryFromCorpus((File)_corpus);
				}
			}			
		} catch (StringSegmenterException | FileNotFoundException | SpellCheckerException | ConfigException e) {
			throw new SpellCheckerException(e);
		}
	}
	
	
	public void setDictionaryFromCorpus() throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = CompiledCorpusRegistry.getCorpus(null);
			__processCorpus();
		} catch (CompiledCorpusRegistryException e) {
			throw new SpellCheckerException(e);
		}
	}

	public void setDictionaryFromCorpus(String _corpusName) throws SpellCheckerException, ConfigException, FileNotFoundException {
		try {
			corpus = CompiledCorpusRegistry.getCorpus(_corpusName);
			__processCorpus();
		} catch (CompiledCorpusRegistryException e) {
			throw new SpellCheckerException(e);
		}
	}

	public void setDictionaryFromCorpus(File compiledCorpusFile) throws SpellCheckerException {
		try {
			corpus = CompiledCorpus.createFromJson(compiledCorpusFile.toString());
			__processCorpus();
		} catch (Exception e) {
			throw new SpellCheckerException(
					"Could not create the compiled corpus from file: " + compiledCorpusFile.toString(), e);
		}
	}
	
	private void __processCorpus() throws ConfigException, FileNotFoundException {
		this.allWords = corpus.decomposedWordsSuite;
		this.ngramStats = corpus.ngramStats;
		corpus.setVerbose(verbose);
		// Ideally, these should be compiled along with allWords and ngramsStats during corpus compilation
		String dataPath = IUConfig.getIUDataPath();
		FileReader fr = new FileReader(dataPath+"/data/numericTermsCorpus.json");
		AnalyzeNumberExpressions numberExpressionsAnalysis = new Gson().fromJson(fr, AnalyzeNumberExpressions.class);
		this.allNormalizedNumericTerms = getNormalizedNumericTerms(numberExpressionsAnalysis);
		this.ngramStatsOfNumericTerms = getNgramsStatsOfNumericTerms(numberExpressionsAnalysis);
		
		return;
	}
	

	/*
	 * Ideally those 2 values should have been compiled during the corpus compilation.
	 * But for now, they are compiled externally and stored in a special corpus compilation (json) file.
	 * (see AnalyseNumberExpressions.java in ca.inuktitutcomputing.utilbin)
	 */
	private Map<String, Long> getNgramsStatsOfNumericTerms(AnalyzeNumberExpressions numberExpressionsAnalysis) {
		return numberExpressionsAnalysis.getNgramStats();
	}


	private String getNormalizedNumericTerms(AnalyzeNumberExpressions numberExpressionsAnalysis) {
		return numberExpressionsAnalysis.getDecomposedNormalizedNumericTermsSuite();
	}


	public void setEditDistanceAlgorithm(EditDistanceCalculatorFactory.DistanceMethod name) throws ClassNotFoundException, EditDistanceCalculatorFactoryException {
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator(name);
	}
	
	public void setVerbose(boolean value) {
		verbose = value;
		if (corpus != null) corpus.setVerbose(value);
	}
	
	public void addCorrectWord(String word) {		
		String[] numericTermParts = null;
		boolean wordIsNumericTerm = (numericTermParts=splitNumericExpression(word)) != null;
		if (wordIsNumericTerm && allNormalizedNumericTerms.indexOf(","+"0000"+numericTermParts[1]+",") < 0) {
			if (allNormalizedNumericTerms == null || allNormalizedNumericTerms.isEmpty()) {
				allNormalizedNumericTerms = "";
			}
			allNormalizedNumericTerms += ",0000"+numericTermParts[1]+",";
		} else {
			if (allWords == null || allWords.isEmpty()) {
				allWords = "";
			}
			allWords += ","+word+",";
		}
		__updateSequenceIDFForWord(word,wordIsNumericTerm);
		clearWordsWithNgramCache();
	}
	
	private void __updateSequenceIDFForWord(String word, boolean wordIsNumericTerm) {
		Set<String> seqSeenInWord = new HashSet<String>();
		try {
		for (int seqLen = 1; seqLen <= MAX_SEQ_LEN; seqLen++) {
			for (int  start=0; start <= word.length() - seqLen; start++) {
				String charSeq = word.substring(start, start+seqLen);
				if (!seqSeenInWord.contains(charSeq)) {
					__updateSequenceIDF(charSeq, wordIsNumericTerm);
				}
				seqSeenInWord.add(charSeq);
			}
		}
		} catch (Exception e) {
		}
	}

	private void __updateSequenceIDF(String charSeq, boolean wordIsNumericTerm) {
		if (wordIsNumericTerm) {
			if (!ngramStatsOfNumericTerms.containsKey(charSeq)) {
				ngramStatsOfNumericTerms.put(charSeq, new Long(0));
			}
			ngramStatsOfNumericTerms.put(charSeq, ngramStatsOfNumericTerms.get(charSeq) + 1);
		} else {
			if (!ngramStats.containsKey(charSeq)) {
				ngramStats.put(charSeq, new Long(0));
			}
			ngramStats.put(charSeq, ngramStats.get(charSeq) + 1);
		}
	}

	public void saveToFile(File checkerFile) throws IOException {
		FileWriter saveFile = new FileWriter(checkerFile);
		Gson gson = new Gson();
		gson.toJson(this, saveFile);
		saveFile.flush();
		saveFile.close();
		//System.out.println("saved in "+checkerFile.getAbsolutePath());
	}

	public SpellChecker readFromFile(File checkerFile) throws FileNotFoundException, IOException {
		FileReader jsonFileReader = new FileReader(checkerFile);
		Gson gson = new Gson();
		SpellChecker checker = gson.fromJson(jsonFileReader, SpellChecker.class);
		jsonFileReader.close();
		this.MAX_SEQ_LEN = checker.getMaxSeqLen();
		this.MAX_CANDIDATES = checker.getMaxCandidates();
		this.allWords = checker.getAllWords();
		this.ngramStats = checker.getIdfStats();
		return this;
	}

	private Map<String, Long> getIdfStats() {
		return this.ngramStats;
	}

	private String getAllWords() {
		return this.allWords;
	}

	private int getMaxCandidates() {
		return this.MAX_CANDIDATES;
	}

	private int getMaxSeqLen() {
		return this.MAX_SEQ_LEN;
	}

	public SpellingCorrection correctWord(String word) throws SpellCheckerException {
		return correctWord(word,-1);
	}

	public SpellingCorrection correctWord(String word, int maxCorrections) throws SpellCheckerException {
		Logger logger = Logger.getLogger("SpellChecker.correctWord");
		
//		SpellTracer.trace("SpellChecker.correctWord", 
//				"Invoked on word="+word, 
//				word, null);
		
		boolean wordIsSyllabic = Syllabics.allInuktitut(word);
		
		String wordInLatin = word;
		if (wordIsSyllabic) {
			wordInLatin = TransCoder.unicodeToRoman(word);
		}
		
		SpellingCorrection corr = new SpellingCorrection(word);
		corr.wasMispelled = isMispelled(wordInLatin);		
		logger.debug("wasMispelled= "+corr.wasMispelled);

//		SpellTracer.trace("SpellChecker.correctWord", 
//				"corr.wasMispelled="+corr.wasMispelled, 
//				word, null);
		
		if (corr.wasMispelled) {
			// set ngramStats and suite of words for candidates according to type of word (normal word or numeric expression)
			String[] numericTermParts = splitNumericExpression(wordInLatin);
			boolean wordIsNumericTerm = numericTermParts != null;
			
			if (partialCorrectionEnabled) {
				computeCorrectPortions(wordInLatin, corr);
			}
			
			Set<String> candidates = firstPassCandidates_TFIDF(wordInLatin, wordIsNumericTerm);
			
			SpellDebug.containsCorrection(
					"SpellChecker.correctWord", 
					"List of string candidates", 
					word, "angajuqqaaqaqtutik", candidates);
			
			List<ScoredSpelling> scoredSpellings = computeCandidateSimilarities(wordInLatin, candidates);

			scoredSpellings = sortCandidatesBySimilarity(scoredSpellings);
			
			if (wordIsNumericTerm) {
				for (int ic=0; ic<scoredSpellings.size(); ic++) {
					ScoredSpelling scoredCandidate = scoredSpellings.get(ic);
					scoredCandidate.spelling = 
							scoredCandidate.spelling.replace("0000",numericTermParts[0]);
				}
			}
			
	 		if (maxCorrections != -1) {
	 			scoredSpellings = 
	 					scoredSpellings
	 					.subList(0, maxCorrections>scoredSpellings.size()? scoredSpellings.size() : maxCorrections); 			
	 		}
			
	 		corr.setPossibleSpellings(scoredSpellings);
		}
		
		if (wordIsSyllabic) {
			transcodeCandidatesToSyllabic(corr);
		}
	
 		return corr;
	}
	
	protected void computeCorrectPortions(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
//		System.out.println("SpellChecker.computeCorrectPortions: invoked with badWordRoman="+badWordRoman);
		computeCorrectLead(badWordRoman, corr);
		computeCorrectTail(badWordRoman, corr);
//		System.out.println("SpellChecker.computeCorrectPortions: DONE");
	}

	private void computeCorrectLead(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
		
		final int MAX_WORDS_TO_TRY = 5;
		
		String amongWords = getAllWordsToBeUsedForCandidates(badWordRoman);
		
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
			Set<String> words = wordsContainingSequ("^"+lead, amongWords);
//			System.out.println("** SpellChecker.computeCorrectLead: lead="+
//					lead+", words.size()="+words.size());
			boolean wordWasFoundForLead = false;
			for (String aWord: words) {
				if (leadRespectsMorphemeBoundaries(lead, aWord)) {
					// Found a word with the right characteristics
					wordWasFoundForLead = true;					
					longestCorrectLead = lead;
//					System.out.println("** SpellChecker.computeCorrectLead: setting longestCorrectLead="+
//							longestCorrectLead);
					break;
				}
			}
			
			if (wordWasFoundForLead) {
//				System.out.println("** SpellChecker.computeCorrectLead: setting longestCorrectLead="+
//						longestCorrectLead);
				break;
			}			
		}
		corr.correctLead = longestCorrectLead;
	}
	
	protected boolean leadRespectsMorphemeBoundaries(String lead, String word) 
			throws SpellCheckerException {
//		System.out.println("** SpellChecker.leadRespectsMorphemeBoundaries: lead="+
//				lead+", word=="+word);
		
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
		} catch(MorphInukException | LinguisticDataException e) {
			throw new SpellCheckerException(e);
		}
		
		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = aDecomp.morphemeSurfaceForms();
//				System.out.println("** SpellChecker.leadRespectsMorphemeBoundaries:"+
//						" looking at morphemes='"+String.join("', '", morphemes)+"'");
				
				String morphLead = "";
				for (String morph: morphemes) {
					morphLead += morph;
					if (morphLead.equals(lead)) {
						answer = true;
						break;
					}
				}
				if (answer != null) { break; }
//				System.out.println("** SpellChecker.leadRespectsMorphemeBoundaries:"+
//						"    after looking at morphemes, answer="+answer);
			}
		}
		
		if (answer == null) { answer = false; }
		
//		System.out.println("** SpellChecker.leadRespectsMorphemeBoundaries: lead="+
//				lead+", word="+word+", returns answer="+answer);
		
		return answer.booleanValue();
	}
	

	private void computeCorrectTail(String badWordRoman, 
			SpellingCorrection corr) throws SpellCheckerException {
		
		final int MAX_WORDS_TO_TRY = 5;
		
		String amongWords = getAllWordsToBeUsedForCandidates(badWordRoman);
		
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
			Set<String> words = wordsContainingSequ(tail+"$", amongWords);
//			System.out.println("** SpellChecker.computeCorrectTail: tail="+
//					tail+", words.size()="+words.size());
			boolean wordWasFoundForTail = false;
			for (String aWord: words) {
				if (tailRespectsMorphemeBoundaries(tail, aWord)) {
					// Found a word with the right characteristics
					wordWasFoundForTail = true;					
					longestCorrectTail = tail;
//					System.out.println("** SpellChecker.computeCorrectTail: setting longestCorrectTail="+
//							longestCorrectTail);
					break;
				}
			}
			
			if (wordWasFoundForTail) {
//				System.out.println("** SpellChecker.computeCorrectTail: setting longestCorrectTail="+
//						longestCorrectTail);
				break;
			}			
		}
		corr.correctTail = longestCorrectTail;
	}
	
	public boolean tailRespectsMorphemeBoundaries(String tail, String word) 
			throws SpellCheckerException {
//		System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries: tail="+
//				tail+", word=="+word);
		
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
		} catch(MorphInukException | LinguisticDataException e) {
			throw new SpellCheckerException(e);
		}
		
//		if (decomps == null) { System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries:"+
//				" decomps is NULL"); }

//		if (decomps.length == 0) { System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries:"+
//				" decomps is Empty"); }
		
		if (decomps != null) {
			for (Decomposition aDecomp: decomps) {
				List<String> morphemes = aDecomp.morphemeSurfaceForms();
//				System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries:"+
//						" looking at morphemes='"+String.join("', '", morphemes)+"'");
				
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
//				System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries:"+
//						"    after looking at morphemes, answer="+answer);
				
			}
		}
		
		if (answer == null) { answer = false; }
		
//		System.out.println("** SpellChecker.tailRespectsMorphemeBoundaries: tail="+
//				tail+", word="+word+", returns answer="+answer);
		
		return answer.booleanValue();
	}
		
	
	/**
	 * Transcode a list of scored candidate spellings to 
	 * syllabic.
	 * 
	 * @param sortedScoredCandidates
	 * @return 
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
		boolean wordIsMispelled = false;
		String[] numericTermParts = null;
		
//		if (corpus!=null && corpus.getWordsFailedSegmentation().contains(word)) {
//			wordIsMispelled = true;
//		} 
//		else 
		if (corpus!=null && corpus.getSegmentsCache().containsKey(word) && corpus.getSegmentsCache().get(word).length != 0) {
			logger.debug("word in segments cache has successfully decomposed");
			wordIsMispelled = false;
		}
		else if (word.matches("^[0-9]+$")) {
			logger.debug("word is all digits");
			wordIsMispelled = false;
		}
		else if (latinSingleInuktitutCharacters.contains(word)) {
			logger.debug("single inuktitut character");
			wordIsMispelled = false;
		}
		else if (wordContainsMoreThanTwoConsecutiveConsonants(word)) {
			logger.debug("more than 2 consecutive consonants in the word");
			wordIsMispelled = true;
		}
		else if ( (numericTermParts=splitNumericExpression(word)) != null) {
			logger.debug("numeric expression: "+word+" ("+numericTermParts[1]+")");
			boolean pseudoWordWithSuffixAnalysesWithSuccess = assessEndingWithIMA(numericTermParts[1]);
			wordIsMispelled = !pseudoWordWithSuffixAnalysesWithSuccess;
			logger.debug("numeric expression - wordIsMispelled: "+wordIsMispelled);
		}
		else if (wordIsPunctuation(word)) {
			logger.debug("word is punctuation");
			wordIsMispelled = false;
		}
		else {
			try {
				String[] segments = segmenter.segment(word);
				logger.debug("word submitted to IMA: "+word);
				if (segments == null || segments.length == 0) {
					wordIsMispelled = true;
				}
			} catch (TimeoutException e) {
//				wordIsMispelled = false;
				wordIsMispelled = true;
			} catch (StringSegmenterException e) {
				throw new SpellCheckerException(e);
			}
			logger.debug("word submitted to IMA - mispelled: "+wordIsMispelled);
		}
		
		return wordIsMispelled;
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


	private List<ScoredSpelling> sortCandidatesBySimilarity(List<ScoredSpelling> scoredSpellings) {
		
		Iterator<ScoredSpelling> iteratorCand = scoredSpellings.iterator();
		Collections.sort(scoredSpellings, (ScoredSpelling p1, ScoredSpelling p2) -> {
			return p1.score.compareTo(p2.score);
		});
		
		return scoredSpellings;
	}

	protected List<ScoredSpelling> computeCandidateSimilarities(String badWord, Set<String> candidates) throws SpellCheckerException {
		List<ScoredSpelling> scoredCandidates = new ArrayList<ScoredSpelling>();
		
		Iterator<String> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			String candidate = iterator.next();
			double similarity = computeCandidateSimilarity(badWord,candidate);
			scoredCandidates.add(new ScoredSpelling(candidate, new Double(similarity)));
		}
		
		return scoredCandidates;
	}

	private double computeCandidateSimilarity(String badWord, String candidate) throws SpellCheckerException {
		
//		SpellTracer.trace("SpellChecker.computeCandidateSimilarity", 
//				"Invoked", 
//				badWord, candidate);
		
		double distance;
		try {
			distance = editDistanceCalculator.distance(badWord, candidate);
		} catch (EditDistanceCalculatorException e) {
			throw new SpellCheckerException(e);
		}
		return distance;
	}

	public Set<String> firstPassCandidates_TFIDF(String badWord, 
			boolean wordIsNumericTerm) {

		// 1. compile ngrams of badWord
		// 2. compile IDF of each ngram
		// 3. add words for top IDFs to the set of candidates until the number 
		//    of candidates exceeds the maximum
		// 4. compute scores for each word and order words highest score first
		//    compute edit distance, etc.
		
		String allWordsForCandidates = 
			getAllWordsToBeUsedForCandidates(wordIsNumericTerm);

		ngramStatsForCandidates = 
			getNgramStatsToBeUsedForCandidates(wordIsNumericTerm);		
		
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMin(3);
		ngramCompiler.includeExtremities(true);
		
		// Step 1: compile ngrams for the bad word
		//
		String[] badWordNgrams = ngrams4word(badWord, ngramCompiler);
		
		// Step 2: compile Inverse Document Frequency (IDF) of each 
		// ngram and sort them from highest to lowest IDF 
		//
		//    IDF(word) = 1 / (#words with this ngram + 1)
		//
		Pair<Pair<String,Double>[],Map<String,Double>> idfInfo = 
				computeNgramIDFs(badWordNgrams);
		Pair<String, Double>[] ngramsIDF = idfInfo.getFirst();
		Map<String, Double> idfHash = idfInfo.getSecond();
		
		// Step 3: Find words that most closely match the ngrams of the bad 
		// (up to a maximum of MAX_CANDIDATES
		//
		Set<String> candidates = 
				candidatesWithBestNGramsMatch(ngramsIDF, 
						allWordsForCandidates);
		
		// Step 4: compute scores for each word and sort them from highest to
		//   lowest score.
		//
		Pair<String,Double>[] arrScoreValues =
				scoreAndSortCandidates(candidates, badWordNgrams, 
						ngramsIDF, ngramCompiler);

		Set<String> allCandidates = new HashSet<String>();
		for (int i=0; i<arrScoreValues.length; i++) {
			allCandidates.add(arrScoreValues[i].getFirst());
		}
		
		SpellDebug.containsCorrection("SpellChecker.firstPassCandidates_TFIDF", 
				"Returned list allCandidates", "maliklugu", "maligluglu", 
				allCandidates);
		
		return allCandidates;
	}

	private Pair<Pair<String,Double>[],Map<String,Double>> computeNgramIDFs(String[] ngrams) {
		Map<String,Double>  idfHash = 
				new HashMap<String,Double>();
		
		Pair<String,Double> idf[] = new Pair[ngrams.length];
		
		for (int i=0; i<ngrams.length; i++) {
			Long ngramStat = ngramStatsForCandidates.get(ngrams[i]); //ngramStats.get(ngrams[i])
			if (ngramStat==null) ngramStat = (long)0;
			double val = 1.0 / (ngramStat + 1);
			idf[i] = new Pair<String,Double>(ngrams[i],val);
			idfHash.put(ngrams[i], val);
		}
		IDFComparator dcomparator = new IDFComparator();
		Arrays.sort(idf,dcomparator);
		
		
		return Pair.of(idf, idfHash);
	}

	private Set<String> candidatesWithBestNGramsMatch(
			Pair<String, Double>[] idf, 
			String amongWords) {
		Set<String> candidates = new HashSet<String>();		
		for (int i=0; i<idf.length; i++) {
			
			Set<String> candidatesWithNgram = 
					wordsContainingSequ(idf[i].getFirst(), amongWords);
			
			SpellDebug.containsCorrection("SpellChecker.firstPassCandidates_TFIDF", 
					"Words that contain ngram="+idf[i].getFirst(), 
					"maliklugu","maligluglu", 
					candidatesWithNgram);
			
			candidates.addAll(candidatesWithNgram);	
			
			if (candidates.size() > MAX_CANDIDATES) {
				break;
			}
		}
		
		return candidates;
	}
	
	private Pair<String, Double>[] scoreAndSortCandidates(
			Set<String> candidates, 
			String[] badWordNGrams, Pair<String, Double>[] ngramsIDF, 
			NgramCompiler ngramCompiler) {
		
		Map<String,Double> idfHash = new HashMap<String,Double>();
		for (Pair<String,Double> ngramInfo: ngramsIDF) {
			idfHash.put(ngramInfo.getFirst(), ngramInfo.getSecond());
		}
		
		Set<String> ngramsOfBadWord = new HashSet<String>();
		for (String ngram: badWordNGrams) {ngramsOfBadWord.add(ngram);}
		
		List<Pair<String,Double>> scoreValues = new ArrayList<Pair<String,Double>>();
		Iterator<String> it = candidates.iterator();
		while (it.hasNext()) {
			String candidate = it.next();
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
				if (ngramsOfBadWord.contains(el) && ngramsOfCandidate.contains(el)) {
					double score = idfHash.get(el);
					totalScore += score;
				}
			}
			scoreValues.add(new Pair<String,Double>(candidate,totalScore));
		}
		WordScoreComparator comparator = new WordScoreComparator();
		Pair<String,Double>[] arrScoreValues = scoreValues.toArray(new Pair[] {});
		Arrays.sort(arrScoreValues, comparator);
		
		return arrScoreValues;
	}
	

	private String[] ngrams4word(String word, 
			NgramCompiler ngramCompiler) {
		Set<String>ngramsOfBadWord = ngramCompiler.compile(word);
		String[] ngrams = ngramsOfBadWord.toArray(new String[] {});
		
		if (SpellDebug.traceIsActive(word)) {
			SpellDebug.trace("SpellChecker.ngrams4word", 
					"word ngrams=['"+String.join("', '", ngrams)+"']", 
					word, null);
		}
		
		return ngrams;
	}

	public class IDFComparator implements Comparator<Pair<String,Double>> {
	    @Override
	    public int compare(Pair<String,Double> a, Pair<String,Double> b) {
	    	if (a.getSecond().longValue() > b.getSecond().longValue())
	    		return -1;
	    	else if (a.getSecond().longValue() < b.getSecond().longValue())
				return 1;
	    	else 
	    		return 0;
	    }
	}

	public class WordScoreComparator implements Comparator<Pair<String,Double>> {
	    @Override
	    public int compare(Pair<String,Double> a, Pair<String,Double> b) {
	    	if (a.getSecond().longValue() > b.getSecond().longValue())
	    		return -1;
	    	else if (a.getSecond().longValue() < b.getSecond().longValue())
				return 1;
	    	else 
	    		return a.getFirst().compareToIgnoreCase(b.getFirst());
	    }
	}

	protected Set<String> wordsContainingSequ(String seq, 
			String amongWords) {
		Logger logger = Logger.getLogger("SpellChecker.wordsContainingSequ");
		
		Set<String> wordsWithSeq = uncacheWordsWithNgram(seq);
		if (wordsWithSeq == null) {
			Pattern p;
			if (seq.charAt(0)=='^' && seq.charAt(seq.length()-1)=='$') {
				seq = seq.substring(1,seq.length()-1);
				p = Pattern.compile(",("+seq+"),");
			}
			else if (seq.charAt(0)=='^') {
				seq = seq.substring(1);
				p = Pattern.compile(",("+seq+"[^,]*),");
			}
			else if (seq.charAt(seq.length()-1)=='$') {
				logger.debug("seq= "+seq);
				seq = seq.substring(0,seq.length()-1);
				logger.debug(">>> seq= "+seq);
				p = Pattern.compile(",([^,]*"+seq+"),");
			}
			else
				p = Pattern.compile(",([^,]*"+seq+"[^,]*),");
						
			Matcher m = p.matcher(amongWords); //p.matcher(allWords)
			wordsWithSeq = new HashSet<String>();
			while (m.find())
				wordsWithSeq.add(m.group(1));
			cacheWordsWithNgram(seq, wordsWithSeq);
		}
		return wordsWithSeq;
	}

	public List<SpellingCorrection> correctText(String text) throws SpellCheckerException {
		return correctText(text, null);
	}

	public List<SpellingCorrection> correctText(String text, Integer nCorrections) throws SpellCheckerException {
		Logger tLogger = Logger.getLogger("SpellChecker.correctText");
		if (nCorrections == null) nCorrections = DEFAULT_CORRECTIONS;
		List<SpellingCorrection> corrections = new ArrayList<SpellingCorrection>();
		
		IUTokenizer iutokenizer = new IUTokenizer();
		iutokenizer.run(text);
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
			} catch (TimeoutException | MorphInukException e) {
			}
			logger.debug("decs: "+(decs==null?"null":decs.length));
			if (decs!=null && decs.length!=0) {
				accepted = true;
				break;
			}
		}
		
		return accepted;
	}
	
	
	Map<String,Long> getNgramStatsToBeUsedForCandidates(boolean wordIsNumericTerm) {
		return wordIsNumericTerm? this.ngramStatsOfNumericTerms : this.ngramStats;
	}

	String getAllWordsToBeUsedForCandidates(boolean wordIsNumericTerm) {
		return wordIsNumericTerm? this.allNormalizedNumericTerms : this.allWords;
	}
	
	String getAllWordsToBeUsedForCandidates(String word) {
		boolean isNumericTerm = (null != splitNumericExpression(word));
		return getAllWordsToBeUsedForCandidates(isNumericTerm);
	}
	
	private void cacheWordsWithNgram(String ngram, Set<String> words) {
		wordsWithNgramCache.put(ngram, words);
	}

	private Set<String> uncacheWordsWithNgram(String ngram) {
		Set<String> words = wordsWithNgramCache.getIfPresent(ngram);
		return words;
	}
	
	private void clearWordsWithNgramCache() {
		wordsWithNgramCache = 
				Caffeine.newBuilder().maximumSize(10000)
				.build();
	}
}
