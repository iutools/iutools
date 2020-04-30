package ca.pirurvik.iutools.corpus;

import java.util.Iterator;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.datastructure.trie.StringSegmenter_Char;

/**
 * This class stores stats about Inuktut words seen in a corpus, such as:
 * 
 * - frequency of words and ngrams
 * - word morphological decompositions
 * 
 * @author desilets
 *
 */
public abstract class CompiledCorpus_Base {
	
	public abstract Iterator<String> allWords();
	public abstract WordInfo info4word(String word);
	public abstract Set<String> wordsContainingNgram(String ngram);
	protected abstract Set<String> wordsContainingMorphNgram(String[] morphemes);	

	protected String segmenterClassName = StringSegmenter_Char.class.getName();
	
	private int decompsSampleSize = Integer.MAX_VALUE;
	
	@JsonIgnore
	public transient boolean verbose = true;
	@JsonIgnore
	public transient String name;
	
	
	public CompiledCorpus_Base() {
		initialize(StringSegmenter_Char.class.getName()); 
	}
	
	public CompiledCorpus_Base(String segmenterClassName) {
		initialize(segmenterClassName);
	}

	public void initialize(String _segmenterClassName) {
		if (_segmenterClassName != null) {
			this.segmenterClassName = _segmenterClassName;
		}
	}
	
	public CompiledCorpus_Base setVerbose(boolean value) {
		verbose = value;
		return this;
	}
	
	public CompiledCorpus_Base setName(String _name) {
		name = _name;
		return this;
	}
	
	public CompiledCorpus_Base setSegmenterClassName(String className) {
		segmenterClassName = className;
		return this;
	}
	

	public CompiledCorpus_Base setDecompsSampleSize(int size) {
		return this;
	}

	public void addWordOccurence(String word) {
		// TODO Auto-generated method stub
		
	}
	
	@JsonIgnore
	public void setWordDecompositions(Decomposition[] decomps) {
		// TODO Auto-generated method stub
		
	}
}
