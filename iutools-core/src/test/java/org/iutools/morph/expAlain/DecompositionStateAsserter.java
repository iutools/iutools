package org.iutools.morph.expAlain;

import java.util.List;

import org.junit.Assert;

import org.iutools.testing.Asserter;

public class DecompositionStateAsserter extends Asserter {

	public static DecompositionStateAsserter assertThat(
			DecompositionState gotState, String mess) throws Exception {
		return new DecompositionStateAsserter(gotState, mess,
			gotState.getClass());
		
	}	
	
	public DecompositionStateAsserter(
		DecompositionState _gotState,
			String mess, Class<?> gotStateClass) throws Exception {
		super(_gotState, mess, gotStateClass);
	}

	private DecompositionState gotState() {
		return ((DecompositionState)gotObject);
	}

	public String decompToString(List<WrittenMorpheme> decomp) {
		WrittenMorpheme[] decompArr = 
			decomp.toArray(new WrittenMorpheme[decomp.size()]);
		return decompToString(decompArr);
	}
	
	public String decompToString(WrittenMorpheme[] decomp) {
		String tos = "[";
		for (WrittenMorpheme morph: decomp) {
			tos += morph.toString()+",";
		}
		tos += "]";
		return tos;
	}
	
	public void containsDecomposition(WrittenMorpheme[] expDecomp) {
		boolean found = false;
		String expDecompStr = decompToString(expDecomp);
		String errMess = 
			baseMessage+
			"\nThe state did not contain the expected decomposition: "+
			expDecompStr+
			"\nGot decompositions:\n";

		List<List<WrittenMorpheme>> gotDecomps = 
				gotState().allDecompsAsWrittenMorphemes();
		for (List<WrittenMorpheme> decomp: gotDecomps) {
			String gotDecompStr = decompToString(decomp);
			if (gotDecompStr.equals(expDecompStr)) {
				found = true;
				break;
			}
			errMess += "  "+gotDecompStr;
		}
		
		Assert.assertTrue(errMess, found);
		
	}
}
