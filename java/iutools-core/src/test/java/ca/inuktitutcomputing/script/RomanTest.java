package ca.inuktitutcomputing.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.junit.Test;

//import org.junit.jupiter.api.Test;

class RomanTest {

	@Test
	void test_determineRootForms() {
		String morpheme;
		String[] forms;
		String[] expected;
		
		morpheme = "umiaq";
		forms = Roman.determineRootForms(morpheme);
		expected = new String[] {"umiaq","umiar","umia"};
		assertEquals("The number of forms is wrong.",expected.length,forms.length);
		for (String expectedForm : expected)
			assertTrue("The expected form '"+expectedForm+"' is not in the forms returned.",Arrays.asList(forms).contains(expectedForm));
		
		morpheme = "sinik";
		forms = Roman.determineRootForms(morpheme);
		expected = new String[] {"sinik","sinig","siniN","sini"};
		assertEquals("The number of forms is wrong.",expected.length,forms.length);
		for (String expectedForm : expected)
			assertTrue("The expected form '"+expectedForm+"' is not in the forms returned.",Arrays.asList(forms).contains(expectedForm));
		
		morpheme = "aput";
		forms = Roman.determineRootForms(morpheme);
		expected = new String[] {"aput","aputi","apul","apun","apu"};
		assertEquals("The number of forms is wrong.",expected.length,forms.length);
		for (String expectedForm : expected)
			assertTrue("The expected form '"+expectedForm+"' is not in the forms returned.",Arrays.asList(forms).contains(expectedForm));
		
		morpheme = "sila";
		forms = Roman.determineRootForms(morpheme);
		expected = new String[] {"sila"};
		assertEquals("The number of forms is wrong.",expected.length,forms.length);
		for (String expectedForm : expected)
			assertTrue("The expected form '"+expectedForm+"' is not in the forms returned.",Arrays.asList(forms).contains(expectedForm));
	}
}
