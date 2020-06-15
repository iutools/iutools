package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

public class CompiledCorpus_InMemory extends CompiledCorpus 
{	
	public int MAX_NGRAM_LEN = 5;
	public static String JSON_COMPILATION_FILE_NAME = "trie_compilation.json";
	
	public Trie_InMemory trie = new Trie_InMemory();
	
	// things related to the compiler's state and operation that need to be saved periodically and at termination
	private Vector<String> wordsFailedSegmentation = new Vector<String>();
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
//		ngramCompilerNoExtremities = new NgramCompiler(3,0,false);
		for (int iw=1; iw<words.length; iw++) {
			updateSequenceNgramsForWord(words[iw]);
		}
	}
	private void updateSequenceNgramsForWord(String word) {
		Set<String> seqsSeenInWord = new HashSet<String>();
		seqsSeenInWord = getNgramCompiler().compile(word);
		Iterator<String> itngram = seqsSeenInWord.iterator();
		while (itngram.hasNext()) {
			String ngram = itngram.next();
			Long freq = ngramStats.get(ngram);
			if (freq==null)
				freq = (long)0;
			ngramStats.put(ngram, ++freq);
		}
	}

	
//	public void setVerbose(boolean value) {
//		verbose = value;
//	}
//	
//	public void setName(String _name) {
//		name = _name;
//	}
	
	
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
		Iterator<String> iter = wordDecomps.keySet().iterator();
		return iter;
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
	
    @Override
	protected void addToWordCharIndex(String word, String[][] decomps) 
			throws CompiledCorpusException {
    	wordDecomps.put(word, decomps);
	}

	private void removeFromListOfFailedSegmentation(String word) {
		if (getWordsFailedSegmentation().contains(word))
			getWordsFailedSegmentation().removeElement(word);
		if (wordsFailedSegmentationWithFreqs.containsKey(word))
				wordsFailedSegmentationWithFreqs.remove(word);
	}
	
	@Override
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
    
    public long getNbWordsThatFailedSegmentations() {
    	return wordsFailedSegmentationWithFreqs.size();
    }
    
    public long getNbOccurrencesThatFailedSegmentations() {
    	Long[] nbOccurrences = wordsFailedSegmentationWithFreqs.values().toArray(new Long[] {});
    	long nb = 0;
    	for (Long nbOcc: nbOccurrences)
    		nb += nbOcc.longValue();
    	return nb;
    }
    
	@Override
	protected TrieNode[] getAllTerminals() throws CompiledCorpusException {
		try {
			return this.trie.getTerminals();
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
    
	public long getNumberOfCompiledOccurrences() throws CompiledCorpusException {
		if (this.terminalsSumFreq == null) {
			long sumFreqs = 0;
			TrieNode[] terminals;
			terminals = this.getAllTerminals();
			for (TrieNode terminal : terminals)
				sumFreqs += terminal.getFrequency();
			this.terminalsSumFreq = new Long(sumFreqs);
		}
		return this.terminalsSumFreq;
	}
	

	public void toConsole(String message) {
		if (verbose) System.out.print(message);
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

	public String[] getMostFrequentSequenceForRoot(String string) throws CompiledCorpusException {
		try {
			return this.trie.getMostFrequentSequenceForRoot(string);
		} catch (TrieException e) {
			throw new CompiledCorpusException(e);
		}
	}
	
	
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
			for (int i=0; i<getWordsFailedSegmentation().size(); i++) {
				wordsWithUnsuccessfulDecomposition += getWordsFailedSegmentation().get(i)+",,";
			}
		}
		return wordsWithUnsuccessfulDecomposition;
	}

	public String[] getWordsThatFailedDecomposition() {
		return getWordsFailedSegmentation().toArray(new String[] {});
	}
	public Vector<String> getWordsFailedSegmentation() {
		return wordsFailedSegmentation;
	}
	public void setWordsFailedSegmentation(Vector<String> wordsFailedSegmentation) {
		this.wordsFailedSegmentation = wordsFailedSegmentation;
	}
	public long getNbOccurrencesOfWord(String word) throws CompiledCorpusException {
		Logger logger = Logger.getLogger("CompiledCorpus.getNbOccurrencesOfWord");
		logger.debug("word: "+word);
		long nbOccurrences = 0;
		Pattern pattern = Pattern.compile(","+word+":"+"(.+?),");
		Matcher matcher = pattern.matcher(wordSegmentations);
		if ( matcher.find() ) {
			String segmentsStr = matcher.group(1);
			String segmentsStrWithSpaces = segmentsStr.replace("}{", "} {");
			String[] segments = segmentsStrWithSpaces.split(" ");
			TrieNode[] terminals;
			try {
				terminals = this.trie.getTerminals(segments);
				if (terminals.length > 0) {
					nbOccurrences = terminals[0].getFrequency();
				}
			} catch (TrieException e) {
				throw new CompiledCorpusException(e);
			}
		}
	
		return nbOccurrences;
	}
	
	public List<WordWithMorpheme> getWordsContainingMorpheme(String morpheme) throws CompiledCorpusException {
		List<WordWithMorpheme> words = new ArrayList<WordWithMorpheme>();
		Pattern pattern = Pattern.compile(",([^:,]+?)"+":([^:]*?\\{("+morpheme+"/.+?)\\}.*?),");
		Matcher matcher = pattern.matcher(wordSegmentations);
		while ( matcher.find() ) {
			String word = matcher.group(1);
			String morphId = matcher.group(3);
			String decomposition = matcher.group(2);
			long freq = this.getNbOccurrencesOfWord(word);
			words.add(new WordWithMorpheme(word,morphId,decomposition,freq));
		}
		
		return words;
	}
	
	public static class WordWithMorpheme {
		public String word;
		public String morphemeId;
		public String decomposition;
		public Long frequency;
		
		public WordWithMorpheme(String _word, String _morphId, String _decomp, Long _freq) {
			this.word = _word;
			this.morphemeId = _morphId;
			this.decomposition = _decomp;
			this.frequency = _freq;
		}
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
	
	public void addWord(String word) throws CompiledCorpusException {
		addWord(word, null);
	}
	
	public void addWord(String word, String[] decomps) throws CompiledCorpusException {
		if (word2infoMap.containsKey(word)) {
			throw new CompiledCorpusException("Attempted to add a word for "+
						"there was already an entry ("+word+").");
		}
		
		key2word.put(nextWordKey, word);
		word2key.put(word, nextWordKey);
		WordInfo info = new WordInfo(this.nextWordKey);
		if (decomps != null) {
			info.setDecompositions(decomps);
		}
	
		this.nextWordKey++;
		word2infoMap.put(word, info);
		
		updateNGramIndex(word);
	}
	
	@Override
	protected void addToWordSegmentations(
			String word, String[][] decomps) 
			throws CompiledCorpusException {
		
		if (decomps != null && decomps.length > 0) {
			String[] bestDecomp = decomps[0];

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
		}
	}	
	
	@Override
	protected void addToWordNGrams(
		String word, String[][] decomps) throws CompiledCorpusException {
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
		if (word2infoMap.containsKey(word)) {
			wInfo = word2infoMap.get(word);
		} else if (getSegmentsCache().containsKey(word)){
			Long wordKey = key4word(word);
			wInfo = new WordInfo(wordKey);
			String[] decomps = getSegmentsCache().get(word);
			wInfo.topDecompositions = decomps;
			wInfo.totalDecompositions = decomps.length;
			wInfo.frequency = computeWordFreq(word);
			word2infoMap.put(word, wInfo);
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
	public void migrateWordInfoToNewDataStructure() throws CompiledCorpusException {
		System.out.println(
			"Migrate corpus to new data structure.\n"+
			"This may take a few minutes...\n");
				
		String mess = "Migrating words for which morphological decomposition were found";
		int numSteps = getSegmentsCache().keySet().size();
		ProgressMonitor_Terminal monitor = 
				new ProgressMonitor_Terminal(numSteps, mess);
		int counter = 0;
		Set<String> words = getSegmentsCache().keySet();
		for (String aWord: words) {
			counter++;
			System.out.println("** migrateWordInfoOutOfPhonemeTrie: SEGMENTED word="+aWord+" ("+counter+" of "+numSteps+
					": "+String.format("%.0f%%",100.0*counter/numSteps)+")"
				);
			long usedMem = Runtime.getRuntime().totalMemory();
			long maxMem = Runtime.getRuntime().maxMemory();
			long remainingMem = maxMem - usedMem;
			double remainingMemPerc = 100.0 * remainingMem / maxMem;
			System.out.println("  Remaining mem: "+remainingMem+" ("+String.format("%.0f%%",remainingMemPerc)+")");
			String[] decomps = getSegmentsCache().get(aWord);
			addWord(aWord, decomps);
			// To save memory during upgrade, set the entry for that word
			// to null;
			getSegmentsCache().put(aWord, null);
			monitor.stepCompleted();
		}
		setSegmentsCache(null);

		mess = "Migrating words for which NO morphological decomposition were found";
		numSteps = wordsFailedSegmentation.size();
		monitor = new ProgressMonitor_Terminal(numSteps, mess);
		counter = 0;
		for (String word: wordsFailedSegmentation) {
			counter++;
			System.out.println("** migrateWordInfoOutOfPhonemeTrie: NON-Segmented word="+word+" ("+counter+" of "+numSteps+")");			
			String[] decomps = new String[0];
			addWord(word, decomps);
			monitor.stepCompleted();
		}
		wordsFailedSegmentation = null;
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
		// TODO Auto-generated method stub
		return null;
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
	
	public String[] topSegmentation(String word) throws CompiledCorpusException {
		Matcher matcher = 
			Pattern.compile(","+word+":([^,]*),").matcher(wordSegmentations);
		
		String[] topSeg = new String[0];
		if (matcher.find()) {
			String topSegStr = matcher.group(1).replaceAll("(^\\{|\\}$)","");
			topSeg = topSegStr.split("\\}\\s*\\{");
		}
		
		return topSeg;
	}
	
	public long totalOccurences() throws CompiledCorpusException {
		return getNumberOfCompiledOccurrences();
	}
}

