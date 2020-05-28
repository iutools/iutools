package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.Trie_InMemory;

/**
 * Unit test for simple App.
 */
public class CompiledCorpus_IUMorphemeTest 
{
    /**
     * Rigorous Test :-)
     * @throws Exception 
     */
	@Test
	public void test__getMostFrequentCompletionForRootType() throws Exception {
		CompiledCorpus_IUMorpheme compiledCorpus = new CompiledCorpus_IUMorpheme();
		StringSegmenter segmenter = new StringSegmenter_IUMorpheme();
		Trie_InMemory trie = new Trie_InMemory();
		trie.add(segmenter.segment("inuit"),"inuit");
		trie.add(segmenter.segment("takujuq"),"takujuq");
		trie.add(segmenter.segment("igluit"),"igluit");
		trie.add(segmenter.segment("takulaaqtuq"),"takulaaqtuq");
		trie.add(segmenter.segment("isumajuq"),"isumajuq");
		trie.add(segmenter.segment("nanuit"),"nanuit");
		trie.add(segmenter.segment("iglumut"),"iglumut");
		compiledCorpus.trie = trie;
		
		String rootType;
		String[] mostFrequentCompletionSequence;
		String[] expected;
		
		rootType = "n";
		mostFrequentCompletionSequence = compiledCorpus.getMostFrequentCompletionForRootType(rootType);
		expected = new String[] {"{it/tn-nom-p}","\\"};
		assertArrayEquals("The returned most frequent completion is wrong.",expected,mostFrequentCompletionSequence);

		rootType = "v";
		mostFrequentCompletionSequence = compiledCorpus.getMostFrequentCompletionForRootType(rootType);
		expected = new String[] {"{juq/1vn}","\\"};
		assertArrayEquals("The returned most frequent completion is wrong.",expected,mostFrequentCompletionSequence);
}
	

}
