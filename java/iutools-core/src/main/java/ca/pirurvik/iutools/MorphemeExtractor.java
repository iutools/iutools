package ca.pirurvik.iutools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class MorphemeExtractor {
	
	protected String dictionary = null;
	
	public MorphemeExtractor() {
	}
	
	public void useDictionary(File file) throws IOException {
		if ( !file.exists() )
			throw new IOException("The file "+file+" does not exist.");
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		dictionary = fileReader.readLine();
		fileReader.close();
	}
	
	public void setDictionary(String _dictionary) {
		this.dictionary = _dictionary;
	}
	
	public List<Words> wordsContainingMorpheme(String morpheme) {
		Logger logger = Logger.getLogger("MorphemeExtractor.wordsContainingMorpheme");
		logger.debug("morpheme= "+morpheme);
		List<Words> words = new ArrayList<Words>();
		HashMap<String,List<String>> wordsForMorphemes = new HashMap<String,List<String>>();
		if (dictionary==null)
			return words;
		String regexp = ",,([^:]+?):[^:]*?\\{("+morpheme+"/.+?)\\}[^:,]*?";
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(this.dictionary);
		while (m.find()) {
			String word = m.group(1);
			String morphemeWithId = m.group(2);
			List<String> wordsForMorpheme;
			if (wordsForMorphemes.containsKey(morphemeWithId)) {
				wordsForMorpheme = wordsForMorphemes.get(morphemeWithId);
			}
			else {
				wordsForMorpheme = new ArrayList<String>();
			}
			wordsForMorpheme.add(word);
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
		public List<String> words;
		
		public Words(String _morphemeWithId, List<String> _words) {
			this.morphemeWithId = _morphemeWithId;
			this.words = _words;
		}
	}

}
