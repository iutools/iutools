package ca.pirurvik.iutools.morphemesearcher;

import ca.nrc.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;

public class MorphemeSearcher_InMemoryTest extends MorphemeSearcherTest {

	@Override
	protected CompiledCorpus makeCorpus() {
		return new CompiledCorpus_InMemory(MockStringSegmenter_IUMorpheme.class.getName());
	}	
}
