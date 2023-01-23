package org.iutools.webservice.tokenize;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.RunOnCases;
import static ca.nrc.testing.RunOnCases.Case;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class WordsAreIUEndpointTest extends EndpointTest {
    @Override
    public Endpoint makeEndpoint() throws Exception {
        return new WordsAreIUEndpoint();
    }

	@Test
	public void test__WordsAreIUEndpoint__VariousCases() throws Exception {
        RunOnCases.Case[] cases = new Case[] {
            new Case("Single roman Inuktitut word", "inuksuk"),
            new Case("Single syll Inuktitut word", "ᐃᓄᒃᓱᒃ"),
            new Case("Single roman Inuktitut word with H", "inuksHuk"),
            new Case("Single english word", "hello", "hello"),

            new Case("Multiple roman Inuktitut words", "inuksuk, ammuumajuq"),
            new Case("multiple syll Inuktitut word", "ᐃᓄᒃᓱᒃ, ᐊᒻᒨᒪᔪᖅ"),
            new Case("Multiple words, some English", "inuksuk, hello", "hello"),
            new Case("Multiple words, all English", "hello, world", "hello", "world"),
        };

        Consumer<Case> runner = (caze) -> {
            String inputText = (String)caze.data[0];
            String[] expNonIUWords = new String[0];
            if (caze.data.length > 1) {
                expNonIUWords = new String[caze.data.length-1];
                for (int ii=1; ii < caze.data.length; ii++) {
                    expNonIUWords[ii-1] = (String)caze.data[ii];
                }
            }
            Boolean expAllIU = (expNonIUWords.length == 0);

            WordsAreIUInputs inputs = new WordsAreIUInputs().setText(inputText);
            WordsAreUIResult epResult = null;
            try {
                epResult = (WordsAreUIResult) endPoint.executeThenConvert(inputs);
                AssertObject.assertDeepEquals("List of non-IU words not as expected",
                    expNonIUWords, epResult.nonIUWords);
                Assertions.assertEquals(expAllIU, epResult.allWordAreIU);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };


        new RunOnCases(cases, runner)
            .run();
	}

}
