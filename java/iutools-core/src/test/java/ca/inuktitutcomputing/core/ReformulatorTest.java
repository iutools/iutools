package ca.inuktitutcomputing.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;

public class ReformulatorTest {

	@Test
	public void test__Reformulator__Synopsis() throws Exception {
		File tmp = File.createTempFile("", "");
		tmp.deleteOnExit();
		tmp.mkdir();
		//
		// Use a Reformulator to return a number of candidates chosen in a
		// trie-compiled corpus related to a given word.
		//
        CompiledCorpus compiledCorpus = getACompiledCorpus(); 
        Reformulator reformulator = new Reformulator(compiledCorpus);
		String[] reformulations = reformulator.getReformulations("some_inuktitut_word");
	}

	@Test
	public void test_getReformulations() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu",
				"takujuq", "takujumajunga"
		};
        CompiledCorpus compiledCorpus = getACompiledCorpus(words);        
        Reformulator reformulator = new Reformulator(compiledCorpus);
        String[] reformulations = reformulator.getReformulations("iglu");
        String[] expected = new String[] {"iglumut","iglu","iglumik"};
        
        assertEquals("The number of reformulations returned is wrong.",3,reformulations.length);
        List<String> reformulationsList = Arrays.asList(reformulations);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}

	@Test
	public void test_getReformulations__Case_with_stepping_back_one_node() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu", "iglumiutaq",
				"takujuq", "takujumajunga"
		};
        CompiledCorpus compiledCorpus = getACompiledCorpus(words);
        Reformulator reformulator = new Reformulator(compiledCorpus);
        String[] reformulations = reformulator.getReformulations("iglumiutaq");
        String[] expected = new String[] {"iglumut","iglu","iglumik","iglumiutaq"};
        
        
        assertEquals("The number of reformulations returned is wrong.",4,reformulations.length);
        List<String> reformulationsList = Arrays.asList(reformulations);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}

	// ---------------
	
	private CompiledCorpus getACompiledCorpus() throws Exception {
		return getACompiledCorpus(new String[] {"nunavut"});
	}
	
	private CompiledCorpus getACompiledCorpus(String[] words) throws Exception {
		File dir = new File(IUConfig.getIUDataPath()+"src/test/temp");
		dir.mkdir();
		String corpusDir = dir.getAbsolutePath();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(corpusDir+"/corpusText.txt")));
		bw.write(String.join(" ", words));
		bw.close();
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.compileCorpusFromScratch(corpusDir);
        FileUtils.deleteDirectory(dir);
        return compiledCorpus;
	}

}
