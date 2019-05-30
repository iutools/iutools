package ca.pirurvik.iutools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.Trie;

public class MorphemeExtractor {
	
	protected String wordSegmentations = null;
	protected CompiledCorpus corpus = null;
	
	public MorphemeExtractor() {
	}
	
	public void useCorpus(CompiledCorpus _corpus) throws IOException {
		corpus = _corpus;
		wordSegmentations = _corpus.getWordSegmentations();
	}
	
	public List<Words> wordsContainingMorpheme(String morpheme) throws Exception {
		Logger logger = Logger.getLogger("MorphemeExtractor.wordsContainingMorpheme");
		logger.debug("morpheme= "+morpheme);
		List<Words> words = new ArrayList<Words>();
		HashMap<String,List<Pair<String,Long>>> wordsForMorphemes = new HashMap<String,List<Pair<String,Long>>>();
		if (wordSegmentations==null)
			throw new Exception("The word extractor has not been defined a compiled corpus.");
		Trie corpusTrie = corpus.getTrie();
		String regexp = ",([^:,]+?):([^:]*?\\{("+morpheme+"/.+?)\\}[^:,]*?),";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(this.wordSegmentations);
		while (m.find()) {
			String word = m.group(1);
			String segments = m.group(2);
			String morphemeWithId = m.group(3);
			List<Pair<String,Long>> wordsForMorpheme;
			if (wordsForMorphemes.containsKey(morphemeWithId)) {
				wordsForMorpheme = wordsForMorphemes.get(morphemeWithId);
			}
			else {
				wordsForMorpheme = new ArrayList<Pair<String,Long>>();
			}
			segments += " \\";
			String segmentsPlusSpaces = segments.replaceAll("\\}\\{", "} {");
			long freq = corpusTrie.getFrequency(segmentsPlusSpaces.split(" "));
			logger.debug(word+": "+freq+" --- "+segmentsPlusSpaces);
			wordsForMorpheme.add(new Pair<String,Long>(word,new Long(freq)));
			wordsForMorphemes.put(morphemeWithId,wordsForMorpheme);
		}
		Iterator<String> it = wordsForMorphemes.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			words.add(new Words(key,wordsForMorphemes.get(key)));
		}
		return words;
	}
	
	public class Words {
		
		public String morphemeWithId;
		public List<Pair<String,Long>> words;
		
		public Words(String _morphemeWithId, List<Pair<String,Long>> _words) {
			this.morphemeWithId = _morphemeWithId;
			this.words = _words;
		}
	}

}

