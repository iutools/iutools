package org.iutools.morph;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MorphologicalAnalyzerTest {

	////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////

	@Test
	public void test__MorphologicalAnalyzer_Synopsis() throws Exception {
		// By default, the analyzer uses the linguistic database in the CSV format
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();

		// By default, the analysis times out after 10 seconds; time ou can be set different
		analyzer.setTimeout(15000); // in milliseconds

		// By default, the timing out is active; it can be disactivated and reactivated
		analyzer.disactivateTimeout();
		analyzer.activateTimeout();
		
		// The purpose of the analyzer is to decompose an inuktitut word into its morphemes
		String word = "iglumik";
		try {
			 DecompositionSimple[] analyses = analyzer.decomposeWord__NEW(word);
		} catch (TimeoutException e) {
			// This happens if the decomposer timed out before it could complete
			// the analysis.
		}
	}

	////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////

	
	@Test(expected=TimeoutException.class)
	public void test__decomposeWord__timeout_2s() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		analyzer.setTimeout(2000);
		String word = "ilisaqsitittijunnaqsisimannginnama";
		analyzer.decomposeWord__NEW(word);
	}

	@Test(expected=TimeoutException.class)
	public void test__decomposeWord__timeout_10s() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "ilisaqsitittijunnaqsisimannginnama";
		analyzer.decomposeWord__NEW(word);
	}

	@Test
	public void test__decomposeWord__maligatigut() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "maligatigut";
		analyzer.disactivateTimeout();
		DecompositionSimple[] decs = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decs)
			// No decomposition produced for this word
			.decompIs();
	}

	@Test
	public void test__decomposeWord__uqaqtiup() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "uqaqtiup";
		analyzer.disactivateTimeout();;

		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple)
			.decompIs("uqaq:uqaq/1v", "ti:ji/1vn", "up:up/tn-gen-s");
	}

	@Test
	public void test__decomposeWord__sivuliuqtii() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "sivuliuqtii";
		analyzer.disactivateTimeout();;

		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple)
			.decompIs("sivuliuqti:sivuliuqti/1n", "i:k/tn-nom-d");
	}

	@Test
	public void test__decomposeWord__ammalu() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "ammalu";
		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple)
			.decompIs("ammalu:ammalu/1c");
	}

		
	/*
	 * Ce test vérifie que les analyses qui contiennent des séquences de
	 * morphèmes pour lesquelles il existe un morphème combiné sont supprimées.
	 * Ex. : la première analyse est supprimée puisque dans la seconde, il y a
	 *       le morphème juksaq qui est la combinaison de juq et ksaq.
	 * {apiq:apiq/1v}{suq:suq/1vv}{ta:jaq/1vn}{u:u/1nv}{ju:juq/1vn}{ksaq:ksaq/1nn}
	 * {apiq:apiq/1v}{suq:suq/1vv}{ta:jaq/1vn}{u:u/1nv}{juksaq:juksaq/1vn}
	 */
	@Test @Ignore
	public void test__decomposeWord__apiqsuqtaujuksaq() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "apiqsuqtaujuksaq";

		analyzer.disactivateTimeout();;

		// AD: This used to work...
//		Decomposition[] decs = analyzer.decomposeWord(word);
//		Assert.assertTrue(decs.length!=0);
//		for (int i=0; i<decs.length; i++) {
//			Assert.assertTrue(!decs[i].toStr2().contains("{ju:juq/1vn}{ksaq:ksaq/1nn}"));
//		}

		// AD: ... but the new DecompositionSimple class does not seem to
		// pass this test
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple)
			.allDecompsContain("ju:juq/1vn ksaq:ksaq/1nn");
	}


	@Test
	public void test__decomposeWord__immagaa() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "immagaa";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple)
			// No decomp for this word
			.decompIs();
	}

	@Test
	public void test__decomposeWord__avunnga_Extensions() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "avunngaqtuq";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("avunngaq:avunngaq/1v", "tuq:juq/1vn");


		word = "avunngaujjijuq";
		decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("avunnga:avunngaq/1v", "ujji:ujji/1vv", "juq:juq/1vn");

		word = "avunngautijuq";
		decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("avunnga:avunngaq/1v", "uti:uti/1vv", "juq:juq/1vn");
	}


	@Test
	public void test__decomposeWord__atuagaq() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "atuagaq";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("atua:atuaq/1v", "gaq:gaq/1vn");
	}


	@Test
	public void test__decomposeWord__maligaliuqtinik() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "maligaliuqtinik";

		analyzer.disactivateTimeout();
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("maliga:maligaq/1n", "liuq:liuq/1nv", "ti:ji/1vn",
				"nik:nik/tn-acc-p");
	}

	@Test
	public void test__decomposeWord__sivungujuq() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "sivungujuq";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.atLeastOneDecompContains("sivu:sivu/1n ngu:u/1nv");
	}

	@Test
	public void test__decomposeWord__with_extendedAnalysis() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "makpiga";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.atLeastOneDecompContains("ga:gaq/1vn");
	}

	@Test
	public void test__decomposeWord__without_extendedAnalysis() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "makpiga";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word, false);
		new AssertDecompositionList(decSimple, "word="+word+", NO extended analysis")
			.decompIs();
	}

	@Test
	public void test__decomposeWord__noun_root_alone() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "angut";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("angut:angut/1n");
	}

	@Test
	public void test__decomposeWord__inungmut() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "inungmut";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("inung:inuk/1n", "mut:mut/tn-dat-s");
	}

	@Test
	public void test__decomposeWord__siniktitsijuq() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "siniktitsijuq";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("sinik:sinik/1v", "tit:tit/1vv", "si:si/1vv", "juq:juq/1vn");
	}

	@Test
	public void test__decomposeWord__siniktittijuq() throws Exception  {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "siniktittijuq";

		analyzer.disactivateTimeout();;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.decompIs("sinik:sinik/1v", "tit:tiq/1vn", "ti:si/4nv", "juq:juq/1vn");
	}


	@Test
	public void test__decomposeWord__pivalliatittinirmut() throws Exception {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "pivalliatittinirmut";

		analyzer.disactivateTimeout();
		;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word=" + word)
			.decompIs("pi:pi/1v", "vallia:vallia/1vv", "tit:tiq/1vn", "ti:si/4nv",
				"nir:niq/2vn", "mut:mut/tn-dat-s");
	}

	@Test
	public void test__decomposeWord__siniktittiniq() throws Exception {
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		String word = "siniktittiniq";

		analyzer.disactivateTimeout();
		;
		DecompositionSimple[] decSimple = analyzer.decomposeWord__NEW(word);
		new AssertDecompositionList(decSimple, "word=" + word)
			.decompIs("sinik:sinik/1v", "tit:tiq/1vn", "ti:si/4nv", "niq:niq/2vn");
	}
}
