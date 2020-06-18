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
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieNode;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.WordInfo;

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
		
		CompiledCorpus_InMemory compiledCorpus = CompiledCorpus_InMemory.createFromJson(compilationFilePath);

		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);
		
		
		long occsWithSuccessfulDecomp = compiledCorpus.totalOccurencesWithDecomps();
		String occSucc = prepend(' ',11,formatter.format(occsWithSuccessfulDecomp));
		
		long occsWithFailedDecomp = compiledCorpus.totalOccurencesWithNoDecomp();
		String occFail = prepend(' ',11,formatter.format(occsWithFailedDecomp));
		
		long wordsWithSuccesfulDecomp = compiledCorpus.totalWordsWithDecomps();
		String wrdSucc = prepend(' ',11,formatter.format(wordsWithSuccesfulDecomp));

		long wordsWithFailedDecomp = compiledCorpus.totalWordsWithNoDecomp();
		String wrdFail = prepend(' ',11,formatter.format(wordsWithFailedDecomp));

		echo("              ____________________________");
		echo("              |             |             |");
		echo("              | with decomp | no decomp  |");
		echo("______________|_____________|_____________|");
		echo("|             |             |             |");
		echo("| Occurrences | "+occSucc+" | "+occFail+" |");
		echo("|_____________|_____________|_____________|");
		echo("|             |             |             |");
		echo("| Words       | "+wrdSucc+" | "+wrdFail+" |");
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
			
			WordInfo winfo = compiledCorpus.mostFrequentWordExtending(morphemes);			
			if (winfo != null) {
				echo("Most frequent word: \n"+winfo.toString());
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
