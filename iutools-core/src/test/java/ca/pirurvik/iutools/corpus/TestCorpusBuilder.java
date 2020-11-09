package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.dtrc.elasticsearch.StreamlinedClient;

public class TestCorpusBuilder {

    private static final String emptyCorpusName = "empty-corpus";

    public static CompiledCorpus makeEmptyCorpus()
        throws Exception {
        new StreamlinedClient(emptyCorpusName).deleteIndex();
        CompiledCorpus corpus =
            new CompiledCorpus_ES(emptyCorpusName)
            .setSegmenterClassName(StringSegmenter_IUMorpheme.class);
        ;
        return corpus;
    }
}
