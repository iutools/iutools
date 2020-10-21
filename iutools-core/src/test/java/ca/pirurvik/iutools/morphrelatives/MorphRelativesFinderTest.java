package ca.pirurvik.iutools.morphrelatives;


import java.io.IOException;

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

	/**********************************
	 * DOCUMENTATION TESTS
	 **********************************/
	
	@Test
	public void test__MorphRelativesFinder__Synopsis() throws Exception {
		//
		// Given an Inuktut word, a MorphRelativesFinder can find a list of words that are
		// semantically close to this input, and are very frequent in a given corpus.
		//
        MorphRelativesFinder finder = makeFinder();
		MorphologicalRelative[] expansions = finder.findRelatives("nunavut");
	}

	/**********************************
	 * VERIFICATION TESTS
	 **********************************/
	
	@Test
	public void test__findRelatives__HappyPath() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu",
				"takujuq", "takujumajunga"
		};

		MorphRelativesFinder finder = makeFinder(words);

        MorphologicalRelative[] expansions = finder.findRelatives("iglu");
        String[] expected = new String[] {"iglumut", "iglumik"};
        
        new AssertMorphologicalRelativeArray(expansions, "")
        	.wordsAre(expected);
	}


	@Test
	public void test__findRelatives__Case_with_stepping_back_one_node() throws Exception {
		String[] words = new String[] {
				"nuna", "nunait", 
				"iglu", "iglumut", "iglumut", "iglumut", "iglumik", "iglu", 
				"iglumiutaq", "takujuq", "takujumajunga"
		};
		MorphRelativesFinder finder = makeFinder(words);

        MorphologicalRelative[] expansions = finder.findRelatives("iglumiutaq");
        String[] expected = new String[] {
        	"iglumut","iglu","iglumik"};
        new AssertMorphologicalRelativeArray(expansions, "")
        	.wordsAre(expected);
	}
	
	@Test
	public void test__findRelatives__Case_takujumaguvit() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
		MorphRelativesFinder finder = makeFinder(corpusWords);

        MorphologicalRelative[] expansions = finder.findRelatives("takujumaguvit");
        String[] expected = new String[] {
        	"takujumajunga","takujumavalliajanginnik","takujuq",};
        
        new AssertMorphologicalRelativeArray(expansions, "")
    		.wordsAre(expected);
	}
	
	@Test
	public void test__findRelatives__LatinInput__ReturnsLatin() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
		MorphRelativesFinder finder = makeFinder(corpusWords);

        MorphologicalRelative[] gotRelatives = finder.findRelatives("takujuq");
		String[] expExpansions = new String[] {
			"takujumajunga", "takujumavalliajanginnik"};
		assertExpansionsAre(expExpansions, gotRelatives);
	}
	

	@Test
	public void test__findRelatives__SyllabicInput__ReturnsSyllabic() throws Exception {
		String[] corpusWords = new String[] {
				"nuna", "nunait", 
				"takujuq", "takujumajunga", "takujumavalliajanginnik",
				"iglumut"
		};
		MorphRelativesFinder finder = makeFinder(corpusWords);

        String taqujuq = "ᑕᑯᔪᖅ";
        MorphologicalRelative[] gotRelatives = finder.findRelatives(taqujuq);
		String[] expExpansions = new String[] {"ᑕᑯᔪᖅ", "ᑕᑯᔪᒪᔪᖓ", "ᑕᑯᔪᒪᕙᓪᓕᐊᔭᖏᓐᓂᒃ"};
		assertExpansionsAre(expExpansions, gotRelatives);
	}

	/**********************************
	 * HELPER METHODS
	 **********************************/

	protected static void assertExpansionsAre(String[] expExpansions, MorphologicalRelative[] gotExpansionObjs) throws IOException {
        String[] gotRelatives = new String[gotExpansionObjs.length];
        for (int i=0; i<gotExpansionObjs.length; i++)
        	gotRelatives[i] = gotExpansionObjs[i].getWord();
        
        AssertObject.assertDeepEquals("", expExpansions, gotRelatives);
		
	}
}
