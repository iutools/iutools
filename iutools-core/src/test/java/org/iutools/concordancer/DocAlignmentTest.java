package org.iutools.concordancer;

import org.junit.Assert;
import org.junit.Test;

public class DocAlignmentTest {

	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__hasContentForBothLanguages__HasContent() throws Exception {
		DocAlignment alignment = new DocAlignment("en", "fr")
			.setPageText("en", "Hello world")
			.setPageText("fr", "Bonjour le monde");
		
		Assert.assertTrue(alignment.hasTextForBothLanguages(DocAlignment.PageSection.ALL));
	}

	@Test
	public void test__hasContentForBothLanguages__OnlyHasContentForOneLang()
	throws Exception {
		DocAlignment alignment = new DocAlignment("en")
			.setPageText("en", "Hello world");
		
		Assert.assertFalse(alignment.hasTextForBothLanguages(DocAlignment.PageSection.MAIN));
	}

	@Test
	public void test__hasContentForBothLanguages__HasNoContentAtAll() {
		DocAlignment alignment = new DocAlignment();
		
		Assert.assertFalse(alignment.hasTextForBothLanguages(DocAlignment.PageSection.MAIN));
	}
}
