package ca.pirurvik.iutools.concordancer;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import ca.nrc.testing.AssertString;

public class LanguageGuesser_IUTest {

	@Test
	public void test__detect__HappyPath() throws Exception {
		LanguageGuesser_IU guesser = new LanguageGuesser_IU();
		
		String text = "Hello world. Take me to your leader.";
		String gotLang = guesser.detect(text);
		Assert.assertEquals("en", gotLang);

		text = "ᐅᓪᓗᒥᒧᑦ ᑎᑭᖦᖢᒍ, ᖃᐅᔨᒪᔭᐅᔪᓂᒃ ᓄᕙᒡᔪᐊᕐᓇᖅ-19−ᒧᑦ ᐱᑕᖃᙱᓚᖅ ᑕᒫᓂ ᐅᑭᐅᖅᑕᖅᑐᒥ ᐊᕕᒃᓯᒪᓂᕆᔭᐅᔪᒥ";
		gotLang = guesser.detect(text);
		Assert.assertEquals("iu", gotLang);
	}

}
