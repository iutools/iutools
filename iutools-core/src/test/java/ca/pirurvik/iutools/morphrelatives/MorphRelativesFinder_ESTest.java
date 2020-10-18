package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.CompiledCorpus_ES;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import org.junit.Ignore;

@Ignore
public class MorphRelativesFinder_ESTest extends MorphRelativesFinderTest {

    @Override
    protected MorphRelativesFinder makeFinder() throws Exception {
        CompiledCorpus corpus = new CompiledCorpus_ES("hansard-1999-2002.v2020-10-06");
        corpus.setSegmenterClassName(StringSegmenter_IUMorpheme.class.getName());
        MorphRelativesFinder_ES finder = new MorphRelativesFinder_ES(corpus);

        return finder;
    }
}
