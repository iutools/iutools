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
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.TrieWithSegmenterClassname;
import ca.nrc.datastructure.trie.Trie_IUMorpheme;
import ca.nrc.datastructure.trie.Trie_IUMorphemeWithSegmenterClassname;
import ca.nrc.json.PrettyPrinter;


/**
 * This creates a Trie of the (Inuktitut) words in the Nunavut Hansard
 *
 */ 
public class CorpusTrieCompiler 
{
	private static Trie corpusTrie;
	private static HashMap<String,Long> index = new HashMap<String, Long>();
	private static long maxFreq = 0;
	private static String entryWithMaxFreq;
	public static String outputFilePath;
	public static File outputFile;
	private static PrintWriter outputPrinter;
	public static String trieFilePath;
	public static File trieFile;
	
	private static String fileBeingProcessed;
	public static int saveFrequency = 1000;
	private static long savedWordCounter;
	
	/*
	 * @param args[0] name of directory with documents (assumed in ca.pirurvik.data)
	 */
	public static void main(String[] args) throws IOException {
		String dirName = args[0];
		System.out.println("\n--- Compiling trie for documents in "+dirName);
		File dir = new File(dirName);
		outputFilePath = dir.getName()+"-"+"trie_compilation.log";
		outputFile = new File(outputFilePath);
		outputPrinter = new PrintWriter(new FileWriter(outputFilePath));
		trieFilePath = dir.getName()+"-"+"trie_dump.txt";
		trieFile = new File(trieFilePath);
		//StringSegmenter morphemeSegmenter = new StringSegmenter_IUMorpheme();
		//corpusTrie = new Trie(morphemeSegmenter);
		corpusTrie = new Trie_IUMorpheme();
		try {
			File corpusDirectory = new File(dirName);
			process(corpusDirectory);
			outputPrinter.flush();
			outputPrinter.close();
			writeJSON();
		} catch (Exception e1) {
			e1.printStackTrace();
			outputPrinter.close();
			System.exit(1);
		}
	}
	
	private static void writeJSON() {
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
	
	public static Trie readTrieFromJSON() throws Exception {
		Gson gson = new Gson();
		String content = new String(Files.readAllBytes(Paths.get(trieFile.getAbsolutePath())));
		BufferedReader br = new BufferedReader(new FileReader(trieFile.getAbsolutePath()));
		Trie_IUMorphemeWithSegmenterClassname trieWithoutSegmenter = gson.fromJson(br, Trie_IUMorphemeWithSegmenterClassname.class);
		// Alternate way to call Gson.fromJson(), with String instead of BufferedReader
		//Trie_IUMorphemeWithSegmenterClassname trieWithoutSegmenter = gson.fromJson(content, Trie_IUMorphemeWithSegmenterClassname.class);
		Trie trie = trieWithoutSegmenter.toTrie();
		return trie;
	}
 

    private static void printTrie(Trie trie) {
    	System.out.println("\nThe trie looks like this:\n");
    	System.out.println(PrettyPrinter.print(trie));
	}

	private static void printIndex() {
		System.out.println("\nIndexed words:\n");
		for (Map.Entry<String, Long> entry : index.entrySet()) {
		    String key = entry.getKey();
		    long value = entry.getValue().longValue();
		    System.out.println(key+" : "+value);
		}
		System.out.println("Nb of individual words: "+index.entrySet().size());
	    System.out.println("Entry with maximum frequency: "+entryWithMaxFreq+" ("+maxFreq+")");
	    System.out.println("Nb. of words in the trie: "+corpusTrie.getSize());
	}

	private static void process(File corpusDirectory) {
    	File [] files = corpusDirectory.listFiles();
    	for (int i=0; i<files.length; i++) {
			processFile(files[i]);
    	}
	}


	private static void processFile(File file) {
		try {
			System.out.println("\n--- compiling document "+file.getName());
			String fileAbsolutePath = file.getAbsolutePath();
			fileBeingProcessed = fileAbsolutePath;
			FileReader fr = new FileReader(fileAbsolutePath);
			BufferedReader br = new BufferedReader(fr);
			String line;
			long wordCounter = 0;
			int limit = -1; // 20;
			while ((line = br.readLine()) != null && limit-- != 0) {
				String[] words = extractWordsFromLine(line);
				for (int n = 0; n < words.length; n++) {
					if (isInuktitutWord(words[n])) {
						if (!index.containsKey(words[n])) {
							addToIndex(words[n]);
							System.out.print(++wordCounter + ". " + words[n]
									+ "...");
							outputPrinter.print(wordCounter + ". " + words[n]
									+ "...");
							try {
								TrieNode result = corpusTrie.add(words[n]);
								if (result != null) {
									System.out.println(result.getText());
									outputPrinter.println(result.getText());
								} else {
									System.out.println(" XXX");
									outputPrinter.println(" X");
								}

							} catch (TrieException e) {
								System.out.println("Problem adding word: "
										+ words[n] + " (" + e.getMessage()
										+ ").");
								outputPrinter.println("Problem adding word: "
										+ words[n] + " (" + e.getMessage()
										+ ").");
							}
							if (wordCounter % saveFrequency == 0) {
								System.out.println("   --- saving verbose and jsoned trie ---");
								outputPrinter.println("   --- saving verbose and jsoned trie ---");
								outputPrinter.flush();
								savedWordCounter = wordCounter;
								writeJSON();
							}
						} else {
							addToIndex(words[n]);
						}
					}
				}
			}
			
			br.close();
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void addToIndex(String string) {
		Long frequency = index.get(string);
		long freq;
		if (frequency == null) {
			freq = 1;
		} else {
			freq = frequency.longValue() + 1;
		}
		index.put(string, new Long(freq));
		//System.out.println("--- " + words[n] + ": "
		//		+ index.get(words[n]).longValue());
		if (freq > maxFreq) {
			maxFreq = freq;
			entryWithMaxFreq = string;
		}
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
