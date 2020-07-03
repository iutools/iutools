package ca.pirurvik.iutools;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import junit.framework.Assert;

// TODO-June2020: Create two versions of this: 
//  - one that uses an _InMemory corpus
//  - one that uses a _InFileSystem
//
public class QueryExpanderTest {
	
	@Test
	public void test_DELETE_ME() {
		Set<QueryExpansion> expansions = new HashSet<QueryExpansion>();
		QueryExpansion exp1 = new QueryExpansion("hello", null, 100);
		QueryExpansion exp2 = new QueryExpansion("hello", null, 100);
		for (QueryExpansion exp: new QueryExpansion[] {exp1, exp2}) {
			expansions.add(exp);
		}
		Assert.assertEquals("Set should have had only one element", 1, expansions.size());
	}
	
	public CompiledCorpus makeCorpus(String[] words) throws Exception {
		CompiledCorpus corpus = new CompiledCorpus_InMemory();
		corpus.addWordOccurences(words);
		return corpus;
	}

	/**********************************
	 * DOCUMENTATION TESTS
	 **********************************/
	
	@Test
	public void test__QueryExpander__Synopsis() throws Exception {
		//
		// Given an Inuktut word, a QueryExpander can find a list of words that are 
		// semantically close to this input, and are very frequent in a given corpus.
		//
        CompiledCorpus_InMemory compiledCorpus = getACompiledCorpus(); 
        QueryExpander expander = new QueryExpander(compiledCorpus);
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
		CompiledCorpus compiledCorpus = makeCorpus(words);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("iglu");
        String[] expected = new String[] {"iglumut", "iglumik"};
        
        new AssertQueryExpansionArray(expansions, "")
        	.wordsAre(expected);
	}


	@Test
	public void test__getExpansions__Case_with_stepping_back_one_node() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu", 
				"iglumiutaq", "takujuq", "takujumajunga"
		};
        CompiledCorpus_InMemory compiledCorpus = compileCorpusFromWords(words);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("iglumiutaq");
        String[] expected = new String[] {
        	"iglumut","iglu","iglumik"};
        new AssertQueryExpansionArray(expansions, "")
        	.wordsAre(expected);
	}
	
	@Test
	public void test__getExpansions__Case_takujumaguvit() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus_InMemory compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander reformulator = new QueryExpander(compiledCorpus);
        QueryExpansion[] expansions = reformulator.getExpansions("takujumaguvit");
        String[] expected = new String[] {
        	"takujumajunga","takujumavalliajanginnik","takujuq",};
        
        new AssertQueryExpansionArray(expansions, "")
    		.wordsAre(expected);
	}
	
	@Test
	public void test__getExpansions__LatinInput__ReturnsLatin() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus_InMemory compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander expander = new QueryExpander(compiledCorpus);
        QueryExpansion[] gotExpansions = expander.getExpansions("takujuq");
		String[] expExpansions = new String[] {
			"takujumajunga", "takujumavalliajanginnik"};
		assertExpansionsAre(expExpansions, gotExpansions);		
	}
	

	@Test
	public void test__getExpansions__SyllabicInput__ReturnsSyllabic() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
        CompiledCorpus_InMemory compiledCorpus = compileCorpusFromWords(corpusWords);
        QueryExpander expander = new QueryExpander(compiledCorpus);
        
        String taqujuq = "ᑕᑯᔪᖅ";
        QueryExpansion[] gotExpansions = expander.getExpansions(taqujuq);
		String[] expExpansions = new String[] {"ᑕᑯᔪᖅ", "ᑕᑯᔪᒪᔪᖓ", "ᑕᑯᔪᒪᕙᓪᓕᐊᔭᖏᓐᓂᒃ"};
		assertExpansionsAre(expExpansions, gotExpansions);		
	}
	
	/**********************************
	 * HELPER METHODS
	 **********************************/

	private void assertExpansionsAre(String[] expExpansions, QueryExpansion[] gotExpansionObjs) throws IOException {
        String[] gotExpansions = new String[gotExpansionObjs.length];
        for (int i=0; i<gotExpansionObjs.length; i++)
        	gotExpansions[i] = gotExpansionObjs[i].getWord();
        
        AssertObject.assertDeepEquals("", expExpansions, gotExpansions);
		
	}

	
	private CompiledCorpus_InMemory getACompiledCorpus() throws Exception {
		return compileCorpusFromWords(new String[] {"nunavut"});
	}
	
	public CompiledCorpus_InMemory compileCorpusFromWords(String[] words) throws Exception {
        CompiledCorpus_InMemory compiledCorpus = new CompiledCorpus_InMemory(StringSegmenter_IUMorpheme.class.getName());
        compiledCorpus.addWordOccurences(words);
        return compiledCorpus;
	}
}
