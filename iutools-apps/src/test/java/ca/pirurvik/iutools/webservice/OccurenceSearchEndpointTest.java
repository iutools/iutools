package ca.pirurvik.iutools.webservice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.ui.web.testing.MockHttpServletResponse;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

public class OccurenceSearchEndpointTest {

	OccurenceSearchEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new OccurenceSearchEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__OccurenceSearchEndpoint__HappyPath() throws Exception {
		String[] corpusWords = new String[] {
			"ujaraqsiurnirmik", "aanniasiuqtiit", "iglumik", "tuktusiuqti"
		};
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
		File tempFile = File.createTempFile("compiled_corpus", ".json");
		compiledCorpus.addWordOccurences(corpusWords);
		RW_CompiledCorpus.write(compiledCorpus, tempFile);
		CompiledCorpusRegistry.registerCorpus("compiled_corpus", tempFile);

		OccurenceSearchInputs occurenceInputs = 
			new OccurenceSearchInputs("siuq","compiled_corpus","2");
		
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.MORPHEME,
					occurenceInputs
				);
		List<String> expListOfWords =
			Arrays.asList(new String[] {"ammuumajuqsiuqtutik","ittuqsiutitaaqpattut"});
		List<Double> expListOfScores =
			Arrays.asList(new Double[] {(double) 10004.0,(double) 10002});
		Map<String,MorphemeSearchResult> expected = new HashMap<String,MorphemeSearchResult>();
		expected.put("siuq/1nv", new MorphemeSearchResult(
				"searching, looking for s.t. (trans.: of, for, about s.o.); travelling through space or time (spend); feasting, celebrating",
				expListOfWords,expListOfScores));
		IUTServiceTestHelpers.assertOccurenceSearchResponseIsOK(response, expected);
	}
	
    public String createTemporaryCorpusDirectory(String[] stringOfWords) throws IOException {
       	Logger logger = Logger.getLogger("CompiledCorpusTest.createTemporaryCorpusDirectory");
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
