package org.iutools.concordancer.tm;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.script.TransCoder;
import org.iutools.worddict.GlossaryEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.iutools.concordancer.tm.TMEvaluator.MatchType;

import java.io.IOException;
import java.util.function.Consumer;

public class TMEvaluatorTest {

	@Test
	public void test__evaluateGlossaryTerm__VariousCases() throws Exception {
		Case[] cases = new Case[] {

			// A case where the IU term is absent from the TM
			new Case("ullugiak",
				"ullugiak", "astronomical object",
				false, false, null, null),

			// A case of multi-word IU entry (which are skipped)
			new Case("inuit qaujimanituqangit",
				"inuit qaujimanituqangit", "Inuit Qaujimajatuqangit",
				true, false, null, null),


			// These next cases are those among the first 20 entries of WP where:
			// - The IU term was single-word
			// - We found the IU term in the TM
			//
			new Case("amiq",
				"amiq", "Wikimedia main page",
				false, true, null, null),

			new Case("nunavut",
				"nunavut", "nunavut",
				false, true, MatchType.STRICT, MatchType.STRICT),
			new Case("annuraanik",
				"annuraanik", "Inuit clothingᒃ",
				false, true, MatchType.LENIENT_OVERLAP, MatchType.LENIENT_OVERLAP),
			new Case("qarasaujaq",
				"qarasaujaq", "computer",
				false, true, MatchType.STRICT, MatchType.STRICT),
			new Case("ilinniaqtuliriniq",
				"ilinniaqtuliriniq", "education",
				false, true, MatchType.STRICT, MatchType.STRICT),
			new Case("titiraujaq",
				"titiraujaq", "engineering",
				false, true, null, null),
 		};

		Consumer<Case> runner = (aCase) -> {
			String iuTerm_roman = (String) aCase.data[0];
			String enTerm = (String) aCase.data[1];
			Boolean expSkipped = (Boolean) aCase.data[2];
			Boolean expIUPresent = (Boolean) aCase.data[3];
			MatchType expENPresent_Sense = (MatchType) aCase.data[4];
			MatchType expENSpotted_Sense = (MatchType) aCase.data[5];

			try {
				String iuTerm_syll = TransCoder.ensureSyllabic(iuTerm_roman);
				GlossaryEntry glossEntry = new GlossaryEntry()
					.setTermInLang("iu_roman", iuTerm_roman)
					.setTermInLang("en", enTerm);
				EvaluationResults results = new EvaluationResults();
				new TMEvaluator().setVerbosity(UserIO.Verbosity.Level2)
					.evaluateGlossaryTerm(glossEntry, results);

				int expTotalSingleWordEntries = 1;
				if (expSkipped) {
					expTotalSingleWordEntries = 0;
				}
				new AssertEvaluationResults(results)
					.totalSingleIUTermEntries(expTotalSingleWordEntries);

				int expTotalIUPresent = 0;
				if (expIUPresent) {
					expTotalIUPresent = 1;
				}
				Assertions.assertEquals(
					expTotalIUPresent, results.totalIUPresent_Orig,
					"Wrong value for totalIUPresent_Orig"
				);

				FrequencyHistogram<MatchType> expENPresent_hist =
					new FrequencyHistogram<MatchType>();
				if (expENPresent_Sense != null) {
						expENPresent_hist.updateFreq(expENPresent_Sense);
				}
				AssertObject.assertDeepEquals(
					"Wrong frequency histogram for enPresent_Histogram",
					expENPresent_hist, results.enPresent_Histogram
				);

				FrequencyHistogram<MatchType> expENSpotted_hist =
					new FrequencyHistogram<MatchType>();
				if (expENSpotted_Sense != null) {
						expENSpotted_hist.updateFreq(expENSpotted_Sense);
				}
				AssertObject.assertDeepEquals(
					"Wrong frequency histogram for enSpotted_Histogram",
					expENSpotted_hist, results.enSpotted_Histogram
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(3)
//			.onlyCasesWithDescr("nunavut")
			.run();
	}

	@Test
	public void test__findTerm__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("singleword STRICT match",
				"hello world", "hello world",
				MatchType.STRICT, "hello world"),
			new Case("singleword LENIENT match",
				"greetings", "greet the world",
				MatchType.LENIENT, "greet*"),
			new Case("singleword NO match",
				"greetings", "hello world",
				null, null),

			new Case("multiword STRICT match",
				"hello world", "I say hello world",
				MatchType.STRICT, "hello world"),
			new Case("multiword LENIENT match",
				"greetings world", "greet world",
				MatchType.LENIENT, "greet* world*"),
			new Case("multiword LENIENT_OVERLAP match",
				"greetings universe", "greet world",
				MatchType.LENIENT_OVERLAP, "greet*"),
			new Case("multiword NO match",
				"greetings universe", "hello world",
				null, null),
		};
		Consumer<Case> runner = (aCase) -> {
			String term = (String)aCase.data[0];
			String inText = (String)aCase.data[1];
			MatchType expType = (MatchType)aCase.data[2];
			String expFound = (String)aCase.data[3];

			try {
				Pair<MatchType,String> occurence = new TMEvaluator()
					.findTerm(term, inText);
				MatchType gotType = occurence.getLeft();
				String gotFound = occurence.getRight();
				Assertions.assertEquals(
					expType, gotType,
					"Wrong match type"
				);
				AssertString.assertStringEquals(
					"Wrong occurence found.",
					expFound, gotFound);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
			.run();
	}


	@Test
	public void test__sameTerm__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("single word STRICT match",
				"hello", "hello", MatchType.STRICT, "hello"),
			new Case("single word LENIENT match",
				"greetings", "greet", MatchType.LENIENT, "greet*"),
			new Case("single word NO match",
				"hello", "greetings", null, null),

			new Case("multi word STRICT match",
				"hello ... world", "hello ... world", MatchType.STRICT, "hello ... world"),
			new Case("multi word LENIENT match",
				"greetings ... universe", "greet ... universal",
				MatchType.LENIENT, "greet* ... unive*"),
			new Case("multi word LENIENT_OVERLAP match",
				"greetings ... universe", "greet the world",
				MatchType.LENIENT_OVERLAP, "greet*"),
			new Case("multi word NO match",
				"hello world", "greetings universe",
				null, null),
		};

		Consumer<Case> runner = (aCase) -> {
			String term1 = (String) aCase.data[0];
			String term2 = (String) aCase.data[1];
			MatchType expType = (MatchType) aCase.data[2];
			String expOcc = (String) aCase.data[3];

			String[] term1Toks = TMEvaluator.tokenize(term1);
			String[] term2Toks = TMEvaluator.tokenize(term2);
			Pair<MatchType,String> match = TMEvaluator.sameTerm(term1Toks, term2Toks);
			MatchType gotType = match.getLeft();
			String gotOcc = match.getRight();
			Assertions.assertEquals(
				expType, gotType,
				"Wrong MatchType for terms: "+term1+","+term2);
			Assertions.assertEquals(
				expOcc, gotOcc,
				"Wrong occurence for terms: "+term1+","+term2);
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(7)
			.run();
	}

	@Test
	public void test__isMoreLenient__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("STRICT == STRICT",
				false, MatchType.STRICT, MatchType.STRICT),
			new Case("STRICT > LENIENT",
				false, MatchType.STRICT, MatchType.LENIENT),
			new Case("STRICT > LENIENT_OVERLAP",
				false, MatchType.STRICT, MatchType.LENIENT_OVERLAP),

			new Case("LENIENT < STRICT",
				true, MatchType.LENIENT, MatchType.STRICT),
			new Case("LENIENT == LENIENT",
				false, MatchType.LENIENT, MatchType.LENIENT),
			new Case("LENIENT > LENIENT_OVERLAP",
				false, MatchType.LENIENT, MatchType.LENIENT_OVERLAP),

			new Case("LENIENT_OVERLAP < STRICT",
				true, MatchType.LENIENT_OVERLAP, MatchType.STRICT),
			new Case("LENIENT_OVERLAP < LENIENT",
				true, MatchType.LENIENT_OVERLAP, MatchType.LENIENT),
			new Case("LENIENT_OVERLAP == LENIENT_OVERLAP",
				false, MatchType.LENIENT_OVERLAP, MatchType.LENIENT_OVERLAP),

			new Case("null vs non-null --> false",
				false, null, MatchType.LENIENT_OVERLAP),
			new Case("non-null vs null --> false",
				false, MatchType.LENIENT_OVERLAP, null),
			new Case("null vs null --> false",
				false, null, null),
		};

		Consumer<Case> runner = (aCase) -> {
			Boolean expResult = (Boolean) aCase.data[0];
			MatchType type1 = (MatchType) aCase.data[1];
			MatchType type2 = (MatchType) aCase.data[2];
			Boolean gotResult = TMEvaluator.isMoreLenient(type1, type2);
			Assertions.assertEquals(
				expResult, gotResult,
				"Wrong answer for isMoreLenient() for type1="+type1+", type2="+type2
			);
		};

		new RunOnCases(cases, runner)
			.run();
	}

	@Test
	public void test__tokenize__SeveralCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("No punctuation",
				"hello world", new String[] {"hello", " ", "world"}),
			new Case("Punctuation",
				"hello, world", new String[] {"hello", ", ", "world"}),
			new Case("Hyphen (should not cause a token split)",
				"first-class", new String[] {"first-class"}),
			new Case("Inuktitut text with & character",
				"ikiaq&iq", new String[] {"ikiaq&iq"}, "iu"),
		};

		Consumer<Case> runner = (aCase) -> {
			String text = (String) aCase.data[0];
			String[] expTokens = (String[]) aCase.data[1];
			String lang = null;
			if (aCase.data.length > 2) {
				lang = (String) aCase.data[2];
			}
			String[] gotTokens = TMEvaluator.tokenize(text, lang);
			try {
				AssertObject.assertDeepEquals(
					"Wrong tokens for text '"+text+"'",
					expTokens, gotTokens
				);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
			.run();

	}

	@Test
	public void test__areSynonyms__VariousCases() throws Exception {
		CaseSynonyms[] cases = new CaseSynonyms[] {
			new CaseSynonyms("mutli-word synonymous",
				"indigenous people", "aboriginal people", true),
			new CaseSynonyms("word always synonym with itself",
				"hello", "hello", true),
			new CaseSynonyms("mutli-word NON synonymous",
				"indigenous people", "canadian people", false),
			new CaseSynonyms("synonymit is case insensitive",
				"indigenous people", "Aboriginal People", true),

		};

		Consumer<Case> runner = (caseUncasted) -> {
			CaseSynonyms aCase = (CaseSynonyms) caseUncasted;
			String expr1 = aCase.expression1;
			String expr2 = aCase.expression2;
			Boolean expSynonimity = aCase.expSynonymity;
			Boolean gotSynonymity = TMEvaluator.areSynonyms(expr1, expr2);
			Assertions.assertEquals(
				expSynonimity, gotSynonymity,
				"Synonymity of expressions "+expr1+" and "+expr2+" was not as expected");
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(2)
			.run();
	}


	////////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////////

	public static class CaseSynonyms extends Case {

		String expression1 = null;
		String expression2 = null;
		Boolean expSynonymity = null;

		public CaseSynonyms(String _descr, String _expr1, String _expr2,
			boolean _expSynonymity) {
			super(_descr, null);
			this.expression1 = _expr1;
			this.expression2 = _expr2;
			this.expSynonymity = _expSynonymity;
		}
	}

}
