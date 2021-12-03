package org.iutools.concordancer.tm;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class TMEvaluatorTest {

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
