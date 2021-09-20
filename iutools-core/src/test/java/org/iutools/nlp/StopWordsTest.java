package org.iutools.nlp;

import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class StopWordsTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__StopWords__Synopsis() throws Exception {
		// Use this class to remove stopwords from some text
		String text = "Joy to the world";
		String lang = "en";
		String withoutSWs = StopWords.remove(lang, text);
	}

	///////////////////////////////////////
	// VERIFICATION TESTS
	///////////////////////////////////////

	@Test
	public void test__StopWords__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("en:Happy path", "en", "Joy to the world", "Joy * * world"),
			new Case("en:some sws are uppercased", "en", "Joy To The World", "Joy * * World"),
			new Case("en:some sws followed by punctuation", "en", "Joy to: the world", "Joy *: * world"),
			new Case("en:null text", "en", null, null),
			new Case("iu:non-empty text", "iu",
				"ᐃᒡᓗᓕᕆᓂᕐᓕ, ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ.", "ᐃᒡᓗᓕᕆᓂᕐᓕ, ᐃᒡᓗᓕᕆᓂᕐᒧᑐᐃᓐᓈᕋᔭᖅᑐᖅ."),
		};
		Consumer<Case> runner = (aCase) -> {
			String lang = (String)aCase.data[0];
			String origText = (String)aCase.data[1];
			String expText = (String)aCase.data[2];
			try {
				String gotText = StopWords.remove(lang, origText);
				AssertString.assertStringEquals(
					aCase.descr, expText, gotText
				);
			} catch (StopWordsException e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
			.run();
	}
}