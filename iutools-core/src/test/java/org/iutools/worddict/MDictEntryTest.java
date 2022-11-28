package org.iutools.worddict;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class MDictEntryTest {

	////////////////////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////////////////////

	@Test
	public void test__MapFromJson() throws Exception {
		String json =
			"{\n" +
			"  \"lang\": \"iu\",\n" +
			"  \"word\": \"inuksuk\"\n" +
			"}"
			;
		ObjectMapper mapper = new ObjectMapper();
		MDictEntry gotEntry = mapper.readValue(json, MDictEntry.class);
		new AssertMDictEntry(gotEntry)
			.isForWord("inuksuk")
			.wordInOtherScriptIs("ᐃᓄᒃᓱᒃ")
			.wordRomanIs("inuksuk")
			.wordSyllIs("ᐃᓄᒃᓱᒃ")
			;
		return;
	}
}
