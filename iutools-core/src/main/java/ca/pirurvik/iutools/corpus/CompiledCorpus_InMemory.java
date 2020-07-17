package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.visitors.TrieNodeVisitor;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

public class CompiledCorpus_InMemory extends CompiledCorpus 
{	
	public int MAX_NGRAM_LEN = 5;
	public static String JSON_COMPILATION_FILE_NAME = "trie_compilation.json";
	
	public Trie_InMemory trie = new Trie_InMemory();
	
	// things related to the compiler's state and operation that need to be saved periodically and at termination
	private Set<String> wordsFailedSegmentation = new HashSet<String>();
	public HashMap<String,Long> wordsFailedSegmentationWithFreqs = new HashMap<String,Long>();
	
	private String wordSegmentations = ",,";
	
	private Map<String,String[][]> wordDecomps = 
			new HashMap<String,String[][]>();
	public String decomposedWordsSuite = ",,";

	protected Long terminalsSumFreq = null;
	
	public Map<String,Long> ngramStats = null;

	private Map<String,WordInfo> word2infoMap = new HashMap<String,WordInfo>();
		
	private Map<Long,String> key2word = new HashMap<Long,String>();
	private Map<String,Long> word2key = new HashMap<String,Long>();
	
	private Map<String,Set<Long>> ngram2wordKeysMap = 
				new HashMap<String,Set<Long>> ();

	// things that do not need to be saved 
	@JsonIgnore
	private transient String corpusDirectory;

	@JsonIgnore
	public transient String wordsWithSuccessfulDecomposition = null;
	@JsonIgnore
	public transient String wordsWithUnsuccessfulDecomposition = null;
	
	private Long nextWordKey = new Long(0);
	
	// ngrams from 1 to MAX_NGRAM_LEN of all decomposed words in the corpus
	public Map<String,Long> getNgramStats() {
		if (ngramStats==null) {
			setNgramStats();
		}
		return ngramStats;
	}
	
	public void setNgramStats() {
		ngramStats = new HashMap<String,Long>();
		String[] words = decomposedWordsSuite.split(",,");
		for (int iw=1; iw<words.length; iw++) {
			updateSequenceNgramsForWord(words[iw]);
		}
	}
	
	@Override
	public void addWordOccurence(String word, String[][] sampleDecomps, 
			Integer totalDecomps, long freqIncr) throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.addWordOccurence");
		Logger tLogger_STEPS = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.addWordOccurence_STEPS");

		TimeUnit tunit = TimeUnit.MILLISECONDS;
		long methodStart = 0;
		if (tLogger.isTraceEnabled()) {
			try {
				methodStart = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			}
			tLogger.trace("Adding word="+word);
		}

		long start = 0;
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			}
		}
		
		updateWordIndex(word, sampleDecomps, totalDecomps, freqIncr);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("addToWordCharIndex took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}
		
		updateDecompositionsIndex(word, sampleDecomps, totalDecomps);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("addToWordSegmentations took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}

		updateCharNgramIndex(word, sampleDecomps, totalDecomps);
		if (tLogger_STEPS.isTraceEnabled()) {
			try {
				tLogger_STEPS.trace("updateCharNgramIndex took "+
					StopWatch.elapsedSince(start, tunit)+" "+tunit);
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};
		}
		
		if (tLogger.isTraceEnabled()) {
			try {
				tLogger.trace("addWordOccurence took "+
					StopWatch.elapsedSince(methodStart, tunit)+" "+tunit+"\n");
			} catch (StopWatchException e) {
				throw new CompiledCorpusException(e);
			};			
		}
	}
	
	
	
	private void updateSequenceNgramsForWord(String word) {
		Set<String> seqsSeenInWord = new HashSet<String>();
		seqsSeenInWord = getCharsNgramCompiler().compile(word);
		Iterator<String> itngram = seqsSeenInWord.iterator();
		while (itngram.hasNext()) {
			String ngram = itngram.next();
			Long freq = ngramStats.get(ngram);
			if (freq==null)
				freq = (long)0;
			ngramStats.put(ngram, ++freq);
		}
	}

	@SuppressWarnings("serial")
	public static class CorpusTrieCompilerException extends Exception {
		public CorpusTrieCompilerException(String mess) {
			super(mess);
		}
	}
	
	// ------- Constructors ----------------------------------------------------
	
	public CompiledCorpus_InMemory() {
		super(); 
	}
	
	public CompiledCorpus_InMemory(String segmenterClassName) {
		super(segmenterClassName);
	}
	
	public Iterator<String> allWords() throws CompiledCorpusException {
		Set<String> allWordsSet = new HashSet<String>();
		allWordsSet.addAll(wordsFailedSegmentationWithFreqs.keySet());
		Collections.addAll(allWordsSet, decomposedWordsSuite.split(",,"));
		allWordsSet.remove("");
		return allWordsSet.iterator();
	}
	
	public String getWordSegmentations() {
		return wordSegmentations;
	}
	
	public String getDecomposedWordsSuite() {
		return decomposedWordsSuite;
	}
	
	public Trie getTrie() {
		return this.trie;
	}
		
	/**
	 * Cette méthode retourne vrai si et seulement si il y a un fichier de sauvegarde pour le répertoire corpusDir.
	 * @param corpusDirPathname
	 * @return
	 */
	
	public boolean canBeResumed(String corpusDirPathname) {
		File jsonFile = new File(corpusDirPathname+"/"+JSON_COMPILATION_FILE_NAME);
		return jsonFile.exists();
	}
	
	protected void updateWordIndex(String word, String[][] sampleDecomps, 
			Integer totalDecomps, long freqIncr) throws CompiledCorpusException {
    	wordDecomps.put(word, sampleDecomps);
    	updateWordInfo(word, sampleDecomps, totalDecomps, freqIncr);
		
		updateNGramIndex(word);    	
	}

	private void updateWordInfo(String word, String[][] sampleDecomps, 
		Integer totalDecomps) throws CompiledCorpusException {
		updateWordInfo(word, sampleDecomps, totalDecomps, 1);
	}

	private void updateWordInfo(String word, String[][] sampleDecomps, 
		Integer totalDecomps, long freqIncr) throws CompiledCorpusException {
		WordInfo info = getWord2infoMap().get(word);
		if (info == null) {
			key2word.put(nextWordKey, word);
			word2key.put(word, nextWordKey);	
			info = new WordInfo(word, this.nextWordKey);
			this.nextWordKey++;			
		}
		
		if (sampleDecomps != null) {
			info.setDecompositions(sampleDecomps, totalDecomps);
		}
		
		info.frequency += freqIncr;

	
		getWord2infoMap().put(word, info);
		
		return;
	}
	
	private void removeFromListOfFailedSegmentation(String word) {
		if (getWordsFailedSegmentation().contains(word)) {
//			getWordsFailedSegmentation().removeElement(word);
			getWordsFailedSegmentation().remove(word);
		}
		if (wordsFailedSegmentationWithFreqs.containsKey(word)) {
			wordsFailedSegmentationWithFreqs.remove(word);
		}
	}
	
	protected void addToDecomposedWordsSuite(String word) {
		decomposedWordsSuite += word+",,";
	}

	private void addToListOfFailedSegmentation(String word) {
		if ( !getWordsFailedSegmentation().contains(word) )
			getWordsFailedSegmentation().add(word);

		long nb;
		if (wordsFailedSegmentationWithFreqs.containsKey(word))
			nb = wordsFailedSegmentationWithFreqs.get(word).longValue()+1;
		else
			nb = 1;
		wordsFailedSegmentationWithFreqs.put(word, new Long(nb));
	}

	public HashMap<String,Long> getWordsThatFailedSegmentationWithFreqs() {
    	return wordsFailedSegmentationWithFreqs;
    }
	
	
	protected void compileExtras() {
		setNgramStats();
	}
    
    // ----------------------------- static -------------------------------
    
    public static CompiledCorpus_InMemory createFromJson(String jsonCompilationFilePathname) throws CompiledCorpusException {
    	try {
    		FileReader jsonFileReader = new FileReader(jsonCompilationFilePathname);
    		Gson gson = new Gson();
    		CompiledCorpus_InMemory compiledCorpus = gson.fromJson(jsonFileReader, CompiledCorpus_InMemory.class);
    		jsonFileReader.close();
    		return compiledCorpus;
    	} catch (FileNotFoundException e) {
    		throw new CompiledCorpusException("File "+jsonCompilationFilePathname+"does not exist. Could not create a compiled corpus.");
    	} catch (IOException e) {
    		throw new CompiledCorpusException(e);
    	}
    }

    
    // ----------------------------- STATISTICS -------------------------------
    
    @Override
    public long totalWordsWithNoDecomp() throws CompiledCorpusException {
    	return wordsFailedSegmentationWithFreqs.size();
    }
    
    public long totalOccurencesWithNoDecomp() {
    	Long[] nbOccurrences = wordsFailedSegmentationWithFreqs.values().toArray(new Long[] {});
    	long nb = 0;
    	for (Long nbOcc: nbOccurrences)
    		nb += nbOcc.longValue();
    	return nb;
    }
    
	public Long totalOccurencesWithDecomps() throws CompiledCorpusException {
		try {
			return getTrie().totalTerminalOccurences();
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	@Override
	public long totalWordsWithDecomps() throws CompiledCorpusException {
		try {
			return getTrie().getSize();
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	protected TrieNode[] getAllTerminals() throws CompiledCorpusException {
		try {
			return this.trie.getTerminals();
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
    // ----------------------------- private -------------------------------

	private static boolean isInuktitutWord(String string) {
		Pattern p = Pattern.compile("[agHijklmnpqrstuv&]+");
		Matcher m = p.matcher(string);
		if (m.matches()) {
			p = Pattern.compile("[aiu]+");
			m = p.matcher(string);
			if (m.find()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private static String[] extractWordsFromLine(String line) {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.corpus.CorpusCompiler.CompiledCorpus.extractWordsFromLine");
		line = line.replace('.', ' ');
		line = line.replace(',', ' ');
		logger.debug("line= '"+line+"'");
		String[] words = line.split("\\s+");
		logger.debug("words= "+PrettyPrinter.print(words));
		if (words.length!=0) {
			if (words[0].equals("")) {
				int n=words.length-1;
				String[] newWords=new String[n];
				System.arraycopy(words,1,newWords,0,n);
				words = newWords;
			}
		}
		logger.debug("words= "+PrettyPrinter.print(words));
		return words;
	}

	public TrieNode getMostFrequentTerminal(TrieNode node) throws CompiledCorpusException {
		try {
			return this.trie.getMostFrequentTerminal(node);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}

	public TrieNode getMostFrequentTerminal(String[] morphemes) throws CompiledCorpusException {		
		try {
			return this.trie.getMostFrequentTerminal(morphemes);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}

//	public String[] getMostFrequentSequenceForRoot(String string) throws CompiledCorpusException {
//		try {
//			return this.trie.getMostFrequentSequenceForRoot(string);
//		} catch (TrieException e) {
//			throw new CompiledCorpusException(e);
//		}
//	}
	
	
	public Boolean isWordInCorpus(String word) throws CompiledCorpusException {
		Boolean result;
		if (getWordsWithSuccessfulDecomposition().contains(",,"+word+",,"))
			result = new Boolean(true);
		else if (getWordsWithUnsuccessfulDecomposition().contains(",,"+word+",,"))
			result = new Boolean(false);
		else
			result = null;
		return result;
	}
	
	public String getWordsWithSuccessfulDecomposition() {
		return decomposedWordsSuite;
	}

	public String getWordsWithUnsuccessfulDecomposition() {
		if (wordsWithUnsuccessfulDecomposition==null) {
			wordsWithUnsuccessfulDecomposition = ",,";
//			for (int i=0; i<getWordsFailedSegmentation().size(); i++) {
//				wordsWithUnsuccessfulDecomposition += getWordsFailedSegmentation().get(i)+",,";
//			}
			for (String aWord: getWordsFailedSegmentation()) {
				wordsWithUnsuccessfulDecomposition += aWord+",,";
			}
		}
		return wordsWithUnsuccessfulDecomposition;
	}

	public String[] getWordsThatFailedDecomposition() {
		return getWordsFailedSegmentation().toArray(new String[] {});
	}

	public Set<String> getWordsFailedSegmentation() {
		return wordsFailedSegmentation;
	}
	
	public void setWordsFailedSegmentation(Set<String> wordsFailedSegmentation) {
		this.wordsFailedSegmentation = wordsFailedSegmentation;
	}
	
	public long totalOccurencesOf(String word) throws CompiledCorpusException {
		Logger logger = Logger.getLogger("CompiledCorpus.getNbOccurrencesOfWord");

		WordInfo winfo = info4word(word);
		long nbOccurrences = winfo.frequency;
		
		return nbOccurrences;
	}
	
	public List<WordWithMorpheme> wordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		List<WordWithMorpheme> words = new ArrayList<WordWithMorpheme>();
		Pattern pattern = Pattern.compile(",([^:,]+?)"+":([^:]*?\\{("+morpheme+"/.+?)\\}.*?),");
		Matcher matcher = pattern.matcher(wordSegmentations);
		while ( matcher.find() ) {
			String word = matcher.group(1);
			String morphId = matcher.group(3);
			String decomposition = matcher.group(2);
			long freq = this.totalOccurencesOf(word);
			words.add(new WordWithMorpheme(word,morphId,decomposition,freq));
		}
		
		return words;
	}

	public Long key4word(String word) {
		Long key = null;
		if (word2key.containsKey(word)) {
			key = word2key.get(word);
		} else {
			nextWordKey++;
			key = nextWordKey;
			word2key.put(word, key);
			key2word.put(key, word);
		}
		return key;
	}
	
	private String word4key(Long aWordKey) {
		String word = null;
		if (key2word.containsKey(aWordKey)) {
			word = key2word.get(aWordKey);
		}
		return word;
	}
	
	@Override
	public void updateDecompositionsIndex(WordInfo winfo) throws CompiledCorpusException {
		updateDecompositionsIndex(winfo.word, winfo.decompositionsSample, 
			winfo.totalDecompositions);
	}

	protected void updateDecompositionsIndex(
			String word, String[][] sampleDecomps, Integer totalDecomps) 
			throws CompiledCorpusException {
		
		if (sampleDecomps != null && sampleDecomps.length > 0) {
			String[] bestDecomp = sampleDecomps[0];

			wordSegmentations += word+":"+String.join("", bestDecomp)+",,";
			try {
				getTrie().add(bestDecomp, word);
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}
		} else {
			Long freq = wordsFailedSegmentationWithFreqs.get(word);
			if (freq == null) {
				freq = new Long(0);
			}
			wordsFailedSegmentationWithFreqs.put(word, freq+1);
			wordsFailedSegmentation.add(word);
		}
		
		addToDecomposedWordsSuite(word);
	}	
	
//	@Override
	protected void updateCharNgramIndex(String word, String[][] sampleDecomps, 
		Integer totalDecomps) throws CompiledCorpusException {
		updateNGramIndex(word);
	}
	
	private void updateNGramIndex(String word) {
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMin(3);
		ngramCompiler.includeExtremities(true);
		Set<String> ngramSet = ngramCompiler.compile(word);
		addAllWordNGrams(word, ngramSet);
	}
	
	private void addAllWordNGrams(String word, Set<String> ngrams) {
		Long key = key4word(word);
		for (String aNgram: ngrams) {
			addSingleWordNGram(key, aNgram);
		}
		
		return;
	}
	
	private void addSingleWordNGram(Long wordKey, String ngram) {
		if (!ngram2wordKeysMap.containsKey(ngram)) {
			ngram2wordKeysMap.put(ngram, new HashSet<Long>());
		}
		ngram2wordKeysMap.get(ngram).add(wordKey);
	}
	
	public Set<String> wordsContainingNgram(String ngram) 
			throws CompiledCorpusException {
		Set<String>  words = null;
		if (ngram2wordKeysMap.containsKey(ngram)) {
			Set<Long> wordKeys = ngram2wordKeysMap.get(ngram);
			words = new HashSet<String>();
			for (Long aWordKey: wordKeys) {
				String aWord = word4key(aWordKey);
				words.add(aWord);
			}
		}
		if (words == null) {
			words = new HashSet<String>();
		}
		return words;		
	}
		
	public WordInfo info4word(String word) throws CompiledCorpusException {
		WordInfo wInfo = null;
		if (getWord2infoMap().containsKey(word)) {
			wInfo = getWord2infoMap().get(word);
		} else if (wordDecomps.containsKey(word)){
			Long wordKey = key4word(word);
			wInfo = new WordInfo(word, wordKey);
			String[][] decomps = wordDecomps.get(word);
			wInfo.decompositionsSample = decomps;
			if (decomps != null && decomps.length > 0) {
				wInfo.topDecompositions = decomps[0];
				wInfo.totalDecompositions = decomps.length;
			}
			wInfo.frequency = computeWordFreq(word);
			getWord2infoMap().put(word, wInfo);
		}
		
		return wInfo;
	}
	
	private long computeWordFreq(String word) {
		long freq = 0;		
		
		// For now, we are only able to compute frequency of 
		// failed words
		if (wordsFailedSegmentationWithFreqs.containsKey(word)) {
			freq = wordsFailedSegmentationWithFreqs.get(word);
		}
		return freq;
	}

	public void incrementWordFreq(String word) throws CompiledCorpusException {
		WordInfo wInfo = info4word(word);
		if (wInfo == null) {
			throw new CompiledCorpusException(
					"Tried to increment frequency of unknown word "+word);
		}
		wInfo.frequency++;
	}

	public boolean containsWord(String word) throws CompiledCorpusException {
		boolean answer = (wordDecomps.keySet().contains(word));
		return answer;
	}
	
	public boolean ngramsAreComputed() {
		return ngramStats != null;
	}

	@Override
	protected Set<String> wordsContainingMorphNgram(String[] morphemes) 
			throws CompiledCorpusException {
		// Note: This method is not supported by this class
		return new HashSet<String>();
	}

	@Override
	public long charNgramFrequency(String ngram) throws CompiledCorpusException {
		long freq = 0;
		Map<String, Long> freqs = getNgramStats();
		if (freqs.containsKey(ngram)) {
			freq = freqs.get(ngram);
		}
		return freq;
	}

	@Override
	public long morphemeNgramFrequency(String[] morphNgram) throws CompiledCorpusException {
		long freq = 0;
		
		try {
			TrieNode node = trie.getNode(morphNgram);
			if (node != null) {
				freq = node.getFrequency();
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return freq;
	}
	
	public String[] bestDecomposition(String word) throws CompiledCorpusException {
		Matcher matcher = 
			Pattern.compile(","+word+":([^,]*),").matcher(wordSegmentations);
		
		String[] topSeg = new String[0];
		if (matcher.find()) {
			String topSegStr = matcher.group(1).replaceAll("(^\\{|\\}$)","");
			topSeg = topSegStr.split("\\}\\s*\\{");
		}
		
		return topSeg;
	}

	@Override
	public long totalOccurences() throws CompiledCorpusException {
		long total = totalOccurencesWithNoDecomp() + totalOccurencesWithDecomps();
		return total;
	}
	
	@Override
	public WordInfo[] mostFrequentWordsExtending(String[] morphemes, Integer N) 
			throws CompiledCorpusException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpus.mostFrequentWordsExtending");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("morphemes="+String.join(",", morphemes));
		}
		
		List<WordInfo> mostFrequentLst = new ArrayList<WordInfo>();
		TrieNode node;
		N = Math.min(N,  Integer.MAX_VALUE);
		try {
			node = trie.getNode(morphemes);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("node for morphemes is:\n"+node);
			}
			if (node != null) {
				TrieNode[] mostFreqExtensions = 
					trie.getMostFrequentTerminals(N, morphemes);
				for (TrieNode anExtension: mostFreqExtensions) {
					for (String word: anExtension.getSurfaceForms().keySet()) {
						WordInfo winfo = info4word(word);
						mostFrequentLst.add(winfo);						
					}
				}
			}
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		
		return mostFrequentLst.toArray(new WordInfo[mostFrequentLst.size()]);
	}
	
	@JsonIgnore @Override
	public Trie getMorphNgramsTrie() throws CompiledCorpusException {
		return trie;
	}
	
	@JsonIgnore
	private Map<String,WordInfo> getWord2infoMap() throws CompiledCorpusException {
		if (word2infoMap == null) {
			generateWord2infoMap();
		}
		return word2infoMap;
	}

	// TODO-June2020: Once we have updated the JSON file for the 
	//  Hansard 1999-2002 and 1999-2018 corpora, make this method
	//  be private
	public void generateWord2infoMap() throws CompiledCorpusException {
		System.out.println("Computing the word2info map. This may take a while...");
		
		word2infoMap = new HashMap<String,WordInfo>();
		
		updateWordInfoMap_WordsWithDecomps();
		updateWordInfoMap_WordsWithoutDecomps();
		
		
		long totalWords = word2infoMap.keySet().size();
		System.out.println("DONE Computing the word2info map. Map now contains "+totalWords+" words");
	}
	
	private void updateWordInfoMap_WordsWithDecomps() throws CompiledCorpusException {
		System.out.println("Adding words that have decomps to the word2info map. This may take a while...");
		
		TrieNode2WordInfoVisitor visitor = new TrieNode2WordInfoVisitor(this);
		try {
			getMorphNgramsTrie().traverseNodes(visitor);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
		System.out.println("DONE - Adding words that have decomps to the word2info map. This may take a while...");
	}
	
	private void updateWordInfoMap_WordsWithoutDecomps() throws CompiledCorpusException {
		System.out.println("Adding words that do NOT have decomps to the word2info map. This may take a while...");
		
		for (Entry<String,Long> failure: wordsFailedSegmentationWithFreqs.entrySet()) {
			String word = failure.getKey();
			Long frequency = failure.getValue();
			updateWordInfo(word, new String[0][], 0, frequency);
		}
		System.out.println("DONE - Adding words that do NOT have decomps to the word2info map. This may take a while...");
		
	}
	
	public static class TrieNode2WordInfoVisitor extends TrieNodeVisitor {

		private CompiledCorpus_InMemory corpus;
		ProgressMonitor_Terminal progressMonitor = null;

		public TrieNode2WordInfoVisitor(CompiledCorpus_InMemory _corpus) 
			throws CompiledCorpusException {
			this.corpus = _corpus;
			long totalWordsWithDecomp = corpus.totalWordsWithDecomps();
			progressMonitor = 
				new ProgressMonitor_Terminal(totalWordsWithDecomp, "");
		}
		
		@Override
		public void visitNode(TrieNode node) throws TrieException {
			if (node.isTerminal()) {
				String word = node.surfaceForm;
				String[][] nullDecomps = null;
				String[] bestDecomp = node.keys;
				String[][] sampleDecomps = new String[][] {bestDecomp};
				int totalDecomps = 1;
				long frequency = node.getFrequency();
				try {
					corpus.updateWordInfo(word, sampleDecomps, totalDecomps, frequency);
				} catch (CompiledCorpusException e) {
					throw new TrieException(e);
				}
				
				progressMonitor.stepCompleted();
			}
			
		}
	}

	@Override
	public Iterator<String> wordsWithNoDecomposition() throws CompiledCorpusException {
		return wordsFailedSegmentation.iterator();
	}
}