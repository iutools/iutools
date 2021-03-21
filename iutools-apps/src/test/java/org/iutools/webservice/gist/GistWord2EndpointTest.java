package org.iutools.webservice.gist;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

public class GistWord2EndpointTest extends EndpointTest {

	@Override
	public Endpoint makeEndpoint() {
		return new GistWord2Endpoint();
	}

	@Test
	public void test__GistWordEndpoint__RomanWord() throws Exception {

		GistWord2Inputs inputs = new GistWord2Inputs("inuktitut");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWord2Result(epResult)
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1,
				"en", "Inuktitut Documentation",
			"iu", "inuktitut titiraqtauniq")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__SyllabicWord() throws Exception {
		GistWord2Inputs inputs = new GistWord2Inputs("ᐃᓄᒃᑎᑐᑦ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWord2Result(epResult)
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1,
				"en", "Inuktitut Documentation",
			"iu", "inuktitut titiraqtauniq")
			;
		return;
	}
}