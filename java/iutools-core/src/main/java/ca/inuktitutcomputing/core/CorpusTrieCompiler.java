package ca.inuktitutcomputing.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

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
 */ 
public class CorpusTrieCompiler 
{
	
	private static String JSON_FILE_BASE_NAME = "trie_compilation.json";
	
	protected Trie trie = new Trie();
	protected HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	
	protected String saveFilePath = null;
	@JsonIgnore
	protected transient File trieFile = null;
	
	private String segmenterClassName = StringSegmenter_Char.class.getName();
	
	@JsonIgnore
	private transient StringSegmenter segmenter = null;
	
		@JsonIgnore
		private StringSegmenter getSegmenter() throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (segmenter == null) {
				Class<StringSegmenter> cls = (Class<StringSegmenter>) Class.forName(segmenterClassName);
				segmenter = (StringSegmenter) cls.getConstructor().newInstance();
			}
			return segmenter;
		}
			
	protected long currentFileWordCounter = -1;
	protected long retrievedFileWordCounter = -1;
	private transient long wordCounter = 0;
	
	public int saveFrequency = 1000;
	public transient int stopAfter = -1;

	protected String dirName = null;
	
	
	public static class CorpusTrieCompilerException extends Exception {
		public CorpusTrieCompilerException(String mess) {
			super(mess);
		}
	}
	
	public CorpusTrieCompiler() {
		initialize(null);
	}
	
	public CorpusTrieCompiler(String segmenterClassName) {
		initialize(segmenterClassName);
	}
	

	public void initialize(String _segmenterClassName) {
		this.segmenterClassName = _segmenterClassName;
	}
	
	/*
	 * @param args[0] name of directory with documents (assumed in ca.pirurvik.data)
	 */
	public static void main(String[] args) throws Exception {
		CorpusTrieCompiler trieCompiler = new CorpusTrieCompiler();
		if (args.length < 1) usage("Need to pass a directory name as first argument");
		String dirName = args[0];
		trieCompiler.setCorpusDirectory(dirName);
		
		trieCompiler.run();
	}
	
	private static void usage(String errMess) {
		if (errMess != null) errMess = "ERROR: "+errMess;
		System.out.println("ERROR: "+errMess);
		System.exit(1);
	}
	
	public void setCorpusDirectory(String _dirFullPathname) {
		this.dirName = _dirFullPathname;
		saveFilePath = this.dirName+"/"+JSON_FILE_BASE_NAME;
		trieFile = new File(saveFilePath);
	}
	
	/**
	 * Cette méthode retourne vrai si et seulement si il y a un fichier de sauvegarde pour le répertoire corpusDir.
	 * @param corpusDirPathname
	 * @return
	 */
	
	public boolean canBeResumed(String corpusDirPathname) {
		File jsonFile = new File(corpusDirPathname+"/"+JSON_FILE_BASE_NAME);
		return jsonFile.exists();
	}
	
	public void toConsole(String message) {
		System.out.println("[INFO] "+message);
	}

	public  void run() throws Exception {
		run(false);
	}
	public  void run(boolean fromScratch) throws Exception {
		toConsole("*** Compiling trie for documents in "+this.dirName);
		segmenter = new StringSegmenter_IUMorpheme();
		
		if ( !fromScratch ) {
			if (this.canBeResumed(this.dirName)) {
				this.retrieveFromJSON();
			}
		} else {
			this.deleteJSON();
			trie = new Trie();
		}
		
		wordCounter = 0;
			
		process();
		toConsole("*** End of compilation --- compiler saved in trie_compilation.json in corpus directory");
		saveAsJSON();
	}
	
	private void deleteJSON() throws IOException {
		File saveFile = new File(saveFilePath);
		if (saveFile.exists())
			saveFile.delete();
	}

	private void saveAsJSON() {
		try {
			FileWriter saveFile = new FileWriter(saveFilePath);
			Gson gson = new Gson();
			String json = gson.toJson(this);
			saveFile.write(json);
			saveFile.flush();
			saveFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 * 
	 * @param
	 * @return void
	 * @throws Exception
	 */
	public void retrieveFromJSON() throws Exception {
		Gson gson = new Gson();
		String jsonFilePath = this.dirName+"/"+JSON_FILE_BASE_NAME;
		File jsonFile = new File(jsonFilePath);
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		CorpusTrieCompiler compiler = gson.fromJson(br, CorpusTrieCompiler.class);
		this.trie = compiler.trie;
		this.segmentsCache = compiler.segmentsCache;
		this.saveFilePath = compiler.saveFilePath;
		this.segmenterClassName = compiler.segmenterClassName;
		this.currentFileWordCounter = compiler.currentFileWordCounter;
		this.retrievedFileWordCounter = compiler.retrievedFileWordCounter;
		this.saveFrequency = compiler.saveFrequency;
		compiler = null;
	}
 

	private void process() throws Exception {
		File corpusDirectory = new File(this.dirName);
    	File [] files = corpusDirectory.listFiles(
    			new FilenameFilter() {
    				public boolean accept(File dir, String name) {
    					return name.toLowerCase().endsWith(".txt");
    				}
    			});
    	Arrays.sort(files);
    	for (int i=0; i<files.length; i++) {
			processFile(files[i]);
    	}
	}

	private void processFile(File file) throws Exception {
		try {
			String fileAbsolutePath = file.getAbsolutePath();
			toConsole("--- compiling document "+file.getName());
			processDocumentContents(fileAbsolutePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void processDocumentContents(String fileAbsolutePath) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
		processDocumentContents(bufferedReader);
	}
    protected void processDocumentContents(BufferedReader bufferedReader) throws Exception {	
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
				if (retrievedFileWordCounter!=-1) {
					if (fileWordCounter<retrievedFileWordCounter) {
						fileWordCounter++;
						++currentFileWordCounter;
						continue;
					} else {
						retrievedFileWordCounter = -1;
					}
				}
				++fileWordCounter;
				++currentFileWordCounter;
				String[] segments = null;
				try {
					segments = fetchSegmentsFromCache(word);
				}
				catch (CorpusTrieCompilerException e) {
					segments = getSegmenter().segment(word);
					addToCache(word,segments);
				}
				try {
					TrieNode result = trie.add(segments);
					if (result != null) {
						toConsole("    "+wordCounter + "(" + currentFileWordCounter + "). " + word + "... " + result.getText());
					} else {
						toConsole("    "+wordCounter + "(" + currentFileWordCounter + "). " + word + "... " + "XXX");
					}

				} catch (TrieException e) {
					System.out.println("Problem adding word: " + words[n] + " (" + e.getMessage() + ").");
				}
				if (wordCounter % saveFrequency == 0) {
					toConsole("    --- saving jsoned compiler ---");
					saveAsJSON();
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

	private void addToCache(String word, String[] segments) {
		segmentsCache.put(word, segments);
	}

	private String[] fetchSegmentsFromCache(String word) throws CorpusTrieCompilerException {
		if (!segmentsCache.containsKey(word))
			throw new CorpusTrieCompilerException("Word '"+word+"' not in cache.");
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


}
