package ca.pirurvik.iutools.morphrelatives;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.*;

public class MorphRelativesFinder_ESTest extends MorphRelativesFinderTest {

    @Override
    protected MorphRelativesFinder makeFinder() throws Exception {
        CorpusTestHelpers.clearESTestIndex();
        CompiledCorpus corpus = new CompiledCorpus_ES(CorpusTestHelpers.ES_TEST_INDEX);
        corpus.setSegmenterClassName(StringSegmenter_IUMorpheme.class.getName());
        MorphRelativesFinder_ES finder = new MorphRelativesFinder_ES(corpus);

        return finder;
    }
}
