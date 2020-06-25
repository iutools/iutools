package ca.pirurvik.iutools.morphemesearcher;

import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.io.Files;

import ca.nrc.datastructure.trie.MockStringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;

public class MorphemeSearcher_InFileSystemTest extends MorphemeSearcherTest {

	@Override
	protected CompiledCorpus makeCorpus() {
		CompiledCorpus corpus = new CompiledCorpus_InFileSystem(Files.createTempDir());
		corpus.setSegmenterClassName(MockStringSegmenter_IUMorpheme.class);
		return corpus;
	}
}
