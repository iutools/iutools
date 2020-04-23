package ca.inuktitutcomputing.core.console;

import java.io.FileReader;
import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import com.google.gson.Gson;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.pirurvik.iutools.CompiledCorpus;

public class CmdSearchTrie extends ConsoleCommand {
	
	public CmdSearchTrie(String name) {
		super(name);
	}


	@Override
	public String getUsageOverview() {
		return "Search in a compiled Trie.";
	}
	

	@Override
	public void execute() throws Exception {
		String compilationFilePath = getCompilationFile();
		String[] morphemes = getMorphemes(false); 
		String word = getWord(false); 
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		
		boolean interactive = false;
		if (morphemes == null && word == null) {
			interactive = true;
		} else if (morphemes == null) {
			morphemes = segmenter.segment(word);
		}

		boolean searchWord = false;
		
		CompiledCorpus compiledCorpus = CompiledCorpus.createFromJson(compilationFilePath);
		Trie trie = compiledCorpus.getTrie();

		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);

		echo("              ____________________________");
		echo("              |             |             |");
		echo("              |   in trie   |    failed   |");
		echo("______________|_____________|_____________|");
		echo("|             |             |             |");
		echo("| Occurrences | "+prepend(' ',11,formatter.format(trie.getNbOccurrences()))+" | "+prepend(' ',11,formatter.format(compiledCorpus.getNbOccurrencesThatFailedSegmentations()))+" |");
		echo("|_____________|_____________|_____________|");
		echo("|             |             |             |");
		echo("| Words       | "+prepend(' ',11,formatter.format(trie.getSize()))+" | "+prepend(' ',11,formatter.format(compiledCorpus.getNbWordsThatFailedSegmentations()))+" |");
		echo("|_____________|_____________|_____________|");
		
		while (true) {
			if (interactive) {
				String searchType;
				if (searchWord)
					searchType = "a word ('m' to switch to morphemes)";
				else
					searchType = "a list of morphemes ('w' to switch to word)";
				String searchStr = prompt("Enter "+searchType);
				if (searchStr == null) break;
				if (searchStr.equals("m")) {
					searchWord = false; continue;
				}
				else if (searchStr.equals("w")) {
					searchWord = true; continue; }
				if (searchWord)
					morphemes = segmenter.segment(searchStr);
				else
					morphemes = searchStr.split("\\s+");
			}
			
			echo("\nSearching for morphemes: "+String.join(" ", morphemes)+"\n");
			
			TrieNode node = trie.getNode(morphemes);
			if (node != null) {
				String nodeString = node.toString();
				TrieNode mostFrequentTerminal = node.getMostFrequentTerminal();
				echo(nodeString);
				echo("Most frequent terminal: "+mostFrequentTerminal.toString());
			} else {
				echo("No node has been found for that sequence of morphemes.");
			}
			
			if (!interactive) break;
		}
		
	}


	private String prepend(char prependChar, int maxPlaces, String numberStr) {
		return CharBuffer.allocate(maxPlaces-numberStr.length()).toString().replace( '\0', prependChar)+numberStr; 
	}
	
//	public static void search_trie_one(String arg1Name, String arg1Value, String arg2Name, String arg2Value) throws ConsoleException {
//		String trieFilePath = null;
//		String searchType = null;
//		String searchText = null;
//		
//		if ( arg1Name.equals("-trie-file") ) {
//			if ( arg2Name.equals("-text") || arg2Name.equals("-surfaceform") ) {
//				trieFilePath = arg1Value;
//				if (arg2Name.equals("-text"))
//					searchType = "text";
//				else
//					searchType = "surfaceform";
//			} else {
//				throw new InvalidArgumentConsoleException("'"+arg2Name+"' is not a valid argument to search_trie_one.");
//			}
//		} else if ( arg1Name.equals("-text") || arg1Name.equals("-surfaceform") ) {
//			if ( arg2Name.equals("-trie-file") ) {
//				trieFilePath = arg1Value;
//				if (arg1Name.equals("-text"))
//					searchType = "text";
//				else
//					searchType = "surfaceform";
//			} else {
//				throw new InvalidArgumentConsoleException("'"+arg2Name+"' is not a valid argument to search_trie_one.");
//			}
//		} else {
//			throw new InvalidArgumentConsoleException("'"+arg1Name+"' is not a valid argument to search_trie_one.");
//		}
//		Trie trie = null;
//		trie = readTrie(trieFilePath);
//		System.out.println("trie: "+trie.getClass().getName());
//		System.out.println(__search_trie(trie,searchType,searchText));
//	}
//
//	private static Object __search_trie(Trie trie, String searchType, String searchText) {
//		if (searchType.equals("surfaceform")) {
//
//		} else {
//			
//		}
//		return null;
//	}
//
//	public static void search_trie(String argName, String argValue) throws ConsoleException {
//		if (!argName.equals("-trie-file"))
//			throw new InvalidArgumentConsoleException("First argument '"+argName+"' is unknown to the method search_trie.");
//		String trieFilePath = argValue;
//		File f = new File(trieFilePath);
//		if(!f.exists() || !f.isFile()) { 
//		    throw new UnknownFileConsoleException("Second argument '"+argName+"' does not refer to an existing file.");
//		}
//		Trie trie = readTrie(trieFilePath);
//		
//		InputStream is = null;
//		BufferedReader br = null;
//		try {
//			is = System.in;
//			br = new BufferedReader(new InputStreamReader(is));
//			String line = "";
//			while ( line != null) {
//				System.out.print("Enter a word ['q' to quit]: ");
//				System.out.flush();
//				line = br.readLine();
//				if (line.equalsIgnoreCase("q")) {
//					break;
//				}
//				System.out.println("Searching for : " + line);
//				System.out.flush();
//			
//				String[] segments = null;
//				segments = new ObjectMapper().readValue(line, segments.getClass());
//				TrieNode trieNode = trie.getNode(segments);
//				System.out.println("frequency of the whole word: "+trieNode.getFrequency());
//				System.out.println(PrettyPrinter.print(trieNode));
//				TrieNode rootNode = trie.getNode(new String[]{segments[0]});
//				System.out.println("root morpheme: "+rootNode.getText());
//				System.out.println("frequency of the root: "+rootNode.getFrequency());
//				TrieNode mostFrequentTerminal = rootNode.getMostFrequentTerminal();
//				System.out.println("most frequent word with this root: "+mostFrequentTerminal.getText()+
//						" ["+mostFrequentTerminal.getFrequency()+" occurrence(s)]");
//				System.out.println("node of most frequent word: "+PrettyPrinter.print(mostFrequentTerminal));
//				//System.out.println("surface form of most frequent word: "+((TrieNode_IUMorpheme)mostFrequentTerminal).getSurfaceForm());
//				System.out.println("\n");
//			}
//		} catch (IOException ioe) {
//			System.out.println("Exception while reading input " + ioe);
//		} catch (Exception e) {
//			System.out.println("Exception while getting the node.");
//		}
//		finally {
//			// close the streams using close method
//			try {
//				if (br != null) {
//					br.close();
//				}
//			}
//			catch (IOException ioe) {
//				System.out.println("Error while closing stream: " + ioe);
//			}
//		}
//		
//	}

		

}
