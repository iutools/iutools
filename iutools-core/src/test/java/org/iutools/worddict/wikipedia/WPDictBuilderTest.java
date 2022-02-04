package org.iutools.worddict.wikipedia;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WPDictBuilderTest {

	/////////////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////////////

	@Test
	public void test__WPDictBuilder__Synopsis() {
		// Use this class to create an iu-en dictionary based on a
		// dump of the Inutktitut Wikipedia
	}


	/////////////////////////////////////////
	// VERIFICATION TESTS
	/////////////////////////////////////////

	@Test
	public void test_parseIUWikipediaDump() throws Exception {

		String xmlDump =
			"<title>ᐊᒥᖅ</title>\n"+
			"<title>ᐊᒻᒨᒪᔪᖅ</title>"
			;

		BufferedReader reader = new BufferedReader(new StringReader(xmlDump));
		List<String> gotWords = new WPDictBuilder().parseIUWikipediaDump(xmlDump);
		AssertObject.assertDeepEquals(
			"Words not correctly parsed from WP dump",
			new String[] {"ᐊᒥᖅ", "ᐊᒻᒨᒪᔪᖅ"},
			gotWords
		);
	}

	@Test
	public void test__parseWikiDataResponse() throws Exception {
		String xml =
			"<api success=\"1\">\n" +
			"  <entities>\n" +
			"    <entity type=\"item\" id=\"Q74560\">\n" +
			"      <labels>\n" +
			"        <label language=\"en\" value=\"spermatozoon\"/>\n" +
			"      </labels>\n" +
			"    </entity>\n" +
			"  </entities>\n" +
			"</api>";

		Map<String,String> gotTranslations = new WPDictBuilder().parseWikiDataResponse(xml);
		Map<String,String> expTranslations = new HashMap<String,String>();
		expTranslations.put("en", "spermatozoon");
		AssertObject.assertDeepEquals(
			"Translations was not as expected",
			expTranslations, gotTranslations);
	}
}
