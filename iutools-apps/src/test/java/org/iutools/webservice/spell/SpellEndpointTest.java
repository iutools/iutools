package org.iutools.webservice.spell;

import ca.nrc.testing.AssertRuntime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.utilities.StopWatch;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.EndpointTest;
import org.iutools.webservice.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class SpellEndpointTest extends EndpointTest {

	TestInfo testInfo = null;

	private static String[] allMisspelledWords = {
		"nakuqmi", "nunavungmi", "nunavuumik", "nunavuumit",
		"ugaalautaa"
	};

	private static final String[] allOKWords = new String[] {
		"maligaliuqti", "juu", "niruaqtaukkannilauqpuq", "niruavigjuarnautillugu",
		"utupiri"
	};

	private static String[] halfOKhalfMisspelledWords = null;
	static {
		halfOKhalfMisspelledWords =
			ArrayUtils.addAll(allOKWords, allMisspelledWords);
	}

	private static String[] mixOfMisspeledAndOKWords = {
		"ᒪᓕᒐᓕᐅᖅᑎ", "ᔫ ᓴᕕᑲᑖᖅ", "ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ", "ᓂᕈᐊᕕᒡᔪᐊᕐᓇᐅᑎᓪᓗᒍ",
		"ᐅᑐᐱᕆ", "30", "2017-ᒥ", "ᑭᒡᒐᖅᑐᖅᑎᐅᓂᐊᖅᖢᓂ", "ᐊᕐᕕᐊᑦ ᓂᒋᐊᓄᑦ",
		"ᑕᓪᓕᒪᖓᓐᓂ", "ᒪᓕᒐᓕᕐᕕᖕᒥᑦ", "ᓄᓇᕗᒻᒥ", "ᔫ ᓴᕕᑲᑖᖅ", ",ᓂᕈᐊᖅᑕᐅᓚᐅᖅᐳᖅ",
		"ᐱᔨᑦᑎᕋᖁᔭᐅᓪᓗᓂᑦ", ",ᓯᕗᓕᖅᑏᑦ", ",ᑲᑎᒪᔨᖏᓐᓄᑦ", "ᓄᕕᐱᕆ"
	 };

	@Override
	public Endpoint makeEndpoint() throws ServiceException {
		return new SpellEndpoint();
	}

	@BeforeEach
	public void setUp(TestInfo info) throws Exception {
		super.setUp();
		this.testInfo = info;
	}

	/***********************
	 * VERIFICATION TESTS
	 ***********************/

	@Test
	public void test__SpellEndpoint__Syllabic__HappyPath() throws Exception {

		SpellInputs inputs = new SpellInputs("ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertSpellResult(epResult)
			.nthCorrectionIs(0, true,
				new String[]{
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᕗᑦ"
				}
			)
			.nthCorrectionIs(1, false)
			;

		return;
	}

	@Test
	public void test__SpellEndpoint__WordWithSyllCharsThatAreExpressedAsTwoRomanChars()
			throws Exception {

		SpellInputs inputs = new SpellInputs("ᒐᕙᒪᒃᑯᑎᒍᑦ");
		EndpointResult epResult = endPoint.execute(inputs);

		new AssertSpellResult(epResult)
			.nthCorrectionIs(0, true,
				new String[]{
					"ᒐᕙᒪᒃᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᑐᑦ",
					"ᒐᕙᒪᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᖏᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᓐᓄᑦ"
				});
	}

	@Test
	public void test__SpellEndpoint__SpeedTest__AllOKWords() throws Exception {
		double percTolerance = 0.5;
		speedTest(allOKWords, testInfo, percTolerance);
	}

    @Test
    public void test__SpellEndpoint__SpeedTest__AllMisspelledWords() throws Exception {
		double percTolerance = 0.25;
		 speedTest(allMisspelledWords, testInfo, percTolerance);
	}

	@Test
	public void test__SpellEndpoint__SpeedTest__HalfOKhalfMisspelledWords() throws Exception {
		double percTolerance = 0.25;
		speedTest(halfOKhalfMisspelledWords, testInfo, percTolerance);
	}

	private void speedTest(String[] words, TestInfo testInfo,
		Double percTolerance) throws Exception {
		int numWords = words.length;
		String text = String.join(" ", words);

		SpellInputs inputs = new SpellInputs(text);

		long startMSecs = StopWatch.nowMSecs();
		EndpointResult epResult = endPoint.execute(inputs);


		double elapsedSecs = StopWatch.elapsedMsecsSince(startMSecs) / 1000.0;
		double gotAvgSecs = elapsedSecs / numWords;

		// Note: We don't flag improvements. Just worsening.
	 	AssertRuntime.runtimeHasNotChanged(gotAvgSecs, Pair.of(percTolerance, null),
			"avg word spell check secs", testInfo);
	}
}
