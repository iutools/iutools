package ca.pirurvik.iutools.corpus;

import java.io.File;

/**
 * Given a JSON file corresponding to a CompiledCorpus_InMemory, 
 * this method will ensure that the word2info map has been 
 * computed and saved to the file 
 * 
 * @author desilets
 *
 */
public class AppGenerateWord2InfoMap {

	public static void main(String[] args) throws Exception {
		File jsonPath = new File(args[0]);
		new AppGenerateWord2InfoMap().run(jsonPath);
	}

	private void run(File jsonPath) throws Exception {
		CompiledCorpus_InMemory corpus = 
			(CompiledCorpus_InMemory) RW_CompiledCorpus.read(jsonPath, CompiledCorpus_InMemory.class);
		
		// Generate the word2info map
		//
		corpus.generateWord2infoMap();
				
		// Save the corpus to file
		RW_CompiledCorpus.write(corpus, jsonPath);
	}

}
