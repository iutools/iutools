package ca.inuktitutcomputing.morph.exp;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.json.PrettyPrinter;

public class WordAnalyzerTest {

	@Test
	public void test_findRoot__Case_1() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
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
	
	@Test
	public void test_findAffix__Case_1() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "lu";
		List<String> affixElements = wordAnalyzer.findAffix(string);
//		for (int i=0; i<affixElements.size(); i++) System.out.println((i+1)+". "+affixElements.get(i));
		assertEquals("",45,affixElements.size());
	}
	
	@Test
	public void test_analyze__Case_inullu() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "inullu";
		List<Decomposition> decompositions = wordAnalyzer.analyze(string);
//		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",1,decompositions.size());
	}

	@Test
	public void test_analyse__Case_tikittuq() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "tikittuq";
		List<Decomposition> decompositions = wordAnalyzer.analyze(string);
//		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",2,decompositions.size());
	}

	@Test
	public void test_analyze__Case_tikinniaqtuq() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "tikinniaqtuq";
		List<Decomposition> analyses = wordAnalyzer.analyze(string);
		assertEquals("",6,analyses.size());
	}

	@Test
	public void test_analyze__Case_umiarjualiuqti() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "umiarjualiuqti";
		List<Decomposition> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",9,decompositions.size());
	}

	@Test @Ignore
	public void test_analyze__Case_maligaliuqti() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "maligaliuqti";
		List<Decomposition> analyses = wordAnalyzer.analyze(string);
		for (int i=0; i<analyses.size(); i++) System.out.println((i+1)+". "+analyses.get(i).toStr());
		assertEquals("",7,analyses.size());
	}

	@Test
	public void test_analyze__Case_niruarut() throws Exception {
		LinguisticDataSingleton.getInstance("csv");
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "niruarut";
		List<Decomposition> analyses = wordAnalyzer.analyze(string);
//		for (int i=0; i<analyses.size(); i++) System.out.println((i+1)+". "+analyses.get(i).toStr());
		assertEquals("",2,analyses.size());
	}

	@Test
	public void test_analyze__Case_umiarut() throws Exception {
		WordAnalyzer wordAnalyzer = new WordAnalyzer();
		String string = "umiarut";
		List<Decomposition> analyses = wordAnalyzer.analyze(string);
		assertEquals("",1,analyses.size());
	}

}
