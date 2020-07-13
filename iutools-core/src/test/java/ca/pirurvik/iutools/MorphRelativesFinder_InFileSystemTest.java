package ca.pirurvik.iutools;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.google.common.io.Files;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;

public class MorphRelativesFinder_InFileSystemTest 
		extends MorphRelativesFinderTest {

	@Override
	protected CompiledCorpus makeCorpus(Class<? extends StringSegmenter> segClass) throws Exception {
		File corpDir = Files.createTempDir();
		corpDir.deleteOnExit();
		CompiledCorpus corpus = new CompiledCorpus_InFileSystem(corpDir);
		corpus.setSegmenterClassName(segClass);
		
		return corpus;
	}

}
