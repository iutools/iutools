package ca.inuktitutcomputing.script;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class SyllabicsTest {
	@Test
	public void test__syllabicCharsRatio__HappyPath() {
		String text = "I need some ᑮᓇᐅᔭᖅ";
		double gotRatio = Syllabics.syllabicCharsRatio(text);
		double expRatio = 0.36;
		Assert.assertEquals("Ratio of syllabic characters was wrong.", expRatio, gotRatio, 0.01);
	}

}
