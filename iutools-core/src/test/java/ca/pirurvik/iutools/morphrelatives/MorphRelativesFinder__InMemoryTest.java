package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;

public class MorphRelativesFinder__InMemoryTest extends MorphRelativesFinderTest {

	@Override
	protected CompiledCorpus makeCorpus(Class<? extends StringSegmenter> segClass) throws Exception {
        CompiledCorpus corpus = new CompiledCorpus_InMemory(segClass.getName());
		return corpus;
	}
}
