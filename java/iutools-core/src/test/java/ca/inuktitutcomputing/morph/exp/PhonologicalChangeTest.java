package ca.inuktitutcomputing.morph.exp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.SurfaceFormInContext;
import ca.inuktitutcomputing.phonology.research.PhonologicalChange;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.testing.AssertHelpers;

public class PhonologicalChangeTest {

	@Test
	public void test_formInDialect() throws FormGeneratorException, IOException {
		String dialect = "southwestbaffin";
		String word = "iksivautaq";
		List<String> expectedForms = new ArrayList<>(Arrays.asList("iksivautaq", "issivautaq"));
		List<String> dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertContainsAll("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "iksivautaq";
		expectedForms = new ArrayList<>(Arrays.asList("iksivautaq", "itsivautaq"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertContainsAll("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "sitamangannik";
		expectedForms = new ArrayList<>(Arrays.asList("tisamangannik","sitamangannik", "tisamanganning", "sitamanganning"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertContainsAll("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));
		
		dialect = "nunavik-northlabrador";
		word = "pinngi&&uni";
		expectedForms = new ArrayList<>(Arrays.asList("pinngitsuni"));
		dialectalForms = PhonologicalChange.formsInDialect(word, dialect);
		AssertHelpers.assertContainsAll("",expectedForms.toArray(new String[] {}),dialectalForms.toArray(new String[] {}));		
	}

	
	@Test
	public void test_formsInAllDialects() throws FormGeneratorException, IOException {
		String word = "ikpaksaq";
		Set<String> expected = new HashSet<String>(Arrays.asList("ikpaksaq","ikpatsaq","ippassaq","ippatsaq"));
		Set<String> allForms = PhonologicalChange.formsInAllDialects(word);
		AssertHelpers.assertContainsAll("", expected.toArray(new String[] {}), allForms.toArray(new String[] {}));
	}
	
//	@Test
//	public void test_formsInAllDialects__Case_ktik() throws FormGeneratorException, IOException {
//		String word = "ptik";
//		Set<String> expected = new HashSet<String>(Arrays.asList("ikpaksaq","ikpatsaq","ippassaq","ippatsaq"));
//		Set<String> allForms = PhonologicalChange.formsInAllDialects(word);
//		AssertHelpers.assertContainsAll("", expected.toArray(new String[] {}), allForms.toArray(new String[] {}));
//	}
	
}
