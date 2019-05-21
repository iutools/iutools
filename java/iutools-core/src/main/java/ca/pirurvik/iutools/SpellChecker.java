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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.inuktitutcomputing.utilities.EditDistanceCalculator;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactory;
import ca.inuktitutcomputing.utilities.EditDistanceCalculatorFactoryException;
import ca.inuktitutcomputing.utilities.Levenshtein;

public class SpellChecker {
	
	protected int MAX_SEQ_LEN = 5;
	protected int MAX_CANDIDATES = 100;
	protected String allWords = ",,";
	protected Map<String,Long> idfStats = new HashMap<String,Long>();
	private String defaultEditDistanceAlgorithmName = "Levenshtein";
	public transient EditDistanceCalculator editDistanceCalculator;
	
	
	public SpellChecker() {
		try {
			editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator(defaultEditDistanceAlgorithmName);
		} catch (EditDistanceCalculatorFactoryException e) {
		}
	}
	
	public void setEditDistanceAlgorithm(String name) throws ClassNotFoundException, EditDistanceCalculatorFactoryException {
		editDistanceCalculator = EditDistanceCalculatorFactory.getEditDistanceCalculator(name);
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
		String json = gson.toJson(this);
		saveFile.write(json);
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

	public List<String> correctWord(String badWord) {
		return correct(badWord,-1);
	}

	public Long idf(String charSeq) {
		Long val = new Long(0);
		if (idfStats.containsKey(charSeq)) {
			val = idfStats.get(charSeq);
		}
		return val;
	}

	public List<String> correct(String badWord, int maxCorrections) {
		
		Set<String> candidates = firstPassCandidates(badWord);
		List<Pair<String,Double>> candidatesWithSim = computeCandidateSimilarities(badWord, candidates);
		List<String> corrections = sortCandidatesBySimilarity(candidatesWithSim);
		Logger logger = Logger.getLogger("SpellChecker.correct");
		logger.debug("corrections for "+badWord+": "+corrections.size());
 		if (maxCorrections== -1)
 			return corrections;
 		else
 			return corrections.subList(0, maxCorrections>corrections.size()? corrections.size() : maxCorrections);
	}

	private List<String> sortCandidatesBySimilarity(List<Pair<String, Double>> candidatesWithSim) {
		Iterator<Pair<String, Double>> iteratorCand = candidatesWithSim.iterator();
//		while (iteratorCand.hasNext()) {
//			Pair<String,Double> pair = iteratorCand.next();
//			System.out.println("-- sortCandidatesBySimiliraty (1): "+"candidate: "+pair.getFirst()+" ; similarity="+pair.getSecond());
//		}
	
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
//		for (int il=0; il < listOfRarest.size(); il++)
//			System.out.println("-- rarestSequencesOf:    rarest["+il+"]= "+
//					((Pair<String,Long>)listOfRarest.get(il)).getFirst()+" ("+
//					((Pair<String,Long>)listOfRarest.get(il)).getSecond()+")");
		
		return listOfRarest;
		
	}

	public List<SpellingCorrection> correctText(String text) {
		
		// TODO Auto-generated method stub
		return null;
	}

}
