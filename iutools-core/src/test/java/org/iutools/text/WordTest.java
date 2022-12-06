package org.iutools.text;

import org.iutools.script.TransCoder;
import static org.iutools.script.TransCoder.Script;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WordTest {

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
	}
}
