package org.iutools.script;

import ca.nrc.testing.AssertCollection;
import ca.nrc.testing.AssertObject;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.testing.AssertString;

import java.util.ArrayList;
import java.util.List;

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
	public void test__ensureScript__Syll2Rom__HappyPath() throws Exception{
		String syllText = "ᐃᓄᒃᑐᑦ, 2020";
		String gotRomanText = TransCoder.ensureScript(TransCoder.Script.ROMAN, syllText);
		AssertString.assertStringEquals("inuktut, 2020", gotRomanText);
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
	public void test__textScript__Roman() {
		String text = "inuktut, 2020";
		TransCoder.Script gotScript = TransCoder.textScript(text);
		Assert.assertEquals("Wrong script for text "+text, 
				TransCoder.Script.ROMAN, gotScript);
	}

	@Test
	public void test__textScript__RomanWithSomeCapitalizedWords() {
		String text = "Inuktut, 2020";
		TransCoder.Script gotScript = TransCoder.textScript(text);
		Assert.assertEquals("Wrong script for text "+text,
		TransCoder.Script.ROMAN, gotScript);
	}

	@Test
	public void test__textScript__Syllabic() {
		String text = "ᐃᓄᒃᑐᑦ";
		TransCoder.Script gotScript = TransCoder.textScript(text);
		Assert.assertEquals("Wrong script for text "+text, 
				TransCoder.Script.SYLLABIC, gotScript);
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
}
