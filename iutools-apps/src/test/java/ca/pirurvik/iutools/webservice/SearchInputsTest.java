package ca.pirurvik.iutools.webservice;

import java.util.List;

import org.junit.Test;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.pirurvik.iutools.webservice.SearchInputs;

public class SearchInputsTest {

	@Test
	public void test__getQuerySyllatic__LatinQuery() {
		SearchInputs inputs = new SearchInputs("inuktitut");
		String gotSyllavic = inputs.convertQueryToSyllabic();
		AssertString.assertStringEquals("ᐃᓄᒃᑎᑐᑦ", gotSyllavic);
	}

	@Test
	public void test__getQuerySyllatic__SyllabicQuery() {
		SearchInputs inputs = new SearchInputs("ᐃᓄᒃᑎᑐᑦ");
		String gotSyllavic = inputs.convertQueryToSyllabic();
		AssertString.assertStringEquals("ᐃᓄᒃᑎᑐᑦ", gotSyllavic);
	}
	
	@Test
	public void test__getTerms__HappyPath() throws Exception {
		SearchInputs inputs = new SearchInputs("nunavut OR nunav");
		List<String> gotTerms = inputs.getTerms();
		String[] expTerms = new String[] {"nunavut", "nunav"};
		AssertObject.assertDeepEquals("Terms not as expected", 
				expTerms, gotTerms);
	}
}
