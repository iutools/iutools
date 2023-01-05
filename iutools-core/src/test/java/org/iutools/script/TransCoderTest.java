package org.iutools.script;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import static org.iutools.script.TransCoder.Script;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.testing.AssertString;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TransCoderTest {

	//////////////////////////
	// DOCUMENTATION TEST
	//////////////////////////
	
	@Test
	public void test__TransCoder__Synopsis() throws Exception {
		// Use this class to transcode between Roman and Syllabics scripts
		//
		// For example, given some text in syllabics
		String text = "ᐃᓄᒃᑐᑦ";
		
		// You can transcode it to Roman like this:
		//
		String romanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, text);
		
		// Note that it's perfectly to pass a text that is already in Roman.
		// In that case, the system will just return the text as is.
		//
		romanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, "inuktut");
		
		// You can also ask that a word be transcoded to the same script 
		// as another
		//
		String textRoman = "inuktut";
		String otherTextSyll = "ᓄᓇᕗᑦ";
		String otherTextInRoman = 
			TransCoder.ensureSameScriptAsSecond(otherTextSyll, textRoman);
		
		// You can ask the TransCoder what script a text seems to be written 
		// in
		//
		@SuppressWarnings("unused")
		TransCoder.Script script = TransCoder.textScript(romanText);

		// Some of the methods can be aplied to collections of String.
		// For example
		List<String> words = new ArrayList<String>();
		words.add("inuksuk"); words.add("ammuumajuq");
		List<String> syllWords = (List<String>) TransCoder.ensureSyllabic(words);
	}

	//////////////////////////
	// VERIFICATION TEST
	//////////////////////////

	@Test
	public void test__ensureScript__VariousCases() {
		Case_inOtherScript[] cases = new Case_inOtherScript[] {
			new Case_inOtherScript("Roman word with 'H' in middle",
				"juHaanaspuug", "ᔪᕺᓇᔅᐴᒡ"),
		};

		Consumer<Case>	runner = (Case caseUncast) -> {
			Case_inOtherScript caze = (Case_inOtherScript) caseUncast;
			String got = null;
			try {
				got = TransCoder.inOtherScript(caze.origWord);
				AssertString.assertStringEquals(caze.descr, caze.expOtherScriptWord, got);
			} catch (TransCoderException e) {
				throw new RuntimeException(e);
			}
		};
	}

	@Test
	public void test__ensureSyllabic__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			// Sometimes, roman words may have a 'b' in them (which should
			// really be a 'p').
			new Case("Roman word with a 'b' in it", "subluit", "ᓱᑉᓗᐃᑦ"),
		};

		Consumer<Case> runner = (Case caze) -> {
			String got = null;
			try {
				got = TransCoder.ensureSyllabic((String)caze.data[0]);
				AssertString.assertStringEquals(caze.descr, (String)caze.data[1], got);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};
		new RunOnCases(cases, runner)
			.run();
	}

	@Test
	public void test__ensureScript__Syll2Rom__HappyPath() throws Exception{
		String syllText = "ᐃᓄᒃᑐᑦ, 2020";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, syllText);
		AssertString.assertStringEquals("inuktut, 2020", gotRomanText);
	}

	@Test
	public void test__ensureScript__inuk() throws Exception{
		String text = "inuk";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, text);
		AssertString.assertStringEquals("inuk", gotRomanText);
	}
	
	@Test
	public void test__ensureScript__Rom2Syll__HappyPath() throws Exception{
		String syllText = "inuktut, 2020";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.SYLLABIC, syllText);
		AssertString.assertStringEquals("ᐃᓄᒃᑐᑦ, 2020", gotRomanText);
	}

	@Test
	public void test__ensureSameScriptAsSecond__FirstSyllSecondRoman() 
			throws Exception {
		String first = "ᓄᓇᕗᑦ, 2020";
		String second = "inuktut, 2019";
		String gotTranscFirst = TransCoder.ensureSameScriptAsSecond(first, second);
		AssertString.assertStringEquals("nunavut, 2020", gotTranscFirst);
	}
	
	@Test
	public void test__ensureSameScriptAsSecond__FirstRomanSecondSyll() 
			throws Exception {
		String first = "inuktut, 2019";
		String second = "ᓄᓇᕗᑦ, 2020";
		String gotTranscFirst = TransCoder.ensureSameScriptAsSecond(first, second);
		AssertString.assertStringEquals("ᐃᓄᒃᑐᑦ, 2019", gotTranscFirst);
	}

	@Test
	public void test__ensureSameScriptAsSecond__BothRoman() 
			throws Exception {
		String first = "inuktut, 2019";
		String second = "nunavut, 2020";
		String gotTranscFirst = TransCoder.ensureSameScriptAsSecond(first, second);
		AssertString.assertStringEquals("inuktut, 2019", gotTranscFirst);
	}

	@Test
	public void test__ensureSameScriptAsSecond__BothSyllabic() 
			throws Exception {
		String first = "ᐃᓄᒃᑐᑦ, 2019";
		String second = "ᓄᓇᕗᑦ, 2020";
		String gotTranscFirst = TransCoder.ensureSameScriptAsSecond(first, second);
		AssertString.assertStringEquals("ᐃᓄᒃᑐᑦ, 2019", gotTranscFirst);
	}

	@Test
	public void test__ensureScript__Rom2Rom__HappyPath() throws Exception{
		String syllText = "inuktut, 2020";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, syllText);
		AssertString.assertStringEquals("inuktut, 2020", gotRomanText);
	}

	@Test
	public void test__ensureScript__Syll2Syll__HappyPath() throws Exception{
		String syllText = "ᐃᓄᒃᑐᑦ, 2020";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.SYLLABIC, syllText);
		AssertString.assertStringEquals("ᐃᓄᒃᑐᑦ, 2020", gotRomanText);
	}

	@Test
	public void test__ensureScript__Mixed2Rom__HappyPath() throws Exception{
		String syllText = "ᐃᓄᒃᑐᑦ-1, inuktut-2";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, syllText);
		AssertString.assertStringEquals("inuktut-1, inuktut-2", gotRomanText);
	}

	@Test
	public void test__ensureScript__Mixed2Syll__HappyPath() throws Exception{
		String syllText = "ᐃᓄᒃᑐᑦ-1, inuktut-2";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.SYLLABIC, syllText);
		AssertString.assertStringEquals("ᐃᓄᒃᑐᑦ-1, ᐃᓄᒃᑐᑦ-2", gotRomanText);
	}

	@Test
	public void test__ensureScript__WordsThatContainADoubleAmpersand() throws Exception{
		String syllText = "ᐊᕐᕕᐊᕐᒦᖦᖢᑎᒃ";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, syllText);
		AssertString.assertStringEquals("arviarmii&&utik", gotRomanText);
	}

	@Test
	public void test__textScript__VariousCase() throws Exception {
		Case_textScript[] cases = new Case_textScript[] {
			new Case_textScript("ROMAN word",
				"nunavut", Script.ROMAN),
			new Case_textScript("ROMAN capitalized word",
				"Inuktut", Script.ROMAN),
			new Case_textScript("ROMAN text with some punctuation",
				"inuktut, 2020", Script.ROMAN),
			new Case_textScript("SYLL word",
				"ᐃᓄᒃᑐᑦ", Script.SYLLABIC),
			new Case_textScript("ROMAN word that starts with upercased 'H' ",
				"Hakirviksaq", Script.ROMAN),
			new Case_textScript("ROMAN word that starts with lowercased 'h'",
				"hakirviksaq", Script.ROMAN),
			new Case_textScript("MIXED script text ",
				"ᐃᓄᒃᑐᑦ inuktut", Script.MIXED),
			new Case_textScript("ROMAN word with 'h' in middle of it ",
				"inukshuk", Script.ROMAN),
			new Case_textScript("ROMAN word with 'H' in middle of it ",
				"juHaanaspuug", Script.ROMAN),
			new Case_textScript("Not sure what's wrong with this one.... ",
				"su", Script.ROMAN),
		};
		Consumer<Case> runner = (caseNoCast) -> {
			Case_textScript aCase = (Case_textScript) caseNoCast;
			Script gotScript = TransCoder.textScript(aCase.text);
			Assertions.assertEquals(
				aCase.expScript, gotScript,
				"Script not as expected for text: "+aCase.text
			);
		};
		new RunOnCases(cases, runner)
//			.onlyCaseNums(10)
			.run();
	}

	@Test
	public void test__textScript__Mixed() {
		String text = "ᐃᓄᒃᑐᑦ inuktut";
		TransCoder.Script gotScript = TransCoder.textScript(text);
		Assert.assertEquals("Wrong script for text "+text, 
				TransCoder.Script.MIXED, gotScript);
	}

	@Test
	public void test__ensureSyllabic__ListInput() throws Exception {
		List<String> origWords = new ArrayList<String>();
		origWords.add("inuksuk"); origWords.add("ammuumajuq");
		List<String> gotSyllWords = (List<String>) TransCoder.ensureSyllabic(origWords);
		String[] expSyllWords = new String[] {"ᐃᓄᒃᓱᒃ", "ᐊᒻᒨᒪᔪᖅ"};
		AssertObject.assertDeepEquals(
			"Words not correctly transcoded to syllabics",
			expSyllWords, gotSyllWords
		);
	}

	///////////////////////////
	// TEST HELPERS
	///////////////////////////

	public static class Case_inOtherScript extends Case {

		private String expOtherScriptWord;
		public String origWord = null;
		public Case_inOtherScript(String _descr, String _origWord, String _expOtherScriptWord) {
			super(_descr, null);
			this.origWord = _origWord;
			this.expOtherScriptWord = _expOtherScriptWord;
		}
	}
	public static class Case_textScript extends Case {

		String text = null;
		TransCoder.Script expScript = null;
		public Case_textScript(String _descr, String _text, TransCoder.Script _expScript) {
			super(_descr, null);
			this.text = _text;
			this.expScript = _expScript;
		}
	}
}
