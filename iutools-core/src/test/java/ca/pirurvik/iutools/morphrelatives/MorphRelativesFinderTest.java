package ca.pirurvik.iutools.morphrelatives;


import java.io.IOException;

import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.testing.AssertObject;
import ca.pirurvik.iutools.corpus.CompiledCorpus;

public abstract class MorphRelativesFinderTest {

	protected abstract MorphRelativesFinder makeFinder() throws Exception;

	protected MorphRelativesFinder makeFinder(String[] words) throws Exception {
		MorphRelativesFinder finder = makeFinder();
		for (String aWord: words) {
			finder.compiledCorpus.addWordOccurence(aWord);
		}

		return finder;
	}


	protected abstract CompiledCorpus makeCorpus(
		Class<? extends StringSegmenter> segClass) throws Exception;
	
	public CompiledCorpus makeCorpus(String[] words) throws Exception {
		CompiledCorpus corpus = makeCorpus(StringSegmenter_IUMorpheme.class);
		corpus.addWordOccurences(words);
		return corpus;
	}
	
	public CompiledCorpus makeCorpus() throws Exception {
		return makeCorpus(new String[0]);
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
        CompiledCorpus compiledCorpus = makeCorpus(); 
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
		MorphologicalRelative[] expansions = expander.findRelatives("nunavut");
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
        MorphologicalRelative[] expansions = reformulator.findRelatives("iglu");
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
        CompiledCorpus compiledCorpus = makeCorpus(words);
        MorphRelativesFinder reformulator = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] expansions = reformulator.findRelatives("iglumiutaq");
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
        CompiledCorpus compiledCorpus = makeCorpus(corpusWords);
        MorphRelativesFinder reformulator = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] expansions = reformulator.findRelatives("takujumaguvit");
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
        CompiledCorpus compiledCorpus = makeCorpus(corpusWords);
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
        MorphologicalRelative[] gotExpansions = expander.findRelatives("takujuq");
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
        CompiledCorpus compiledCorpus = makeCorpus(corpusWords);
        MorphRelativesFinder expander = new MorphRelativesFinder(compiledCorpus);
        
        String taqujuq = "ᑕᑯᔪᖅ";
        MorphologicalRelative[] gotExpansions = expander.findRelatives(taqujuq);
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
}
