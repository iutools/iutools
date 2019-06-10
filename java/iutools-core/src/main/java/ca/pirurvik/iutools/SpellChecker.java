package ca.pirurvik.iutools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.nrc.string.StringUtils;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilities.EditDistanceCalculator;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactory;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactoryException;

public class SpellChecker {
	
	public int MAX_SEQ_LEN = 5;
	public int MAX_CANDIDATES = 100;
	public int DEFAULT_CORRECTIONS = 5;
	public String allWords = ",,";
	public Map<String,Long> idfStats = new HashMap<String,Long>();
	public transient EditDistanceCalculator editDistanceCalculator;
	public transient boolean verbose = true;
	
	public CompiledCorpus corpus = null;
	private static StringSegmenter_IUMorpheme segmenter = new StringSegmenter_IUMorpheme();
	
	public SpellChecker() throws SpellCheckerException {
			editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
			
			try {
				corpus = CompiledCorpusRegistry.getCorpus();
				corpus.setVerbose(verbose);
			} catch (CompiledCorpusRegistryException e) {
				throw new SpellCheckerException(e);
			}
	}

	public SpellChecker(String _corpusName) throws SpellCheckerException {
		initialize(_corpusName, null);
	}

	public SpellChecker(File compiledCorpusFile) throws SpellCheckerException {
		initialize(null, compiledCorpusFile);
	}
	
	private void initialize(String _corpusName, File _compiledCorpusJsonPath) throws SpellCheckerException { 
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator();
		if (_corpusName != null) {
			try {
				corpus = CompiledCorpusRegistry.getCorpus();
				corpus.setVerbose(verbose);
			} catch (CompiledCorpusRegistryException e) {
				throw new SpellCheckerException(e);
			}
		} else {
			if (_compiledCorpusJsonPath != null)  {
				try {
					corpus = CompiledCorpus.createFromJson(_compiledCorpusJsonPath.toString());
					corpus.setVerbose(verbose);
				} catch (Exception e) {
					throw new SpellCheckerException("Could not create the compiled corpus from file: "+_compiledCorpusJsonPath.toString(), e);
				}				
			} else {
				throw new SpellCheckerException("Corpus name AND compiled corpus file path were both null");
			}
		}
	}

	public void setEditDistanceAlgorithm(EditDistanceCalculatorFactory.DistanceMethod name) throws ClassNotFoundException, EditDistanceCalculatorFactoryException {
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator(name);
	}
	
	public void setVerbose(boolean value) {
		verbose = value;
		if (corpus != null) corpus.setVerbose(value);
	}
	
	public void addCorrectWord(String word) {
		//System.out.println("-- addCorrectWord: word="+word);;
		allWords += word+",,";
		Set<String> seqSeen = new HashSet<String>();
		try {
		for (int seqLen = 1; seqLen <= MAX_SEQ_LEN; seqLen++) {
			for (int  start=0; start <= word.length() - seqLen; start++) {
				String charSeq = word.substring(start, start+seqLen);
				//System.out.println("-- addCorrectWord:    charSeq="+charSeq);;
				if (!seqSeen.contains(charSeq)) {
					//System.out.println("-- addCorrectWord:    updateSequenceIDF");;
					updateSequenceIDF(charSeq);
					//System.out.println("-- addCorrectWord:    sequenceIDF updated");;
				}
				seqSeen.add(charSeq);
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateSequenceIDF(String charSeq) {
		if (!idfStats.containsKey(charSeq)) {
			idfStats.put(charSeq, new Long(0));
		}
		idfStats.put(charSeq, idfStats.get(charSeq)+1);
		//System.out.println("-- udpateSequenceIDF:    upon exit, idfStats.get(charSeq)="+idfStats.get(charSeq));
	}

	public void saveToFile(File checkerFile) throws IOException {
		FileWriter saveFile = new FileWriter(checkerFile);
		Gson gson = new Gson();
		gson.toJson(this, saveFile);
		saveFile.flush();
		saveFile.close();
		//System.out.println("saved in "+checkerFile.getAbsolutePath());
	}

	public SpellChecker readFromFile(File checkerFile) throws FileNotFoundException, IOException {
		FileReader jsonFileReader = new FileReader(checkerFile);
		Gson gson = new Gson();
		SpellChecker checker = gson.fromJson(jsonFileReader, SpellChecker.class);
		jsonFileReader.close();
		this.MAX_SEQ_LEN = checker.getMaxSeqLen();
		this.MAX_CANDIDATES = checker.getMaxCandidates();
		this.allWords = checker.getAllWords();
		this.idfStats = checker.getIdfStats();
		return this;
	}

	private Map<String, Long> getIdfStats() {
		return this.idfStats;
	}

	private String getAllWords() {
		return this.allWords;
	}

	private int getMaxCandidates() {
		return this.MAX_CANDIDATES;
	}

	private int getMaxSeqLen() {
		return this.MAX_SEQ_LEN;
	}

	public SpellingCorrection correctWord(String word) throws SpellCheckerException {
		return correctWord(word,-1);
	}

	public SpellingCorrection correctWord(String word, int maxCorrections) throws SpellCheckerException {

//		boolean wordIsLatin = Pattern.compile("[a-zA-Z]").matcher(word).find();
		boolean wordIsSyllabic = Syllabics.allInuktitut(word);
		
		if (wordIsSyllabic) {
			word = TransCoder.unicodeToRoman(word);
		}
		
		SpellingCorrection corr = new SpellingCorrection(word);
		corr.wasMispelled = isMispelled(word);
		if (corr.wasMispelled) {
		
			Set<String> candidates = firstPassCandidates(word);
			List<Pair<String,Double>> candidatesWithSim = computeCandidateSimilarities(word, candidates);
			List<String> corrections = sortCandidatesBySimilarity(candidatesWithSim);
			Logger logger = Logger.getLogger("SpellChecker.correct");
			logger.debug("corrections for "+word+": "+corrections.size());
			
	 		if (maxCorrections== -1)
	 			corr.setPossibleSpellings(corrections);
	 		else
	 			corr.setPossibleSpellings(corrections.subList(0, maxCorrections>corrections.size()? corrections.size() : maxCorrections));
		}
		
		if (wordIsSyllabic) {
			// Transcode the spellings back to Syllabic
			List<String> possSpellingsSyll = new ArrayList<String>();
			for (String aSpelling: corr.getPossibleSpellings()) {
				aSpelling = TransCoder.romanToUnicode(aSpelling);
				possSpellingsSyll.add(aSpelling);
			}
			corr.setPossibleSpellings(possSpellingsSyll);
		}
 		
 		return corr;
	}
	
	protected boolean isMispelled(String word) throws SpellCheckerException {
		Boolean answer = null;
		if (corpus.wordsFailedSegmentation.contains(word)) {
			answer = true;
		} else if (corpus.segmentsCache.containsKey(word)) {
			answer = false;
		} else {
			answer = true;
			try {
				String[] segments = segmenter.segment(word);
				if (segments != null && segments.length > 0) {
					answer = false;
				}
			} catch (TimeoutException e) {
				answer = false;
			} catch (StringSegmenterException e) {
				throw new SpellCheckerException(e);
			}
		}
		
		return answer;
	}

	public Long idf(String charSeq) {
		Long val = new Long(0);
		if (idfStats.containsKey(charSeq)) {
			val = idfStats.get(charSeq);
		}
		return val;
	}
	

	private List<String> sortCandidatesBySimilarity(List<Pair<String, Double>> candidatesWithSim) {
		Iterator<Pair<String, Double>> iteratorCand = candidatesWithSim.iterator();
	
		Collections.sort(candidatesWithSim, (Pair<String,Double> p1, Pair<String,Double> p2) -> {
			return p1.getSecond().compareTo(p2.getSecond());
		});
		List<String> candidates = new ArrayList<String>();
		iteratorCand = candidatesWithSim.iterator();
		while (iteratorCand.hasNext()) {
			Pair<String,Double> pair = iteratorCand.next();
			candidates.add(pair.getFirst());
			//System.out.println("-- sortCandidatesBySimiliraty (2): "+"candidate: "+pair.getFirst()+" ; similarity="+pair.getSecond());
		}
		return candidates;
	}

	protected List<Pair<String, Double>> computeCandidateSimilarities(String badWord, Set<String> candidates) {
		List<Pair<String,Double>> candidateSimilarities = new ArrayList<Pair<String,Double>>();
		Iterator<String> iterator = candidates.iterator();
		while (iterator.hasNext()) {
			String candidate = iterator.next();
			double similarity = computeCandidateSimilarity(badWord,candidate);
			candidateSimilarities.add(new Pair<String,Double>(candidate,new Double(similarity)));
			//System.out.println("-- computeCandidateSimilarities:    "+"candidate: "+candidate+" ; similarity="+similarity);
		}
		return candidateSimilarities;
	}

	private double computeCandidateSimilarity(String badWord, String candidate) {
		// TODO Auto-generated method stub
		int distance = editDistanceCalculator.distance(candidate,badWord);
		return (double)distance;
	}

	public Set<String> firstPassCandidates(String badWord) {
		Logger logger = Logger.getLogger("SpellChecker.firstPassCandidates");
		Set<String> candidates = new HashSet<String>();
		List<Pair<String,Long>> rarest = rarestSequencesOf(badWord);
		while (!rarest.isEmpty()) {
			Pair<String,Long> currSeqInfo = rarest.remove(0);
			logger.debug("sequence: "+currSeqInfo.getFirst()+" ("+currSeqInfo.getSecond()+")");
			Set<String> wordsWithSequence = wordsContainingSequ(currSeqInfo.getFirst());
			logger.debug("  wordsWithSequence: "+wordsWithSequence.size());
			
			candidates.addAll(wordsWithSequence);
			logger.debug("  candidates: "+candidates.size());
			if (candidates.size() >= MAX_CANDIDATES) {
				if (!rarest.isEmpty()) {
					Pair<String,Long> nextSeqInfo = rarest.get(0);
					if (currSeqInfo.getSecond() == nextSeqInfo.getSecond()) {
						// The next sequence is just as rare as the current one, so 
						// keep going.
						continue;
					} else {
						break;
					}
				}
			}
		}
		logger.debug("Nb. candidates: "+candidates.size());
		Iterator<String> itcand = candidates.iterator();
		//while (itcand.hasNext())
		//	logger.debug("candidate: "+itcand.next());
		return candidates;
	}

	protected Set<String> wordsContainingSequ(String seq) {
		Pattern p = Pattern.compile(",([^,]*"+seq+"[^,]*),");
		Matcher m = p.matcher(allWords);
		Set<String> wordsWithSeq = new HashSet<String>();
		while (m.find())
			wordsWithSeq.add(m.group(1));
		return wordsWithSeq;
	}

	public List<Pair<String, Long>> rarestSequencesOf(String string) {
		List<Pair<String,Long>> listOfRarest = new ArrayList<Pair<String,Long>>();
		// TODO:
		for (int seqLen = 1; seqLen <= MAX_SEQ_LEN; seqLen++) {
			for (int  start=0; start <= string.length() - seqLen; start++) {
				String charSeq = string.substring(start, start+seqLen);
				Long idf = idf(charSeq);
				//System.out.println("-- rarestSequencesOf:    charSeq="+charSeq+" ("+idf+")");;
				if (idf != 0) {
					Pair<String,Long> pair = new Pair<String,Long>(charSeq,idf);
					if ( !listOfRarest.contains(pair) )
						listOfRarest.add(pair);
				}
			}
		}
		Collections.sort(listOfRarest,(Object p1, Object p2) -> {
			return ((Pair<String,Long>)p1).getSecond().compareTo(((Pair<String,Long>)p2).getSecond());
		});
		
		return listOfRarest;
		
	}
	
	
	

	public List<SpellingCorrection> correctText(String text) throws SpellCheckerException {
		return correctText(text, null);
	}

	public List<SpellingCorrection> correctText(String text, Integer nCorrections) throws SpellCheckerException {
		if (nCorrections == null) nCorrections = DEFAULT_CORRECTIONS;
		List<SpellingCorrection> corrections = new ArrayList<SpellingCorrection>();
		
		List<Pair<String, Boolean>> tokens = StringUtils.tokenizeNaively(text);
		
		for (Pair<String,Boolean> aToken: tokens) {
			String tokString = aToken.getFirst();
			Boolean isDelimiter = aToken.getSecond();
			SpellingCorrection correction = null;
			if (isDelimiter) {
				correction = new SpellingCorrection(tokString);
			} else {
				correction = this.correctWord(tokString, nCorrections);
			}
			corrections.add(correction);
			
		}
		
		return corrections;
	}


}
