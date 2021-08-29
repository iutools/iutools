package org.iutools.morph.l2r;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertSet;
import ca.nrc.testing.AssertString;
import org.iutools.morph.AssertDecompositionList;
import org.iutools.morph.DecompositionSimple;
import org.junit.Ignore;
import org.junit.Test;

import ca.nrc.json.PrettyPrinter;


public class MorphologicalAnalyzer_L2RTest {

	@Test
	public void test_findRoot__Case_1() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "inullu";
		List<String> rootElements = wordAnalyzer.findRoot(string);
		assertAtLeastNMorphemes(
			3, rootElements,
			"List of roots was too small");
		assertIncludesMorphemes(
			"Missing a root for word "+string,
			rootElements,
			"{\"surfaceForm\":\"i\",\"morphemeId\":\"ik/rad-sc\"}");
	}

	@Test
	public void test_findRoot__Case_2() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "tikinniaqtuq";
		List<String> rootElements = wordAnalyzer.findRoot(string);
		assertAtLeastNMorphemes(
			3, rootElements,
			"List of roots was too small for word "+string);
		assertIncludesMorphemes(
			"Missing a root for word "+string,
			rootElements,
			"{\"surfaceForm\":\"tiki\",\"morphemeId\":\"tikiq/1n\"}");


		string = "tikivviulaurama";
		rootElements = wordAnalyzer.findRoot(string);
		assertIncludesMorphemes(
			"Missing a root for word "+string,
			rootElements,
			"{\"surfaceForm\":\"tiki\",\"morphemeId\":\"tikiq/1n\"}");
		assertAtLeastNMorphemes(
			3, rootElements,
			"List of roots was too small for word "+string);
	}
	
	@Test
	public void test_findAffix__Case_1() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "lu";
		List<String> affixElements = wordAnalyzer.findAffix(string);
		assertIncludesMorphemes(
			"Missing an affix for word "+string,
			affixElements,
			"{\"surfaceForm\":\"l\",\"endOfStem\":\"2V\",\"context\":\"q\",\"morphemeId\":\"k/tn-gen-d\"}");
		assertAtLeastNMorphemes(
			20, affixElements,
			"List of affixes was too small for word "+string);
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyze__Case_inullu() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "inullu";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",1,decompositions.size());
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyse__Case_tikittuq() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "tikittuq";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
//		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",2,decompositions.size());
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyze__Case_tikinniaqtuq() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "tikinniaqtuq";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		assertEquals("",6,analyses.size());
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyze__Case_umiarjualiuqti() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "umiarjualiuqti";
		List<DecompositionSimple> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",9,decompositions.size());
	}

	@Test
	public void test_analyze__Case_maligaliuqti() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "maligaliuqti";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		new AssertDecompositionList(
			analyses.toArray(new DecompositionSimple[0]),
			"Decompositions for word "+string)
			.producesAtLeastNDecomps(7)
			.includesDecomps("{{maliga:maligaq/1n}}{{liuq:liuq/1nv}}{{ti:ji/1vn}}");
	}

	@Test
	public void test_analyze__Case_niruarut() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "niruarut";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		new AssertDecompositionList(analyses.toArray(new DecompositionSimple[0]))
			.includesDecomps("{{nirua:niruaq/1v}}{{rut:ut/1vn}}")
			.producesAtLeastNDecomps(4);
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyze__Case_umiarut() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "umiarut";
		List<DecompositionSimple> analyses = wordAnalyzer.analyze(string);
		assertEquals("",1,analyses.size());
	}

	/////////////////////////////////
	// TEST HELPERS
	/////////////////////////////////

	private void assertAtLeastNMorphemes(
		int expMin, List<String> gotAffixes, String mess) {
		String gotAffixesStr = StringUtils.join(gotAffixes.iterator(), "\n   ");
		AssertNumber.isGreaterOrEqualTo(
			mess+"\nNumber of affixes found was too low\n"+
			"Got affixes:\n   "+gotAffixesStr,
			gotAffixes.size(), expMin);
	}

	private void assertIncludesMorphemes(
		String mess, List<String> gotMorphemes, String... expMorphemes) {
		String allMorphemes = "   "+StringUtils.join(gotMorphemes.iterator(), "\n   ");
		for (String anExpMorpheme: expMorphemes) {
			AssertString.assertStringContains(
				mess+"\nMorpheme "+anExpMorpheme+" was missing.",
				allMorphemes, anExpMorpheme
			);
		}

	}

}
