package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.google.common.io.Files;

import ca.nrc.testing.AssertObject;

public class RW_CompiledCorpusTest {

	///////////////////////////////////
	// DOCUMENTATION TESTs
	///////////////////////////////////
	
	@Test
	public void test__RW_CompiledCorpus__Synopsis() throws Exception {
		
		// Say you have a CompiledCorpus object
		File tmpDir = Files.createTempDir(); tmpDir.deleteOnExit();
		CompiledCorpus_InFileSystem corpus = 
			new CompiledCorpus_InFileSystem(tmpDir);
		
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
	public void test__read_write__InMemory_Corpus() throws Exception {
		
		CompiledCorpus_Base origCorpus = new CompiledCorpus_InMemory();
		origCorpus.addWordOccurences(new String[] {"hello", "world"});
		
		File savePath = File.createTempFile("corpus", ".json");
		RW_CompiledCorpus.write(origCorpus, savePath);
		
		CompiledCorpus_Base readCorpus = 
			RW_CompiledCorpus
				.read(savePath, CompiledCorpus_InMemory.class);
		
		checkOrigAgainsRead(origCorpus, readCorpus);
	}

	@Test
	public void test__read_write__InFileSystem_Corpus() throws Exception {
		
		File corpusDir = Files.createTempDir();
		CompiledCorpus_InFileSystem origCorpus = 
			new CompiledCorpus_InFileSystem(corpusDir);
		origCorpus.addWordOccurences(new String[] {"hello", "world"});
		
		File savePath = Files.createTempDir();
		RW_CompiledCorpus.write(origCorpus, savePath);
		
		CompiledCorpus_Base readCorpus = 
			RW_CompiledCorpus
				.read(savePath, CompiledCorpus_InFileSystem.class);
		
		checkOrigAgainsRead(origCorpus, readCorpus);
	}

	
	///////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////
		
	private void checkOrigAgainsRead(
			CompiledCorpus_Base origCorpus, CompiledCorpus_Base readCorpus) 
			throws Exception {
			String corpName = "orig";
			for (CompiledCorpus_Base corpus: 
				new CompiledCorpus_Base[] {origCorpus, readCorpus}) {
				
				new AssertCompiledCorpus(corpus, 
						"The "+corpName+" corpus was not as expectee")
					.wordsAre("hello", "world");
				
				corpName = "read";
			}
		}
}
