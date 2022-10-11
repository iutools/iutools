package org.iutools.morphemedict;

import org.apache.commons.lang.StringUtils;
import org.iutools.corpus.WordInfo;

/**
 * Word that provides an example of use for a given morpheme
 */
public class MorphWordExample implements Comparable {
	public String word = null;
	public String isExampleOfMorpheme = null;
	private Double _score = null;
	public Long frequency = null;
	public String topDecompStr = null;
	public String[][] decompsSample = null;
	public String root = null;


	public MorphWordExample(WordInfo winfo, String morphemeID) {
		init__MorphWordExample(winfo.word, morphemeID,
			winfo.frequency, winfo.decompositionsSample);
	}

	public MorphWordExample(String _word, String _morphemeID, Long _frequency,
		String _singleDecompStr) {
		String[] decomp = _singleDecompStr.split("\\s+");
		init__MorphWordExample(_word, _morphemeID,
		_frequency, new String[][] {decomp});
	}


	private void init__MorphWordExample(String _word, String _isExampleOfMorpheme,
		Long _freq, String[][] _decompositionsSample) {
		this.word = _word;
		this.isExampleOfMorpheme = _isExampleOfMorpheme;
		this.frequency = _freq;
		setDecompositionsSample(_decompositionsSample);
	}

	private void setDecompositionsSample(String[][] _sample) {
		if (_sample != null) {
			decompsSample = _sample;
			if (decompsSample.length > 0) {
				String[] topDecomp = decompsSample[0];
				topDecompStr = String.join(" ", topDecomp);
				if (topDecomp.length > 0) {
					root = topDecomp[0];
				}
			}
			_score = null;
		}
	}

	public double getScore() {
		if (_score == null) {
			double analysesFitness = morphFrequencyInAnalyses();
			_score = 10000*analysesFitness + frequency;
		}
		return _score;
	}

	protected double morphFrequencyInAnalyses() {
		double freq = 0.0;
		int numDecsWithMorpheme = 0;
		for (String[] decomp: decompsSample) {
			String decompStr = StringUtils.join(decomp, " ");
			if (decompStr.contains(isExampleOfMorpheme)) {
				numDecsWithMorpheme++;
			} else {
				int x = 1;
			}

		}
		freq = 1.0 * numDecsWithMorpheme / decompsSample.length;
		return freq;

	}

	@Override
	public int compareTo(Object obj) {
		if (this.getScore() < ((MorphWordExample)obj).getScore())
			return 1;
		else if (this.getScore() > ((MorphWordExample)obj).getScore())
			return -1;
		else
			return 0;
	}
}
