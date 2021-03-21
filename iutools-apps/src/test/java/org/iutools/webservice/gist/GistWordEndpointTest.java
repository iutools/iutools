package org.iutools.webservice.gist;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.junit.jupiter.api.Test;

public class GistWordEndpointTest extends EndpointTest {

	@Override
	public Endpoint makeEndpoint() {
		return new GistWordEndpoint();
	}

	@Test
	public void test__GistWordEndpoint__RomanWord() throws Exception {

		GistWordInputs inputs = new GistWordInputs("inuktitut");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWordResult(epResult)
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1,
				"en", "Inuktitut Documentation",
			"iu", "inuktitut titiraqtauniq")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__SyllabicWord() throws Exception {
		GistWordInputs inputs = new GistWordInputs("ᐃᓄᒃᑎᑐᑦ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWordResult(epResult)
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.nthAlignmentIs(1,
				"en", "Inuktitut Documentation",
			"iu", "inuktitut titiraqtauniq")
			;
		return;
	}
}