package org.iutools.text;

import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.iutools.script.TransCoder;
import static org.iutools.script.TransCoder.Script;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class WordTest {

	///////////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////////

	@Test
	public void test__Word__Synopsis() throws Exception {
		/**
		 * The Word class encapsulates a word in such a way that we know and remember
		 * the language of the word (en or iu) and, in the case of an Inuktitut wordm
		 * the Script it is written in.
		 *
		 * Furthermore, for an Inuktitut Word, we can get it in either Scripts, without
		 * having to constantly transcode it from one script to another.
		 *
		 * Word is an abstract class and there are two concrete subclasses:
		 * - Word: for Inuktitut words
		 * - NonWord: for non-Inuktitut words
		 */

		// For example, say you have an Inukitut Word in ROMAN script
		IUWord iuWord = new IUWord("inuksuk");

		// You can ask to have the Word in either script
		//
		// Note that the Word only every gets transcoded once.
		// So if you keep changing between Roman and Syllabic, you will
		// only encur the transcoding overhead once.
		//
		String inRoman = iuWord.inRoman();
		Assertions.assertEquals("inuksuk", inRoman);
		String inSyll = iuWord.inSyll();
		Assertions.assertEquals("ᐃᓄᒃᓱᒃ", inSyll);


		// Alternatively, you can this instead
		inRoman = iuWord.inScript(TransCoder.Script.ROMAN);
		Assertions.assertEquals("inuksuk", inRoman);
		inSyll = iuWord.inScript(TransCoder.Script.SYLLABIC);
		Assertions.assertEquals("ᐃᓄᒃᓱᒃ", inSyll);

		// You can also ask for the script that the Word was originally written
		// in, or the Word as it was originally written
		Script origScript = iuWord.origScript();
		Assertions.assertEquals(Script.ROMAN, origScript);
		String origWord = iuWord.word();
		Assertions.assertEquals("inuksuk", origWord);

		// Although the Word class is mostly useful for Inuktitut words, you can
		// also use it to wrap English words. This is mostly useful to avoid passing
		// different types of objects (String vs Word) to methods that may receive
		// either an English or Inuktitut Word
		//
		NonIUWord enWord = new NonIUWord("hello", "en");

		// You can distinguish between English and Inuktitut words by their
		// lang() attribute
		//
		Assertions.assertEquals("en", enWord.lang());
		Assertions.assertEquals("iu", iuWord.lang());

		// If you are not sure if a word is in Inuktitut or English, you can
		// use the Word.make() method to create a Word object.
		// The returned Word will be either an IUWord or a NonIUWord, depending
		// on whether or not the word's written form is in Inuktitut
		Word word = Word.build("computing");
		Assertions.assertTrue(word instanceof NonIUWord);
		word = Word.build("inuksuk");
		Assertions.assertTrue(word instanceof IUWord);
	}

	//////////////////////////////////////////
	// VERIFICATION TESTS
	//////////////////////////////////////////

	@Test
	public void test__make__VariousCases() throws Exception {
		CaseMake[] cases = new CaseMake[] {
			new CaseMake("Roman word", "inuksuk", IUWord.class),
			new CaseMake("Syll word", "ᐃᓄᒃᓱᒃ", IUWord.class),
			new CaseMake("English world", "computing", NonIUWord.class),
		};
		Consumer<Case> runner = (caseUncast) -> {
			CaseMake aCase = (CaseMake) caseUncast;
			Word word = null;
			try {
				word = Word.build(aCase.word);
			} catch (WordException e) {
				throw new RuntimeException(e);
			}
			Assertions.assertTrue(aCase.expClass.isInstance(word));
		};

		new RunOnCases(cases, runner)
			.run();
	}


	//////////////////////////////////////////
	// TEST HELPERS
	//////////////////////////////////////////

	public static class CaseMake extends Case {

		public String word;
		public Class<? extends Word> expClass;

		public CaseMake(String _descr, String _word, Class<? extends Word> _expClass) {
			super(_descr, null);
			this.word = _word;
			this.expClass = _expClass;
		}
	}
}
