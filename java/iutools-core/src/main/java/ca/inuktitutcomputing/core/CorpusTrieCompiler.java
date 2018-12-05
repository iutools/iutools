package ca.inuktitutcomputing.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.introspection.Introspection;
import ca.nrc.json.PrettyPrinter;


/**
 * This creates a Trie of the (Inuktitut) words in the Nunavut Hansard
 *
 */ 
public class CorpusTrieCompiler 
{
	private static String JSON_FILE_BASE_NAME = "trie_compilation.json";
	
	protected Trie trie = new Trie();
	protected HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	
	protected String trieFilePath = null;
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
			
	private Vector<String> filesProcessed = new Vector<String>();
	private String pathOfFileCurrentlyProcessed = null;
	protected long currentFileWordCounter = -1;
	protected long retrievedFileWordCounter = -1;
	private long wordCounter = 0;
	
	public int saveFrequency = 1000;
	public int stopAfter = -1;

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
	public static void main(String[] args) throws IOException {
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
	
	public void setCorpusDirectory(String _dirName) {
		this.dirName = _dirName;
		File corpusDirectory = new File(this.dirName);
		trieFilePath = corpusDirectory.getName()+"-"+JSON_FILE_BASE_NAME;
		trieFile = new File(trieFilePath);
	}

	public  void run() throws IOException {
		run(false);
	}
	
	public  void run(boolean unitTesting) throws IOException {
		System.out.println("\n--- Compiling trie for documents in "+this.dirName);
		segmenter = new StringSegmenter_IUMorpheme();
		wordCounter = 0;
		if (currentFileWordCounter != -1) {
			retrievedFileWordCounter = currentFileWordCounter;
		} else {
			currentFileWordCounter = 0;
			trie = new Trie();
		}
		try {
			process();
			saveAsJSON();
		} catch (Exception e1) {
			if (!unitTesting) {
				e1.printStackTrace();
				System.exit(1);
			}
		}
	}
	
	private void initializeProcess() {
		// TODO Auto-generated method stub
		
	}

	private void saveAsJSON() {
		try {
			FileWriter trieFile = new FileWriter(trieFilePath);
			Gson gson = new Gson();
			String json = gson.toJson(this);
			trieFile.write(json);
			trieFile.flush();
			trieFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static CorpusTrieCompiler readFromJSON(String corpusDirectoryPath) throws Exception {
		Gson gson = new Gson();
		File corpusDirectory = new File(corpusDirectoryPath);
		String jsonFilePath = corpusDirectory.getName()+"-"+JSON_FILE_BASE_NAME;
		File jsonFile = new File(jsonFilePath);
		BufferedReader br = new BufferedReader(new FileReader(jsonFile));
		CorpusTrieCompiler compiler = gson.fromJson(br, CorpusTrieCompiler.class);
		return compiler;
	}
 

    private void printTrie(Trie trie) {
    	System.out.println("\nThe trie looks like this:\n");
    	System.out.println(PrettyPrinter.print(trie));
	}

	private void process() throws Exception {
		File corpusDirectory = new File(this.dirName);
    	File [] files = corpusDirectory.listFiles();
    	for (int i=0; i<files.length; i++) {
			processFile(files[i]);
    	}
	}

	private void processFile(File file) throws Exception {
		try {
			String fileAbsolutePath = file.getAbsolutePath();
			if ( !filesProcessed.contains(fileAbsolutePath) ) {
				System.out.println("\n--- compiling document "+file.getName());
				processDocumentContents(fileAbsolutePath);
				filesProcessed.add(fileAbsolutePath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void processDocumentContents(String fileAbsolutePath) throws Exception {
		pathOfFileCurrentlyProcessed = fileAbsolutePath;
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
		processDocumentContents(bufferedReader);
	}
    protected void processDocumentContents(BufferedReader bufferedReader) throws Exception {	
		String line;
		boolean stopBecauseOfStopAfter = false;
		long fileWordCounter = 0;
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
						continue;
					} else {
						retrievedFileWordCounter = -1;
					}
				}
				++fileWordCounter;
				++currentFileWordCounter;
				System.out.print(wordCounter + "(" + currentFileWordCounter + "). " + word + "...");
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
						System.out.println(result.getText());
					} else {
						System.out.println(" XXX");
					}

				} catch (TrieException e) {
					System.out.println("Problem adding word: " + words[n] + " (" + e.getMessage() + ").");
				}
				if (wordCounter % saveFrequency == 0) {
					System.out.println("   --- saving jsoned compiler ---");
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
