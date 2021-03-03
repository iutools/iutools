package org.iutools.morphrelatives;

import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Outcome (expected or actual) of running the MorphRelativesFinder on a word
 */
public class WordOutcome {
	public String word = null;
	public String[] relsProduced = new String[0];

	public WordOutcome() {};

	public WordOutcome(String _word, String[] _relsProduced) {
		init_WordOutcome(_word, _relsProduced);
	}

	public WordOutcome(String _word, MorphologicalRelative[] _relObjs) {
		String[] _rels = null;
		if (_relObjs != null) {
			_rels = new String[_relObjs.length];
			for (int ii=0; ii < _relObjs.length; ii++) {
				_rels[ii] = _relObjs[ii].getWord();
			}
		}
		init_WordOutcome(_word, _rels);
	}

	private void init_WordOutcome(String _word, String[] _relsProduced) {
		this.word = _word;
		this.relsProduced = _relsProduced;
	}

	public List<String> correctRelatives(String[] goldStandardRels) {
		List<String> correct = new ArrayList<String>();
		for (String rel: relsProduced) {
			if (ArrayUtils.contains(goldStandardRels, rel)) {
				correct.add(rel);
			}
		}

		return correct;
	}

	public List<String> incorrectRelatives(String[] goldStandardRels) {
		List<String> incorrect = new ArrayList<String>();
		for (String rel: relsProduced) {
			if (!ArrayUtils.contains(goldStandardRels, rel)) {
				incorrect.add(rel);
			}
		}
		return incorrect;
	}

	public String fitnessToGoldStandard(String[] goldStandardRelatives) {
		return fitnessToGoldStandard(goldStandardRelatives, "  ");
	}

	public String fitnessToGoldStandard(String[] goldStandardRelatives, String padding) {
		String toS = "";
		toS += padding+"Word: "+word+"\n";

		toS += padding+"Precision: "+precision(goldStandardRelatives)+"\n";
		toS += padding+"Recall: "+recall(goldStandardRelatives)+"\n";

		toS += padding+"Relatives produced (** = correct): "+"\n";

		List<String> goodRels = correctRelatives(goldStandardRelatives);
		for (String goodRel: goodRels) {
			toS += padding+padding+goodRel+"**\n";
		}

		for (String badRel: incorrectRelatives(goldStandardRelatives)) {
			toS += padding+padding+badRel+"\n";
		}

		toS += padding+"Gold Standard relatives  (** = found):\n";
		for (String gsRel: goldStandardRelatives) {
			toS += padding+padding+gsRel;
			if (goodRels.contains(gsRel)) {
				toS += "**";
			}
			toS += "\n";
		}

		return toS;
	}

	public double recall(String[] gsRels) {
		double rec =  0.0;
		if (gsRels != null && gsRels.length > 0) {
			rec = 1.0 * correctRelatives(gsRels).size() / gsRels.length;
		}
		return rec;
	}

	public double precision(String[] gsRels) {
		double prec = 0.0;
		String[] produced = relsProduced;
		if (produced != null && produced.length > 0) {
			prec = 1.0 * correctRelatives(gsRels).size() / produced.length;
		}
		return prec;
	}
}
