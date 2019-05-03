package ca.pirurvik.iutools.webservice;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.webservice.SearchInputs;

public class SearchInputsTest {

	@Test
	public void test__getQuerySyllatic__LatinQuery() {
		SearchInputs inputs = new SearchInputs("inuktitut");
		String gotSyllavic = inputs.getQuerySyllabic();
		AssertHelpers.assertStringEquals("ᐃᓄᒃᑎᑐᑦ", gotSyllavic);
	}

	@Test
	public void test__getQuerySyllatic__SyllabicQuery() {
		SearchInputs inputs = new SearchInputs("ᐃᓄᒃᑎᑐᑦ");
		String gotSyllavic = inputs.getQuerySyllabic();
		AssertHelpers.assertStringEquals("ᐃᓄᒃᑎᑐᑦ", gotSyllavic);
	}
}
