package org.iutools.concordancer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

public class SentencePair {

	public boolean misaligned = false;
	public Map<String,String> langText = new HashMap<String,String>();
	public Map<String,String[]> tokens4lang = new HashMap<String,String[]>();
	public Map<String,String[]> tokens4langLowercased = new HashMap<String,String[]>();
	public Integer[][] tokenAlignments = null;
	public Integer[][] _invertedTokenAlignments = null;
	private Pair<String, String> tokenAlignmentsDir = null;
	public WordAlignment walign = null;

	public SentencePair() {
		init_SentencePair(null, null, null, null,
			null, null, null, null);
	}

	public SentencePair(String lang1, String textLang1,
		String lang2, String textLang2) {
		init_SentencePair(lang1, textLang1, lang2, textLang2, (Boolean)null,
		(String[])null, (String[])null, null);
	}
	
	public SentencePair(String lang1, String textLang1,
		String lang2, String textLang2, boolean _misaligned,
		String[] _lang1Tokens, String[] _lang2Tokens,
		List<Pair<Integer,Integer>> _tokensAlignment) {
		init_SentencePair(lang1, textLang1, lang2, textLang2, _misaligned,
			_lang1Tokens, _lang2Tokens, _tokensAlignment);
	}

	public  String joinTokens(String lang, int[] tokens) {
		String[] allTokens = tokens4lang.get(lang);
		String joined = "";
		for (int ii=0; ii < tokens.length; ii++) {
			if (!joined.isEmpty()) {
				joined += " ";
			}
			joined += tokenText(lang, tokens[ii]);
		}
		return joined;
	}

	private String tokenText(String lang, int tokenNum) {
		String tokText = null;
		String[] tokens = tokens4lang.get(lang);
		String langText = getText(lang);
		String token = tokens[tokenNum].toLowerCase();
		Matcher matcher =
			stemmedTokensPattern(token)
			.matcher(langText);
		while (matcher.find()) {
			String thisMatch = matcher.group(1);
			if (tokText == null || tokText.length() < thisMatch.length()) {
				tokText = thisMatch;
			}
		}
		return tokText;
	}

	protected void init_SentencePair(String lang1, String textLang1,
		String lang2, String textLang2, Boolean _misaligned,
		String[] _lang1Tokens, String[] _lang2Tokens,
		List<Pair<Integer,Integer>> tokenMatchings) {
		if (_misaligned == null) {
			_misaligned = false;
		}
		langText.put(lang1, textLang1);
		langText.put(lang2, textLang2);
		misaligned = _misaligned;
	}

	@JsonIgnore
	public SentencePair setMisaligned(boolean _misaligned) {
		this.misaligned = _misaligned;
		return this;
	}

	@JsonIgnore
	public SentencePair setTokenAlignments(WordAlignment walign) {
		Pair<String,String> langs = walign.langs();
		String l1 = langs.getLeft();
		String l2 = langs.getRight();
		String[] tokPairingStrs = walign.tokensPairing;
		Integer[][] tokPairings = new Integer[tokPairingStrs.length][];
		for (int ii=0; ii < tokPairingStrs.length; ii++) {
			String[] toksStr = tokPairingStrs[ii].split("-");
			Integer[] toks = new Integer[2];
			for (int jj=0; jj < 2; jj++) {
				toks[jj] = Integer.parseInt(toksStr[jj]);
			}
			tokPairings[ii] = toks;
		}
		return setTokenAlignments(
			l1, walign.tokens4lang.get(l1),
			l2, walign.tokens4lang.get(l2),
			tokPairings);
	}

	@JsonIgnore
	public SentencePair setTokenAlignments(
		String _l1, String[] _lang1Tokens,
		String _l2, String[] _lang2Tokens,
		Integer[][] _l12l1_tokenAlignments) {
		if (_lang1Tokens != null) {
			tokens4lang.put(_l1, _lang1Tokens);
			String[] tokensLowercased = new String[_lang1Tokens.length];
			for (int ii=0; ii < _lang1Tokens.length; ii++) {
				tokensLowercased[ii] = _lang1Tokens[ii].toLowerCase();
			}
			tokens4langLowercased.put(_l1, tokensLowercased);
		}
		if (_lang2Tokens != null) {
			tokens4lang.put(_l2, _lang2Tokens);
			String[] tokensLowercased = new String[_lang2Tokens.length];
			for (int ii=0; ii < _lang2Tokens.length; ii++) {
				tokensLowercased[ii] = _lang2Tokens[ii].toLowerCase();
			}
			tokens4langLowercased.put(_l2, tokensLowercased);
		}

		if (_l12l1_tokenAlignments != null) {
			this.tokenAlignments = sortTokenAlignments(_l12l1_tokenAlignments);
		}
		this.tokenAlignmentsDir = Pair.of(_l1, _l2);
		return this;
	}

	private Integer[][] sortTokenAlignments(Integer[][] l12l1_tokenAlignments) {
		List<Integer[]> tokenAlignmentsLst = new ArrayList<Integer[]>();
		Collections.addAll(tokenAlignmentsLst, l12l1_tokenAlignments);
		return sortTokenAlignments(tokenAlignmentsLst);
	}

	private Integer[][] sortTokenAlignments(List<Integer[]> alignments) {
		Collections.sort(alignments, new Comparator<Integer[]>(){
			public int compare(Integer[] t1, Integer[] t2){
				return t1[0].compareTo(t2[0]);
			}
		});

		return alignments.toArray(new Integer[0][]);
	}

	public Pair<String,String> langs() {
		String[] langsArr = langText.keySet().toArray(new String[langText.keySet().size()]);

		Pair<String,String> langPair = null;
		if (langsArr[0].compareTo(langsArr[1]) < 0) {
			langPair = Pair.of(langsArr[0], langsArr[1]);
		} else {
			langPair = Pair.of(langsArr[1], langsArr[0]);
		}

		return langPair;
	}

	@JsonIgnore
	public String[] getTokens(String lang) {
		String[] tokens = tokens4lang.get(lang);

		return tokens;
	}

	@JsonIgnore
	public String getText(String lang) {
		return langText.get(lang);
	}

	@JsonIgnore
	public Pair<String[], String[]> getCorrespondingTokens(
		String lang, int start) {
		return getCorrespondingTokens(lang, start, start);
	}

	@JsonIgnore
	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>
		getCorrespondingTokenOffsets(String lang, int start) {
		return getCorrespondingTokenOffsets(lang, start, start);
	}

	@JsonIgnore
	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>
		getCorrespondingTokenOffsets(String lang, int start, int end) {
		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> offsets =
			Pair.of(null, null);
		return offsets;
	}

	@JsonIgnore
	public Pair<String[], String[]> getCorrespondingTokens(
		String lang, int start, int end) {
		Pair<String[],String[]> corresp = Pair.of(new String[0], new String[0]);

		return corresp;
	}


	public String toString() {
		Pair<String,String> langPair = langs();
		
		String toStr = 
				"(" + 
				langPair.getLeft()+":\"" + getText(langPair.getLeft()) +
				"\" <--> " +
				langPair.getRight()+":\"" + getText(langPair.getRight()) +
				"\")"
				;

		return toStr;
	}

	public String otherLangThan(String lang) {

		Pair<String,String> langPair = langs();
		String otherLang = langPair.getLeft();
		if (otherLang.equals(lang)) {
			otherLang = langPair.getRight();
		}
		return otherLang;
	}

	public int[] otherLangTokens(String lang, int[] inputTokens) {
		Arrays.sort(inputTokens);
		int[] otherTokens = new int[0];
		List<Integer> matchingTokens= new ArrayList<Integer>();
		Integer[][] tokAlignments = tokenAlignments;
		if (!tokenAlignmentsDir.getLeft().equals(lang)) {
			tokAlignments = invertTokenAlignmentsDirection();
		}

		if (inputTokens != null && inputTokens.length > 0) {
			int cursorTokAlignments = 0;
			int cursorInputTokens = 0;
			while (true) {
				if (cursorTokAlignments >= tokAlignments.length) {
					// No more alignments to choose from
					break;
				}
				if (cursorInputTokens >= inputTokens.length) {
					// No more input tokens to match.
					break;
				}
				Integer[] tokPair = tokAlignments[cursorTokAlignments];
				int inpToken = inputTokens[cursorInputTokens];
				if (tokPair[0] > inpToken) {
					// The token alignment cursor has gone passed the input tokens
					// cursor.
					// --> Move the input tokens cursor by one position;
					//
					cursorInputTokens++;
					continue;
				}
				if (tokPair[0].equals(inputTokens[cursorInputTokens])) {
					// Current token alignment matches the current input token
					// Add that tokens's other language token
					matchingTokens.add(tokPair[1]);
				}
				cursorTokAlignments++;
			}
		}

		otherTokens = new int[matchingTokens.size()];
		for (int ii=0; ii < otherTokens.length; ii++) {
			otherTokens[ii] = matchingTokens.get(ii);
		}

		Arrays.sort(otherTokens);
		return otherTokens;
	}

	private Integer[][] invertTokenAlignmentsDirection() {
		if (_invertedTokenAlignments == null) {
			List<Integer[]> invertedLst = new ArrayList<Integer[]>();
			for (Integer[] anOrigAlg : tokenAlignments) {
				Integer[] anInvAlg = new Integer[2];
				anInvAlg[0] = anOrigAlg[1];
				anInvAlg[1] = anOrigAlg[0];
				invertedLst.add(anInvAlg);
			}
			_invertedTokenAlignments =  sortTokenAlignments(invertedLst);
		}
		return _invertedTokenAlignments;
	}

	public Pair<String,String> textPair(String l1, String l2) {
		return Pair.of(getText(l1), getText(l2));
	}

	public String[] canonicalTokenText(String lang) {
		String[] origTokens = tokens4lang.get(lang);
		String[] canonicalTokens = new String[origTokens.length];
		for (int ii=0; ii < origTokens.length; ii++) {
			canonicalTokens[ii] = canonizeText(origTokens[ii]);
		}
		return canonicalTokens;
	}

	public static String canonizeText(String origText) {
		String canonized = origText.toLowerCase();
		canonized = canonized.replaceAll("\\s+", " ");
		canonized = canonized.replaceAll("(^\\s*|\\s*$)", "");
		return canonized;
	}

	public boolean hasWordLevel() {
		return (tokenAlignments != null && tokenAlignments.length > 0);
	}

	public static Pattern stemmedTokensPattern(String tokens) {
		return stemmedTokensPattern(tokens, (Boolean)null);
	}

	public static Pattern stemmedTokensPattern(
		String token, Boolean matchWholeText) {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.SentencePair.stemmedTokensPattern");
		tLogger.trace("token='"+token+"', matchWholeText="+matchWholeText);
		if (matchWholeText == null) {
			matchWholeText = false;
		}
		token = escapeRegexpSpecialChars(token);
		String tokenRegex =
			"("+token.replaceAll("@@$", "[\\\\s\\\\S]*?")+
			")($|\\s|\\p{Punct})";
		if (matchWholeText) {
			tokenRegex = "^\\s*"+tokenRegex+"\\s*$";
		}
		Pattern patt =
			Pattern.compile(tokenRegex, Pattern.CASE_INSENSITIVE);
		tLogger.trace("Did not crash");

		return patt;
	}

	public static String escapeRegexpSpecialChars(String text) {
		text = text.replaceAll("([\\(\\)\\[\\]\\.\\*\\?\\<\\\\>=])", "\\$1");
		return text;
	}

	public String highlightedText(String lang, String tag) {
		return highlightedText(lang, tag, (Boolean)null);
	}

	public String highlightedText(String lang, String tag, Boolean lowercase) {
		if (lowercase == null) {
			lowercase = true;
		}
		Pattern patt = Pattern.compile("<"+tag+">([^<]*)</"+tag+">");
		Matcher matcher = patt.matcher(langText.get(lang));
		String highlighted = null;
		if (matcher.find()) {
			highlighted = matcher.group(1);
		}
		if (lowercase && highlighted != null) {
			highlighted = highlighted.toLowerCase();
		}
		return highlighted;
	}
}
