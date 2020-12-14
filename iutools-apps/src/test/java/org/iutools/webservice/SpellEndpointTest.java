package org.iutools.webservice;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.nrc.testing.AssertRuntime;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;

import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class SpellEndpointTest {

	SpellEndpoint endPoint = null;
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


	@BeforeEach
	public void setUp(TestInfo info) throws Exception {
		this.testInfo = info;
		endPoint = new SpellEndpoint();
	}
	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	

	@Test
	public void test__SpellEndpoint__Syllabic__HappyPath() throws Exception {
		
		SpellInputs spellInputs = new SpellInputs("ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
		SpellCheckerAssertion.assertThat(response, "")
			.raisedNoError()
			.nthCorrectionIs(0, true,
				new String[] {
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᕗᑦ",
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
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
		
		SpellInputs spellInputs = new SpellInputs("ᒐᕙᒪᒃᑯᑎᒍᑦ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
		SpellCheckerAssertion.assertThat(response, "")
			.raisedNoError()
			.nthCorrectionIs(0, true,
				new String[]{
					"ᒐᕙᒪᒃᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᑐᑦ",
					"ᒐᕙᒪᒃᑯᑎᓐᓄᑦ",
					"ᒐᕙᒪᒃᑯᖏᑎᒍᑦ",
					"ᒐᕙᒪᑎᒍᑦ",
					"ᒐᕙᒪᒃᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᑐᑦ",
					"ᒐᕙᒪᒃᑯᑎᓐᓄᑦ",
					"ᒐᕙᒪᒃᑯᖏᑎᒍᑦ",
					"ᒐᕙᒪᑎᒍᑦ"
				});
		
		return;	
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

		SpellInputs spellInputs = new SpellInputs(text);

		long startMSecs = StopWatch.nowMSecs();
		MockHttpServletResponse response =
				IUTServiceTestHelpers.postEndpointDirectly(
						IUTServiceTestHelpers.EndpointNames.SPELL,
						spellInputs
				);

		double elapsedSecs = StopWatch.elapsedMsecsSince(startMSecs) / 1000.0;
		double gotAvgSecs = elapsedSecs / numWords;

		// Note: We don't flag improvements. Just worsening.
	 	AssertRuntime.runtimeHasNotChanged(gotAvgSecs, Pair.of(percTolerance, null),
			"avg word spell check secs", testInfo);
	}
}
