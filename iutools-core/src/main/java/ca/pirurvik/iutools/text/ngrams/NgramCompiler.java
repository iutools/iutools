package ca.pirurvik.iutools.text.ngrams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NgramCompiler {

	private int min = 1;
	private int max = Integer.MAX_VALUE;
	private boolean includeExtremities = false;
	
	public NgramCompiler() {
		init_NgramCompiler(null, null, null);
	}
	
	public NgramCompiler(int _min) {
		init_NgramCompiler(_min, null, null);
	}

	public NgramCompiler(int _min, int _max) {
		init_NgramCompiler(_min, _max, null);
	}
	
	public NgramCompiler(int _min, int _max, boolean _includeExtremities) {
		init_NgramCompiler(_min, _max, _includeExtremities);
	}
	
	public NgramCompiler(boolean _includeExtremities) {
		init_NgramCompiler(null, null, _includeExtremities);
		this.includeExtremities = _includeExtremities;		
	}

	public NgramCompiler(int _min, boolean _includeExtremities) {
		init_NgramCompiler(_min, null, _includeExtremities);
	}
	
	protected void init_NgramCompiler(Integer _min, Integer _max, 
			Boolean _includeExtremities) {
		if (_min != null) {
			if (_min < 1) _min = 1;
			this.min = _min;
		}
		if (_max != null) {
			this.max = _max;
		}
		if (_includeExtremities != null) {
			this.includeExtremities = _includeExtremities;
		}
	}

	public Set<String> compile(String word) {
		Set<String> ngrams = new HashSet<String>();
		if (word != null && !word.isEmpty()) {
			String[] wordChars = word.split("");
			Set<String[]> ngramsAsCharArrays = compile(wordChars);
			for (String[] aNgram: ngramsAsCharArrays) {
				ngrams.add(String.join("", aNgram));
			}
		}		
		return ngrams;
	}

	public Set<String[]> compile(String[] tokens) {
		HashSet<String[]> ngrams = new HashSet<String[]>();
		if (tokens != null && tokens.length != 0) {
			int maxseq = Math.min(max, tokens.length);
			String[] tokensAugmented = tokens;
			if (includeExtremities) {
				tokensAugmented = new String[tokens.length+2];
				tokensAugmented[0] = "^";
				for (int ii=0; ii < tokens.length; ii++) {
					tokensAugmented[ii+1] = tokens[ii];
				}
				tokensAugmented[tokensAugmented.length-1] = "$";
			}
			//
			// Note: We allow the ngramLen to be +2 longer than the 
			// maxseq, because some of the tokens might be 
			// the extremity indicators (^ and $) which do not count 
			// in the ngram length
			//
			for (int ngramLen = min; ngramLen <= maxseq + 2; ngramLen++) {
				int x = 0;
				for (int start=0; start < tokensAugmented.length - ngramLen + 1; start++) {
					int end = start + ngramLen;
					String[] ngram = Arrays.copyOfRange(tokensAugmented, start, end);
					int actualNgranLen = ngram.length;
					if (ngram[0].equals("^")) {
						actualNgranLen--;
					}
					if (ngram[ngram.length-1].equals("$")) {
						actualNgranLen--;
					}
					if (actualNgranLen < min || actualNgranLen > max) {
						continue;
					}
					ngrams.add(ngram);
				}
			}
		}
		return ngrams;
	}
	
	public NgramCompiler setMin(int val) {
		if (val<1)
			val = 1;
		this.min = val;
		return this;
	}

	public NgramCompiler setMax(int val) {
		if (val<0)
			val = 0;
		this.max = val;
		return this;
	}

	public void includeExtremities(boolean val) {
		this.includeExtremities = val;
	}
	
	public static String[] atBeginningOfString(String[] ngram) {
		String[] extNgram = ngram;
		if (ngram != null) {
			if (ngram.length == 0 || !ngram[0].equals("^")) {
				extNgram = new String[ngram.length+1];
				extNgram[0] = "^";
				for (int ii=0; ii < ngram.length; ii++) {
					extNgram[ii+1] = ngram[ii];
				}
			}
		}
		return extNgram;
	}

	public static String[] atEndOfString(String[] ngram) {
		String[] extNgram = ngram;
		if (ngram != null) {
			if (ngram.length == 0 || !ngram[ngram.length-1].equals("$")) {
				extNgram = new String[ngram.length+1];
				extNgram[extNgram.length-1] = "$";
				for (int ii=0; ii < ngram.length; ii++) {
					extNgram[ii] = ngram[ii];
				}
			}
		}
		return extNgram;
	}
}
