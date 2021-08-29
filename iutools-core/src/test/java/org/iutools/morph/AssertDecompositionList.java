package org.iutools.morph;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;

public class AssertDecompositionList extends Asserter<DecompositionSimple[]> {
	public AssertDecompositionList(DecompositionSimple[] _gotObject) {
		super(_gotObject);
	}

	public AssertDecompositionList(DecompositionSimple[] _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public DecompositionSimple[] decompositions() {
		return (DecompositionSimple[])gotObject;
	}

	public AssertDecompositionList decompIs(String... morphStrings)
		throws Exception {
		return decompIs((Integer)null, morphStrings);
	}

	public AssertDecompositionList decompIs(Integer nth, String... expMorphemes)
		throws Exception {
		if (nth == null) {
			nth = 0;
		}

		String[] gotMorphemes = new String[0];
		if (topDecomposition() != null) {
			gotMorphemes = topDecomposition().components();
		}

		AssertObject.assertDeepEquals(
			baseMessage+"\nDecompositionState #"+nth+" was not as expected",
			expMorphemes, gotMorphemes
		);

		return this;
	}

	private DecompositionSimple topDecomposition() {
		DecompositionSimple top = null;
		if (decompositions() != null && decompositions().length > 0) {
			top = decompositions()[0];
		}
		return top;
	}

	public AssertDecompositionList allDecompsContain(String expMorphSeqStr) {
		for (int ii=0; ii < decompositions().length; ii++) {
			String iithDecompStr = decompositions()[ii].toStr();
			AssertString.assertStringContains(
				baseMessage+ii+"th decompoisition did not contain '"+expMorphSeqStr+"'",
				iithDecompStr, expMorphSeqStr
			);
		}
		return this;
	}

	public AssertDecompositionList atLeastOneDecompContains(String expMorphSequ) {
		String allDecomps = "";
		if (decompositions() != null) {
			for (int ii=0; ii < decompositions().length; ii++) {
				if (ii > 0) {
					allDecomps += "\n";
				}
				allDecomps += decompositions()[ii].toStr();
			}
		}
		AssertString.assertStringContains(
			baseMessage+"\nNone of the decompositions contained '"+expMorphSequ+"'",
			allDecomps, expMorphSequ
		);
		return this;
	}
}
