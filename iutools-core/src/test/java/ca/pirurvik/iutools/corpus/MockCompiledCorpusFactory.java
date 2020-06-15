package ca.pirurvik.iutools.corpus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

public class MockCompiledCorpusFactory {

	public static MockCompiledCorpus makeSmallCorpus() 
			throws CompiledCorpusException {
		HashMap<String,String> mockDecomps = new HashMap<String,String>();
		mockDecomps.put("inuit", "{inuk/1n} {it/tn-nom-p}");
		mockDecomps.put("nunami", "{nuna/1n} {mi/tn-loc-s}");
		mockDecomps.put("iglumik", "{iglu/1n} {mik/tn-acc-s}");
		mockDecomps.put("inuglu", "{inuk/1n} {lu/1q}");
		
		MockCompiledCorpus mockCorpus;
		try {
			mockCorpus = new MockCompiledCorpus();
		} catch (CompiledCorpusException e1) {
			throw new CompiledCorpusException(e1);
		}
		mockCorpus.setVerbose(false);
		
		// The MockCompiledCorpus's segmenter will use this dictionary instead 
		// of calling the morphological analyzer.
		mockCorpus.setDictionary(mockDecomps);
		mockCorpus.addWordOccurences(mockDecomps.keySet());
		
		return mockCorpus;
	}
	
    private static String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
        File corpusDirectory = Files.createTempDirectory("").toFile();
        corpusDirectory.deleteOnExit();
        String corpusDirPath = corpusDirectory.getAbsolutePath();
        for (int i=0; i<stringOfWords.length; i++) {
        	File wordFile = new File(corpusDirPath+"/contents"+(i+1)+".txt");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(wordFile));
        	bw.write(stringOfWords[i]);
        	bw.close();
        }
        return corpusDirPath;
	}
	
}
