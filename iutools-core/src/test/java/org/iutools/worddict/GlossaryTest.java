package org.iutools.worddict;

import static ca.nrc.testing.RunOnCases.Case;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.RunOnCases;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
			new GlossaryCase("En term - SINGLE IU equivalent", "en", "transport")
				.translationsAre("iu", "ingirrajjutit"),
			new GlossaryCase(
			   "IU Roman term - SINGLE EN equivalent", "iu", "ingirrajjutit")
				.translationsAre("en", "transport"),
			new GlossaryCase(
			   "En term - MULTIPLE IU equivalents", "en", "tea")
				.translationsAre("iu", "niuqqaq", "tiirlu"),
			new GlossaryCase(
			   "IU word that only appears in an iutools glossary", "iu", "Haakiq")
				.definitionInLang("en", "to play hockey")
				.definitionInLang("fr", "jouer au hockey"),
		};

		Consumer<Case> runner = (uncastCase) -> {
			GlossaryCase aCase = (GlossaryCase) uncastCase;
			try {
				List<GlossaryEntry> entries =
					Glossary.get().entries4word(aCase.lang, aCase.term);

				List<String> gotOtherTerms = new ArrayList<String>();
				Map<String,String> gotDefinitions = new HashMap<String,String>();
				for (GlossaryEntry entry : entries) {
					if (aCase.otherLang != null) {
						gotOtherTerms.addAll(entry.termsInLang(aCase.otherLang));
					}
					gotDefinitions.put("en", entry.getEn_def());
					gotDefinitions.put("iu", entry.getIu_def());
					gotDefinitions.put("fr", entry.getFr_def());
				}
				if (aCase.otherTerms != null) {
					AssertObject.assertDeepEquals(
						"Translations in " + aCase.otherLang + " not as expected for term " + aCase.term,
						aCase.otherTerms, gotOtherTerms);
				}
				for (String lang: aCase.expectedDefinitions.keySet()) {
					String expectedDef = aCase.expectedDefinitions.get(lang);
					String gotDef = gotDefinitions.get(lang);
					AssertObject.assertDeepEquals(
						"Definition in " + lang + " not as expected for term " + aCase.term,
						expectedDef, gotDef);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(4)
			.run();
	}

	@Test
	public void test__keyFor__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("Single IU word with nothing special", "inuksuk", "iu", "iu:inuksuk"),
			new Case("IU word with a hyphen", "inuk-suk", "iu", "iu:inuk-suk"),
			new Case("Multi word IU term", "inuk suk", "iu", "iu:inuk suk"),

			new Case("Single EN word with nothing special", "hello", "en", "en:hello"),
			new Case("EN word with a hyphen", "hello-world", "en", "en:hello-world"),
			new Case("Multi word EN term", "hello world", "en", "en:hello world"),
		};

		Consumer<Case> runner = (caze) -> {
			String term = (String)caze.data[0];
			String lang = (String)caze.data[1];
			String expKey = (String)caze.data[2];
			String gotKey = null;
			try {
				gotKey = Glossary.keyFor(lang, term);
			} catch (GlossaryException e) {
				throw new RuntimeException(e);
			}
			AssertString.assertStringEquals("Bad key for", expKey, gotKey);
		};
		new RunOnCases(cases, runner)
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

		Map<String,String> expectedDefinitions = new HashMap<String,String>();

		public GlossaryCase(String _descr, String _lang, String _term) {
			super(_descr, null);
			this.lang = _lang;
			this.term = _term;
		}

		public GlossaryCase translationsAre(String _otherLang, String... _otherTerms) {
			otherTerms = _otherTerms;
			otherLang = _otherLang;
			return this;
		}

		public GlossaryCase definitionInLang(String lang, String def) {
			expectedDefinitions.put(lang, def);
			return this;
		}
	}

}
