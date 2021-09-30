package org.iutools.webservice.spell;

import ca.nrc.testing.AssertString;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpellResultTest {

	@Test
	public void test__resultLogEntry__MisspelledWord() throws Exception {
		SpellResult result = new SpellResult().addCorrection("inukssuk", "inuksuk");
		String gotEntry = result.resultLogEntry().toString();
		String expEntry = "{\"misspelledWords\":[{\"orig\":\"inukssuk\",\"suggestedSpellings\":[\"inuksuk\"]}]}";
		AssertString.assertStringEquals(
			"Result log entry was wrong for a mispelled word",
			expEntry, gotEntry
		);
	}

	@Test
	public void test__resultLogEntry__CorrectlySpelledWord() throws Exception {
		SpellResult result = new SpellResult().addCorrection("inuksuk", "inuksuk");
		JSONObject gotEntry = result.resultLogEntry();
		Assertions.assertTrue(null == gotEntry,
			"Result log entry should have been null for a correctly spelled word"
			);
	}

}
