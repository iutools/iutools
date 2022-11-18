package org.iutools.worddict;

import static ca.nrc.testing.RunOnCases.Case;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GlossaryTest {

	////////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////////

	@Test
	public void test__Glossary__Synopsis() throws Exception {
		// The Glossary class is a human-generated glossary of iu-en words and terms.
		// You typically create it by loading one or more glossary files into it.
		//
		Glossary glossary = Glossary.get();

		// Given a word or term in one language, you can get a list of glossary
		// entries for it
		String enWord = "transport";
		List<GlossaryEntry> entries = glossary.entries4word("en", enWord);

		for (GlossaryEntry entry: entries) {
			// This provides the translations of the word in IU
			List<String> iuTerms = entry.termsInLang("iu");
			// Source of the entry (ex: wikipedia)
			String source = entry.source;
			// Reference for the entry, usually a URL to a specific page in the source
			String reference = entry.reference;
		}
	}

	////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////

	@Test
	public void test__entries4word__VariousCases() throws Exception {
		GlossaryCase[] cases = new GlossaryCase[] {
			new GlossaryCase(
			   "En term - SINGLE IU equivalent",
				"en", "transport",
				"iu", "ingirrajjutit"),
			new GlossaryCase(
			   "IU Roman term - SINGLE EN equivalent",
				"iu", "ingirrajjutit",
				"en", "transport"),
			new GlossaryCase(
			   "En term - MULTIPLE IU equivalents",
				"en", "tea",
				"iu", "niuqqaq", "tiirlu"),
		};

		Consumer<Case> runner = (uncastCase) -> {
			GlossaryCase aCase = (GlossaryCase) uncastCase;
			try {
				List<GlossaryEntry> entries =
					Glossary.get().entries4word(aCase.lang, aCase.term);

				List<String> gotOtherTerms = new ArrayList<String>();
				for (GlossaryEntry entry : entries) {
					gotOtherTerms.addAll(entry.termsInLang(aCase.otherLang));
				}
				AssertObject.assertDeepEquals(
					"Translations in " + aCase.otherLang + " not as expected for term " + aCase.term,
					aCase.otherTerms, gotOtherTerms
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(3)
			.run();
	}

	////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////

	public static class GlossaryCase extends Case {

		String lang = null;
		String term = null;
		String otherLang = null;
		String[] otherTerms = null;

		public GlossaryCase(String _descr, String _lang, String _term,
			String _otherLang, String... _otherTerms) {
			super(_descr, null);
			this.lang = _lang;
			this.term = _term;
			this.otherLang = _otherLang;
			this.otherTerms = _otherTerms;
		}
	}

}
