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
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculator;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorException;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactory;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactoryException;
import ca.inuktitutcomputing.config.IUConfig;
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
	public int MAX_CANDIDATES = 300; //200; //100;
	public int DEFAULT_CORRECTIONS = 5;
	public String allWords = ",,";
	public Map<String,Long> ngramStats = new HashMap<String,Long>();
	
	public String allNormalizedNumericTerms = ",,";
	public Map<String,Long> ngramStatsOfNumericTerms = new HashMap<String,Long>();
	
	public transient String allWordsForCandidates = null;
	public transient Map<String,Long> ngramStatsForCandidates = null;

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
		boolean wordIsNumericTerm = (numericTermParts=wordIsNumberWithSuffix(word)) != null;
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
			String[] numericTermParts = wordIsNumberWithSuffix(wordInLatin);
			boolean wordIsNumericTerm = numericTermParts != null;
			allWordsForCandidates = getAllWordsToBeUsedForCandidates(wordIsNumericTerm);
			ngramStatsForCandidates = getNgramStatsToBeUsedForCandidates(wordIsNumericTerm);
			Set<String> candidates = firstPassCandidates_TFIDF(wordInLatin);
			
//			SpellTracer.containsCorrection(
//					"SpellChecker.correctWord", 
//					"List of string candidates", 
//					word, "tamainni", candidates);
			
//			SpellTracer.trace("SpellChecker.correctWord", 
//					"first pass candidates: "+StringUtils.join(candidates.iterator(), ", "), 
//					word, null);
			
//			if (logger.isDebugEnabled()) logger.debug("candidates= "+PrettyPrinter.print(candidates));
			
			List<ScoredSpelling> scoredSpellings = computeCandidateSimilarities(wordInLatin, candidates);

//			SpellTracer.containsCorrection(
//					"SpellChecker.correctWord", 
//					"Unsorted List of scored spellings", 
//					word, "tamainni", scoredSpellings);
			
			scoredSpellings = sortCandidatesBySimilarity(scoredSpellings);
			
//			SpellTracer.trace("SpellChecker.correctWord", 
//					"SORTED scored candidates: "+StringUtils.join(scoredSpellings.iterator(), ", "), 
//					word, null);
			
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
		boolean wordIsMispelled = false;
		String[] numericTermParts = null;
		
		if (corpus!=null && corpus.getWordsFailedSegmentation().contains(word)) {
			wordIsMispelled = true;
		} 
		else if (corpus!=null && corpus.getSegmentsCache().containsKey(word)) {
			wordIsMispelled = false;
		}
		else if (word.matches("^[0-9]+$")) {
			wordIsMispelled = false;
		}
		else if (latinSingleInuktitutCharacters.contains(word)) {
			wordIsMispelled = false;
		}
		else if (wordContainsMoreThanTwoConsecutiveConsonants(word)) {
			wordIsMispelled = true;
		}
		else if ( (numericTermParts=wordIsNumberWithSuffix(word)) != null) {
			boolean pseudoWordWithSuffixAnalysesWithSuccess = assessEndingWithIMA(numericTermParts[1]);
			wordIsMispelled = !pseudoWordWithSuffixAnalysesWithSuccess;
		}
		else if (wordIsPunctuation(word)) {
			wordIsMispelled = false;
		}
		else {
			try {
				String[] segments = segmenter.segment(word);
				if (segments == null || segments.length == 0) {
					wordIsMispelled = true;
				}
			} catch (TimeoutException e) {
//				wordIsMispelled = false;
				wordIsMispelled = true;
			} catch (StringSegmenterException e) {
				throw new SpellCheckerException(e);
			}
		}
		
		return wordIsMispelled;
	}

	protected boolean wordIsPunctuation(String word) {
		Pattern p = Pattern.compile("(\\p{Punct}|[–])+");
		Matcher mp = p.matcher(word);
		return mp.matches();
	}


	protected String[] wordIsNumberWithSuffix(String word) {
		Pattern p = Pattern.compile("^(\\$?\\d+(?:[.,:]\\d+)?(?:[.,:]\\d+)?-?)([agijklmnpqrstuv]+)$");
		Matcher mp = p.matcher(word);
		if (mp.matches())
			return new String[] {mp.group(1),mp.group(2)};
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


	private void traceScoredCandidates(String mess, List<Pair<String, Double>> candidatesWithSim) {
		System.out.println("** printScoredCandidates("+mess+"): Scored candiates are:\n");
		Iterator<Pair<String, Double>> iteratorCand = candidatesWithSim.iterator();
		while (iteratorCand.hasNext()) {
			Pair<String, Double> cand = iteratorCand.next();
			System.out.println("  corr="+cand.getFirst()+"; score="+cand.getSecond());
		}
	}
	
	private List<ScoredSpelling> sortCandidatesBySimilarity(List<ScoredSpelling> scoredSpellings) {
		
//		traceScoredCandidates("sortCandidatesBySimilarity on START", candidatesWithSim);
		
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
			if (candidate.equals("niggiani")) {
				System.out.println("** computeCandidateSimilarities: similarity score for 'niggiani' = "+similarity);
			}
		}
		
		
		
		return scoredCandidates;
	}

	private double computeCandidateSimilarity(String badWord, String candidate) throws SpellCheckerException {
		
//		SpellTracer.trace("SpellChecker.computeCandidateSimilarity", 
//				"Invoked", 
//				badWord, candidate);
		
		double distance;
		try {
			distance = editDistanceCalculator.distance(candidate,badWord);
		} catch (EditDistanceCalculatorException e) {
			throw new SpellCheckerException(e);
		}
		return distance;
	}

	public Set<String> firstPassCandidates_TFIDF(String badWord) {
		Logger logger = Logger.getLogger("SpellChecker.firstPassCandidates_TFIDF");
		// 1. compile ngrams of badWord
		// 2. compile IDF of each ngram = 1 / #words with this ngram + 1 and order highest first
		// 3. add words for top IDFs to the set of candidates until the number of candidates exceeds the maximum
		// 4. compute scores for each word and order words highest score first
		// compute edit distance, etc.
		
		// 1. compile ngrams of badWord
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMin(3);
		ngramCompiler.includeExtremities(true);
		Set<String>ngramsOfBadWord = ngramCompiler.compile(badWord);
		String[] ngrams = ngramsOfBadWord.toArray(new String[] {});
		logger.debug("ngramsOfBadWord= "+String.join("; ", ngrams));
		
		// 2. compile IDF of each ngram = 1 / #words with this ngram + 1 and order highest first
		Pair<String,Double> idf[] = new Pair[ngrams.length];
		HashMap<String,Double> idfHash = new HashMap<String,Double>();
		for (int i=0; i<ngrams.length; i++) {
			Long ngramStat = ngramStatsForCandidates.get(ngrams[i]); //ngramStats.get(ngrams[i])
			if (ngramStat==null) ngramStat = (long)0;
			double val = 1.0 / (ngramStat + 1);
			idf[i] = new Pair<String,Double>(ngrams[i],val);
			idfHash.put(ngrams[i], val);
		}
		IDFComparator dcomparator = new IDFComparator();
		Arrays.sort(idf,dcomparator);
		
		// 3. add words for top IDFs to the set of candidates until the number of candidates exceeds the maximum
		Set<String> candidates = new HashSet<String>();
		for (int i=0; i<idf.length; i++) {
			Set<String> candidatesWithNgram = wordsContainingSequ(idf[i].getFirst());
//			SpellTracer.trace("SpellChecker.firstPassCandidates_TFIDF", 
//					"Adding candidates that contain ngram="+idf[i].getFirst()+
//					", namely: "+StringUtils.join(candidatesWithNgram.iterator(), ","),
//					badWord, "tamainni");
			candidates.addAll(candidatesWithNgram);	
			if (candidates.size() > MAX_CANDIDATES)
				break;
		}
		
//		SpellTracer.containsCorrection(
//				"SpellChecker.firstPassCandidates_TFIDF", 
//				"Unsorted candidates", 
//				badWord, "tamainni", candidates);
		
		
		// 4. compute scores for each word and order words highest score first
		List<Pair<String,Double>> scoreValues = new ArrayList<Pair<String,Double>>();
		Iterator<String> it = candidates.iterator();
		while (it.hasNext()) {
			String candidate = it.next();
			if (candidate.equals("niggiani")) {
				System.out.println("** firstPassCandidates_TFIDF: scoring tfidf of 'niggiani'");
			}
			Set<String> ngramsOfCandidate = ngramCompiler.compile(candidate);
			Set<String> all = new HashSet<String>();
			all.addAll(ngramsOfBadWord);
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
			if (candidate.equals("niggiani")) {
				System.out.println("** firstPassCandidates_TFIDF: tfidf score for 'niggiani' = "+totalScore);
			}
			scoreValues.add(new Pair<String,Double>(candidate,totalScore));
		}
		WordScoreComparator comparator = new WordScoreComparator();
		Pair<String,Double>[] arrScoreValues = scoreValues.toArray(new Pair[] {});
		Arrays.sort(arrScoreValues, comparator);

		Set<String> allCandidates = new HashSet<String>();
		for (int i=0; i<arrScoreValues.length; i++) {
			allCandidates.add(arrScoreValues[i].getFirst());
		}
		return allCandidates;
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

	protected Set<String> wordsContainingSequ(String seq) {
		Logger logger = Logger.getLogger("SpellChecker.wordsContainingSequ");
		
		Set<String> wordsWithSeq = uncacheWordsWithNgram(seq);
//		Set<String> wordsWithSeq = null;
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
			
			if (seq.startsWith("tamain")) {
				System.out.println("** wordsContainingSequ: looking for p="+p+
						" in allWordsForCandidates="+allWordsForCandidates);
			}
			
			Matcher m = p.matcher(allWordsForCandidates); //p.matcher(allWords)
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
