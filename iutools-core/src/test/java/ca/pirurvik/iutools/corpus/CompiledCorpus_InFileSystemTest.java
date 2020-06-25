package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import com.google.common.io.Files;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertObject;

public class CompiledCorpus_InFileSystemTest extends CompiledCorpusTest {

	@Override
	protected CompiledCorpus makeCorpusUnderTest(
			Class<? extends StringSegmenter> segmenterClass) {
		File rootDir = Files.createTempDir();
		CompiledCorpus corpus = new CompiledCorpus_InFileSystem(rootDir);
		corpus.setSegmenterClassName(segmenterClass.getName());
		return corpus;
	}
	
	@Test
	public void test__morphemesWithCanonicalForm__HappyPath() throws Exception {
		CompiledCorpus_InFileSystem corpus = 
			(CompiledCorpus_InFileSystem) 
			makeCorpusUnderTest(StringSegmenter_IUMorpheme.class);
		String[] words = new String[] {"inuk", "iglu"};
		corpus.addWordOccurences(words);
		
		Set<String> gotMorphemes = corpus.morphemesWithCanonicalForm("inuk");
		String[] expMorphemes = new String[] {"{inuk/1n}"};
		AssertObject.assertDeepEquals(
			"Morpheme IDs were not as expected for canonical form inuk", 
			expMorphemes, gotMorphemes);
	}
	
}
