package org.iutools.concordancer;

import ca.nrc.testing.AssertString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AlignmentTest {

	@Test
	public void test__constructor() {
		Alignment alignment =
			new Alignment("somedoc", 13);
		Assertions.assertNotNull(alignment.type);
		AssertString.assertStringEquals(
			"Alignment ID not as expected",
			"Alignment:somedoc-p13", alignment.getId());
	}
}
