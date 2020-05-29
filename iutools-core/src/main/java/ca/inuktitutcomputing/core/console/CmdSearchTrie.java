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
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieNode_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpus;

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
		Trie_InMemory trie = compiledCorpus.getTrie();

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
			
			TrieNode_InMemory node = trie.getNode(morphemes);
			if (node != null) {
				String nodeString = node.toString();
				TrieNode_InMemory mostFrequentTerminal = compiledCorpus.getMostFrequentTerminal(node);
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
}
