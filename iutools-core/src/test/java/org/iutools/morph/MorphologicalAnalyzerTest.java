package org.iutools.morph;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public abstract class MorphologicalAnalyzerTest {

	private MorphologicalAnalyzer analyzer;

	public abstract MorphologicalAnalyzer makeAnalyzer();

	@Before
	public void setUp() {
		this.analyzer = makeAnalyzer();
	}

	////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////

	@Test
	public void test__MorphologicalAnalyzer_Synopsis() throws Exception {
		// By default, the analysis times out after 10 seconds; time ou can be set different
		analyzer.setTimeout(15000); // in milliseconds

		// By default, the timing out is active; it can be disactivated and reactivated
		analyzer.disactivateTimeout();
		analyzer.activateTimeout();
		
		// The purpose of the analyzer is to decompose an inuktitut word into its morphemes
		String word = "iglumik";
		try {
			 Decomposition[] analyses = analyzer.decomposeWord(word);
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
		analyzer.setTimeout(2000);
		String word = "ilisaqsitittijunnaqsisimannginnama";
		analyzer.decomposeWord(word);
	}

	@Test(expected=TimeoutException.class)
	public void test__decomposeWord__timeout_10s() throws Exception  {
		String word = "ilisaqsitittijunnaqsisimannginnama";
		analyzer.decomposeWord(word);
	}

	@Test
	public void test__decomposeWord__maligatigut() throws Exception  {
		String word = "maligatigut";
		analyzer.disactivateTimeout();
		Decomposition[] decs = analyzer.decomposeWord(word);
		new AssertDecompositionList(decs)
			// No decomposition produced for this word
			.includesAtLeastOneOfDecomps();
	}

	@Test
	public void test__decomposeWord__uqaqtiup() throws Exception  {
		String word = "uqaqtiup";
		analyzer.disactivateTimeout();;

		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple)
			.includesAtLeastOneOfDecomps(
				"{uqaq:uqaq/1v}{ti:ji/1vn}{up:up/tn-gen-s}",
				"{uqaq:uqaq/1v}{ti:tiq/1vn}{up:ut/2nn}");
	}

	@Test
	public void test__decomposeWord__sivuliuqtii() throws Exception  {
		String word = "sivuliuqtii";
		analyzer.disactivateTimeout();;

		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple)
			.includesAtLeastOneOfDecomps(
				"{sivuliuqti:sivuliuqti/1n}{i:k/tn-nom-d}",
				"{sivuliuqti:sivuliuqti/1n}{i:it/3nv}");
	}

	@Test
	public void test__decomposeWord__ammalu() throws Exception  {
		String word = "ammalu";
		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple)
			.includesAtLeastOneOfDecomps(
				"{ammalu:ammalu/1c}");
	}

		
	/*
	 * Ce test vérifie que les analyses qui contiennent des séquences de
	 * morphèmes pour lesquelles il existe un morphème combiné sont supprimées.
	 * Ex. : la première analyse est supprimée puisque dans la seconde, il y a
	 *       le morphème juksaq qui est la combinaison de juq et ksaq.
	 * {apiq:apiq/1v}{suq:suq/1vv}{ta:jaq/1vn}{u:u/1nv}{ju:juq/1vn}{ksaq:ksaq/1nn}
	 * {apiq:apiq/1v}{suq:suq/1vv}{ta:jaq/1vn}{u:u/1nv}{juksaq:juksaq/1vn}
	 */
	@Test
	public void test__decomposeWord__apiqsuqtaujuksaq() throws Exception  {
		String word = "apiqsuqtaujuksaq";

		analyzer.disactivateTimeout();;

		// AD: This is how Benoit used to test this and it worked.
//		DecompositionState[] decs = analyzer.decomposeWord(word);
//		Assert.assertTrue(decs.length!=0);
//		for (int i=0; i<decs.length; i++) {
//			Assert.assertTrue(!decs[i].toStr2().contains("{ju:juq/1vn}{ksaq:ksaq/1nn}"));
//		}

		// AD: This is how Alain now tests this, but the new Decomposition class does not seem to
		// pass this test
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple)
			.allDecompsContain("ju:juq/1vn ksaq:ksaq/1nn");
	}


	@Test
	public void test__decomposeWord__immagaa() throws Exception  {
		String word = "immagaa";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple)
			// No decomp for this word
			.includesAtLeastOneOfDecomps();
	}

	@Test
	public void test__decomposeWord__avunnga_Extensions() throws Exception  {
		String word = "avunngaqtuq";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps("{avunngaq:avunngaq/1v}{tuq:juq/1vn}");


		word = "avunngaujjijuq";
		decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps("{avunnga:avunngaq/1v}{ujji:ujji/1vv}{juq:juq/1vn}");

		word = "avunngautijuq";
		decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{avunnga:avunngaq/1v}{uti:uti/1vv}{juq:juq/1vn}");
	}


	@Test
	public void test__decomposeWord__atuagaq() throws Exception  {
		String word = "atuagaq";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{atua:atuaq/1v}{gaq:gaq/1vn}");
	}

	@Test
	public void test_decomposeWord__maligaliuqti() throws Exception {
		String string = "maligaliuqti";
		Decomposition[] analyses = analyzer.decomposeWord(string);
		new AssertDecompositionList(
			analyses,
			"Decompositions for word "+string)
			.producesAtLeastNDecomps(7)
			.includesDecomps("{maliga:maligaq/1n}{liuq:liuq/1nv}{ti:ji/1vn}");
	}

	@Test
	public void test__decomposeWord__maligaliuqtinik() throws Exception  {
		String word = "maligaliuqtinik";

		analyzer.disactivateTimeout();
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{maliga:maligaq/1n}{liuq:liuq/1nv}{ti:ji/1vn}{nik:nik/tn-acc-p}",
				"{maliga:maligaq/1n}{liuq:liuq/1nv}{tin:tit/1vv}{ik:it/1vv}");
	}

	@Test
	public void test__decomposeWord__sivungujuq() throws Exception  {
		String word = "sivungujuq";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{sivu:sivu/1n}{ngu:ngu/1nv}{juq:juq/1vn}"
			);
	}

	@Test
	public void test__decomposeWord__with_extendedAnalysis() throws Exception  {
		String word = "makpiga";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.atLeastOneDecompContains("ga:gaq/1vn");
	}

	@Test
	public void test__decomposeWord__without_extendedAnalysis() throws Exception  {
		String word = "makpiga";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word, false);
		new AssertDecompositionList(decSimple, "word="+word+", NO extended analysis")
			.includesAtLeastOneOfDecomps();
	}

	@Test
	public void test__decomposeWord__noun_root_alone() throws Exception  {
		String word = "angut";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps("{angut:angut/1n}");
	}

	@Test
	public void test__decomposeWord__inungmut() throws Exception  {
		String word = "inungmut";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{inung:inuk/1n}{mut:mut/tn-dat-s}");
	}

	@Test
	public void test__decomposeWord__siniktitsijuq() throws Exception  {
		String word = "siniktitsijuq";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{sinik:sinik/1v}{tit:tit/1vv}{si:si/1vv}{juq:juq/1vn}",
				"{sinik:sinik/1n}{titsi:gipsi/tv-imp-2p}{juq:juq/tv-ger-3s}");
	}

	@Test
	public void test__decomposeWord__siniktittijuq() throws Exception  {
		String word = "siniktittijuq";

		analyzer.disactivateTimeout();;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word="+word)
			.includesAtLeastOneOfDecomps(
				"{sinik:sinik/1v}{tit:tiq/1vn}{ti:si/4nv}{juq:juq/1vn}",
				"{sinik:sinik/1v}{titti:gissik/tv-imp-2d}{juq:juq/tv-ger-3s}",
				"{sinik:sinik/1n}{titti:gissik/tv-imp-2d}{juq:juq/tv-ger-3s}");
	}

	@Test
	public void test__decomposeWord__pivalliatittinirmut() throws Exception {
		String word = "pivalliatittinirmut";

		analyzer.disactivateTimeout();
		;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word=" + word)
			.includesAtLeastOneOfDecomps(
				"{pi:pi/1v}{vallia:vallia/1vv}{tit:tiq/1vn}{ti:si/4nv}{nir:niq/2vn}{mut:mut/tn-dat-s}",
				"{piv:vit/tv-int-2s}{allia:alliaq/1n}{tit:tik/tn-nom-p-2d}{t:t/tn-nom-p}{inir:iniq/1v}{mut:mut/tn-dat-s}");
	}

	@Test
	public void test__decomposeWord__siniktittiniq() throws Exception {
		String word = "siniktittiniq";

		analyzer.disactivateTimeout();
		;
		Decomposition[] decSimple = analyzer.decomposeWord(word);
		new AssertDecompositionList(decSimple, "word=" + word)
			.includesAtLeastOneOfDecomps(
				"{sinik:sinik/1v}{tit:tiq/1vn}{ti:si/4nv}{niq:niq/2vn}",
				"{sinik:sinik/1n}{tit:tik/tn-nom-p-2d}{t:t/tn-nom-p}{iniq:iniq/1v}");
	}

	@Test
	public void test_decomposeWord__niruarut() throws Exception {
		String string = "niruarut";
		Decomposition[] analyses = analyzer.decomposeWord(string);
		new AssertDecompositionList(analyses)
			.includesAtLeastOneOfDecomps(
				"{nirua:niruaq/1v}{rut:ut/1vn}",
				"{niruar:niruaq/1v}{ut:ut/1vn}")
			.producesAtLeastNDecomps(4);
	}
}
