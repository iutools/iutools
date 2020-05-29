package ca.pirurvik.iutools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.TrieNode_InMemory;
import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;
import ca.pirurvik.iutools.corpus.CompiledCorpus;

public class QueryExpanderTest {

	/**********************************
	 * DOCUMENTATION TESTS
	 **********************************/
	
	@Test
	public void test__QueryExpander__Synopsis() throws Exception {
		//
		// Given an Inuktut word, a QueryExpander can find a list of words that are 
		// semantically close to this input, and are very frequent in a given corpus.
		//
        CompiledCorpus compiledCorpus = getACompiledCorpus(); 
        QueryExpander expander = new QueryExpander(compiledCorpus);
        expander.setVerbose(false);
		QueryExpansion[] expansions = expander.getExpansions("nunavut");
	}

	/**********************************
	 * VERIFICATION TESTS
	 **********************************/

	
	@Test
	public void test__getExpansions__HappyPath() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu",
				"takujuq", "takujumajunga"
		};
        CompiledCorpus compiledCorpus = compileCorpusFromWords(words);        
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        reformulator.setVerbose(false);
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
	public void test__getExpansions__Case_with_stepping_back_one_node() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu", "iglumiutaq",
				"takujuq", "takujumajunga"
		};
        CompiledCorpus compiledCorpus = compileCorpusFromWords(words);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        reformulator.setVerbose(false);
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
	public void test__getExpansions__Case_takujumaguvit() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        reformulator.setVerbose(false);
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
	public void test__getExpansions__LatinInput__ReturnsLatin() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander expander = new QueryExpander(compiledCorpus);
        expander.setVerbose(false);
        QueryExpansion[] gotExpansions = expander.getExpansions("takujuq");
		String[] expExpansions = new String[] {"takujuq", "takujumajunga", "takujumavalliajanginnik"};
		assertExpansionsAre(expExpansions, gotExpansions);		
	}
	

	@Test
	public void test__getExpansions__SyllabicInput__ReturnsSyllabic() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander expander = new QueryExpander(compiledCorpus);
        expander.setVerbose(false);
        
        String taqujuq = "ᑕᑯᔪᖅ";
        QueryExpansion[] gotExpansions = expander.getExpansions(taqujuq);
		String[] expExpansions = new String[] {"ᑕᑯᔪᖅ", "ᑕᑯᔪᒪᔪᖓ", "ᑕᑯᔪᒪᕙᓪᓕᐊᔭᖏᓐᓂᒃ"};
		assertExpansionsAre(expExpansions, gotExpansions);		
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
        CompiledCorpus compiledCorpus = compileCorpusFromWords(words);
        QueryExpander expander = new QueryExpander(compiledCorpus);        
        expander.setVerbose(false);

		
		// test n < number of terminals
        TrieNode_InMemory tutsi = compiledCorpus.trie.getNode(new String[]{"{tuksiq/1v}"});
        List<QueryExpansion> mostFrequentTerminalsAL = expander.getNMostFrequentForms(tutsi,5,"tuksiraut",new ArrayList<QueryExpansion>());
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
	
	

	/**********************************
	 * HELPER METHODS
	 **********************************/

	private void assertExpansionsAre(String[] expExpansions, QueryExpansion[] gotExpansionObjs) throws IOException {
        String[] gotExpansions = new String[gotExpansionObjs.length];
        for (int i=0; i<gotExpansionObjs.length; i++)
        	gotExpansions[i] = gotExpansionObjs[i].word;
        
        AssertHelpers.assertDeepEquals("", expExpansions, gotExpansions);
		
	}

	
	private CompiledCorpus getACompiledCorpus() throws Exception {
		return compileCorpusFromWords(new String[] {"nunavut"});
	}
	
	public CompiledCorpus compileCorpusFromWords(String[] words) throws Exception {
		File dir = Files.createTempDirectory("").toFile();
		dir.deleteOnExit();
		String corpusDir = dir.getAbsolutePath();
		BufferedWriter bw = new BufferedWriter(new FileWriter(
				new File(corpusDir+"/corpusText.txt")));
		bw.write(String.join(" ", words));
		bw.close();
        CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.setVerbose(false);
        compiledCorpus.compileCorpusFromScratch(corpusDir);
        return compiledCorpus;
	}

}
