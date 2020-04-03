package ca.pirurvik.iutools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.script.TransCoder;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;


/**
 * This creates a Trie of the (Inuktitut) words in the Nunavut Hansard
 * 
 * segmentsCache: for each compiled word, contains the segments 
 *                (may be empty list if word did not get segmented)
 *                
 * 
 *
 */ 
public class CompiledCorpus 
{
	
	public int MAX_NGRAM_LEN = 5;
	public static String JSON_COMPILATION_FILE_NAME = "trie_compilation.json";
	
	protected Trie trie = new Trie();
	
	// things related to the compiler's state and operation that need to be saved periodically and at termination
	private HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	private Vector<String> wordsFailedSegmentation = new Vector<String>();
	protected HashMap<String,Long> wordsFailedSegmentationWithFreqs = new HashMap<String,Long>();
	
	public String wordSegmentations = ",,";
	public String decomposedWordsSuite = ",,";

	protected Vector<String> filesCompiled = new Vector<String>();	
	protected String saveFilePath = null;	
	protected String segmenterClassName = StringSegmenter_Char.class.getName();	
	protected long currentFileWordCounter = -1;
	protected long retrievedFileWordCounter = -1;	
	public int saveFrequency = 1000;	
	protected Long terminalsSumFreq = null;
	protected String completeCompilationResultsFilePathname = null; // file for completed compilation of corpus
	
	public Map<String,Long> ngramStats = null;

	private Map<String,WordInfo> word2infoMap = new HashMap<String,WordInfo>();
		
	private Map<Long,String> key2word = new HashMap<Long,String>();
	private Map<String,Set<Long>> ngram2wordKeysMap = 
				new HashMap<String,Set<Long>> ();

	// things that do not need to be saved 
	@JsonIgnore
	private transient long wordCounter = 0;
	@JsonIgnore
	private transient String corpusDirectory;
	@JsonIgnore
	private transient StringSegmenter segmenter = null;
	@JsonIgnore
	public transient int stopAfter = -1;
	@JsonIgnore
	public transient boolean verbose = true;
	@JsonIgnore
	public transient String name;
	@JsonIgnore
	public transient NgramCompiler ngramCompiler;
	@JsonIgnore
	public transient CorpusDocument_File fileBeingProcessed = new CorpusDocument_File("");
	
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
		ngramCompiler = new NgramCompiler(3,0,false);
		for (int iw=1; iw<words.length; iw++) {
			updateSequenceNgramsForWord(words[iw]);
		}
	}
	private void updateSequenceNgramsForWord(String word) {
		Set<String> seqsSeenInWord = new HashSet<String>();
		seqsSeenInWord = ngramCompiler.compile(word);
		Iterator<String> itngram = seqsSeenInWord.iterator();
		while (itngram.hasNext()) {
			String ngram = itngram.next();
			Long freq = ngramStats.get(ngram);
			if (freq==null)
				freq = (long)0;
			ngramStats.put(ngram, ++freq);
		}
	}

	
	public void setVerbose(boolean value) {
		verbose = value;
	}
	
	public void setName(String _name) {
		name = _name;
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public StringSegmenter getSegmenter() throws CompiledCorpusException {
		if (segmenter == null) {
			//Class<StringSegmenter> cls = (Class<StringSegmenter>) Class.forName(segmenterClassName);
			Class cls;
			try {
				cls = Class.forName(segmenterClassName);
			} catch (ClassNotFoundException e) {
				throw new CompiledCorpusException(e);
			}
//			Constructor<?> constr;
//			try {
//				constr = cls.getConstructor();
//			} catch (NoSuchMethodException | SecurityException e) {
//				throw new CompiledCorpusException(e);
//			}
			try {
//				segmenter = (StringSegmenter) constr.newInstance();
				segmenter = (StringSegmenter) cls.newInstance();
			} catch (Exception e) {
				throw new CompiledCorpusException(e);
			}
		}
		return segmenter;
	}
			
	
	
	@SuppressWarnings("serial")
	public static class CorpusTrieCompilerException extends Exception {
		public CorpusTrieCompilerException(String mess) {
			super(mess);
		}
	}
	
	// ------- Constructors ----------------------------------------------------
	
	public CompiledCorpus() {
		initialize(StringSegmenter_Char.class.getName()); 
	}
	
	public CompiledCorpus(String segmenterClassName) {
		initialize(segmenterClassName);
	}
	//--------------------------------------------------------------------------
	

	public void initialize(String _segmenterClassName) {
		if (_segmenterClassName != null) this.segmenterClassName = _segmenterClassName;
	}
	
	public  void compileCorpus(String corpusDirectoryPathname) throws CompiledCorpusException, StringSegmenterException {
		_compileCorpus(corpusDirectoryPathname,false);
	}
	
	public  void compileCorpusFromScratch(String corpusDirectoryPathname) throws CompiledCorpusException, StringSegmenterException {
		_compileCorpus(corpusDirectoryPathname,true);
	}
	
	public  void _compileCorpus(String corpusDirectoryPathname, boolean fromScratch) throws CompiledCorpusException, StringSegmenterException {
		toConsole("[INFO] *** Compiling trie for documents in "+corpusDirectoryPathname+"\n");
		segmenter = getSegmenter(); //new StringSegmenter_IUMorpheme();
		
		if ( !fromScratch ) {
			if (this.canBeResumed(corpusDirectoryPathname)) {
				this.__resumeCompilation(corpusDirectoryPathname);
			} else {
				// no json compilation in the corpus directory: will do as if from scratch
			}
		} else {
			this.deleteJSON(corpusDirectoryPathname);
			trie = new Trie();
		}
		
		wordCounter = 0;
			
		process(corpusDirectoryPathname);
		
		compileExtras();
		
		toConsole("[INFO] *** Compilation completed."+"\n");
		saveCompilerInDirectory(corpusDirectoryPathname);
	}
	
	public String getWordSegmentations() {
		//if (wordSegmentations==null) {
		//	__compileWordSegmentations();
		//	saveCompilerInDirectory(corpusDirectoryPathname);
		//	if (completeCompilationResultsFilePathname != null)
		//		saveCompilerInJSONFile(completeCompilationResultsFilePathname);
		//}
		return wordSegmentations;
	}
	
	public String getDecomposedWordsSuite() {
		return decomposedWordsSuite;
	}
	
	/*private void __compileWordSegmentations() {
		Collection<String> segmentationsKeys = segmentsCache.keySet();
		int nbDecompositions = segmentationsKeys.size();
		int i = 1;
		Iterator<String> it = segmentationsKeys.iterator();
		while (it.hasNext()) {
			String word = it.next();
			String[] segments = segmentsCache.get(word);
			System.out.println("<<>> "+(i++)+"/"+nbDecompositions+" : "+word+" ("+segments.length+")");
			System.out.flush();
			if ( segments.length != 0 ) 
				addToWordSegmentations(word,segments);
			//if (i>10) break;
		}
	}*/
	
	
	/**
	 * Recompile only the words that failed morphological analysis in a previous run
	 * @param corpusDirectoryPathname
	 * @throws StringSegmenterException 
	 * @throws CompiledCorpusException 
	 * @throws LinguisticDataException 
	 * @throws IOException 
	 */
	public void recompileWordsThatFailedAnalysis(String corpusDirectoryPathname) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
		toConsole("[INFO] *** Recompiling into trie the words that failed analysis previously"+"\n");
		segmenter = new StringSegmenter_IUMorpheme();
		wordCounter = 0;
		Object[][] objs = new Object[wordsFailedSegmentationWithFreqs.size()][2];
		Iterator<String> iterator = wordsFailedSegmentationWithFreqs.keySet().iterator();
		int i = 0;
	    while(iterator.hasNext()) {
	    	String word = iterator.next();
	        objs[i++] = new Object[] {word,wordsFailedSegmentationWithFreqs.get(word)};
	    }
	    Arrays.sort(objs, (Object[] a, Object[] b) -> {
	    	return ((Long)b[1]).compareTo((Long)a[1]);
	    });
		for (Object[] wordFreq : objs) {
			String word = (String)wordFreq[0];
			++wordCounter;
			processWord(word,true); // true: overrun lookup of segments in cache
			if (wordCounter % saveFrequency == 0) {
				toConsole("[INFO]     --- saving jsoned compiler ---"+"\n");
				saveCompilerInDirectory(corpusDirectoryPathname);
			}
		}
		saveCompilerInDirectory(corpusDirectoryPathname);
	}
	
	public Trie getTrie() {
		return this.trie;
	}
	
	public HashMap<String,String[]> getSegments() {
		return this.getSegmentsCache();
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
	
	private void deleteJSON(String corpusDirectoryPathname) {
		File saveFile = new File(corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}

	public void saveCompilerInDirectory(String corpusDirectoryPathname) throws CompiledCorpusException  {
		String saveFilePathname = corpusDirectoryPathname + "/" + JSON_COMPILATION_FILE_NAME;
		saveCompilerInJSONFile(saveFilePathname);
	}
	
	public void saveCompilerInJSONFile (String saveFilePathname) throws CompiledCorpusException {
		FileWriter saveFile = null;
		try {
			saveFile = new FileWriter(saveFilePathname);
		} catch (IOException e1) {
			throw new CompiledCorpusException(e1);
		}
		Gson gson = new Gson();
		long savedRetrievedFileWordCounter = this.retrievedFileWordCounter;
		this.retrievedFileWordCounter = this.currentFileWordCounter;
		try {
			gson.toJson(this, saveFile);
			saveFile.flush();
			saveFile.close();
			this.retrievedFileWordCounter = savedRetrievedFileWordCounter;
		} catch (JsonIOException | IOException e) {
			throw new CompiledCorpusException(e);
		}
		toConsole("saved in "+saveFilePathname);
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 * @throws CompiledCorpusException 
	 */
	protected void __resumeCompilation(String corpusDirectoryPathname) throws CompiledCorpusException {
		String jsonFilePath = corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME;
		CompiledCorpus compiledCorpus = createFromJson(jsonFilePath);
		
		this.trie = compiledCorpus.trie;
		this.setSegmentsCache(compiledCorpus.getSegmentsCache());
		this.saveFilePath = compiledCorpus.saveFilePath;
		this.segmenterClassName = compiledCorpus.segmenterClassName;
		this.currentFileWordCounter = compiledCorpus.currentFileWordCounter;
		this.retrievedFileWordCounter = compiledCorpus.retrievedFileWordCounter;
		this.saveFrequency = compiledCorpus.saveFrequency;
		this.filesCompiled = compiledCorpus.filesCompiled;
		
		this.setWordsFailedSegmentation(compiledCorpus.getWordsFailedSegmentation());
		this.wordsFailedSegmentationWithFreqs = compiledCorpus.wordsFailedSegmentationWithFreqs;
		this.wordSegmentations = compiledCorpus.wordSegmentations;
		this.decomposedWordsSuite = compiledCorpus.decomposedWordsSuite;
		this.terminalsSumFreq = compiledCorpus.terminalsSumFreq;
		this.ngramStats = null;
}
 

	private void process(String corpusDirectoryPathname) throws CompiledCorpusException, StringSegmenterException {
		toConsole("[INFO] --- compiling directory "+corpusDirectoryPathname+"\n");
		this.corpusDirectory = corpusDirectoryPathname;
		processDirectory(corpusDirectoryPathname);
	}
	
	private void processDirectory(String directoryPathname) throws CompiledCorpusException, StringSegmenterException {
		Logger logger = Logger.getLogger("CompiledCorpus.processDirectory");
    	CorpusReader_Directory corpusReader = new CorpusReader_Directory();
    	Iterator<CorpusDocument_File> files = (Iterator<CorpusDocument_File>) corpusReader.getFiles(directoryPathname);
    	while (files.hasNext()) {
    		CorpusDocument_File corpusDocumentFile = files.next();
    		File file = new File(corpusDocumentFile.id);
    		logger.debug("file: "+file.getAbsolutePath());
    		if (file.isDirectory()) {
    			processDirectory(corpusDocumentFile.id);
    		} else {
    			processFile(corpusDocumentFile);
    		}
    	}
	}

	private void processFile(CorpusDocument_File file) throws CompiledCorpusException, StringSegmenterException {
			String fileAbsolutePath = file.id;
			fileBeingProcessed = file;
			toConsole("[INFO] --- compiling document "+new File(fileAbsolutePath).getName()+"\n");
			processDocumentContents();
			if ( !this.filesCompiled.contains(fileAbsolutePath) )
				filesCompiled.add(fileAbsolutePath);
	}
	
	
	public void processDocumentContents()
			throws CompiledCorpusException, StringSegmenterException {
		Logger logger = Logger.getLogger("CorpusCompiler.processDocumentContents");
		currentFileWordCounter = 0;
		String contents;
		try {
			contents = fileBeingProcessed.getContents();
			IUTokenizer iuTokenizer = new IUTokenizer();
			List<String> words = iuTokenizer.tokenize(contents);
			processWords(words.toArray(new String[] {}));
		} catch (CompiledCorpusException e) {
			throw e;
		} catch (StringSegmenterException e) {
			throw e;
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}


	protected void processDocumentContents(String fileAbsolutePath) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
		} catch (FileNotFoundException e) {
			throw new CompiledCorpusException(e);
		}
		processDocumentContents(bufferedReader,fileAbsolutePath);
	}
	
	
	
	public void processDocumentContents(BufferedReader bufferedReader, String fileAbsolutePath)
			throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
		Logger logger = Logger.getLogger("CompiledCorpus.processDocumentContents");
		String line;
		currentFileWordCounter = 0;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				logger.debug("line: '"+line+"'");
				String[] words = extractWordsFromLine(line);
				processWords(words);
			}
			bufferedReader.close();
		} catch (IOException e) {
			try {
				bufferedReader.close();
			} catch (IOException e1) {
				throw new CompiledCorpusException(e);
			}
			throw new CompiledCorpusException(e);
		}
	}
    

	
	
	private void processWords(String[] words) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
    	Logger logger = Logger.getLogger("CompiledCorpus.processWords");
		logger.debug("words: "+words.length);
		for (int n = 0; n < words.length; n++) {
			String word = words[n];
			String wordInRomanAlphabet = TransCoder.unicodeToRoman(word);
			logger.debug("wordInRomanAlphabet: " + wordInRomanAlphabet);
			if (!isInuktitutWord(wordInRomanAlphabet))
				continue;
			++wordCounter;

			if (!filesCompiled.contains(fileBeingProcessed.id)) {
				++currentFileWordCounter;
				if (retrievedFileWordCounter != -1) {
					if (currentFileWordCounter < retrievedFileWordCounter) {
						continue;
					} else {
						retrievedFileWordCounter = -1;
					}
				}

				processWord(wordInRomanAlphabet);

				// this line allows to make the compiler stop at a given point (for tests
				// purposes only)
				if (stopAfter != -1 && wordCounter == stopAfter) {
					throw new CompiledCorpusException(
							"processDocumentContents:: Simulating an error during trie compilation of corpus.");
				}
				if (wordCounter % saveFrequency == 0) {
					toConsole("[INFO]     --- saving jsoned compiler ---" + "\n");
					logger.debug("size of trie: " + trie.getSize());
					saveCompilerInDirectory(this.corpusDirectory);
				}
			}
		}
	}

	
	
	private void processWord(String word) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
    	processWord(word,false);
    }
    private void processWord(String word, boolean recompilingFailedWord) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException {
    	Logger logger = Logger.getLogger("CompiledCorpus.processWord");
		toConsole("[INFO]     "+wordCounter + "(" + currentFileWordCounter + "+). " + word + "... ");
		String[] segments = fetchSegmentsFromCache(word);
		// either null if word has not yet been met
		//     or String[0] if word could not be decomposed by analyzer
		if (segments==null || segments.length==0 ) {
			String now = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
			toConsole("[segmenting] ("+now+") ");
			try {
				StringSegmenter segmenter = getSegmenter();
				segments = segmenter.segment(word);
				// new word decomposed or word that now decomposed: add to word segmentations string
				if (segments.length != 0) {
					addToWordSegmentations(word,segments);
					addToDecomposedWordsSuite(word);
				}
			} catch (TimeoutException e) {
				toConsole("** EXCEPTION RAISED");
				toConsole(" ??? " + e.getClass().getName() + " --- " + e.getMessage() + " ");
				segments = new String[] {};
			}
			addToCache(word, segments);
		}
		try {
			TrieNode result = null;
			if (segments.length != 0) {
				result = trie.add(segments,word);
				if (recompilingFailedWord) {
					int freq = wordsFailedSegmentationWithFreqs.get(word).intValue()-1;
					for (int ifr=0; ifr<freq; ifr++)
						trie.add(segments, word);
				}
				toConsole(result.getKeysAsString()+"\n");
				removeFromListOfFailedSegmentation(word);
			} else {
				toConsole("XXX\n");
				if ( !recompilingFailedWord )
					addToListOfFailedSegmentation(word);
			}

		} catch (TrieException e) {
			toConsole("--** Problem adding word: " + word + " (" + e.getMessage() + ").");
			throw new CompiledCorpusException(e);
		}
	}

	private void removeFromListOfFailedSegmentation(String word) {
		if (getWordsFailedSegmentation().contains(word))
			getWordsFailedSegmentation().removeElement(word);
		if (wordsFailedSegmentationWithFreqs.containsKey(word))
				wordsFailedSegmentationWithFreqs.remove(word);
	}
	
	private void addToDecomposedWordsSuite(String word) {
		decomposedWordsSuite += word+",,";
	}

	private void addToWordSegmentations(String word,String[] segments) {
		wordSegmentations += word+":"+String.join("", segments)+",,";
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
    
    public static CompiledCorpus createFromJson(String jsonCompilationFilePathname) throws CompiledCorpusException {
    	try {
    		FileReader jsonFileReader = new FileReader(jsonCompilationFilePathname);
    		Gson gson = new Gson();
    		CompiledCorpus compiledCorpus = gson.fromJson(jsonFileReader, CompiledCorpus.class);
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
    
	public long getNumberOfCompiledOccurrences() {
		if (this.terminalsSumFreq == null) {
			long sumFreqs = 0;
			TrieNode[] terminals = this.trie.getAllTerminals();
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

	private void addToCache(String word, String[] segments) {
		getSegmentsCache().put(word, segments);
	}

	private String[] fetchSegmentsFromCache(String word) {
		String[] segmentsFromCache = null;
		if (!getSegmentsCache().containsKey(word))
			segmentsFromCache = new String[] {};
		else
			segmentsFromCache = getSegmentsCache().get(word);
		return segmentsFromCache;
	}

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
		Logger logger = Logger.getLogger("CompiledCorpus.extractWordsFromLine");
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

	public TrieNode getMostFrequentTerminal(String[] segments) {
		return this.trie.getMostFrequentTerminal(segments);
	}

	public String[] getMostFrequentSequenceForRoot(String string) {
		return this.trie.getMostFrequentSequenceForRoot(string);
	}
	
	
	public Boolean isWordInCorpus(String word) {
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
	public HashMap<String,String[]> getSegmentsCache() {
		return segmentsCache;
	}
	public void setSegmentsCache(HashMap<String,String[]> segmentsCache) {
		this.segmentsCache = segmentsCache;
	}
	public long getNbOccurrencesOfWord(String word) {
		Logger logger = Logger.getLogger("CompiledCorpus.getNbOccurrencesOfWord");
		logger.debug("word: "+word);
		long nbOccurrences = 0;
		Pattern pattern = Pattern.compile(","+word+":"+"(.+?),");
		Matcher matcher = pattern.matcher(wordSegmentations);
		if ( matcher.find() ) {
			String segmentsStr = matcher.group(1);
			String segmentsStrWithSpaces = segmentsStr.replace("}{", "} {");
			String[] segments = segmentsStrWithSpaces.split(" ");
			TrieNode[] terminals = this.trie.getAllTerminals(segments);
			nbOccurrences = terminals[0].getFrequency();
			}
	
		return nbOccurrences;
	}
	
	public List<WordWithMorpheme> getWordsContainingMorpheme(String morpheme) {
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
		if (word2infoMap.containsKey(word)) {
			key = word2infoMap.get(word).key;
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
		WordInfo info = new WordInfo(this.nextWordKey);
		if (decomps != null) {
			info.setDecompositions(decomps);
		}
	
		this.nextWordKey++;
		word2infoMap.put(word, info);
		
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
	
	public Set<String> wordsContainingNgram(String ngram) {
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
		
	public WordInfo info4word(String word) {
		WordInfo wInfo = null;
		if (word2infoMap.containsKey(word)) {
			wInfo = word2infoMap.get(word);
		}
		
		return wInfo;
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
		int numSteps = segmentsCache.keySet().size();
		ProgressMonitor_Terminal monitor = 
				new ProgressMonitor_Terminal(numSteps, mess);
		int counter = 0;
		Set<String> words = segmentsCache.keySet();
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
			String[] decomps = segmentsCache.get(aWord);
			addWord(aWord, decomps);
			// To save memory during upgrade, set the entry for that word
			// to null;
			segmentsCache.put(aWord, null);
			monitor.stepCompleted();
		}
		segmentsCache = null;

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
}

