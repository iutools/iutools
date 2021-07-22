package org.iutools.webservice.gist;

import ca.nrc.testing.AssertObject;
import org.iutools.concordancer.SentencePair;
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

		GistWordInputs inputs = new GistWordInputs("inuksuk");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWordResult(epResult)
			.gistMorphemesEqual(new String[] {"inuksuk"})
			.mostAlignmentsContains("iu", 0.02, "inuksuk")
			.mostAlignmentsContains("en", 0.02, "innusuk", "Innuksuk")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__SyllabicWord() throws Exception {
		GistWordInputs inputs = new GistWordInputs("ᐃᓄᒃᓱᒃ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertGistWordResult(epResult)
			.gistMorphemesEqual(new String[] {"inuksuk"})
			.mostAlignmentsContains("iu", 0.02, "inuksuk")
			.mostAlignmentsContains("en", 0.02, "innuksuk", "Innuksuk")
			;

		return;
	}

	@Test
	public void test__GistWordEndpoint__AlignmentsShouldBeSameNoMatterTheScriptUsed() throws Exception {
		GistWordInputs inputs = new GistWordInputs("ᐃᓄᒃᑎᑐᑦ");
		GistWordResult epResult = (GistWordResult) endPoint.execute(inputs);
		SentencePair[] syllAlignments = epResult.alignments;

		inputs = new GistWordInputs("inuktitut");
		epResult = (GistWordResult) endPoint.execute(inputs);
		SentencePair[] romanAlignments = epResult.alignments;

		AssertObject.assertDeepEquals(
			"Alignments should be the same no matter the script used for the input word",
			syllAlignments, romanAlignments);

		return;
	}
}