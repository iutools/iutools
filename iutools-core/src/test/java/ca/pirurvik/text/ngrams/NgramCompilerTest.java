package ca.pirurvik.text.ngrams;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.*;

import ca.nrc.testing.AssertHelpers;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

public class NgramCompilerTest {
	
	@Before
	public void setUp() {
	}

	@Test
	public void test__NgramCompiler__Synopsis() {
		NgramCompiler ngramCompiler = new NgramCompiler();
		// By default, compilation starts at the beginning of the word (index 0).
		// That can be changed by changing the start index this way:
		ngramCompiler.setMin(2); // will start at the 3rd character
		// By default, maximum ngram length is length of the word.
		// That can be changed this way:
		ngramCompiler.setMax(5);
		// It is possible to include extremities, that is, to take into account
		// the beginning and the end of the word by adding ^ at the beginning
		// and $ at the end of the word:
		ngramCompiler.includeExtremities(true); // cancel: set to false
		String word = "anyword";
		Set<String> ngrams = ngramCompiler.compile(word);
	}

	@Test
	public void test_compile() throws IOException {
		NgramCompiler ngramCompiler = new NgramCompiler();
		
		String word;
		word= "";
		Set<String> ngramsForEmptyWord = ngramCompiler.compile(word);
		Assert.assertTrue(ngramsForEmptyWord.size()==0);
		
		word = "anyword";
		Set<String> ngrams = ngramCompiler.compile(word);
		Set<String> expected = new HashSet<String>();
		expected.add("a"); expected.add("an"); expected.add("any"); expected.add("anyw"); 
		expected.add("anywo"); expected.add("anywor"); expected.add("anyword"); 
		expected.add("n"); expected.add("ny"); expected.add("nyw"); expected.add("nywo"); 
		expected.add("nywor"); expected.add("nyword"); 
		expected.add("y"); expected.add("yw"); expected.add("ywo"); expected.add("ywor"); 
		expected.add("yword"); 
		expected.add("w"); expected.add("wo"); expected.add("wor"); expected.add("word"); 
		expected.add("o"); expected.add("or"); expected.add("ord"); 
		expected.add("r"); expected.add("rd"); 
		expected.add("d"); 
		AssertHelpers.assertDeepEquals("", expected, ngrams);
	}
	
	@Test
	public void test_compile__Case_with_extremities() throws IOException {
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.includeExtremities(true);
		String word = "any";
		Set<String> ngrams = ngramCompiler.compile(word);
		Set<String> expected = new HashSet<String>();
		expected.add("^a"); expected.add("^an"); expected.add("^any$"); 
		expected.add("n"); expected.add("ny$");
		expected.add("y$"); 
		AssertHelpers.assertDeepEquals("", expected, ngrams);
	}
	
	@Test
	public void test_compile__Case_min_other_than_1() throws IOException {
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMin(3);
		
		String word;
		word= "";
		Set<String> ngramsForEmptyWord = ngramCompiler.compile(word);
		Assert.assertTrue(ngramsForEmptyWord.size()==0);
		
		word = "anyword";
		Set<String> ngrams = ngramCompiler.compile(word);
		Set<String> expected = new HashSet<String>();
		expected.add("any"); expected.add("anyw"); 
		expected.add("anywo"); expected.add("anywor"); expected.add("anyword"); 
		expected.add("nyw"); expected.add("nywo"); 
		expected.add("nywor"); expected.add("nyword"); 
		expected.add("ywo"); expected.add("ywor"); 
		expected.add("yword"); 
		expected.add("wor"); expected.add("word"); 
		expected.add("ord"); 
		AssertHelpers.assertDeepEquals("", expected, ngrams);
	}
	
	@Test
	public void test_compile__Case_max_other_than_0() throws IOException {
		NgramCompiler ngramCompiler = new NgramCompiler();
		ngramCompiler.setMax(3);

		String word;
		word= "";
		Set<String> ngramsForEmptyWord = ngramCompiler.compile(word);
		Assert.assertTrue(ngramsForEmptyWord.size()==0);
		
		word = "anyword";
		Set<String> ngrams = ngramCompiler.compile(word);
		Set<String> expected = new HashSet<String>();
		expected.add("a"); expected.add("an"); expected.add("any"); 
		expected.add("n"); expected.add("ny"); expected.add("nyw"); 
		expected.add("y"); expected.add("yw"); expected.add("ywo"); 
		expected.add("w"); expected.add("wo"); expected.add("wor");
		expected.add("o"); expected.add("or"); expected.add("ord"); 
		expected.add("r"); expected.add("rd"); 
		expected.add("d"); 
		AssertHelpers.assertDeepEquals("", expected, ngrams);
	}
	
}
