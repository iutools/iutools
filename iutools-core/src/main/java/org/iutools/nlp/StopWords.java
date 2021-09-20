package org.iutools.nlp;

import ca.nrc.string.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Remove stopwords from text **/
public class StopWords {

	private List<String> words = new ArrayList<String>();
	Pattern _pattWords = null;

	private StopWords() {

	}

	public static String remove(String lang, String text) throws StopWordsException {
		return stopWords4lang(lang).remove(text);
	}

	Pattern pattWords() {
		if (_pattWords == null) {
			String regex = "("+StringUtils.join(words.iterator(),"|")+")";
			regex =
				"(?<=(^|\\s|\\p{Punct}))" +
				regex +
				"(?=($|\\s|\\p{Punct}))"
				;
			_pattWords = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
		return _pattWords;
	}

	public String remove(String text) {
		String withoutSWs = text;
		if (!words.isEmpty()) {
			if (text != null) {
				Matcher matcher = pattWords().matcher(text);
				withoutSWs = matcher.replaceAll("*");
				withoutSWs = withoutSWs.replaceAll("\\s+", " ");
			}
		}
		return withoutSWs;
	}

	private  static StopWords stopWords4lang(String lang) throws StopWordsException {
		StopWords sw = new StopWords();
		String[] swArray = null;
		if (lang.equals("en")) {
			swArray = enWords();
		} else if (lang.equals("iu")) {
			swArray = iuWords();
		} else {
			throw new StopWordsException("No default stopwords list for language "+lang);
		}
		Collections.addAll(sw.words, swArray);
		
		return sw;
	}

	private static String[] enWords() {
		return new String[] {
			"i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", 
			"you're", "you've", "you'll", "you'd", "your", "yours", "yourself", 
			"yourselves", "he", "him", "his", "himself", "she", "she's", "her", 
			"hers", "herself", "it", "it's", "its", "itself", "they", "them", 
			"their", "theirs", "themselves", "what", "which", "who", "whom", 
			"this", "that", "that'll", "these", "those", "am", "is", "are", "was", 
			"were", "be", "been", "being", "have", "has", "had", "having", "do", 
			"does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", 
			"because", "as", "until", "while", "of", "at", "by", "for", "with", 
			"about", "against", "between", "into", "through", "during", "before", 
			"after", "above", "below", "to", "from", "up", "down", "in", "out", 
			"on", "off", "over", "under", "again", "further", "then", "once", 
			"here", "there", "when", "where", "why", "how", "all", "any", "both", 
			"each", "few", "more", "most", "other", "some", "such", "no", "nor", 
			"not", "only", "own", "same", "so", "than", "too", "very", "s", "t", 
			"can", "will", "just", "don", "don't", "should", "should've", "now", 
			"d", "ll", "m", "o", "re", "ve", "y", "ain", "aren", "aren't", 
			"couldn", "couldn't", "didn", "didn't", "doesn", "doesn't", "hadn", 
			"hadn't", "hasn", "hasn't", "haven", "haven't", "isn", "isn't", "ma", 
			"mightn", "mightn't", "mustn", "mustn't", "needn", "needn't", "shan", 
			"shan't", "shouldn", "shouldn't", "wasn", "wasn't", "weren", "weren't",
			"won", "won't", "wouldn", "wouldn't"
		};
	}

	private static String[] iuWords() {
		// No stopwords for IU yet
		return new String[0];
	}
}
