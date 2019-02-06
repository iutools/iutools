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
import org.junit.jupiter.api.Test;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;

class ReformulatorTest {

	@Test
	void test_getReformulations() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu",
				"takujuq", "takujumajunga"
		};
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
        
        Reformulator reformulator = new Reformulator(compiledCorpus);
        String[] reformulations = reformulator.getReformulations("iglu");
        String[] expected = new String[] {"iglumut","iglu","iglumik"};
        
        assertEquals("The number of reformulations returned is wrong.",3,reformulations.length);
        List<String> reformulationsList = Arrays.asList(reformulations);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}

	@Test
	void test_getReformulations__Case_with_stepping_back_one_node() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu", "iglumiutaq",
				"takujuq", "takujumajunga"
		};
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
        
        Reformulator reformulator = new Reformulator(compiledCorpus);
        String[] reformulations = reformulator.getReformulations("iglumiutaq");
        String[] expected = new String[] {"iglumut","iglu","iglumik","iglumiutaq"};
        
        
        assertEquals("The number of reformulations returned is wrong.",4,reformulations.length);
        List<String> reformulationsList = Arrays.asList(reformulations);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}

}
