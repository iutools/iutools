package org.iutools.script;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.Test;

import ca.nrc.testing.AssertObject;

//import org.junit.jupiter.api.Test;

public class RomanTest {

	@Test
	public void test_determineRootForms() {
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
	
	@Test
	public void test_separateSyllables() {
		String word;
		String[] syllables;
		String[] expected;
		
		word = "ila";
		expected = new String[] {"i","la"};
		syllables = Roman.separateSyllables(word);
		assertArrayEquals(word,expected,syllables);
		
		word = "inuk";
		expected = new String[] {"i","nu","k"};
		syllables = Roman.separateSyllables(word);
		assertArrayEquals(word,expected,syllables);
		
		word = "inna";
		expected = new String[] {"i","n","na"};
		syllables = Roman.separateSyllables(word);
		assertArrayEquals(word,expected,syllables);
		
		word = "innaq";
		expected = new String[] {"i","n","na","q"};
		syllables = Roman.separateSyllables(word);
		assertArrayEquals(word,expected,syllables);
		
		word = "nuna";
		expected = new String[] {"nu","na"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "mumiq";
		expected = new String[] {"mu","mi","q"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "mummi";
		expected = new String[] {"mu","m","mi"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "mummiq";
		expected = new String[] {"mu","m","mi","q"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "nuata";
		expected = new String[] {"nu","a","ta"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "nuatta";
		expected = new String[] {"nu","a","t","ta"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "nuataq";
		expected = new String[] {"nu","a","ta","q"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "nuattaq";
		expected = new String[] {"nu","a","t","ta","q"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "naata";
		expected = new String[] {"naa","ta"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
		
		word = "nunavummiut";
		expected = new String[] {"nu","na","vu","m","mi","u","t"};
		syllables = Roman.separateSyllables(word);
		//System.out.println(PrettyPrinter.print(syllables));
		assertArrayEquals(word,expected,syllables);
	}	
	
	@Test
	public void test__splitChars__HappyPath() throws Exception {
		String word = "nakurmiit";
		String[] gotChars = Roman.splitChars(word);
		
		// Notice how the double 'ii' is considered to be a single 
		//    character.
		String[] expChars = new String[] {"n","a","k","u","r","m","ii","t"};
		
		AssertObject.assertDeepEquals("Word "+word+" was not correctly split", 
				expChars, gotChars);
	}

	@Test
	public void test__splitChars__EmptyString() throws Exception {
		String emptyString = "";
		String[] gotChars = Roman.splitChars(emptyString);
		
		String[] expChars = new String[] {};
		
		AssertObject.assertDeepEquals("Word "+emptyString+" was not correctly split", 
				expChars, gotChars);
	}

	@Test
	public void test__splitChars__NullString() throws Exception {
		String nullString = null;
		String[] gotChars = Roman.splitChars(nullString);
		
		String[] expChars = null;
		
		AssertObject.assertDeepEquals("Word "+nullString+" was not correctly split", 
				expChars, gotChars);
	}
}
