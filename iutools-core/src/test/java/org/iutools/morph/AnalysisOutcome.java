package org.iutools.morph;

import org.apache.commons.lang3.ArrayUtils;
import org.iutools.linguisticdata.LinguisticDataException;

import java.util.Collections;

/**
 * Outcome of calling decomposeWord() on a test word.
 * 
 * @author desilets
 *
 */
public class AnalysisOutcome {
	public boolean timedOut = false;
	public Decomposition[] decompositions = new Decomposition[0];
	
	public boolean includesAtLeastOneOfDecomps(String[] decomps) throws LinguisticDataException {
		boolean includes = (null != decompRank(decomps));
		return includes;
	}
	
	public Integer decompRank(String[] correctDecomps) throws LinguisticDataException {
		Integer rank = null;
		for (int ii = 0; ii < decompositions.length; ii++) {
			String itthDecomp = decompositions[ii].toStr2();
			if (ArrayUtils.contains(correctDecomps, itthDecomp)) {
				rank = ii;
				break;
			}
		}
		
		return rank;
	}

	
	public String joinDecomps() {
		String joined = "";
		for (Decomposition aDecomp: decompositions) {
			joined += "  "+aDecomp.toString()+"\n";
		}
		return joined;
	}
}
