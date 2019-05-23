package ca.pirurvik.iutools.search;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nrc.testing.AssertHelpers;

public class PageOfHitsTest {

	@Test
	public void test__getQueryTerms__MultiWordQuery() throws Exception {
		PageOfHits page = new PageOfHits("(inukshuk OR inuk)");
		String[] gotQueryTerms = page.getQueryTerms();
		String[] expQueryTerms = new String[] {"inukshuk", "inuk"};
		AssertHelpers.assertDeepEquals("Query terms were not as expected", expQueryTerms, gotQueryTerms);
	}

	@Test
	public void test__getQueryTerms__SingleWordQuery() throws Exception {
		PageOfHits page = new PageOfHits("inukshuk");
		String[] gotQueryTerms = page.getQueryTerms();
		String[] expQueryTerms = new String[] {"inukshuk"};
		AssertHelpers.assertDeepEquals("Query terms were not as expected", expQueryTerms, gotQueryTerms);
	}
}
