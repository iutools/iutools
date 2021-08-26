package org.iutools.morph;

import ca.nrc.testing.Asserter;
import org.junit.jupiter.api.Assertions;

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

	public AssertDecompositionList decompIs(String... morphStrings) {
		Assertions.fail("Implement this assertion");
		return this;
	}
}
