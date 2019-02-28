package ca.inuktitutcomputing.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;

public class QueryExpanderTest {

	@Test
	public void test__QueryExpander__Synopsis() throws Exception {
		//
		// Use a Reformulator to return a number of candidates chosen in a
		// trie-compiled corpus related to a given word.
		//
        CompiledCorpus compiledCorpus = getACompiledCorpus(); 
        QueryExpander expander = new QueryExpander(compiledCorpus);
		QueryExpansion[] expansions = expander.getExpansions("some_inuktitut_word");
	}

	@Test
	public void test_getReformulations() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu",
				"takujuq", "takujumajunga"
		};
        CompiledCorpus compiledCorpus = getACompiledCorpus(words);        
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("iglu");
        String[] gotExpansions = new String[expansions.length];
        for (int i=0; i<expansions.length; i++)
        	gotExpansions[i] = expansions[i].word;
        String[] expected = new String[] {"iglumut","iglu","iglumik"};
        
        assertEquals("The number of reformulations returned is wrong.",3,gotExpansions.length);
        List<String> reformulationsList = Arrays.asList(gotExpansions);
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
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("iglumiutaq");
        String[] gotExpansions = new String[expansions.length];
        for (int i=0; i<expansions.length; i++)
        	gotExpansions[i] = expansions[i].word;
        String[] expected = new String[] {"iglumut","iglu","iglumik","iglumiutaq"};
        
        
        assertEquals("The number of reformulations returned is wrong.",4,gotExpansions.length);
        List<String> reformulationsList = Arrays.asList(gotExpansions);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}
	
	@Test
	public void test_getReformulations__Case_takujumaguvit() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus compiledCorpus = getACompiledCorpus(words);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("takujumaguvit");
        String[] gotExpansions = new String[expansions.length];
        for (int i=0; i<expansions.length; i++)
        	gotExpansions[i] = expansions[i].word;
        String[] expected = new String[] {"takujuq","takujumavalliajanginnik","takujumajunga"};
        
        
        assertEquals("The number of reformulations returned is wrong.",3,gotExpansions.length);
        List<String> reformulationsList = Arrays.asList(gotExpansions);
        for (String expectedRef : expected)
        	assertTrue("The word '"+expectedRef+"' should have been returned.",reformulationsList.contains(expectedRef));
	}
	
	@Test
	public void test_getNMostFrequentForms() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", "nunavummi", "nunavummut",
				"nunavummiutait", "nunavummit",
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"takujumalauqtuq", "takujumanngittuq", "takujaujuq",
				"takujaujumajuq","takujumajuq", "takujumajuq", "takujumajuq",
				"takujaujut",
				"takujumajunga", "takujumajunga",
				"iglumut",
				"tutsirautiit", "tussirautiit",
				"tutsiraut",
				"tuksiraut","tuksiraut",
				"tussiraut",  
				"tussiraummut", 
				"tutsiraummut", "tutsiraummut", "tutsiraummut", 
				"tuksiraummut", "tuksiraummut"
		};
		
		// tussiraummut : 1
		// tutsiraummut : 3
		// tuksiraummut : 2
		// tussiraut : 1
		// tutsiraut : 1
		// tuksiraut : 2
		// tussirautiit : 1
		// tutsirautiit : 1
		// 
		// attendu : tutsiraummut, tuksiraummut, tuksiraut, tussiraut, tutsiraut
        CompiledCorpus compiledCorpus = getACompiledCorpus(words);
        QueryExpander expander = new QueryExpander(compiledCorpus);
		
		// test n < number of terminals
        TrieNode tutsi = compiledCorpus.trie.getNode(new String[]{"{tuksiq/1v}"});
        ArrayList<QueryExpansion> mostFrequentTerminalsAL = expander.getNMostFrequentForms(tutsi,5,"tuksiraut",new ArrayList<QueryExpansion>());
        QueryExpansion[] mostFrequentTerminals = mostFrequentTerminalsAL.toArray(new QueryExpansion[] {});
        QueryExpansion[] expected = new QueryExpansion[] {
        		new QueryExpansion("tutsiraummut", null, 3),
        		new QueryExpansion("tuksiraut", null, 2),
        		new QueryExpansion("tuksiraummut", null, 2),
        		new QueryExpansion("tussiraut", null, 1),
        		new QueryExpansion("tutsiraut", null, 1)
        		};
        assertEquals("The number of terminals returned is not right.",5,mostFrequentTerminals.length);
        for (int i=0; i<expected.length; i++) {
        	assertEquals("The terminal at index "+i+" is not right.",expected[i].word,mostFrequentTerminals[i].word);
        	assertEquals("The frequency of the terminal at index "+i+" is not right.",expected[i].frequency,mostFrequentTerminals[i].frequency);
        }
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
