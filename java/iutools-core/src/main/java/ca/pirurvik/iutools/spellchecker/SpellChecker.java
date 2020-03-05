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

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilbin.AnalyzeNumberExpressions;
import ca.inuktitutcomputing.utilities.EditDistanceCalculator;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactory;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactoryException;
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
	
	
	public SpellChecker() throws StringSegmenterException {
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
		segmenter = new StringSegmenter_IUMorpheme();
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
		//System.out.println("-- addCorrectWord: word="+word);
		String[] numericTermParts = null;
		boolean wordIsNumericTerm = (numericTermParts=wordIsNumberWithSuffix(word)) != null;
		if (wordIsNumericTerm && allNormalizedNumericTerms.indexOf(","+"0000"+numericTermParts[1]+",") < 0)
			allNormalizedNumericTerms += "0000"+numericTermParts[1]+",,";
		else
			allWords += word+",,";
		__updateSequenceIDFForWord(word,wordIsNumericTerm);
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

		boolean wordIsSyllabic = Syllabics.allInuktitut(word);
		
		String wordInLatin = word;
		if (wordIsSyllabic) {
			wordInLatin = TransCoder.unicodeToRoman(word);
		}
		
		SpellingCorrection corr = new SpellingCorrection(word);
		corr.wasMispelled = isMispelled(wordInLatin);
		logger.debug("wasMispelled= "+corr.wasMispelled);
		if (corr.wasMispelled) {
			// set ngramStats and suite of words for candidates according to type of word (normal word or numeric expression)
			String[] numericTermParts = wordIsNumberWithSuffix(wordInLatin);
			boolean wordIsNumericTerm = numericTermParts != null;
			allWordsForCandidates = getAllWordsToBeUsedForCandidates(wordIsNumericTerm);
			ngramStatsForCandidates = getNgramStatsToBeUsedForCandidates(wordIsNumericTerm);
			Set<String> candidates = firstPassCandidates_TFIDF(wordInLatin);
			logger.debug("candidates= "+PrettyPrinter.print(candidates));
			List<Pair<String,Double>> candidatesWithSim = computeCandidateSimilarities(wordInLatin, candidates);
			List<String> corrections = sortCandidatesBySimilarity(candidatesWithSim);
			logger.debug("corrections for "+word+": "+corrections.size());
			
			if (wordIsNumericTerm)
				for (int ic=0; ic<corrections.size(); ic++) {
					corrections.set(ic, corrections.get(ic).replace("0000",numericTermParts[0]));
				}
			
	 		if (maxCorrections== -1)
	 			corr.setPossibleSpellings(corrections);
	 		else
	 			corr.setPossibleSpellings(corrections.subList(0, maxCorrections>corrections.size()? corrections.size() : maxCorrections));
		}
		
		if (wordIsSyllabic) {
			// Transcode the spellings back to Syllabic
			List<String> possSpellingsSyll = new ArrayList<String>();
			for (String aSpelling: corr.getPossibleSpellings()) {
				aSpelling = TransCoder.romanToUnicode(aSpelling);
				possSpellingsSyll.add(aSpelling);
			}
			corr.setPossibleSpellings(possSpellingsSyll);
		}
 		
 		return corr;
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
				wordIsMispelled = false;
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


//	public Long ngramStat(String charSeq) {
//		Long val = new Long(0);
//		if (ngramStats.containsKey(charSeq)) {
//			val = ngramStats.get(charSeq);
//		}
//		return val;
//	}
	

	private List<String> sortCandidatesBySimilarity(List<Pair<String, Double>> candidatesWithSim) {
		Iterator<Pair<String, Double>> iteratorCand = candidatesWithSim.iterator();
	
		Collections.sort(candidatesWithSim, (Pair<String,Double> p1, Pair<String,Double> p2) -> {
			return p1.getSecond().compareTo(p2.getSecond());
		});
		List<String> candidates = new ArrayList<String>();
		iteratorCand = candidatesWithSim.iterator();
		while (iteratorCand.hasNext()) {
			Pair<String,Double> pair = iteratorCand.next();
			candidates.add(pair.getFirst());
			//System.out.println("-- sortCandidatesBySimiliraty (2): "+"candidate: "+pair.getFirst()+" ; similarity="+pair.getSecond());
		}
		return candidates;
	}

	protected List<Pair<String, Double>> computeCandidateSimilarities(String badWord, Set<String> candidates) {
		List<Pair<String,Double>> candidateSimilarities = new ArrayList<Pair<String,Double>>();
		Iterator<String> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			String candidate = iterator.next();
			double similarity = computeCandidateSimilarity(badWord,candidate);
			candidateSimilarities.add(new Pair<String,Double>(candidate,new Double(similarity)));
			//System.out.println("-- computeCandidateSimilarities:    "+"candidate: "+candidate+" ; similarity="+similarity);
		}
		return candidateSimilarities;
	}

	private double computeCandidateSimilarity(String badWord, String candidate) {
		// TODO Auto-generated method stub
		int distance = editDistanceCalculator.distance(candidate,badWord);
		return (double)distance;
	}

	// Not used anymore, replaced by firstPassCandidates_TFIDF - to be deleted
//	public Set<String> firstPassCandidates(String badWord) {
//		Logger logger = Logger.getLogger("SpellChecker.firstPassCandidates");
//		Set<String> candidates = new HashSet<String>();
//		List<Pair<String,Long>> rarest = rarestSequencesOf(badWord);
//		while (!rarest.isEmpty()) {
//			Pair<String,Long> currSeqInfo = rarest.remove(0);
//			logger.debug("sequence: "+currSeqInfo.getFirst()+" ("+currSeqInfo.getSecond()+")");
//			Set<String> wordsWithSequence = wordsContainingSequ(currSeqInfo.getFirst());
//			logger.debug("  wordsWithSequence: "+wordsWithSequence.size());
//			
//			candidates.addAll(wordsWithSequence);
//			logger.debug("  candidates: "+candidates.size());
//			if (candidates.size() >= MAX_CANDIDATES) {
//				if (!rarest.isEmpty()) {
//					Pair<String,Long> nextSeqInfo = rarest.get(0);
//					if (currSeqInfo.getSecond() == nextSeqInfo.getSecond()) {
//						// The next sequence is just as rare as the current one, so 
//						// keep going.
//						continue;
//					} else {
//						break;
//					}
//				}
//			}
//		}
//		logger.debug("Nb. candidates: "+candidates.size());
//		Iterator<String> itcand = candidates.iterator();
//		//while (itcand.hasNext())
//		//	logger.debug("candidate: "+itcand.next());
//		return candidates;
//	}


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
			candidates.addAll(candidatesWithNgram);	
			if (candidates.size() > MAX_CANDIDATES)
				break;
		}
		
		// 4. compute scores for each word and order words highest score first
		List<Pair<String,Double>> scoreValues = new ArrayList<Pair<String,Double>>();
		Iterator<String> it = candidates.iterator();
		while (it.hasNext()) {
			String candidate = it.next();
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
		Matcher m = p.matcher(allWordsForCandidates); //p.matcher(allWords)
		Set<String> wordsWithSeq = new HashSet<String>();
		while (m.find())
			wordsWithSeq.add(m.group(1));
		return wordsWithSeq;
	}

//	public List<Pair<String, Long>> rarestSequencesOf(String string) {
//		List<Pair<String,Long>> listOfRarest = new ArrayList<Pair<String,Long>>();
//		// TODO:
//		for (int seqLen = 1; seqLen <= MAX_SEQ_LEN; seqLen++) {
//			for (int  start=0; start <= string.length() - seqLen; start++) {
//				String charSeq = string.substring(start, start+seqLen);
//				Long idf = ngramStat(charSeq);
//				//System.out.println("-- rarestSequencesOf:    charSeq="+charSeq+" ("+idf+")");;
//				if (idf != 0) {
//					Pair<String,Long> pair = new Pair<String,Long>(charSeq,idf);
//					if ( !listOfRarest.contains(pair) )
//						listOfRarest.add(pair);
//				}
//			}
//		}
//		Collections.sort(listOfRarest,(Object p1, Object p2) -> {
//			return ((Pair<String,Long>)p1).getSecond().compareTo(((Pair<String,Long>)p2).getSecond());
//		});
//		
//		return listOfRarest;
//		
//	}
	
	
	
	

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
		
		tLogger.trace("tokens= "+PrettyPrinter.print(tokens));
		
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
}
