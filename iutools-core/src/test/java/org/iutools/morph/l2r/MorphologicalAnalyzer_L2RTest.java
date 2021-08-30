package org.iutools.morph.l2r;

import static org.junit.Assert.*;

import java.util.List;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertNumber;
import ca.nrc.testing.AssertString;
import org.iutools.morph.Decomposition;
import org.iutools.morph.MorphologicalAnalyzerAbstract;
import org.iutools.morph.MorphologicalAnalyzerAbstractTest;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class MorphologicalAnalyzer_L2RTest extends MorphologicalAnalyzerAbstractTest {

	@Override
	public MorphologicalAnalyzerAbstract makeAnalyzer() {
		MorphologicalAnalyzer_L2R analyzer = null;
		try {
			analyzer = new MorphologicalAnalyzer_L2R();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return analyzer;
	}

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
	public void test_decomposeWord__Case_inullu() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "inullu";
		List<Decomposition> decompositions = wordAnalyzer.analyze(string);
		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",1,decompositions.size());
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_analyse__Case_tikittuq() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "tikittuq";
		Decomposition[] decompositions = wordAnalyzer.decomposeWord(string);
//		for (int i=0; i<decompositions.size(); i++) System.out.println((i+1)+". "+decompositions.get(i).toStr());
		assertEquals("",2,decompositions.length);
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_decomposeWord__Case_tikinniaqtuq() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "tikinniaqtuq";
		Decomposition[] analyses = wordAnalyzer.decomposeWord(string);
		assertEquals("",6,analyses.length);
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_decomposeWord__Case_umiarjualiuqti() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "umiarjualiuqti";
		Decomposition[] decompositions = wordAnalyzer.decomposeWord(string);
		for (int i=0; i<decompositions.length; i++) System.out.println((i+1)+". "+decompositions[i].toStr());
		assertEquals("",9,decompositions.length);
	}

	// Produces null pointer exception
	@Test @Ignore
	public void test_decomposeWord__Case_umiarut() throws Exception {
		MorphologicalAnalyzer_L2R wordAnalyzer = new MorphologicalAnalyzer_L2R();
		String string = "umiarut";
		Decomposition[] analyses = wordAnalyzer.decomposeWord(string);
		assertEquals("",1,analyses.length);
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
