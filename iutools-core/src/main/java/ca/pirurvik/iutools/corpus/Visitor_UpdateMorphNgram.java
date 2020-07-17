package ca.pirurvik.iutools.corpus;

import java.util.List;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.Trie_InFileSystem;
import ca.nrc.datastructure.trie.visitors.TrieNodeVisitor;

/**
 * Given the node for a word in a CompiledCorpus_InFileSystem, this visitor 
 * update's the corpus' morpheme ngram trie to include information about 
 * the word's decomposition. 
 */

public class Visitor_UpdateMorphNgram extends TrieNodeVisitor {

	private CompiledCorpus corpus = null;
	private long wordCount = 0;

	public Visitor_UpdateMorphNgram(CompiledCorpus _corpus) {
		this.corpus = _corpus;
	}

	@Override
	public void visitNode(TrieNode wordNode) throws TrieException {
		wordCount++;
		String word = wordNode.surfaceForm;
		System.out.println("Updating morphemes ngram trie with decomps of word #"
				+wordCount+": "+word);
		long wordFreq = wordNode.getFrequency();
		String[][] nullSample = null;
		String[][] sampleDecomps = 
			wordNode.getField("sampleDecompositions", nullSample);		
		String[] topDecomp = null;
		if (sampleDecomps != null && sampleDecomps.length > 0) {
			topDecomp = sampleDecomps[0];
		}
		try {
			WordInfo winfo = corpus.info4word(word);
			corpus.updateDecompositionsIndex( winfo);
		} catch (CompiledCorpusException e) {
			throw new TrieException(e);
		}
	}
}
