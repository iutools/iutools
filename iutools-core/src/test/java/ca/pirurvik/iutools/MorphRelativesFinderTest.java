package ca.pirurvik.iutools;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.MorphRelativesFinder;
import ca.pirurvik.iutools.MorphologicalRelative;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import junit.framework.Assert;

// TODO-June2020: Create two versions of this: 
//  - one that uses an _InMemory corpus
//  - one that uses a _InFileSystem
//
public class MorphRelativesFinderTest {
	
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
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
		MorphologicalRelative[] expansions = expander.getRelatives("nunavut");
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
        MorphRelativesFinder reformulator = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] expansions = reformulator.getRelatives("iglu");
        String[] expected = new String[] {"iglumut", "iglumik"};
        
        new AssertMorphologicalRelativeArray(expansions, "")
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
        MorphRelativesFinder reformulator = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] expansions = reformulator.getRelatives("iglumiutaq");
        String[] expected = new String[] {
        	"iglumut","iglu","iglumik"};
        new AssertMorphologicalRelativeArray(expansions, "")
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
        MorphRelativesFinder reformulator = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] expansions = reformulator.getRelatives("takujumaguvit");
        String[] expected = new String[] {
        	"takujumajunga","takujumavalliajanginnik","takujuq",};
        
        new AssertMorphologicalRelativeArray(expansions, "")
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
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] gotExpansions = expander.getRelatives("takujuq");
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
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
        
        String taqujuq = "ᑕᑯᔪᖅ";
        MorphologicalRelative[] gotExpansions = expander.getRelatives(taqujuq);
		String[] expExpansions = new String[] {"ᑕᑯᔪᖅ", "ᑕᑯᔪᒪᔪᖓ", "ᑕᑯᔪᒪᕙᓪᓕᐊᔭᖏᓐᓂᒃ"};
		assertExpansionsAre(expExpansions, gotExpansions);		
	}
	
	/**********************************
	 * HELPER METHODS
	 **********************************/

	private void assertExpansionsAre(String[] expExpansions, MorphologicalRelative[] gotExpansionObjs) throws IOException {
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
