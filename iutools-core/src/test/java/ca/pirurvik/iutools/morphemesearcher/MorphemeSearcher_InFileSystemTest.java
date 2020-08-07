package ca.pirurvik.iutools.morphemesearcher;

import com.google.common.io.Files;

import ca.nrc.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_v2FS;

public class MorphemeSearcher_InFileSystemTest extends MorphemeSearcherTest {

	@Override
	protected CompiledCorpus makeCorpus() {
		CompiledCorpus corpus = new CompiledCorpus_v2FS(Files.createTempDir());
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class);
		return corpus;
	}
}
