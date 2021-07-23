package org.iutools.concordancer;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

//	@JsonIgnore
//	public SentencePair setTokens(String lang, String... tokens) {
//		tokens4lang.put(lang, tokens);
//		return this;
//	}

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
		List<Integer[]> sorted = new ArrayList<Integer[]>();
		Collections.addAll(sorted, l12l1_tokenAlignments);
		Collections.sort(sorted, new Comparator<Integer[]>(){
			public int compare(Integer[] t1, Integer[] t2){
				return t1[0].compareTo(t2[0]);
			}
		});

		return sorted.toArray(new Integer[0][]);
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

	public String otherLangText(String lang, String langText) {
		Integer[] langTokens = tokens4text(lang, langText);
		Integer[] otherLangTokens = otherLangTokens(lang, langTokens);
		String otherLang = otherLangThan(lang);

		String otherText = "";
		for (Integer otherToken: otherLangTokens) {
			if (!otherText.isEmpty()) {
				otherText += " ";
			}
			otherText += tokens4lang.get(otherLang)[otherToken];
		}

		return otherText;
	}

	public String otherLangThan(String lang) {

		Pair<String,String> langPair = langs();
		String otherLang = langPair.getLeft();
		if (otherLang.equals(lang)) {
			otherLang = langPair.getRight();
		}
		return otherLang;
	}

	protected Integer[] tokens4text(String lang, String langText) {
		langText = langText.toLowerCase();
		langText.replaceAll("\\s+", " ");
		langText.replaceAll("(^\\s*|\\s*$)", "");

		List<Integer> tokens = new ArrayList<Integer>();
		String[] langTokens = tokens4lang.get(lang);
		String remainingWordText = langText;
		for (int ii=0; ii < langTokens.length; ii++) {
			if (remainingWordText.isEmpty()) {
				break;
			}
			String tokenText = langTokens[ii];
			remainingWordText = remainingWordText.replaceAll("^\\s+", "");
			tokenText = tokenText.toLowerCase();
			if (remainingWordText.startsWith(tokenText)) {
				tokens.add(ii);
				remainingWordText = remainingWordText.replace(tokenText, "");
			} else {
				if (!tokens.isEmpty()) {
					// We matched part, but not all of the lang text.
					// Reinitialize the tokens and remaining text
					remainingWordText = langText;
					tokens = new ArrayList<Integer>();
				}
			}
		}

		return tokens.toArray(new Integer[0]);
	}

	public Integer[] otherLangTokens(String lang, Integer[] langTokens) {
		List<Integer> matchingTokens= new ArrayList<Integer>();
		Integer[][] tokAlignments = tokenAlignments;
		if (!tokenAlignmentsDir.getLeft().equals(lang)) {
			tokAlignments = invertedTokenAlignmentsDirection();
		}

		if (langTokens != null && langTokens.length > 0) {
			List<Integer> inputTokens = new ArrayList<Integer>();
			Collections.addAll(inputTokens, langTokens);
			int currTokAlignmentIndex = 0;
			while (true) {
				if (currTokAlignmentIndex >= tokAlignments.length) {
					break;
				}
				if (inputTokens.isEmpty()) {
					// No more input tokens to match.
					break;
				}
				Integer[] tokPair = tokAlignments[currTokAlignmentIndex];
				if (tokPair[0] == inputTokens.get(0)) {
					// Current aligned token pair matches the current input token
					// Add that tokens's other language token
					matchingTokens.add(tokPair[1]);
				} else if (tokPair[0] > inputTokens.get(0)){
					// Current aligned token pair is passed the current input token.
					// Remove some input tokens until we catch up with the current
					// aligned token pair.
					while (!inputTokens.isEmpty() &&
					tokPair[0] > inputTokens.get(0)) {
						inputTokens = inputTokens.subList(1, inputTokens.size());
					}
				}
				currTokAlignmentIndex++;
			}
		}

		return matchingTokens.toArray(new Integer[0]);
	}

	private Integer[][] invertedTokenAlignmentsDirection() {
		if (_invertedTokenAlignments == null) {
			List<Integer[]> inverted = new ArrayList<Integer[]>();
			for (Integer[] anOrigAlg : tokenAlignments) {
				Integer[] anInvAlg = new Integer[2];
				anInvAlg[0] = anOrigAlg[1];
				anInvAlg[1] = anOrigAlg[0];
				inverted.add(anInvAlg);
			}
			_invertedTokenAlignments = inverted.toArray(new Integer[0][]);
		}
		return _invertedTokenAlignments;
	}

	public Pair<String,String> textPair(String l1, String l2) {
		return Pair.of(getText(l1), getText(l2));
	}

	public Pair<String, String> markupPair(
		String l1, String l1Expr, String tagName) {
		String l1Text = getText(l1);
		l1Text = highlightExpression(l1Expr, l1Text, tagName);

		String l2Text = getText(otherLangThan(l1));
		String l2Expr = otherLangText(l1, l1Expr);
		l2Text = highlightExpression(l2Expr, l2Text, tagName);

		Pair<String,String> markedUp = Pair.of(l1Text, l2Text);
		return markedUp;
	}

	private String highlightExpression(String exp, String text, String tagName) {
		String startTag = "<"+tagName+">";
		String endTag = "<"+tagName+"/>";
		text.replaceAll("("+exp+")", startTag+"$1"+endTag);
		return text;
	}
}
