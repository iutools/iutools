package ca.pirurvik.iutools.text.ngrams;

import java.util.HashSet;

public class NgramCompiler {

	private int min = 1;
	private int max = 0;
	private boolean includeExtremities = false;
	
	public NgramCompiler() {
		
	}
	public NgramCompiler(int _min, int _max) {
		this.min = _min;
		this.max = _max;
	}
	public NgramCompiler(int _min, int _max, boolean _includeExtremities) {
		this.min = _min;
		this.max = _max;
		this.includeExtremities = _includeExtremities;
	}
	
	public NgramCompiler(boolean _includeExtremities) {
		this.includeExtremities = _includeExtremities;		
	}

	public HashSet<String> compile(String word) {
		HashSet<String> seqSeenInWord = new HashSet<String>();
		if (word.length() != 0) {
			int maxseq = max;
			if (maxseq == 0)
				maxseq = word.length();
			for (int seqLen = min; seqLen <= maxseq; seqLen++) {
				for (int start = 0; start <= word.length() - seqLen; start++) {
					int end = start+seqLen;
					String charSeq = word.substring(start, end);
					if (start == 0 && includeExtremities)
						charSeq = "^"+charSeq;
					if (end == word.length() && includeExtremities)
						charSeq = charSeq+"$";
					seqSeenInWord.add(charSeq);
				}
			}
		}
		return seqSeenInWord;
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

}
