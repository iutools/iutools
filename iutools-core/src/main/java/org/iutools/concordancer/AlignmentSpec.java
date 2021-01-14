package org.iutools.concordancer;

import org.apache.commons.lang3.tuple.Pair;

public class AlignmentSpec {
	public String lang1 = null;
	public String lang2 = null;
	public int l1SentStart = -1;
	public int l1SentEnd = -1;
	public int l2SentStart = -1;
	public int l2SentEnd = -1;
	public String sentAlignmentSpec = null;
	public String[] tokens1 = null;
	public String[] tokens2 = null;
	public String tokenAlignmentSpec = null;

	public AlignmentSpec(String _lang1, String _lang2,
		String _sentAlignmentSpec) throws DocAlignmentException {
		init_AlignmentSpec(_lang1, _lang2, _sentAlignmentSpec);
	}

	private void init_AlignmentSpec(String _lang1, String _lang2,
		String _sentAlignmentSpec) throws DocAlignmentException {
		this.lang1 = _lang1;
		this.lang2 = _lang2;
		parseSentAlignmentSpec(_sentAlignmentSpec);
	}

	private void parseSentAlignmentSpec(String _sentAlignmentSpec)
		throws DocAlignmentException {
		this.sentAlignmentSpec = _sentAlignmentSpec;
		String[] l1and2ranges = _sentAlignmentSpec.split(":");
		if (l1and2ranges.length != 2) {
			throw new DocAlignmentException(
				"Unparsable sentence alignment spec: "+_sentAlignmentSpec);
		}
		Pair<Integer,Integer> startEnd = parseSentencesRange(l1and2ranges[0]);
		l1SentStart = startEnd.getLeft();
		l1SentEnd = startEnd.getRight();

		startEnd = parseSentencesRange(l1and2ranges[1]);
		l2SentStart = startEnd.getLeft();
		l2SentEnd = startEnd.getRight();
	}

	private Pair<Integer, Integer> parseSentencesRange(String range) throws DocAlignmentException {
		String[] startEnd = range.split("-");
		if (startEnd.length == 0 || startEnd.length > 2) {
			throw new DocAlignmentException("Unparsable sentence range: "+range);
		}
		Integer start = null; Integer end = null;
		try {
			if (startEnd.length == 1) {
				start = Integer.parseInt(startEnd[0]);
				end = start;
			} else {
				start = Integer.parseInt(startEnd[0]);
				end = Integer.parseInt(startEnd[1]);
			}
		} catch (Exception e) {
			throw new DocAlignmentException("Unparsable sentence range: "+range);
		}
		return Pair.of(start, end);
	}

	public AlignmentSpec setTokenAlignment(String[] _tokens1, String[] _tokens2,
		String tokAligmentSpec) {
		this.tokens1 = _tokens1;
		this.tokens2 = _tokens2;
		parseTokenAlignmentSpec(tokAligmentSpec);
		return this;
	}

	private void parseTokenAlignmentSpec(String _tokAligmentSpec) {
		this.tokenAlignmentSpec = _tokAligmentSpec;
	}
}
