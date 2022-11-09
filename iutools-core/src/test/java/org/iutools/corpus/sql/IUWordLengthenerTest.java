package org.iutools.corpus.sql;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class IUWordLengthenerTest {

	//////////////////////////////////////////
	// DOCUMENTATION TESTS
	//////////////////////////////////////////

	@Test
	public void test__IUWordLengthener__Synopsis() {
		// There is a problem with SQL FULLTEXT indexing of inuktitut text namely,
		// SQL ignores words whose lenght is shorter than 4 characters.
		// This doesn't matter for english text because most of those short
		// words are stopwords anyway.
		//
		// But in Inuktitut there are lots of non-stopwords whose length is <= 4.
		//
		// The IUWordLengthener artificially lengthens words contained in some IU
		// text so they will not be ignored.
		//
		// For example, this text contains words that have 4 characters or less...
		//
		String textWithShortWords = "inuksuk, iglu, tut";

		// But this lengthened version does not
		String textWithLengthenedWords = IUWordLengthener.lengthen(textWithShortWords);
		Assertions.assertEquals("inuksuk, igluZZZZZ, tutZZZZZ", textWithLengthenedWords);

		// Given some text whose words may have been lengthened, you can restore
		// it back to the original text
		String restoredWord = IUWordLengthener.restoreLengths(textWithLengthenedWords);
		Assertions.assertEquals(textWithShortWords, restoredWord);
	}

	//////////////////////////////////////////
	// VERIFICATION
	//////////////////////////////////////////

	@Test
	public void test__lengthen__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("latin-multiwords", "tut, inuksuk, iglu", "tutZZZZZ, inuksuk, igluZZZZZ"),
			new Case("latin-singleword-short", "tut", "tutZZZZZ"),
			new Case("latin-singleword-long", "inuksuk", "inuksuk"),

			new Case("syll-multiwords", "ᑐᑦ, ᐃᓄᒃᓱᒃ, ᐃᒡᓗ", "ᑐᑦZZZZZ, ᐃᓄᒃᓱᒃ, ᐃᒡᓗZZZZZ"),
			new Case("syll-singleword-short", "ᑐᑦ", "ᑐᑦZZZZZ"),
			new Case("syll-singleword-long", "ᐃᓄᒃᓱᒃ", "ᐃᓄᒃᓱᒃ"),

			new Case("empty string", "", ""),
			new Case("null string", null, null),
		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			try {
				String origText = (String)aCase.data[0];
				String expLengthenedText = (String)aCase.data[1];
				String gotLengthenedText = IUWordLengthener.lengthen(origText);
				AssertString.assertStringEquals(
					aCase.descr+"\nWords not properly lengthened for text '"+origText+"'",
						expLengthenedText, gotLengthenedText);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
			.run();
	}

	@Test
	public void test__restoreLengths__VariousCases() throws Exception {
		Case[] cases = new Case[]{
		new Case("latin-multiwords", "tutZZZZZ, inuksuk, igluZZZZZ", "tut, inuksuk, iglu"),
		new Case("latin-singleword-short", "tutZZZZZ", "tut"),
		new Case("latin-singleword-long", "inuksuk", "inuksuk"),

		new Case("syll-multiwords", "ᑐᑦZZZZZ, ᐃᓄᒃᓱᒃ, ᐃᒡᓗZZZZZ", "ᑐᑦ, ᐃᓄᒃᓱᒃ, ᐃᒡᓗ"),
		new Case("syll-singleword-short", "ᑐᑦZZZZZ", "ᑐᑦ"),
		new Case("syll-singleword-long", "ᐃᓄᒃᓱᒃ", "ᐃᓄᒃᓱᒃ"),

		new Case("empty string", "", ""),
		new Case("null string", null, null),
		};

		Consumer<RunOnCases.Case> runner = (aCase) -> {
			try {
				String lengthenedText = (String) aCase.data[0];
				String expRestoredText = (String) aCase.data[1];
				String gotRestoredText = IUWordLengthener.restoreLengths(lengthenedText);
				AssertString.assertStringEquals(
				aCase.descr + "\nWords not properly restored to original lengths for text '" + lengthenedText + "'",
				expRestoredText, gotRestoredText);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
		.run();
	}
}
