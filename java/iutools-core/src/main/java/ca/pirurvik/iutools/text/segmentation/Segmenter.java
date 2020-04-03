package ca.pirurvik.iutools.text.segmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public abstract class Segmenter {
	
	public static final String ENDS_WITH_PERIOD = "[\\.\\!\\?](\\s*$)$";
	public static final String CONTAINS_SUCCESSIVE_SPACES = "\\s\\s";

	protected abstract String[] tokenize(String text);
	
	public static Segmenter makeSegmenter() {
		return makeSegmenter(null);
	}

	
	public static Segmenter makeSegmenter(String lang) {
		Segmenter segmenter = null;
		if (lang == null) {
			segmenter = new Segmenter_Generic();
		} else {
			if (lang.equals("iu")) {
				segmenter = new Segmenter_IU();
			} else {
				segmenter = new Segmenter_Generic();
			}
		}
		return segmenter;
	}
	
	protected Segmenter() {
		
	}
	
	public List<String[]> segmentTokenized(String text) {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.text.segmentTokenized");
		tLogger.trace("invoked with text='"+text+"'");
		String[] tokens = tokenize(text);
		List<String[]> sentences = new ArrayList<String[]>();
		List<String> currSentence = new ArrayList<String>();
		for (int nn=0; nn < tokens.length; nn++) {
			currSentence.add(tokens[nn]);
			if (tokenIsSentenceEnd(nn, tokens)) {
				sentences.add(currSentence.toArray(new String[currSentence.size()]));
				currSentence = new ArrayList<String>();
			}
		}
		
		if (currSentence.size() > 0) {
			sentences.add(currSentence.toArray(new String[currSentence.size()]));
		}

		return sentences;
	}
	
	
	public List<String> segment(String text) {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.text.segmentation.segment");
		tLogger.trace("invoked with text='"+text+"'");
		List<String[]> tokenizedSentences = segmentTokenized(text);
		List<String> sentences = new ArrayList<String>();
		for (String[] aTokenizeSent: tokenizedSentences) {
			sentences.add(String.join("", aTokenizeSent));
		}

		return sentences;
	}
	
	private boolean tokenIsSentenceEnd(int nn, String[] tokens) {
		Boolean isSentenceEnd = null;
		String currToken = tokens[nn];
		
		if (currToken.contains("\n")) {
			// A newline indicates end of a sentence.
			isSentenceEnd = true;
		}
		
		Matcher matcher = 
			Pattern.compile(CONTAINS_SUCCESSIVE_SPACES).matcher(currToken);
		if (matcher.find()) {
			isSentenceEnd = true;
		}
		
		if (isSentenceEnd == null) {
			matcher = Pattern.compile(ENDS_WITH_PERIOD).matcher(currToken);
			if (isSentenceEnd == null && matcher.find()) {
				if (matcher.group(0).length() > 0) {
					// Current token ends with a period, followed by some spaces
					isSentenceEnd = true;
				} else {
					// No space after the period in current token. Check if next 
					// token starts with a space
					if (tokens.length > nn && tokens[nn+1].matches("^\\s+.*")) {
						isSentenceEnd = true;
					}
				}
			}
		}
		
		if (isSentenceEnd == null) {
			isSentenceEnd = false;
		}

		return isSentenceEnd;
	}

}
