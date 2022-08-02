package org.iutools.corpus;

import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;

public class RW_CompiledCorpusTest {

	///////////////////////////////////
	// DOCUMENTATION TESTs
	///////////////////////////////////
	
//	@Test
//	public void test__RW_CompiledCorpus__Synopsis() throws Exception {
//
//		// Say you have a CompiledCorpus_ES object
//		File tmpDir = Files.createTempDir(); tmpDir.deleteOnExit();
//		CompiledCorpus_ES corpus =
//			new CompiledCorpus_ES(CorpusTestHelpers.ES_TEST_INDEX);
//
//		// You can save it to disk using a RW object
//		//
//		File savePath = Files.createTempDir();
//		RW_CompiledCorpus.write(corpus, savePath);
//
//		// You can also use a RW object to read the file
//		// back into a CompiledCorpus_ES instance
//		//
//		RW_CompiledCorpus.read(savePath, corpus.getClass());
//	}

	///////////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////////
	
//	@Test
//	public void test__read_write__ES_Corpus() throws Exception {
//
//		CompiledCorpus_ES origCorpus = new CompiledCorpus_ES();
//		origCorpus.addWordOccurences(new String[] {"hello", "world"});
//
//		File savePath = File.createTempFile("corpus", ".json");
//		RW_CompiledCorpus.write(origCorpus, savePath);
//
//		CompiledCorpus_ES readCorpus =
//			RW_CompiledCorpus
//				.read(savePath, CompiledCorpus_InMemory.class);
//
//		checkOrigAgainsRead(origCorpus, readCorpus);
//	}


	///////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////
		
	private void checkOrigAgainsRead(
	CompiledCorpus_ES origCorpus, CompiledCorpus_ES readCorpus)
			throws Exception {
			String corpName = "orig";
			for (CompiledCorpus_ES corpus:
				new CompiledCorpus_ES[] {origCorpus, readCorpus}) {
				
				new AssertCompiledCorpus(corpus, 
						"The "+corpName+" corpus was not as expectee")
					.wordsAre("hello", "world");
				
				corpName = "read";
			}
		}
}
