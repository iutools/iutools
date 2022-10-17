package org.iutools.bin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.TestCorpusBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import org.iutools.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertObject;

public class FreqVerbRootsTest {

	@Test
	public void test__compileFreqs__HappyPath() throws Exception {
		String[] corpusWords = new String[] {
				"nunami", "takujuq", "iglumik", "siniktuq", "takujaujuq", "angijuq"
				};

	  CompiledCorpus compiledCorpus = TestCorpusBuilder.makeEmptyCorpus();
		new CompiledCorpusRegistry().makeCorpus("test-corpus")
			.setSegmenterClassName(StringSegmenter_IUMorpheme.class);
	  compiledCorpus.addWordOccurences(corpusWords);
        
		FreqVerbRootsCompiler freqVerbRootsCompiler = new FreqVerbRootsCompiler();
		HashMap<String,Long> freqsVerbRoots = freqVerbRootsCompiler.compileFreqs(compiledCorpus);
		
		Map<String,Long> expVerbRoots = new HashMap<String,Long>();
		{
			expVerbRoots.put("taku/1v", new Long(2));
			expVerbRoots.put("sinik/1v", new Long(1));
			expVerbRoots.put("angi/1v", new Long(1));
		}

		AssertObject.assertDeepEquals("", expVerbRoots, freqsVerbRoots);
	}
	
	// -----------------------------------------------------------------

    private String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
    	Logger logger = LogManager.getLogger("CompiledCorpusTest.createTemporaryCorpusDirectory");
        File corpusDirectory = Files.createTempDirectory("").toFile();
        corpusDirectory.deleteOnExit();
        String corpusDirPath = corpusDirectory.getAbsolutePath();
        for (int i=0; i<stringOfWords.length; i++) {
        	File wordFile = new File(corpusDirPath+"/contents"+(i+1)+".txt");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(wordFile));
        	bw.write(stringOfWords[i]);
        	bw.close();
        	logger.debug("wordFile= "+wordFile.getAbsolutePath());
        	logger.debug("contents= "+wordFile.length());
        }
        return corpusDirPath;
	}
}
