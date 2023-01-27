package org.iutools.text;

import static ca.nrc.testing.RunOnCases.Case;

import static org.iutools.script.TransCoder.Script;

import ca.nrc.testing.RunOnCases;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.function.Consumer;

public class IUWordTest {

	@Test
	public void test__IUWord__VariousCases() throws Exception {
		CaseIUWord[] cases = new CaseIUWord[] {
			new CaseIUWord("ROMAN word", "inuksuk")
				.expectScript(Script.ROMAN)
				.expectRoman("inuksuk")
				.expectSyllabic("ᐃᓄᒃᓱᒃ"),

			new CaseIUWord("SYLL word", "ᐃᓄᒃᓱᒃ")
				.expectScript(Script.SYLLABIC)
				.expectRoman("inuksuk")
				.expectSyllabic("ᐃᓄᒃᓱᒃ"),

			new CaseIUWord("LATIN numeric expresions", "1987-mut")
				.expectScript(Script.ROMAN)
				.expectRoman("1987-mut")
				.expectSyllabic("1987-ᒧᑦ"),

			new CaseIUWord("English word -- should raise exception", "hello")
				.expectInvalid(),

			new CaseIUWord("ROMAN word that starts with uppercase 'H'", "Hakirviksaq")
				.expectScript(Script.ROMAN)
				.expectRoman("Hakirviksaq")
				// TODO: This is actually wrong... maybe the H should be deleted?
				.expectSyllabic("ᕼᐊᑭᕐᕕᒃᓴᖅ"),

			new CaseIUWord("ROMAN word that starts with lowercase 'h'", "hakirviksaq")
				.expectScript(Script.ROMAN)
				.expectRoman("hakirviksaq")
				.expectSyllabic("ᓴᑭᕐᕕᒃᓴᖅ"),

			new CaseIUWord("ROMAN word that has 'h' but NOT at start of word",
				"inukshuk")
				.expectScript(Script.ROMAN)
				.expectRoman("inukshuk")
				// Actually, this is the transcoding for "inukssuk", not
				// the input "inukshuk". I guess the transcoder automatically
				// replaces 'sh' -> 'ss'.
				//
				.expectSyllabic("ᐃᓄᒃᔅᓱᒃ"),

			new CaseIUWord("ROMAN word that has 'H' but NOT at start of word",
				"juHaanaspuug")
				.expectScript(Script.ROMAN)
				.expectRoman("juHaanaspuug")
				.expectSyllabic("ᔪᕼᐋᓇᔅᐴᒡ"),

			new CaseIUWord("ROMAN word with ???",
					"angaadjuvik")
					.expectScript(Script.ROMAN)
					.expectRoman("BLAH")
					.expectSyllabic("BLAH"),
		};
		Consumer<Case> runner = (caseNoCast) -> {
			CaseIUWord aCase = (CaseIUWord) caseNoCast;
			IUWord iuWord = null;
			try {
				iuWord = new IUWord(aCase.word);
				if (aCase.expInvalid) {
					throw new RuntimeException("Creating an IUWord for "+aCase.word+" SHOULD have raised exception");
				}
			} catch (WordException e) {
				if (!aCase.expInvalid) {
					throw new RuntimeException("Creating an IUWord for "+aCase.word+" should NOT have raised exception", e);
				}
			}
			if (aCase.expScript != null) {
				Assertions.assertEquals(
					aCase.expScript, iuWord.origScript(),
					"Word script not as expected"
				);
			}
			if (aCase.expRoman != null) {
				Assertions.assertEquals(
					aCase.expRoman, iuWord.inRoman(),
					"Word in Roman was not as expected"
				);
			}
			if (aCase.expSyll != null) {
				Assertions.assertEquals(
					aCase.expSyll, iuWord.inSyll(),
					"Word in Syllabic was not as expected"
				);
			}
		};
		new RunOnCases(cases, runner)
			.onlyCaseNums(9)
			.run();
	}

	////////////////////////////////////////
	// TEST HELPERS
	////////////////////////////////////////

	public static class CaseIUWord extends Case {

		public String word;
		public boolean expInvalid = false;
		public String expRoman;
		public String expSyll;
		public Script expScript;

		public CaseIUWord(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
		}

		public CaseIUWord expectScript(Script _expScript) {
			this.expScript = _expScript;
			return this;
		}

		public CaseIUWord expectInvalid() {
			this.expInvalid = true;
			return this;
		}

		public CaseIUWord expectRoman(String _expRoman) {
			this.expRoman = _expRoman;
			return this;
		}

		public CaseIUWord expectSyllabic(String _expSyll) {
			this.expSyll = _expSyll;
			return this;
		}

	}
}
