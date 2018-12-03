package ca.pirurvik.iutools.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_Char;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.json.PrettyPrinter;


/**
 * This creates a Trie of the (Inuktitut) words in the Nunavut Hansard
 *
 */ 
public class CorpusTrieCompiler 
{
	protected Trie corpusTrie = new Trie();
	private HashMap<String,String[]> segmentsCache = new HashMap<String, String[]>();
	private long maxFreq = 0;
	private String entryWithMaxFreq = null;
	public String outputFilePath = null;
	public File outputFile = null;
//	private PrintWriter outputPrinter;
	public String trieFilePath = null;
	public File trieFile = null;
	private StringSegmenter segmenter = new StringSegmenter_Char();
	
	private String fileBeingProcessed;
	public int saveFrequency = 1000;
	private long savedWordCounter;

	protected String dirName = null;
	
	public static class CorpusTrieCompilerException extends Exception {
		public CorpusTrieCompilerException(String mess) {
			super(mess);
		}
	}
	
	public CorpusTrieCompiler() {
		initialize(null);
	}
	
	public CorpusTrieCompiler(StringSegmenter _segmenter) {
		initialize(_segmenter);
	}
	
	public void initialize(StringSegmenter _segmenter) {
		if (_segmenter != null) {
			this.segmenter = _segmenter;
		}
		
	}
	
	/*
	 * @param args[0] name of directory with documents (assumed in ca.pirurvik.data)
	 */
	public static void main(String[] args) throws IOException {
		CorpusTrieCompiler trieCompiler = new CorpusTrieCompiler();
		if (args.length < 1) usage("Need to pass a directory name as first argument");
		String dirName = args[0];
		trieCompiler.dirName = dirName;
		
		trieCompiler.run();
	}
	
	private static void usage(String errMess) {
		if (errMess != null) errMess = "ERROR: "+errMess;
		System.out.println("ERROR: "+errMess);
		System.exit(1);
	}

	public  void run() throws IOException {
		System.out.println("\n--- Compiling trie for documents in "+this.dirName);
		File dir = new File(dirName);
		outputFilePath = dir.getName()+"-"+"trie_compilation.log";
		outputFile = new File(outputFilePath);
//		outputPrinter = new PrintWriter(new FileWriter(outputFilePath));
		trieFilePath = dir.getName()+"-"+"trie_dump.txt";
		trieFile = new File(trieFilePath);
		segmenter = new StringSegmenter_IUMorpheme();
		corpusTrie = new Trie();
		try {
			File corpusDirectory = new File(dirName);
			process(corpusDirectory);
//			outputPrinter.flush();
//			outputPrinter.close();
			writeJSON();
		} catch (Exception e1) {
			e1.printStackTrace();
//			outputPrinter.close();
			System.exit(1);
		}
	}
	
	private void writeJSON() {
		try {
			FileWriter trieFile = new FileWriter(trieFilePath);
			String json = corpusTrie.toJSON();
			trieFile.write(json);
			trieFile.flush();
			trieFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Trie readTrieFromJSON() throws Exception {
		Gson gson = new Gson();
		String content = new String(Files.readAllBytes(Paths.get(trieFile.getAbsolutePath())));
		BufferedReader br = new BufferedReader(new FileReader(trieFile.getAbsolutePath()));
		Trie trie = gson.fromJson(br, Trie.class);
		return trie;
	}
 

    private void printTrie(Trie trie) {
    	System.out.println("\nThe trie looks like this:\n");
    	System.out.println(PrettyPrinter.print(trie));
	}

	private void process(File corpusDirectory) throws Exception {
    	File [] files = corpusDirectory.listFiles();
    	for (int i=0; i<files.length; i++) {
			processFile(files[i]);
    	}
	}

	private void processFile(File file) throws Exception {
		try {
			System.out.println("\n--- compiling document "+file.getName());
			String fileAbsolutePath = file.getAbsolutePath();
			fileBeingProcessed = fileAbsolutePath;
			FileReader fr = new FileReader(fileAbsolutePath);
			BufferedReader br = new BufferedReader(fr);
			processDocumentContents(br);
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void processDocumentContents(BufferedReader bufferedReader) throws Exception {
		String line;
		long wordCounter = 0;
		int limit = -1; // 20;
		while ((line = bufferedReader.readLine()) != null && limit-- != 0) {
			String[] words = extractWordsFromLine(line);
			for (int n = 0; n < words.length; n++) {
				String word = words[n];
				if (!isInuktitutWord(word))
					continue;
				String[] segments = null;
				try {
					segments = fetchSegmentsFromCache(word);
				}
				catch (CorpusTrieCompilerException e) {
					segments = segmenter.segment(word);
					addToCache(word,segments);
				}
						System.out.print(++wordCounter + ". " + words[n]
								+ "...");
//						outputPrinter.print(wordCounter + ". " + words[n]
//								+ "...");
						try {
							TrieNode result = corpusTrie.add(segments);
							if (result != null) {
								System.out.println(result.getText());
//								outputPrinter.println(result.getText());
							} else {
								System.out.println(" XXX");
//								outputPrinter.println(" X");
							}

						} catch (TrieException e) {
							System.out.println("Problem adding word: "
									+ words[n] + " (" + e.getMessage()
									+ ").");
//							outputPrinter.println("Problem adding word: "
//									+ words[n] + " (" + e.getMessage()
//									+ ").");
						}
						if (wordCounter % saveFrequency == 0) {
							System.out.println("   --- saving verbose and jsoned trie ---");
//							outputPrinter.println("   --- saving verbose and jsoned trie ---");
//							outputPrinter.flush();
							savedWordCounter = wordCounter;
							writeJSON();
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
