package ca.pirurvik.iutools.concordancer;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class DocAlignmentTest {

	//////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////

	@Test
	public void test__hasContentForBothLanguages__HasContent() {
		DocAlignment alignment = new DocAlignment()
			.setPageContent("en", "Hello world")
			.setPageContent("fr", "Bonjour le monde");
		
		Assert.assertTrue(alignment.hasContentForBothLanguages());
	}

	@Test
	public void test__hasContentForBothLanguages__OnlyHasContentForOneLang() {
		DocAlignment alignment = new DocAlignment()
			.setPageContent("en", "Hello world");
		
		Assert.assertFalse(alignment.hasContentForBothLanguages());
	}

	@Test
	public void test__hasContentForBothLanguages__HasNoContentAtAll() {
		DocAlignment alignment = new DocAlignment();
		
		Assert.assertFalse(alignment.hasContentForBothLanguages());
	}
}
