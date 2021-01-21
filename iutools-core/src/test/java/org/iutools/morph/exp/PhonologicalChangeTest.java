package org.iutools.morph.exp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.nrc.testing.AssertObject;
import org.junit.Assert;
import org.junit.Test;

import org.iutools.phonology.research.PhonologicalChange;
import ca.nrc.testing.AssertHelpers;

public class PhonologicalChangeTest {

	@Test
	public void test_formInDialect() throws FormGeneratorException, IOException {
		String dialect = "southwestbaffin";
		String word = "iksivautaq";
		List<String> expectedForms = new ArrayList<>(Arrays.asList("iksivautaq", "issivautaq"));
		List<String> dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertUnOrderedSameElements("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "iksivautaq";
		expectedForms = new ArrayList<>(Arrays.asList("itsivautaq"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertUnOrderedSameElements("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "sitamangannik";
		expectedForms = new ArrayList<>(Arrays.asList("tisamangannik","sitamangannik"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertUnOrderedSameElements("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "pinngi&&uni";
		expectedForms = new ArrayList<>(Arrays.asList("pinngitsuni"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertUnOrderedSameElements("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
	}


	@Test
	public void test_formsInAllDialects() throws FormGeneratorException, IOException {
		String word = "ikpaksaq";
		Set<String> expectedForms = new HashSet<String>(Arrays.asList("ikpaksaq","ippassaq","ippatsaq"));
		Set<String> dialectalForms = PhonologicalChange.formsInAllDialects(word);
		AssertHelpers.assertUnOrderedSameElements("", expectedForms.toArray(new String[] {}), dialectalForms.toArray(new String[] {}));

		word = "aglak";
		expectedForms = new HashSet<String>(Arrays.asList("aglak","allak"));
		dialectalForms = PhonologicalChange.formsInAllDialects(word);
		AssertHelpers.assertUnOrderedSameElements("", expectedForms.toArray(new String[] {}), dialectalForms.toArray(new String[] {}));

		word = "allak";
		expectedForms = new HashSet<String>(Arrays.asList("aglak","allak"));
		dialectalForms = PhonologicalChange.formsInAllDialects(word);
		AssertHelpers.assertUnOrderedSameElements("", expectedForms.toArray(new String[] {}), dialectalForms.toArray(new String[] {}));
	}



}
