package org.iutools.concordancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Alignment {

	public boolean misaligned = false;
	public Map<String,String> langText = new HashMap<String,String>();
	public Map<String,String[]> tokens4lang = new HashMap<String,String[]>();
	public int[][] tokenAlignments = null;

	public Alignment() {
		init_Alignment(null, null, null, null,
			null, null, null, null);
	}

	public Alignment(String lang1, String textLang1, 
			String lang2, String textLang2) {
		init_Alignment(lang1, textLang1, lang2, textLang2, (Boolean)null,
		(String[])null, (String[])null, null);
	}
	
	public Alignment(String lang1,  String textLang1, 
		String lang2, String textLang2, boolean _misaligned,
	  	String[] _lang1Tokens, String[] _lang2Tokens,
	  	List<Pair<Integer,Integer>> _tokensAlignment) {
		init_Alignment(lang1, textLang1, lang2, textLang2, _misaligned,
			_lang1Tokens, _lang2Tokens, _tokensAlignment);
	}

	protected void init_Alignment(String lang1, String textLang1, 
		String lang2, String textLang2, Boolean _misaligned,
		String[] _lang1Tokens, String[] _lang2Tokensa,
		List<Pair<Integer,Integer>> tokensAlignment) {
		if (_misaligned == null) {
			_misaligned = false;
		}
		langText.put(lang1, textLang1);
		langText.put(lang2, textLang2);
		misaligned = _misaligned;
	}

	@JsonIgnore
	public Alignment setMisaligned(boolean _misaligned) {
		this.misaligned = _misaligned;
		return this;
	}

	@JsonIgnore
	public Alignment setTokens(String lang, String... tokens) {
		tokens4lang.put(lang, tokens);
		return this;
	}

	@JsonIgnore
	public Alignment setTokenAlignments(
		String[] _lang1Tokens, String[] _lang2Tokens,
		int[][] _tokenAlignments) {
		return this;
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
}
