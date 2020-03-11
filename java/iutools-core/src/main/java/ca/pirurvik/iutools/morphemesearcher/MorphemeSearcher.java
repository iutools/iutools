package ca.pirurvik.iutools.morphemesearcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieNode;
import ca.pirurvik.iutools.CompiledCorpus;

public class MorphemeSearcher {
	
	protected String wordSegmentations = null;
	protected CompiledCorpus corpus = null;
	
	public MorphemeSearcher() {
	}
	
	public void useCorpus(CompiledCorpus _corpus) throws IOException {
		corpus = _corpus;
		wordSegmentations = _corpus.getWordSegmentations();
	}
	
	public List<Words> wordsContainingMorpheme(String morpheme) throws Exception {
		Logger logger = Logger.getLogger("MorphemeExtractor.wordsContainingMorpheme");
		logger.debug("morpheme= "+morpheme);
		List<Words> words = new ArrayList<Words>();
		HashMap<String,List<ScoredExample>> wordsForMorphemes = new HashMap<String,List<ScoredExample>>();
		if (wordSegmentations==null)
			throw new Exception("The word extractor has not been defined a compiled corpus.");
		Trie corpusTrie = corpus.getTrie();
		String regexp = ",([^:,]+?):([^:]*?\\{("+morpheme+"/.+?)\\}[^:,]*?),";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(this.wordSegmentations);
		while (m.find()) {
			String word = m.group(1);
//			String segments = m.group(2);
			String morphemeWithId = m.group(3);
			List<ScoredExample> wordsForMorpheme;
			if (wordsForMorphemes.containsKey(morphemeWithId)) {
				wordsForMorpheme = wordsForMorphemes.get(morphemeWithId);
			}
			else {
				wordsForMorpheme = new ArrayList<ScoredExample>();
			}
//			segments += " \\";
//			String segmentsPlusSpaces = segments.replaceAll("\\}\\{", "} {");
//			long freq = corpusTrie.getFrequency(segmentsPlusSpaces.split(" "));
//			logger.debug(word+": "+freq+" --- "+segmentsPlusSpaces);
			
			ScoredExample scoredEx = generateScoredExample(word,morphemeWithId);
			
			wordsForMorpheme.add(scoredEx);
			wordsForMorphemes.put(morphemeWithId,wordsForMorpheme);
		}
		Iterator<String> it = wordsForMorphemes.keySet().iterator();
		while (it.hasNext()) {
			String morphemeID = it.next();
			words.add(new Words(morphemeID,wordsForMorphemes.get(morphemeID)));
		}
		return words;
	}
	
	
	private ScoredExample generateScoredExample(String word, String morphemeWithId) throws MorphemeSearcherException {
		ScoredExample scoredEx = null;
		try {
			boolean allowAnalysisWithAdditionalFinalConsonant = false;
			Double morphemeFreq = morphFreqInAnalyses(morphemeWithId, word, allowAnalysisWithAdditionalFinalConsonant);
			Long wordFreq = wordFreqInCorpus(word,allowAnalysisWithAdditionalFinalConsonant);
			Double score = 10000*morphemeFreq + wordFreq;
			scoredEx = new ScoredExample(word, score, wordFreq);
		} catch (LinguisticDataException | TimeoutException | MorphInukException e) {
			throw new MorphemeSearcherException(e);
		}
		return scoredEx;
	}

	private Long wordFreqInCorpus(String word, boolean allowAnalysisWithAdditionalFinalConsonant) throws MorphemeSearcherException {
		MorphologicalAnalyzer analyzer;
		long nbOccurrencesOfWord = 0;
		try {
			analyzer = new MorphologicalAnalyzer();
			Decomposition[] decompositions = analyzer.decomposeWord(word,allowAnalysisWithAdditionalFinalConsonant);
			for (int idec=0; idec<decompositions.length; idec++) {
				long nwords = numberOfWordsInCorpusWithSuiteOfMorphemes(decompositions[idec].toString());
				nbOccurrencesOfWord += nwords;
			}
			// TODO: additionner la fréq du noeud terminal pour chaque décomposition = fréquence du mot
		} catch (LinguisticDataException | TimeoutException | MorphInukException e) {
			throw new MorphemeSearcherException(e);
		}
		return nbOccurrencesOfWord;
	}

	public long numberOfWordsInCorpusWithSuiteOfMorphemes(String decompositionExpression) {
    	DecompositionExpression expr = new DecompositionExpression(decompositionExpression);
    	String exprWithoutSurfaceForms = expr.toStringWithoutSurfaceForms();
    	String[] sequenceOfMorphemes = exprWithoutSurfaceForms.split(" ");
    	Trie trie = corpus.getTrie();
    	TrieNode[] terminals = trie.getAllTerminals(sequenceOfMorphemes);
    	long nbWord = 0;
    	for (int iterm=0; iterm<terminals.length; iterm++) {
    		nbWord += terminals[iterm].getFrequency();
    	}
		return nbWord;
	}

	public Double morphFreqInAnalyses(String morpheme, String word, boolean allowAnalysisWithAdditionalFinalConsonant) throws LinguisticDataException, TimeoutException, MorphInukException {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decompositions = analyzer.decomposeWord(word,allowAnalysisWithAdditionalFinalConsonant);
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

}


