package org.iutools.morph;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.*;
import org.junit.jupiter.api.Assertions;

public class AssertDecompositionList extends Asserter<Decomposition[]> {
	public AssertDecompositionList(Decomposition[] _gotObject) {
		super(_gotObject);
	}

	public AssertDecompositionList(Decomposition[] _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public Decomposition[] decompositions() {
		return (Decomposition[])gotObject;
	}

	public String decompositionStrings() {
		String decStrings = "";
		for (Decomposition aDecomp: decompositions()) {
			if (!decStrings.isEmpty()) {
				decStrings += "\n";
			}
			decStrings += aDecomp.toString();
		}
		return decStrings;
	}

	public AssertDecompositionList includesDecomps(String... expDecomps) {
		String gotDecomps = "";
		for (int ii=0; ii < decompositions().length; ii++) {
			if (ii > 0) {
				gotDecomps += "\n";
			};
			gotDecomps += decompositions()[ii].toString();
		}

		for (String anExpDecomp: expDecomps) {
			if (!gotDecomps.contains(anExpDecomp)) {
				Assertions.fail(
					baseMessage+"\nDecomposition '"+anExpDecomp+"' was missing.\n"+
					"Decompositions were:\n"+gotDecomps);
			}
		}
		return this;
	}

	public AssertDecompositionList includesAtLeastOneOfDecomps(
		String... expDecompStrings)
		throws Exception {

		String gotDecompStrings = decompositionStrings();
		if (expDecompStrings.length == 0) {
			Assertions.assertTrue(
				gotDecompStrings.isEmpty(),
				baseMessage+"Decompositions should have been empty, but were:\n"+
				gotDecompStrings+"\n"
			);
		} else {
			boolean found = false;
			for (String anExpDecomp : expDecompStrings) {
				if (gotDecompStrings.indexOf(anExpDecomp) >= 0) {
					found = true;
					break;
				}
			}
			Assertions.assertTrue(
			found,
			baseMessage + "\nDecompositions did not include any of the expected possibilities.\n" +
			"Expected one of :\n   " + String.join("\n   ", expDecompStrings) + "\n" +
			"Got             :\n   " + gotDecompStrings + "\n");
		}

		return this;
	}

	private Decomposition topDecomposition() {
		Decomposition top = null;
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

	public AssertDecompositionList producesAtLeastNDecomps(int minDecomps) {
		int gotDecomps = decompositions().length;
		AssertNumber.isGreaterOrEqualTo(
			baseMessage+"Number of decompositions produced was too low",
			gotDecomps, minDecomps
		);
		return this;
	}
}
