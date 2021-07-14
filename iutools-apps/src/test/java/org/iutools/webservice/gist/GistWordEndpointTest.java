package org.iutools.webservice.gist;

import ca.nrc.testing.AssertObject;
import org.iutools.concordancer.Alignment;
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
			.mostAlignmentsContains("iu", "ᐃᓄᒃᑎᑐᑦ")
			.mostAlignmentsContains("en", 0.1, "inu", "language")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__SyllabicWord() throws Exception {
		GistWordInputs inputs = new GistWordInputs("ᐃᓄᒃᑎᑐᑦ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWordResult(epResult)
			.gistMorphemesEqual(new String[] {"inuk", "titut"})
			.mostAlignmentsContains("iu", "ᐃᓄᒃᑎᑐᑦ")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__AlignmentsShouldBeSameNoMatterTheScriptUsed() throws Exception {
		GistWordInputs inputs = new GistWordInputs("ᐃᓄᒃᑎᑐᑦ");
		GistWordResult epResult = (GistWordResult) endPoint.execute(inputs);
		Alignment[] syllAlignments = epResult.alignments;

		inputs = new GistWordInputs("inuktitut");
		epResult = (GistWordResult) endPoint.execute(inputs);
		Alignment[] romanAlignments = epResult.alignments;

		AssertObject.assertDeepEquals(
			"Alignments should be the same no matter the script used for the input word",
			syllAlignments, romanAlignments);

		return;
	}
}