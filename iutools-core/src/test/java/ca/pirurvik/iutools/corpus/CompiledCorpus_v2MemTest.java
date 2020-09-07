package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.StringSegmenter;
import com.google.common.io.Files;

import java.io.File;

public class CompiledCorpus_v2MemTest extends CompiledCorpusTest {

    @Override
    protected CompiledCorpus makeCorpusWithDefaultSegmenter() throws Exception {
        File rootDir = Files.createTempDir();
        CompiledCorpus corpus = new CompiledCorpus_v2Mem(rootDir);
        return corpus;
    }
}
