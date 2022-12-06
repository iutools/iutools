package org.iutools.webservice.spell;

import ca.nrc.testing.AssertString;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CheckWordResultTest {

	@Test
	public void test__resultLogEntry__MisspelledWord() throws Exception {
		CheckWordResult result = new CheckWordResult().setCorrection("inukssuk", "inuksuk");
		String gotEntry = result.resultLogEntry().toString();
		String expEntry = "{\"misspelledWord\":\"inukssuk\"}";
		AssertString.assertStringEquals(
			"Result log entry was wrong for a mispelled word",
			expEntry, gotEntry
		);
	}

	@Test
	public void test__resultLogEntry__CorrectlySpelledWord() throws Exception {
		CheckWordResult result = new CheckWordResult().setCorrection("inuksuk", "inuksuk");
		JSONObject gotEntry = result.resultLogEntry();
		Assertions.assertTrue(null == gotEntry,
			"Result log entry should have been null for a correctly spelled word"
			);
	}
}
