package ca.pirurvik.iutools.corpus;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;

import ca.nrc.datastructure.trie.AssertTrieNode;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.testing.AssertHelpers;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertCompiledCorpus extends Asserter<CompiledCorpus> {
	
	public AssertCompiledCorpus(
			CompiledCorpus _gotObject, String mess) {
		super(_gotObject, mess);
		ignoreFields.add("segmenter");
	}
	
	protected CompiledCorpus corpus() {
		return gotObject;
	}

	public AssertCompiledCorpus containsNWords(long expN) throws Exception {
		long gotN = corpus().getTrie().getTerminals().length;
		Assert.assertEquals(
			baseMessage+"\nNumber of words in compiled corpus was not as expected", 
			expN, gotN);
		return this;
	}

	public AssertCompiledCorpus containsWord(String word, String... morphemes) 
			throws Exception {
		String message = baseMessage+"\nNode for word '"+word+"' was not as expected";
		
		TrieNode gotNode = corpus().getTrie().getNode(morphemes);
		Assert.assertTrue(
				message+"\nNo node found for this sequence of morphemes.", 
				gotNode != null);
		
		TrieNode gotTerminalNode = gotNode.childTerminalNode();
		Assert.assertTrue(
				message+"\nNo terminal node found for this sequence of morphemes.", 
				gotTerminalNode != null);
		
		AssertTrieNode nodeAsserter = 
			new AssertTrieNode(gotTerminalNode, message);
		
		nodeAsserter.isNotNull();
		nodeAsserter.isTerminal().hasMostFrequentForm(word);
		
		return this;
	}

	public AssertCompiledCorpus charNgramFrequencyEquals(String ngram, long expFreq) {
		long gotFreq = corpus().charNgramFrequency(ngram);
		Assert.assertEquals(baseMessage+"\nFrequency of char ngram "+
		ngram+" was not as expected",  
		expFreq, gotFreq);
		
		return this;
	}

	public AssertCompiledCorpus morphemeNgramFreqEquals(
			long expFreq, String... ngram) throws Exception {
		long gotFreq = corpus().morphemeNgramFrequency(ngram);
		Assert.assertEquals(baseMessage+"\nFrequency of morpheme ngram "+
		ngram+" was not as expected",  
		expFreq, gotFreq);
		
		return this;
	}

	public AssertCompiledCorpus isEmpty() throws Exception {
		Assert.assertEquals(
			baseMessage+"\nNumber of terminals should have been 0.",
			0, corpus().getAllTerminals().length);
		return this;
	}

	public AssertCompiledCorpus doesNotContainWords(String... words) {
		for (String aWord: words) {
			Assert.assertFalse(
				baseMessage+"\nCorpus should not have contained word "+aWord, 
				corpus().containsWord(aWord));
		}
		return this;
	}
	
	public AssertCompiledCorpus containsWords(String... words) {
		for (String aWord: words) {
			Assert.assertTrue(
				baseMessage+"\nCorpus should have contained word "+aWord, 
				corpus().containsWord(aWord));
		}
		return this;
	}
	

	public AssertCompiledCorpus doesNotContainCharNgrams(String... ngrams) 
		throws Exception {
		for (String aNgram: ngrams) {
			Assert.assertFalse(
				baseMessage+"\nCorpus should not have contained ngram "+aNgram, 
				corpus().containsCharNgram(aNgram));
		}
		return this;
	}

	public AssertCompiledCorpus containsCharNgrams(String... ngrams) 
			throws Exception {
		for (String aNgram: ngrams) {
			Assert.assertTrue(
				baseMessage+"\nCorpus should  have contained ngram "+aNgram, 
				corpus().containsCharNgram(aNgram));
		}
		return this;
	}

	public AssertCompiledCorpus topSegmentationIs(
			String word, String expTopSegmentation) {
		String[] gotTopSegmentation = corpus().topSegmentation(word);
		
		return this;
	}

	public AssertCompiledCorpus charNgramFrequencyIs(
		String ngram, long expFreq) {
		long gotFreq = corpus().charNgramFrequency(ngram);
		Assert.assertEquals(
			baseMessage+"\nFrequency of char ngram '"+ngram+"' was not as expected", 
			expFreq, gotFreq);
		return this;
	}

	public void wordsAre(String... expWordsArr) throws Exception {
		Iterator<String> iter = corpus().allWords();
		Set<String> gotWords = new HashSet<String>();
		while (iter.hasNext()) {
			gotWords.add(iter.next());
		}
		Set<String> expWords = new HashSet<String>();
		for (String word: expWordsArr) {
			expWords.add(word);
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nThe corpus did not have the expected list of words", 
			expWords, gotWords);
	}
}
