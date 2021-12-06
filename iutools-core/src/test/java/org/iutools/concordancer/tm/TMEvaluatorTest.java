package org.iutools.concordancer.tm;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.iutools.concordancer.tm.TMEvaluator.MatchType;

import java.util.function.Consumer;

public class TMEvaluatorTest {

	@Test @Disabled
	public void test__findTerm__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("singleword STRICT match",
				"hello", "hello world",
				MatchType.STRICT, "hell"),
			new Case("singleword LENIENT match",
				"greetings", "greet the world",
				MatchType.LENIENT, "hell"),
			new Case("singleword NO match",
				"greetings", "hello world",
				null, null),

			new Case("multiword STRICT match",
				"hello world", "I say hello world",
				MatchType.STRICT, "hello world"),
			new Case("multiword LENIENT match",
				"greetings world", "greet world",
				MatchType.LENIENT, "greet"),
			new Case("multiword LENIENT_OVERLAP match",
				"greetings universe", "greet world",
				MatchType.LENIENT_OVERLAP, "greet"),
			new Case("multiword NO match",
				"greetings universe", "greet world",
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
//			.onlyCaseNums(7)
			.run();
	}


	@Test @Disabled
	public void test__sameTerm__VariousCases() {
		Assertions.fail("IMPLEMENT");
	}

	@Test @Disabled
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
	public void test__findText__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("singleword-strict-found", "hello", false, "hello world", "hello"),
			new Case("singleword-strict-notfound", "greetings", false, "hello world", null),
			new Case("multiword-strict-found", "hello world", false, "i say hello world", "hello world"),
			new Case("multiword-strict-notfound", "greetings world", false, "hello world", null),

			new Case("singleword-lenient-found", "Hello", true, "hello world", "hello"),
			new Case("singleword-lenient-notfound", "greetings", true, "hello world", null),
			new Case("multiword-lenient-found", "hello worldss", true, "i say hello world", "hello world"),
			new Case("multiword-lenient-notfound", "greetings world", true, "hello world", null),
			new Case("lenient-contains-special-chars", "Hello [world]!", true, "hello world", "hello world"),


		};
		Consumer<Case> runner = (aCase) -> {
			String toFind = (String)aCase.data[0];
			Boolean lenient = (Boolean)aCase.data[1];
			String inText = (String)aCase.data[2];
			String expFound = (String)aCase.data[3];

			try {
				String gotFound = new TMEvaluator().findText(toFind, inText, lenient);
				AssertString.assertStringEquals(
					"Result of findText not as expected.",
					expFound, gotFound);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(7)
			.run();
	}

	@Test
	public void test__partialOverlap__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("Two SINGLE-word strings, that OVERLAP; STRICT --> NO overlap",
				false, "truncating", "truncation", null),
			new Case("Two SINGLE-word strings, that do NOT OVERLAP; STRICT --> NO overlap",
				false, "hello", "world", null),
			new Case("Two SINGLE-word strings, that OVERLAP; LENIENT --> OVERLAP",
				true, "truncating", "truncation", "trunc"),
			new Case("Two SINGLE-word strings, that do NOT overlap; LENIENT --> NO overlap",
				true, "hello", "world", null),

			new Case("Two MULTI-word strings, with ONE word that OVERLAP; STRICT --> NO overlap",
				false, "truncating words", "truncation of strings", null),
			new Case("Two MULTI-word strings, with NO word that OVERLAP; STRICT --> NO overlap",
				false, "hello world", "greetings universe", null),
			new Case("Two MULTI-word strings, with ONE word that OVERLAP; LENIENT --> OVERLAP",
				true, "truncating words", "truncation of strings", "trunc"),
			new Case("Two MULTI-word strings, with NO word that OVERLAP; LENIENT --> NO overlap",
				true, "hello world", "greetings universe", null),
		};
		Consumer<Case> runner = (aCase) -> {
			Boolean lenient = (Boolean)aCase.data[0];
			String str1 = (String)aCase.data[1];
			String str2 = (String)aCase.data[2];
			String expOverlap = (String)aCase.data[3];

			try {
				String gotOverlaps = new TMEvaluator()
					.partialOverlap(str1, str2, lenient);
				Assertions.assertEquals(
					expOverlap, gotOverlaps,
					"Output of partiallyOverlap() not as expected.");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
			.run();
	}
}
