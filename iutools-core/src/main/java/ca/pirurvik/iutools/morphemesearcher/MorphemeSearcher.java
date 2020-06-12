package ca.pirurvik.iutools.morphemesearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzerException;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory.WordWithMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpusException;
import ca.pirurvik.iutools.corpus.CompiledCorpus;

public class MorphemeSearcher {
	
	protected String wordSegmentations = null;
	protected CompiledCorpus corpus = null;
	protected int nbWordsToBeDisplayed = 20;
	protected int maxNbInitialCandidates = 100;
	
	public MorphemeSearcher() {
	}
	
	public void useCorpus(CompiledCorpus _corpus) throws IOException {
		corpus = _corpus;
		wordSegmentations = _corpus.getWordSegmentations();
	}
	
	public void setNbDisplayedWords(int n) {
		this.nbWordsToBeDisplayed = n;
	}
	
	public List<Words> wordsContainingMorpheme(String morpheme) throws Exception {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher.wordsContainingMorpheme");
		tLogger.trace("morpheme= "+morpheme);
		
		HashMap<String,List<WordWithMorpheme>> morphid2wordsFreqs = getMostFrequentWordsWithMorpheme(morpheme);
		Bin[] rootBins = separateWordsByRoot(morphid2wordsFreqs);

		HashMap<String,List<ScoredExample>> morphids2scoredExamples = null;
		try {
			morphids2scoredExamples = computeWordsWithScoreFromBins(rootBins);
		} catch (Exception e) {
			throw new MorphemeSearcherException(e);
		}
		tLogger.trace("morphids2scoredExamples: "+morphids2scoredExamples.size());
		List<Words> words = new ArrayList<Words>();
		Set<String> keys = morphids2scoredExamples.keySet();
		Iterator<String> iter = keys.iterator();
		while ( iter.hasNext()) {
			String morphId = iter.next();
			tLogger.trace("iteration for generation of Words - morphId: "+morphId);
			List<ScoredExample> scoredExamples = morphids2scoredExamples.get(morphId);
			Words wordsObject = new Words(morphId,scoredExamples);
			words.add(wordsObject);
		}
		tLogger.trace("words: "+words.size());
		return words;
	}
	
	
	protected HashMap<String, List<ScoredExample>> computeWordsWithScoreFromBins(
			Bin[] rootBins) throws MorphemeSearcherException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher.computeWordsWithScoreFromBins");
		
		HashMap<String, List<ScoredExample>> morphids2limitedScoredExamples = 
				new HashMap<String, List<ScoredExample>>();
		for (int ib=0; ib<rootBins.length; ib++) {
			tLogger.trace("Looking at bin #"+ib);
			HashMap<String, List<ScoredExample>> morphid2scoredWords = 
					computeWordsWithScore(rootBins[ib].morphid2wordsFreqs);
			tLogger.trace("Finished scoring words in bin");

			for (Map.Entry<String,List<ScoredExample>> mapElement : morphid2scoredWords.entrySet()) { 
	            String morphid = mapElement.getKey(); 
				tLogger.trace("Looking at bin #"+ib+"; morphid="+morphid);
	            List<ScoredExample> listOfScoredExamples = mapElement.getValue();
				if (tLogger.isTraceEnabled()) {
					tLogger.trace("Finished scoring words in bin. "+
						"listOfScoredExamples.size()="+listOfScoredExamples.size());
				}

				if (!listOfScoredExamples.isEmpty()) {
					tLogger.trace("Bin has some examples");
		            ScoredExample firstScoredExampleInBinForMorphid = 
		            		listOfScoredExamples.get(0);
		            if ( !morphids2limitedScoredExamples.containsKey(morphid) ) {
		            	morphids2limitedScoredExamples.put(morphid, new ArrayList<ScoredExample>());
		            }
		            if (morphids2limitedScoredExamples.get(morphid).size() < nbWordsToBeDisplayed)
		            	morphids2limitedScoredExamples.get(morphid).add(firstScoredExampleInBinForMorphid);
				} else {
					tLogger.trace("Bin is EMPTY");					
				}
			}
		}
		
		return morphids2limitedScoredExamples;
	}

	protected Bin[] separateWordsByRoot(HashMap<String, List<WordWithMorpheme>> morphid2wordsFreqs) {
		HashMap<String,Bin> bins = new HashMap<String,Bin>();
		Set<String> keys = morphid2wordsFreqs.keySet();
		Iterator<String> iter = keys.iterator();
		while ( iter.hasNext() ) {
			String morphId = iter.next();
			List<WordWithMorpheme> wordWithMorphemeS = morphid2wordsFreqs.get(morphId);
			for (int im=0; im<wordWithMorphemeS.size(); im++) {
				WordWithMorpheme wordWithMorpheme = wordWithMorphemeS.get(im);
				DecompositionExpression decomposition = new DecompositionExpression(wordWithMorpheme.decomposition);
				String rootId = decomposition.parts[0].morphid;
				Bin bin = null;
				if ( !bins.containsKey(rootId) ) {
					bin = new Bin(rootId, new HashMap<String,List<WordWithMorpheme>>());
					bins.put(rootId, bin);
				} 
				bin = bins.get(rootId);
				if ( !bin.morphid2wordsFreqs.containsKey(morphId) ) {
					bin.morphid2wordsFreqs.put(morphId, new ArrayList<WordWithMorpheme>());
				}
				List<WordWithMorpheme> list = bin.morphid2wordsFreqs.get(morphId);
				list.add(wordWithMorpheme);
				bin.morphid2wordsFreqs.put(morphId,list);
			}
		}
		
		return bins.values().toArray(new Bin[] {});
	}

	private HashMap<String, List<ScoredExample>> computeWordsWithScore(HashMap<String, List<WordWithMorpheme>> mostFrequentWordsWithMorpheme) throws MorphemeSearcherException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher.computeWordsWithScore");
		if (logger.isTraceEnabled()) {
			logger.trace("invoked with mostFrequentWordsWithMorpheme.size()="+mostFrequentWordsWithMorpheme.size());
		}
		HashMap<String, List<ScoredExample>> morphids2scoredExamples = new HashMap<String, List<ScoredExample>>();
		Set<String> morphIds = mostFrequentWordsWithMorpheme.keySet();
		Iterator<String> iter = morphIds.iterator();
		while ( iter.hasNext() ) {
			List<ScoredExample> scoredExamples = new ArrayList<ScoredExample>();
			String morphId = iter.next();
			List<WordWithMorpheme> wordsFreqs = mostFrequentWordsWithMorpheme.get(morphId);
			logger.trace("wordsFreqs.size()="+wordsFreqs.size());
			for (int iwf=0; iwf<wordsFreqs.size(); iwf++) {
				logger.trace("iwf: "+iwf);
				WordWithMorpheme wordFreq = wordsFreqs.get(iwf);
				try {
					ScoredExample scoredEx = generateScoredExample(wordFreq.word,morphId);
					scoredExamples.add(scoredEx);
				} catch (MorphemeSearcherException e) {
					// generateScoredExample calls the morphpological analyzer, 
					// which may time out; catch the exception, do not register a 
					// scored example and continue with next word
				}
			}
//			Collections.sort(scoredExamples);
			Collections.sort(scoredExamples, ScoredExamplesComparator);
				
			morphids2scoredExamples.put(morphId, scoredExamples);
		}
		
		return morphids2scoredExamples;
	}

	public static Comparator<ScoredExample> ScoredExamplesComparator = new Comparator<ScoredExample>() {

		public int compare(ScoredExample s1, ScoredExample s2) {
			if (s1.score < s2.score)
				return 1;
			else if (s1.score > s2.score)
				return -1;
			else
				return 0;
	   }};	
	   
	private HashMap<String,List<WordWithMorpheme>> getMostFrequentWordsWithMorpheme(String morpheme) throws MorphemeSearcherException {
		List<WordWithMorpheme> wordsWithMorpheme;
		try {
			wordsWithMorpheme = this.corpus.getWordsContainingMorpheme(morpheme);
		} catch (CompiledCorpusException e) {
			throw new MorphemeSearcherException(e);
		}
		HashMap<String,List<WordWithMorpheme>> morphid2WordsFreqs = new HashMap<String,List<WordWithMorpheme>>();
		for (int iw=0; iw<wordsWithMorpheme.size(); iw++) {
			WordWithMorpheme wordAndMorphid = wordsWithMorpheme.get(iw);
			String word = wordAndMorphid.word;
			String morphId = wordAndMorphid.morphemeId;
			List<WordWithMorpheme> wordsFreqs = morphid2WordsFreqs.get(morphId);
			if (wordsFreqs==null) {
				wordsFreqs = new ArrayList<WordWithMorpheme>();
			}
			wordsFreqs.add(wordAndMorphid);
			morphid2WordsFreqs.put(morphId, wordsFreqs);
		}
		
		Set<String> morphIds = morphid2WordsFreqs.keySet();
		Iterator<String> iter = morphIds.iterator();
		while (iter.hasNext()) {
			String morphId = iter.next();
			List<WordWithMorpheme> wordsFreqs = morphid2WordsFreqs.get(morphId);
			Collections.sort(wordsFreqs, (WordWithMorpheme p1, WordWithMorpheme p2) -> {
				if (p1.frequency < p2.frequency)
					return 1;
				else if (p1.frequency > p2.frequency)
					return -1;
				else
					return 0;
			});
			morphid2WordsFreqs.put(morphId,wordsFreqs.subList(0, Math.min(wordsFreqs.size(), maxNbInitialCandidates)));
		}

		return morphid2WordsFreqs;
	}

	
	private ScoredExample generateScoredExample(String word, String morphemeWithId) throws MorphemeSearcherException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher.generateScoredExample");
		ScoredExample scoredEx = null;
		try {
			boolean allowAnalysisWithAdditionalFinalConsonant = false;
			logger.trace("    generateScoredExample --- step 1: morphFreqInAnalyses");
			Double morphemeFreq = morphFreqInAnalyses(morphemeWithId, word, allowAnalysisWithAdditionalFinalConsonant);
			logger.trace("    generateScoredExample --- step 2: wordFreqInCorpus");
			Long wordFreq = wordFreqInCorpus(word,allowAnalysisWithAdditionalFinalConsonant);
			Double score = 10000*morphemeFreq + wordFreq;
			scoredEx = new ScoredExample(word, score, wordFreq);
			logger.trace("    generateScoredExample --- finished");
		} catch (LinguisticDataException | TimeoutException | MorphInukException e) {
			throw new MorphemeSearcherException(e);
		}
		return scoredEx;
	}

	private Long wordFreqInCorpus(String word, boolean allowAnalysisWithAdditionalFinalConsonant) throws MorphemeSearcherException {
		long nbOccurrencesOfWord;
		try {
			nbOccurrencesOfWord = this.corpus.getNbOccurrencesOfWord(word);
		} catch (CompiledCorpusException e) {
			throw new MorphemeSearcherException(e);
		}
		return nbOccurrencesOfWord;
	}

	public long numberOfWordsInCorpusWithSuiteOfMorphemes(
			String decompositionExpression) throws MorphemeSearcherException {
    	DecompositionExpression expr = new DecompositionExpression(decompositionExpression);
    	String exprWithoutSurfaceForms = expr.toStringWithoutSurfaceForms();
    	String[] sequenceOfMorphemes = exprWithoutSurfaceForms.split(" ");
    	Trie trie = corpus.getTrie();
    	TrieNode[] terminals;
		try {
			terminals = trie.getTerminals(sequenceOfMorphemes);
		} catch (TrieException e) {
			throw new MorphemeSearcherException(e);
		}
    	long nbWord = 0;
    	for (int iterm=0; iterm<terminals.length; iterm++) {
    		nbWord += terminals[iterm].getFrequency();
    	}
		return nbWord;
	}

	public Double morphFreqInAnalyses(String morpheme, String word, boolean allowAnalysisWithAdditionalFinalConsonant) throws LinguisticDataException, TimeoutException, MorphInukException, MorphemeSearcherException {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decompositions;
		try {
			decompositions = analyzer.decomposeWord(word,allowAnalysisWithAdditionalFinalConsonant);
		} catch (MorphologicalAnalyzerException e) {
			throw new MorphemeSearcherException(e);
		}
		int numDecsWithMorpheme = 0;
		for (int idec=0; idec<decompositions.length; idec++) {
			Decomposition dec = decompositions[idec];
			if (dec.containsMorpheme(morpheme)) {
				numDecsWithMorpheme++;
			}
		}
		Double freq = 1.0*numDecsWithMorpheme / decompositions.length;
		return freq;
	}
	
	//--------------------------------------------------------------------------

	public class Words {
		
		public String morphemeWithId;
		public List<ScoredExample> words;
		
		public Words(String _morphemeWithId, List<ScoredExample> _words) {
			this.morphemeWithId = _morphemeWithId;
			this.words = _words;
		}
	}
	
	public static class WordFreq {
		public String word;
		public Long freq;
		public Double score = 0.0;
		
		public WordFreq(String _word, Long _freq) {
			this.word = _word;
			this.freq = _freq;
		}
	}

	public class WordFreqComparator implements Comparator<ScoredExample> {
	    @Override
	    public int compare(ScoredExample a, ScoredExample b) {
	    	if (a.score.longValue() > b.score.longValue())
	    		return -1;
	    	else if (a.score.longValue() < b.score.longValue())
				return 1;
	    	else 
	    		return a.word.compareToIgnoreCase(b.word);
	    }
	}
	
	public class Bin {
		public String rootId;
		public HashMap<String,List<WordWithMorpheme>> morphid2wordsFreqs;
		
		public Bin(String _rootid, HashMap<String,List<WordWithMorpheme>> _morphid2wordsFreqs) {
			this.rootId = _rootid;
			this.morphid2wordsFreqs = _morphid2wordsFreqs;
		}
		
		
	}

}


