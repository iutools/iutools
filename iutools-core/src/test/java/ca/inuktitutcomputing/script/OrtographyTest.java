package ca.inuktitutcomputing.script;

import ca.nrc.testing.AssertString;
import org.junit.Test;

public class OrtographyTest {

    @Test
    public void test__trimTrailingConsonant__WordEndsWithConsonant() {
        String word = "inuktitut";
        String gotTrimmed = Orthography.trimTrailingConsonant(word);
        AssertString.assertStringEquals(
        "Trimming did not produce expected results for word: "+word,
            "inuktitu", gotTrimmed);
    }

    @Test
    public void test__trimTrailingConsonant__WordDoesNOTEndWithConsonant() {
        String word = "nunavu";
        String gotTrimmed = Orthography.trimTrailingConsonant(word);
        AssertString.assertStringEquals(
                "Trimming did not produce expected results for word: "+word,
                "nunavu", gotTrimmed);
    }
}
