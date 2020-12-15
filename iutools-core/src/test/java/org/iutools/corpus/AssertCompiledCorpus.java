package org.iutools.corpus;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.iutools.morph.Decomposition;
import static ca.inuktitutcomputing.data.Morpheme.MorphFormat;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;

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
		long gotN = corpus().totalWords();
		Assert.assertEquals(
			baseMessage+"\nNumber of words in compiled corpus was not as expected", 
			expN, gotN);
		return this;
	}

	public AssertCompiledCorpus containsWord(String word, String... morphemes) 
			throws Exception {
		String message = baseMessage+"\nNode for word '"+word+"' was not as expected";
		
		WordInfo gotWinfo = corpus().info4word(word);
		Assert.assertTrue(
				message+"\nNo word found for this sequence of morphemes.", 
				gotWinfo != null);
		
		return this;
	}

	public AssertCompiledCorpus morphemeNgramFreqEquals(
			long expFreq, String... ngram) throws Exception {
		long gotFreq = corpus().morphemeNgramFrequency(ngram);
		Assert.assertEquals(
		baseMessage+"\nFrequency of morpheme ngram "+
		String.join(",", ngram)+" was not as expected",  
		expFreq, gotFreq);
		
		return this;
	}

	public AssertCompiledCorpus isEmpty() throws Exception {
		Assert.assertEquals(
			baseMessage+"\nNumber of terminals should have been 0.",
			0, corpus().totalOccurences());
		return this;
	}

	public AssertCompiledCorpus doesNotContainWords(String... words) 
		throws Exception {
		for (String aWord: words) {
			Assert.assertFalse(
				baseMessage+"\nCorpus should not have contained word "+aWord, 
				corpus().containsWord(aWord));
		}
		return this;
	}
	
	public AssertCompiledCorpus containsWords(String... words) 
		throws Exception {
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

	public AssertCompiledCorpus bestDecompositionIs(
			String word, String expTopSegmentation) throws Exception {
		String[] gotTopSegmentation = corpus().bestDecomposition(word);
		
		return this;
	}

	public AssertCompiledCorpus charNgramFrequencyIs(
		String ngram, long expFreq) throws Exception {
		long gotFreq = corpus().totalWordsWithCharNgram(ngram);
		Assert.assertEquals(
			baseMessage+"\nFrequency of char ngram '"+ngram+"' was not as expected", 
			expFreq, gotFreq);
		return this;
	}

	public AssertCompiledCorpus wordsAre(String... expWordsArr) throws Exception {
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
		
		return this;
	}
	
	public AssertCompiledCorpus wordsContainingMorphemeAre(
		String morpheme, Triple<String,String,String>... expWordsArr) throws Exception {
		Set<Triple<String,String,String>> expWords = new HashSet<Triple<String,String,String>>();
		Collections.addAll(expWords, expWordsArr);
		List<WordWithMorpheme> gotWordWithMorph =
			corpus().wordsContainingMorpheme(morpheme);
		Set<Triple<String,String,String>> gotWords = new HashSet<Triple<String,String,String>>();
		for (WordWithMorpheme wrdWithMorph: gotWordWithMorph) {
			gotWords.add(
				Triple.of(
					wrdWithMorph.word, wrdWithMorph.morphemeId,
						Decomposition.formatDecompStr(
							wrdWithMorph.decomposition,
							MorphFormat.WITH_BRACES)));
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\nList of words containing morpheme '"+morpheme+"' was wrong", 
			expWords, gotWords);
		return this;
	}

	public AssertCompiledCorpus totalOccurencesIs(long expTotal) 
			throws Exception {
		long gotTotal = corpus().totalOccurences();
		Assert.assertEquals(
			baseMessage+"\nTotal number of occurences not as expected.", 
			expTotal, gotTotal);
		return this;
	}
	
	public AssertCompiledCorpus totalOccurencesWithNoDecompIs(long expTotal) 
		throws Exception {
		long gotTotal = corpus().totalOccurencesWithNoDecomp();
		Assert.assertEquals(
				baseMessage+"\nNumber of occurences with no decomp is wrong.", 
				expTotal, gotTotal);
		return this;
	}
	
	public AssertCompiledCorpus totalOccurencesWithDecompIs(long expTotal) 
		throws Exception {
		long gotTotal = corpus().totalOccurencesWithDecomps();
		Assert.assertEquals(
				baseMessage+"\nNumber of occurences with decomps is wrong.", 
				expTotal, gotTotal);
		return this;
	}
	

	public AssertCompiledCorpus totalWordsIs(long expTotal) throws Exception {
		long gotTotal = corpus().totalWords();
		Assert.assertEquals(
			baseMessage+"\nTotal number of words not as expected", 
			expTotal, gotTotal);
		
		return this;
	}

	public AssertCompiledCorpus totalWordsWithDecompIs(long expTotal) 
		throws Exception {
		long gotTotal = corpus().totalWordsWithDecomps();
		Assert.assertEquals(
			baseMessage+"\nTotal number of words with Decomp not as expected", 
			expTotal, gotTotal);
		
		return this;
	}

	public AssertCompiledCorpus totalWordsWithoutDecompsIs(long expTotal) 
		throws Exception {
		long gotTotal = corpus().totalWordsWithNoDecomp();
		Assert.assertEquals(
			baseMessage+"\nTotal number of words with no Decomp not as expected", 
			expTotal, gotTotal);
		
		return this;
	}

	public AssertCompiledCorpus wordsWithNoDecompositionAre(
		String[] expWordsArr) throws Exception {
		Iterator<String> iterator = corpus().wordsWithNoDecomposition();
		Set<String> gotWords = new HashSet<String>();
		while (iterator.hasNext()) {
			gotWords.add(iterator.next());
		}
		Set<String> expWords = new HashSet<String>();
		for (String aWord: expWordsArr) {
			expWords.add(aWord);
		}
		AssertObject.assertDeepEquals(
			"Words with no decompositions were not as expected", 
			expWords, gotWords);
		
		return this;
	}

	public AssertCompiledCorpus infoForWordIs(String word, long expFreq, int expTotalDecomps,
			String[][] expSampleDecomps) throws Exception {
		WordInfo winfo = corpus().info4word(word);
		String message = 
			baseMessage + "\nInfo was not as expected for word "+word;
		Assert.assertEquals(
			message+"\nFrequency not as expected", 
			expFreq, winfo.frequency);
		Assert.assertEquals(
			message+"\nTotal number of decompositions was not as expected.", 
			new Integer(expTotalDecomps), winfo.totalDecompositions);
		AssertObject.assertDeepEquals(
			message+"\nSample decompositions were not as expected.", 
			expSampleDecomps, winfo.decompositionsSample);
		return this;
	}

    public AssertCompiledCorpus totalWordsWithNgramEquals(String ngram, long expTotal)
		throws Exception {
		long gotTotal = corpus().totalWordsWithCharNgram(ngram);
		Assert.assertEquals(
			"Wrong number of words for ngram="+ngram,
			expTotal, gotTotal);
		return this;
    }
}
