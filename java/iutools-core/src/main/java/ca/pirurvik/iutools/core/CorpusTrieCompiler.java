package ca.pirurvik.iutools.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;


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
	
	/*
	 * @param args[0] name of directory with documents (assumed in ca.pirurvik.data)
	 */
	public static void main(String[] args) {
		String dirName = args[0];
		StringSegmenter morphemeSegmenter = new StringSegmenter_IUMorpheme();
		corpusTrie = new Trie(morphemeSegmenter);
		try {
			File corpusDirectory = new File(dirName);
			process(corpusDirectory);
			printIndex();
			printTrie(corpusTrie);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
	}
    
    private static void printTrie(Trie trie) {
    	System.out.println("\nThe trie looks like this:\n");
    	System.out.println(ca.nrc.json.PrettyPrinter.print(trie));
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

	private static void process(File hansardDirectory) {
    	File [] files = hansardDirectory.listFiles();
    	for (int i=0; i<files.length; i++) {
    		try {
				FileReader fr = new FileReader(files[i].getAbsolutePath());
				BufferedReader br = new BufferedReader(fr);
				processFile(br);
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}   		
    	}
	}


	/*private static BufferedReader openFile(String pirurvikDataDir,String fileName) throws Exception {
        ClassLoader classLoader = HansardTrie.class.getClassLoader();
        String packagePath = "ca/pirurvik";
        String dataPath = packagePath+"/"+pirurvikDataDir;
        String fullPath = dataPath+"/"+fileName;
        String filePathFull = null;
    	URL res = classLoader.getResource(fullPath);
    	if (res==null)
    		throw new Exception("File "+fileName+" cannot be found in "+dataPath+".");
    	filePathFull = res.getPath();
        FileReader fr;
        BufferedReader br = null;
		fr = new FileReader(filePathFull);
		br = new BufferedReader(fr);
    	return br;
    }*/
    
	private static void processFile(BufferedReader br) {
		try {
			String line;
			int limit = 20;
			while ((line = br.readLine()) != null && limit--!=0) {
				//System.out.println(line);
				String[] words = extractWordsFromLine(line);
				for (int n = 0; n < words.length; n++) {
					if (isInuktitutWord(words[n])) {
						System.out.print(words[n]+"...");
						addToIndex(words[n]);
						corpusTrie.add(words[n]);
						System.out.print("\n");
					}
				}
			}
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
