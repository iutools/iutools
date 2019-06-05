package ca.pirurvik.iutools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;


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
	
	public static String JSON_COMPILATION_FILE_NAME = "trie_compilation.json";
	
	protected Trie trie = new Trie();
	
	// things related to the compiler's state and operation that need to be saved periodically and at termination
	protected HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	protected Vector<String> wordsFailedSegmentation = new Vector<String>();
	protected HashMap<String,Long> wordsFailedSegmentationWithFreqs = new HashMap<String,Long>();
	
	public String wordSegmentations = ",,";

	protected Vector<String> filesCompiled = new Vector<String>();	
	protected String saveFilePath = null;	
	protected String segmenterClassName = StringSegmenter_Char.class.getName();	
	protected long currentFileWordCounter = -1;
	protected long retrievedFileWordCounter = -1;	
	public int saveFrequency = 1000;	
	protected Long terminalsSumFreq = null;
	protected String completeCompilationResultsFilePathname = null; // file for completed compilation of corpus

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
	
	
	public void setVerbose(boolean value) {
		verbose = value;
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public StringSegmenter getSegmenter() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (segmenter == null) {
			//Class<StringSegmenter> cls = (Class<StringSegmenter>) Class.forName(segmenterClassName);
			Class<?> cls = (Class<?>) Class.forName(segmenterClassName);
			Constructor<?> constr = cls.getConstructor();
			segmenter = (StringSegmenter) constr.newInstance();
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
	
	public  void compileCorpus(String corpusDirectoryPathname) throws Exception {
		_compileCorpus(corpusDirectoryPathname,false);
	}
	
	public  void compileCorpusFromScratch(String corpusDirectoryPathname) throws Exception {
		_compileCorpus(corpusDirectoryPathname,true);
	}
	
	public  void _compileCorpus(String corpusDirectoryPathname, boolean fromScratch) throws Exception {
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
		
		toConsole("[INFO] *** Compilation completed."+"\n");
		saveCompilerInDirectory(corpusDirectoryPathname);
		if (completeCompilationResultsFilePathname != null)
			saveCompilerInJSONFile(completeCompilationResultsFilePathname);
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
	 * @throws IOException 
	 */
	public void recompileWordsThatFailedAnalysis(String corpusDirectoryPathname) throws IOException {
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
		if (completeCompilationResultsFilePathname != null)
			saveCompilerInJSONFile(completeCompilationResultsFilePathname);
	}
	
	public boolean setCompleteCompilationFilePath(String _trieFilePath) {
		File f = new File(_trieFilePath);
		File dirF = f.getParentFile();
		if ( dirF != null && !dirF.isDirectory() ) {
			completeCompilationResultsFilePathname = null;
			return false;
		}
		completeCompilationResultsFilePathname = _trieFilePath;
		return true;
	}
	
	public Trie getTrie() {
		return this.trie;
	}
	
	public HashMap<String,String[]> getSegments() {
		return this.segmentsCache;
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
	
	private void deleteJSON(String corpusDirectoryPathname) throws IOException {
		File saveFile = new File(corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}

	private void saveCompilerInDirectory(String corpusDirectoryPathname) throws IOException {
		String saveFilePathname = corpusDirectoryPathname + "/" + JSON_COMPILATION_FILE_NAME;
		saveCompilerInJSONFile(saveFilePathname);
	}
	
	public void saveCompilerInJSONFile (String saveFilePathname) throws IOException {
		FileWriter saveFile = new FileWriter(saveFilePathname);
		Gson gson = new Gson();
		long savedRetrievedFileWordCounter = this.retrievedFileWordCounter;
		this.retrievedFileWordCounter = this.currentFileWordCounter;
		String json = gson.toJson(this);
		this.retrievedFileWordCounter = savedRetrievedFileWordCounter;
		saveFile.write(json);
		saveFile.flush();
		saveFile.close();
		toConsole("saved in "+saveFilePathname);
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 */
	protected void __resumeCompilation(String corpusDirectoryPathname) throws Exception  {
		Gson gson = new Gson();
		String jsonFilePath = corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME;
		CompiledCorpus compiledCorpus = createFromJson(jsonFilePath);
//		File jsonFile = new File(jsonFilePath);
//		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
//		CompiledCorpus compiledCorpus = gson.fromJson(br, CompiledCorpus.class);
		this.trie = compiledCorpus.trie;
		this.segmentsCache = compiledCorpus.segmentsCache;
		this.saveFilePath = compiledCorpus.saveFilePath;
		this.segmenterClassName = compiledCorpus.segmenterClassName;
		this.currentFileWordCounter = compiledCorpus.currentFileWordCounter;
		this.retrievedFileWordCounter = compiledCorpus.retrievedFileWordCounter;
		this.saveFrequency = compiledCorpus.saveFrequency;
		this.filesCompiled = compiledCorpus.filesCompiled;
	}
 

	private void process(String corpusDirectoryPathname) throws Exception {
		this.corpusDirectory = corpusDirectoryPathname;
    	CorpusReader_Directory corpusReader = new CorpusReader_Directory();
    	Iterator<CorpusDocument_File> files = (Iterator<CorpusDocument_File>) corpusReader.getFiles(corpusDirectoryPathname);
    	while (files.hasNext())
    		processFile(files.next());
    	
	}

	private void processFile(CorpusDocument_File file) throws Exception {
		try {
			String fileAbsolutePath = file.id;
			toConsole("[INFO] --- compiling document "+new File(fileAbsolutePath).getName()+"\n");
			processDocumentContents(fileAbsolutePath);
			if ( !this.filesCompiled.contains(fileAbsolutePath) )
				filesCompiled.add(fileAbsolutePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void processDocumentContents(String fileAbsolutePath) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
		processDocumentContents(bufferedReader,fileAbsolutePath);
	}
	
    public void processDocumentContents(BufferedReader bufferedReader, String fileAbsolutePath) throws Exception {	
    	Logger logger = Logger.getLogger("CorpusTrieCompiler.processDocumentContents");
		String line;
		boolean stopBecauseOfStopAfter = false;
		currentFileWordCounter = 0;
		while ((line = bufferedReader.readLine()) != null && !stopBecauseOfStopAfter) {
			String[] words = extractWordsFromLine(line);
			for (int n = 0; n < words.length; n++) {
				String word = words[n];
				if (!isInuktitutWord(word))
					continue;
				++wordCounter;
				logger.debug("word: "+word);
				logger.debug("retrievedFileWordCounter: "+retrievedFileWordCounter);
				logger.debug("currentFileWordCounter: "+currentFileWordCounter);
				logger.debug("fileCompiled: "+filesCompiled.contains(fileAbsolutePath));
				
				if ( !filesCompiled.contains(fileAbsolutePath) ) {
					++currentFileWordCounter;
					if (retrievedFileWordCounter != -1) {
						if (currentFileWordCounter < retrievedFileWordCounter) {
							continue;
						} else {
							retrievedFileWordCounter = -1;
						}
					}
					
					processWord(word);
					
					// this line allows to make the compiler stop at a given point (for tests purposes only)
					if (stopAfter != -1 && wordCounter == stopAfter) {
						bufferedReader.close();
						throw new Exception("Simulating an error during trie compilation of corpus.");
					}
					if (wordCounter % saveFrequency == 0) {
						toConsole("[INFO]     --- saving jsoned compiler ---"+"\n");
						logger.debug("size of trie: "+trie.getSize());
						saveCompilerInDirectory(this.corpusDirectory);
					}
				}
			}
		}		
		bufferedReader.close();
	}
    
    private void processWord(String word) {
    	processWord(word,false);
    }
    private void processWord(String word, boolean recompilingFailedWord) {
    	Logger logger = Logger.getLogger("CompiledCorpus.processWord");
		toConsole("[INFO]     "+wordCounter + "(" + currentFileWordCounter + "+). " + word + "... ");
		String[] segments = fetchSegmentsFromCache(word);
		// either null if word has not yet been met
		//     or String[0] if word could not be decomposed by analyzer
		if (segments==null || segments.length==0 ) {
			String now = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
			toConsole("[segmenting] ("+now+") ");
			try {
					segments = getSegmenter().segment(word);
					// new word decomposed or word that now decomposed: add to word segmentations string
					if (segments.length != 0)
						addToWordSegmentations(word,segments);
			} catch (Exception e) {
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
		}
	}

	private void removeFromListOfFailedSegmentation(String word) {
		if (wordsFailedSegmentation.contains(word))
			wordsFailedSegmentation.removeElement(word);
		if (wordsFailedSegmentationWithFreqs.containsKey(word))
				wordsFailedSegmentationWithFreqs.remove(word);
	}
	
	private void addToWordSegmentations(String word,String[] segments) {
		wordSegmentations += word+":"+String.join("", segments)+",,";
	}

	private void addToListOfFailedSegmentation(String word) {
		if ( !wordsFailedSegmentation.contains(word) )
			wordsFailedSegmentation.add(word);

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
    
    // ----------------------------- static -------------------------------
    
    public static CompiledCorpus createFromJson(String jsonCompilationFilePathname) throws Exception {
    	try {
    		FileReader jsonFileReader = new FileReader(jsonCompilationFilePathname);
    		Gson gson = new Gson();
    		CompiledCorpus compiledCorpus = gson.fromJson(jsonFileReader, CompiledCorpus.class);
    		jsonFileReader.close();
    		return compiledCorpus;
    	} catch (FileNotFoundException e) {
    		throw new Exception("File "+jsonCompilationFilePathname+"does not exist. Could not create a compiled corpus.");
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
		segmentsCache.put(word, segments);
	}

	private String[] fetchSegmentsFromCache(String word) {
		String[] segmentsFromCache = null;
		if (!segmentsCache.containsKey(word))
			segmentsFromCache = new String[] {};
		else
			segmentsFromCache = segmentsCache.get(word);
		return segmentsFromCache;
	}

	private static boolean isInuktitutWord(String string) {
		Pattern p = Pattern.compile("[agHijklmnpqrstuv]+");
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
		line = line.replace('.', ' ');
		line = line.replace(',', ' ');
		String[] words = line.split("\\s+");
		if (words.length!=0) {
			if (words[0].equals("")) {
				int n=words.length-1;
				String[] newWords=new String[n];
				System.arraycopy(words,1,newWords,0,n);
				words = newWords;
			}
		}
		return words;
	}

	public TrieNode getMostFrequentTerminal(String[] segments) {
		return this.trie.getMostFrequentTerminal(segments);
	}

	public String[] getMostFrequentSequenceForRoot(String string) {
		return this.trie.getMostFrequentSequenceForRoot(string);
	}



}

