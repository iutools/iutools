package ca.pirurvik.iutools.corpus;

import java.util.Arrays;
import java.util.HashMap;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.TrieNode;

public class CompiledCorpus_IUMorpheme extends CompiledCorpus {
	
	public CompiledCorpus_IUMorpheme() {
		super(StringSegmenter_IUMorpheme.class.getName());
	}
	
	public CompiledCorpus_IUMorpheme(String notImportant) {
		super(StringSegmenter_IUMorpheme.class.getName());
	}
	
	public String[] getMostFrequentCompletionForRootType(String rootType) {
		HashMap<String,Long> completionKeysFreqs = new HashMap<String,Long>();
		long maxFreq = 0;
		String mostFrequentCompletionKeys = null;
		TrieNode[] terminals = this.trie.getAllTerminals();
		for (TrieNode terminal : terminals) {
			String terminalRootKey = terminal.keys[0]; // {surface_form/id}
			String[] partsRootKey = terminalRootKey.split("/");
			String rootTypePart = partsRootKey[1].substring(0, partsRootKey[1].length()-1);
			String terminalRootType = rootTypePart.substring(rootTypePart.length()-1);
			
			if (terminalRootType.equals(rootType)) {
				String[] suffixKeys = Arrays.copyOfRange(terminal.keys, 1, terminal.keys.length);
				if (suffixKeys.length==1)
					continue;
				String completionKeys = String.join(" ", suffixKeys);
				long freqCompletionKeys;
				if (!completionKeysFreqs.containsKey(completionKeys))
					freqCompletionKeys = 1;
				else
					freqCompletionKeys = completionKeysFreqs.get(completionKeys).longValue()+1;
				completionKeysFreqs.put(completionKeys, new Long(freqCompletionKeys));
				if (freqCompletionKeys > maxFreq) {
					maxFreq = freqCompletionKeys;
					mostFrequentCompletionKeys = completionKeys;
				}
			}
		}
		return mostFrequentCompletionKeys.split(" ");
	}


}
