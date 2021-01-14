package org.iutools.concordancer;

import org.junit.Test;

public class AlignmentSpecTest {

	/////////////////////////////////
	// VERIFICATION TESTS
	/////////////////////////////////

	@Test
	public void test__AlignmentSpec__HappyPath() throws Exception {
		AlignmentSpec spec =
			new AlignmentSpec("en", "fr", "1:2");
		new AssertAlignmentSpec(spec)
			.assertL1SentsEqual(1, 1)
			.assertL2SentsEqual(2, 2)
			;
	}
}
