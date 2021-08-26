package org.iutools.morph.exp;

import static org.junit.Assert.*;

import java.util.List;

import org.iutools.morph.DecompositionSimple;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.json.PrettyPrinter;

@Ignore
public class WordAnalyzerTest {

	@Test
	public void test_findRoot__Case_1() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "inullu";
		List<String> rootElements = wordAnalyzer.findRoot(string);
//		System.out.println(PrettyPrinter.print(rootElements));
		assertEquals("",3,rootElements.size());
	}
	
	@Test
	public void test_findRoot__Case_2() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "tikinniaqtuq";
		List<String> rootElements = wordAnalyzer.findRoot(string);
		System.out.println(PrettyPrinter.print(rootElements));
		assertEquals("",4,rootElements.size());
		
		string = "tikivviulaurama";
		rootElements = wordAnalyzer.findRoot(string);
//		System.out.println(PrettyPrinter.print(rootElements));
		assertEquals("",3,rootElements.size());
	}
	
	@Test @Ignore
	public void test_findAffix__Case_1() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "lu";
		List<String> affixElements = wordAnalyzer.findAffix(string);
//		for (int i=0; i<affixElements.size(); i++) System.out.println((i+1)+". "+affixElements.get(i));
		assertEquals("",45,affixElements.size());
	}
	
	@Test @Ignore
	public void test_analyze__Case_inullu() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "inullu";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",1,decompositions.size());
	}

	@Test
	public void test_analyse__Case_tikittuq() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "tikittuq";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
//		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",2,decompositions.size());
	}

	@Test
	public void test_analyze__Case_tikinniaqtuq() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "tikinniaqtuq";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		assertEquals("",6,analyses.size());
	}

	@Test
	public void test_analyze__Case_umiarjualiuqti() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "umiarjualiuqti";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",9,decompositions.size());
	}

	@Test @Ignore
	public void test_analyze__Case_maligaliuqti() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "maligaliuqti";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		for (int i=0; i<analyses.size(); i++) System.out.println((i+1)+". "+analyses.get(i).toStr());
		assertEquals("",7,analyses.size());
	}

	@Test
	public void test_analyze__Case_niruarut() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "niruarut";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
//		for (int i=0; i<analyses.size(); i++) System.out.println((i+1)+". "+analyses.get(i).toStr());
		assertEquals("",2,analyses.size());
	}

	@Test
	public void test_analyze__Case_umiarut() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "umiarut";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		assertEquals("",1,analyses.size());
	}

}
