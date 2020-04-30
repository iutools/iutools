package ca.pirurvik.iutools.corpus;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;

public abstract class CompiledCorpus_BaseTest {
	
	protected abstract CompiledCorpus_Base makeCorpusUnderTest();

	//////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////
	
	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
		//
		// Use a CompiledCorpus to trie-compile a corpus and compute statistics.
		//
		//
		CompiledCorpus_Base compiledCorpus = makeCorpusUnderTest();
		
		// 
		// By default, the compiler always computes character-ngrams.
		// 
		// But you can also provide a morpheme segmenter which will allow the 
		// corpus to keep stats on morphme-ngrams
		// 
		// But you can also pass it a different segmenter. For example, the following
		// compiler will segment words by inuktitut morphemes.
		//
		compiledCorpus.setSegmenterClassName(
				StringSegmenter_IUMorpheme.class.getName());

		// set verbose to false for tests only
		compiledCorpus.setVerbose(false); 
		
		// Set the maximum number of morphological decompositions that you want 
		// to keep for each word. Note that the entry for a word will always 
		// know how many decompositions existed, even if it only stores the 
		// first few of them.
		//
		compiledCorpus.setDecompsSampleSize(10);
		
		// Whenever you encounter an occurence of a word, invoke 
		// addWordOccurence(word)
		//
		// For example, say you encounter an occurence of word 'inuksuk'...
		// This will:
		// - Create a new entry for this word if this is the first time the 
		//   is encountered
		// - Increment the word's frequency by 1
		// - Increment frequency of each char-ngram contained in that word
		//
		String word = "inuksuk";
		compiledCorpus.addWordOccurence(word);
		
		// You can also provide a list of possible morphological decompositions 
		// for that word. This will:
		//
		// - Store the decompositions in the word's entry
		// - Update the stats for all the morpheme-ngrams contained in any of  
		//   the provided analyses.
		// 
		// Note that the word's entry will only store the first 
		// N=decompsSampleSize decompositions, but it will remember how many 
		// decompositions were passed to setWordDecompositions()
		//
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decomps = analyzer.decomposeWord(word);
		compiledCorpus.setWordDecompositions(decomps);
		
		// Once you have added all this information to the CompiledCorpus, you 
		// can do all sort of useful stuff with that info.
		//
		// For example:
		//
		
		// Loop through all words in the corpus
		Iterator<String> iter = compiledCorpus.allWords();
		while (iter.hasNext()) {
			String aWord = iter.next();
			
			// Get that word's information
			{
				WordInfo wInfo = compiledCorpus.info4word(aWord);
				if (wInfo == null) {
					// Means the corpus does not know about this word
					//
					// Note: Should not happen in this case, because we obtained 
					// 'word' through the allWords() iterator (so it know that this
					// word was seen in the corpus).
					//
				} else {
					// Frequency of the word
					long freq = wInfo.frequency;
					
					// Total number of morphological decompositions for this word, 
					// as well as a short list of the first few decompositions 
					// found.
					// 
					// If those two values are 'null', it means that the decomps 
					// have not been provided.
					// It does NOT mean that no decomps can be computed for this 
					// word.
					//
					Integer numDecomps = wInfo.totalDecompositions;
					String[] sampleDecomps = wInfo.topDecompositions;
				}			
			}
		}
		

		// You can ask for information about the various character-ngrams 
		// that were seen in the corpus.
		//
		{
			// This returns all the words that START with "nuna"
			//
			Set<String> wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("^nuna");
			
			// Words that END with "vut"
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("vut$");
	
			// Words that have "nav" ANYWHERE
			//
			wordsWithNgram = 
					compiledCorpus.wordsContainingNgram("nav");
		}
		
		// Similarly, you can also ask for information about words that contain 
		// certain sequences of ngrams (aka morphem-ngrams)
		//
		{
			// This will find all the words that START with morphemes
			// inuk/1n and titut/tn-sim-p
			//
			String[] morphemes = new String[] {
				"^", "inuk/1n", "titut/tn-sim-p"};
			Set<String> wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);

			// This will find all the words that END with titut/tn-sim-p
			//
			morphemes = new String[] {
				"titut/tn-sim-p", "$"};
			wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);
		
			// This will find all the words that contain morphemes 
			// nasuk/1vv and niq/2vn ANYWHERE
			//
			morphemes = new String[] {
				"nasuk/1vv", "niq/2vn"};
			wordsWithMorphemes = 
				compiledCorpus.wordsContainingMorphNgram(morphemes);
		}
		
		
//		
//		// The most common way to populate a corpus is to compile it from a file 
//		// or series of text files.
//		//
//		// But you can also manually add some words to it.
//		// 
//		// You can add the word by itself, WITH or WITHOUT decompositions.
//		//
//		String word = "inukshuk";
//		compiledCorpus.addWord(word); // WITHOUT decompositions
//		word = "inuktut";
//		Decomposition[] decomps = new MorphologicalAnalyzer().decomposeWord(word);
//		String[] decompsStr = new String[decomps.length];
//		for (int ii=0; ii < decomps.length; ii++) {
//			decompsStr[ii] = decomps[ii].toStr2();
//		}
//		compiledCorpus.addWord(word, decompsStr); // WITH decompositions
//		
//		// You can override the decompositions of a word that is already in the 
//		// corpus
//		//
//		compiledCorpus.info4word(word)
//			.setDecompositions(new String[0]);
//		
//		// Attempting to add a word that is already registered raises a 
//		// WordAlreadinInCorpusException exception.
//		// 
//		// So you should check for the existence of 
//		// the word before adding it.
//		//
//		if (compiledCorpus.info4word(word) == null) {
//			compiledCorpus.addWord(word);
//		}
//				
//		// When you encounter an occurence of a word, you can tell 
//		// the corpus about it as follows.
//		//
//		compiledCorpus.incrementWordFreq(word);
	}
}
