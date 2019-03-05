package ca.inuktitutcomputing.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
	protected HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	protected Vector<String> filesCompiled = new Vector<String>();
	protected Vector<String> wordsFailedSegmentation = new Vector<String>();
	protected HashMap<String,Long> wordsFailedSegmentationWithFreqs = new HashMap<String,Long>();
	
	protected String saveFilePath = null;
	
	protected transient String trieFilePath = null;
	
	private String segmenterClassName = StringSegmenter_Char.class.getName();
	
	@JsonIgnore
	private transient long wordCounter = 0;
	@JsonIgnore
	private transient String corpusDirNeededForSavingPurposes;
	@JsonIgnore
	private transient StringSegmenter segmenter = null;
	
		@SuppressWarnings("unchecked")
		@JsonIgnore
		public StringSegmenter getSegmenter() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (segmenter == null) {
				Class<StringSegmenter> cls = (Class<StringSegmenter>) Class.forName(segmenterClassName);
				segmenter = (StringSegmenter) cls.getConstructor().newInstance();
			}
			return segmenter;
		}
			
	protected long currentFileWordCounter = -1;
	protected long retrievedFileWordCounter = -1;
	
	public int saveFrequency = 1000;
	public transient int stopAfter = -1;

	
	protected Long terminalsSumFreq = null;
	
	
	@SuppressWarnings("serial")
	public static class CorpusTrieCompilerException extends Exception {
		public CorpusTrieCompilerException(String mess) {
			super(mess);
		}
	}
	
	// Constructors ------------------------------------------------------------
	
	public CompiledCorpus() {
		initialize(null);
	}
	
	public CompiledCorpus(String segmenterClassName) {
		initialize(segmenterClassName);
	}
	//--------------------------------------------------------------------------
	

	public void initialize(String _segmenterClassName) {
		this.segmenterClassName = _segmenterClassName;
	}
	
	public  void compileCorpus(String corpusDirectoryPathname) throws Exception {
		_compileCorpus(corpusDirectoryPathname,false);
	}
	public  void compileCorpusFromScratch(String corpusDirectoryPathname) throws Exception {
		_compileCorpus(corpusDirectoryPathname,true);
	}
	public  void _compileCorpus(String corpusDirectoryPathname, boolean fromScratch) throws Exception {
		toConsole("[INFO] *** Compiling trie for documents in "+corpusDirectoryPathname+"\n");
		segmenter = new StringSegmenter_IUMorpheme();
		
		if ( !fromScratch ) {
			if (this.canBeResumed(corpusDirectoryPathname)) {
				this.readFromJson(corpusDirectoryPathname);
			}
		} else {
			this.deleteJSON(corpusDirectoryPathname);
			trie = new Trie();
		}
		
		wordCounter = 0;
			
		process(corpusDirectoryPathname);
		toConsole("[INFO] *** Compilation completed."+"\n");
		saveCompilerAsJSON_toDir(corpusDirectoryPathname);
		if (trieFilePath != null)
			saveCompilerAsJSON_toFile(trieFilePath);
	}
	
	public boolean setTrieFilePath(String _trieFilePath) {
		File f = new File(_trieFilePath);
		File dirF = f.getParentFile();
		if ( dirF != null && !dirF.isDirectory() ) {
			trieFilePath = null;
			return false;
		}
		trieFilePath = _trieFilePath;
		return true;
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
	
	private void deleteJSON(String corpusDirectoryPathname) throws IOException {
		File saveFile = new File(corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}

	private void saveCompilerAsJSON_toDir(String corpusDirectoryPathname) throws IOException {
		String saveFilePathname = corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME;
		saveCompilerAsJSON_toFile(saveFilePathname);
	}
	
	private void saveCompilerAsJSON_toFile(String filePathname) throws IOException {
			FileWriter saveFile = new FileWriter(filePathname);
			Gson gson = new Gson();
			long savedRetrievedFileWordCounter = this.retrievedFileWordCounter;
			this.retrievedFileWordCounter = this.currentFileWordCounter;
			String json = gson.toJson(this);
			this.retrievedFileWordCounter = savedRetrievedFileWordCounter;
			saveFile.write(json);
			saveFile.flush();
			saveFile.close();
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 */
	public void readFromJson(String corpusDirectoryPathname) throws FileNotFoundException  {
		Gson gson = new Gson();
		String jsonFilePath = corpusDirectoryPathname+"/"+JSON_COMPILATION_FILE_NAME;
		File jsonFile = new File(jsonFilePath);
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		CompiledCorpus compiledCorpus = gson.fromJson(br, CompiledCorpus.class);
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
		this.corpusDirNeededForSavingPurposes = corpusDirectoryPathname;
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
    protected void processDocumentContents(BufferedReader bufferedReader, String fileAbsolutePath) throws Exception {	
    	Logger logger = Logger.getLogger("CorpusTrieCompiler.processDocumentContents");
		String line;
		boolean stopBecauseOfStopAfter = false;
		long fileWordCounter = 0;
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
				logger.debug("fileWordCounter: "+fileWordCounter);
				logger.debug("fileCompiled: "+filesCompiled.contains(fileAbsolutePath));
				
				if ( !filesCompiled.contains(fileAbsolutePath) ) {
					if (retrievedFileWordCounter != -1) {
						if (fileWordCounter < retrievedFileWordCounter) {
							fileWordCounter++;
							++currentFileWordCounter;
							continue;
						} else {
							retrievedFileWordCounter = -1;
						}
					}
				}
				++fileWordCounter;
				++currentFileWordCounter;
				
				if ( filesCompiled.contains(fileAbsolutePath) ) {
					continue;
				}
				

		        
				toConsole("[INFO]     "+wordCounter + "(" + currentFileWordCounter + "+). " + word + "... ");
				String[] segments = fetchSegmentsFromCache(word);
				if (segments==null) {
					String now = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date());
						toConsole("[segmenting] ("+now+")");
						try {
							segments = getSegmenter().segment(word);
							toConsole("** AFTER segment()");
						} catch (Exception e) {
							toConsole("** EXCEPTION RAISED");
							toConsole(" ??? " + e.getClass().getName() + " --- " + e.getMessage() + " ");
							segments = new String[] {};
						}
						toConsole("** addToCache");
						addToCache(word, segments);
						toConsole("** AFTER addToCache");
				}
				try {
					TrieNode result = null;
					if (segments.length != 0) {
						toConsole("** we have some segments...trie.add");
						result = trie.add(segments,word);
						toConsole("** AFTER trie.add");
// trie.add cannot return null when called with non-empty list of segments (see comment in Trie.add method)
//					}
//					if (result != null) {
						toConsole(result.getKeysAsString()+"\n");
					} else {
						toConsole("** no segments...");
						toConsole("XXX\n");
						wordsFailedSegmentation.add(word);
						toConsole("** AFTER wordsFailedSegmentation...");

						long nb;
						if (wordsFailedSegmentationWithFreqs.containsKey(word))
							nb = wordsFailedSegmentationWithFreqs.get(word).longValue()+1;
						else
							nb = 1;
						wordsFailedSegmentationWithFreqs.put(word, nb);
						toConsole("** DONE updating list of words with no analysis");
					}

				} catch (TrieException e) {
					toConsole("--** Problem adding word: " + words[n] + " (" + e.getMessage() + ").");
					System.out.println("Problem adding word: " + words[n] + " (" + e.getMessage() + ").");
				}
				
				if (wordCounter % saveFrequency == 0) {
					toConsole("[INFO]     --- saving jsoned compiler ---"+"\n");
					logger.debug("size of trie: "+trie.getSize());
					saveCompilerAsJSON_toDir(this.corpusDirNeededForSavingPurposes);
				}
				// this line allows to make the compiler stop at a given point (for tests purposes only)
				if (stopAfter != -1 && wordCounter == stopAfter) {
					bufferedReader.close();
					throw new Exception("Simulating an error during trie compilation of corpus.");
				}
			}
		}		
		bufferedReader.close();
	}
    
    public HashMap<String,Long> getWordsThatFailedSegmentationWithFreqs() {
    	return wordsFailedSegmentationWithFreqs;
    }
    
    // ----------------------------- STATS -------------------------------
    
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
		System.out.print(message);
	}


    // ----------------------------- private -------------------------------

	private void addToCache(String word, String[] segments) {
		segmentsCache.put(word, segments);
	}

	private String[] fetchSegmentsFromCache(String word) {
		if (!segmentsCache.containsKey(word))
			return null;
		return segmentsCache.get(word);
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
		//System.out.println(Arrays.toString(words));
		return words;
	}

	public TrieNode getMostFrequentTerminal(String[] segments) {
		return this.trie.getMostFrequentTerminal(segments);
	}

	public String[] getMostFrequentSequenceForRoot(String string) {
		return this.trie.getMostFrequentSequenceForRoot(string);
	}



}

