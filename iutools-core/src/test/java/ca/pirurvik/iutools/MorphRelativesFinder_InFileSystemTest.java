package ca.pirurvik.iutools;

import java.io.File;

import com.google.common.io.Files;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_v2FS;

public class MorphRelativesFinder_InFileSystemTest 
		extends MorphRelativesFinderTest {

	@Override
	protected CompiledCorpus makeCorpus(Class<? extends StringSegmenter> segClass) throws Exception {
		File corpDir = Files.createTempDir();
		corpDir.deleteOnExit();
		CompiledCorpus corpus = new CompiledCorpus_v2FS(corpDir);
		corpus.setSegmenterClassName(segClass);
		
		return corpus;
	}

}
