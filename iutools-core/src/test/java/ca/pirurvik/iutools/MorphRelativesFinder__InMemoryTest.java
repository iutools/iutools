package ca.pirurvik.iutools;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;

public class MorphRelativesFinder__InMemoryTest extends MorphRelativesFinderTest {

	@Override
	protected CompiledCorpus makeCorpus(Class<StringSegmenter_IUMorpheme> segClass) throws Exception {
        CompiledCorpus corpus = new CompiledCorpus_InMemory(segClass.getName());
		return corpus;
	}
}
