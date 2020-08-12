package ca.pirurvik.iutools.corpus;

import java.io.File;

import org.junit.Test;

import com.google.common.io.Files;

public class RW_CompiledCorpusTest {

	///////////////////////////////////
	// DOCUMENTATION TESTs
	///////////////////////////////////
	
	@Test
	public void test__RW_CompiledCorpus__Synopsis() throws Exception {
		
		// Say you have a CompiledCorpus object
		File tmpDir = Files.createTempDir(); tmpDir.deleteOnExit();
		CompiledCorpus_v2FS corpus =
			new CompiledCorpus_v2FS(tmpDir);
		
		// You can save it to disk using a RW object 
		//
		File savePath = Files.createTempDir();
		RW_CompiledCorpus.write(corpus, savePath);
		
		// You can also use a DAO object to read the file 
		// back into a CompiledCorpus instance
		//
		RW_CompiledCorpus.read(savePath, corpus.getClass());
	}

	///////////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////////
	
	@Test
	public void test__read_write__v1_Corpus() throws Exception {
		
		CompiledCorpus origCorpus = new CompiledCorpus_InMemory();
		origCorpus.addWordOccurences(new String[] {"hello", "world"});
		
		File savePath = File.createTempFile("corpus", ".json");
		RW_CompiledCorpus.write(origCorpus, savePath);
		
		CompiledCorpus readCorpus = 
			RW_CompiledCorpus
				.read(savePath, CompiledCorpus_InMemory.class);
		
		checkOrigAgainsRead(origCorpus, readCorpus);
	}

	@Test
	public void test__read_write__v2FS_Corpus() throws Exception {
		
		File corpusDir = Files.createTempDir();
		CompiledCorpus_v2FS origCorpus =
			new CompiledCorpus_v2FS(corpusDir);
		origCorpus.addWordOccurences(new String[] {"hello", "world"});
		
		File savePath = Files.createTempDir();
		RW_CompiledCorpus.write(origCorpus, savePath);
		
		CompiledCorpus readCorpus = 
			RW_CompiledCorpus
				.read(savePath, CompiledCorpus_v2FS.class);
		
		checkOrigAgainsRead(origCorpus, readCorpus);
	}

	@Test
	public void test__read_write__v2Mem_Corpus() throws Exception {

		File corpusDir = Files.createTempDir();
		CompiledCorpus_v2Mem origCorpus =
				new CompiledCorpus_v2Mem(corpusDir);
		origCorpus.addWordOccurences(new String[] {"hello", "world"});

		File savePath = Files.createTempDir();
		RW_CompiledCorpus.write(origCorpus, savePath);

		CompiledCorpus readCorpus =
				RW_CompiledCorpus
						.read(savePath, CompiledCorpus_v2Mem.class);

		checkOrigAgainsRead(origCorpus, readCorpus);
	}

	///////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////
		
	private void checkOrigAgainsRead(
			CompiledCorpus origCorpus, CompiledCorpus readCorpus) 
			throws Exception {
			String corpName = "orig";
			for (CompiledCorpus corpus: 
				new CompiledCorpus[] {origCorpus, readCorpus}) {
				
				new AssertCompiledCorpus(corpus, 
						"The "+corpName+" corpus was not as expectee")
					.wordsAre("hello", "world");
				
				corpName = "read";
			}
		}
}
