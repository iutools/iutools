package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;

public class MorphRelativesFinder__InMemoryTest extends MorphRelativesFinderTest {

	@Override
	protected MorphRelativesFinder makeFinder() throws Exception {
		CompiledCorpus corpus =
			new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());

		MorphRelativesFinder_InMemory finder =
			new MorphRelativesFinder_InMemory(corpus);

		return finder;
	}

	@Override
	protected CompiledCorpus makeCorpus(Class<? extends StringSegmenter> segClass) throws Exception {
        CompiledCorpus corpus = new CompiledCorpus_InMemory(segClass.getName());
		return corpus;
	}
}
