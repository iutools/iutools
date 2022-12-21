package org.iutools.webservice.spell;

import ca.nrc.testing.AssertRuntime;
import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
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

import java.util.function.Consumer;

public class CheckWordEndpointTest extends EndpointTest {

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
		return new CheckWordEndpoint();
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
	public void test__CheckWordEndpoint__VariousCases() throws Exception {

		// This one used to raise an exception.
		Case[] cases = new CheckWordCase[] {
			new CheckWordCase("inukssuk = ROMAN word with Level1 error, being corrected at Level2", "inukssuk")
				.usingLevel(2)
				.expectSuggestions("inu[ks]suk", "inuksuk", "inukshuk", "inuksui",
					"inuksiutik", "inukku"),


			new CheckWordCase("ᐃᓄᑦᒧᑦ: Syllabics misspeled", "ᐃᓄᑦᒧᑦ")
				.usingLevel(2)
				.expectSuggestions("ᐃᓄ[ᑦᒧ]ᑦ", "ᐃᓄᒻᒧᑦ", "ᐃᓄᑐᐊᒧᑦ", "ᐃᓄᒃᑐᑦ", "ᐃᓄᑐᐊᑦ", "ᐃᓄᕗᑦ"),

			new CheckWordCase("ᑕᑯᔪᖅ: Syllabics correctly spelled", "ᑕᑯᔪᖅ")
				.usingLevel(2)
				.isCorrectlySpelled(),

			new CheckWordCase(
				"ᒐᕙᒪᒃᑯᑎᒍᑦ: Contains syll chars that transcode to 2 latin chars",
				"ᒐᕙᒪᒃᑯᑎᒍᑦ")
				.usingLevel(2)
				.expectSuggestions(
					"ᒐᕙᒪᒃᑎᒍᑦ", "ᒐᕙᒪᒃᑯᑎᑐᑦ", "ᒐᕙᒪᑎᒍᑦ", "ᒐᕙᒪᒃᑯᖏᑎᒍᑦ", "ᒐᕙᒪᒃᑯᑎᓐᓄᑦ"),

			new CheckWordCase("English word -- Should be left alone", "computing")
				.usingLevel(1)
				.isCorrectlySpelled(),
		};

		Consumer<Case> runner = (caseUncast) -> {
			CheckWordCase aCase = (CheckWordCase)caseUncast;
			String word = aCase.word;
			Boolean expMisspelled = aCase.expectMisspelled;
			String[] expSuggestions = aCase.expSuggestions;

			try {
				CheckWordInputs inputs = new CheckWordInputs(word);
				inputs.checkLevel = aCase.checkLevel;
				EndpointResult epResult = endPoint.execute(inputs);

				new AssertCheckWordResult(epResult)
					.correctionIs(expMisspelled, expSuggestions);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
			.run()
			;

		return;
	}


	@Test
	public void test__CheckWordEndpoint__SpeedTest__AllOKWords() throws Exception {
		double percTolerance = 0.5;
		speedTest(allOKWords, testInfo, percTolerance);
	}

    @Test
    public void test__CheckWordEndpoint__SpeedTest__AllMisspelledWords() throws Exception {
		double percTolerance = 0.25;
		 speedTest(allMisspelledWords, testInfo, percTolerance);
	}

	@Test
	public void test__CheckWordEndpoint__SpeedTest__HalfOKhalfMisspelledWords() throws Exception {
		double percTolerance = 0.25;
		speedTest(halfOKhalfMisspelledWords, testInfo, percTolerance);
	}

	private void speedTest(String[] words, TestInfo testInfo,
		Double percTolerance) throws Exception {
		int numWords = words.length;
		String text = String.join(" ", words);
		long startMSecs = StopWatch.nowMSecs();
		for (String aWord: words) {
			CheckWordInputs inputs = new CheckWordInputs(aWord);
			EndpointResult epResult = endPoint.execute(inputs);
		}

		double elapsedSecs = StopWatch.elapsedMsecsSince(startMSecs) / 1000.0;
		double gotAvgSecs = elapsedSecs / numWords;

		// Note: We don't flag improvements. Just worsening.
	 	AssertRuntime.runtimeHasNotChanged(gotAvgSecs, Pair.of(percTolerance, null),
			"avg word spell check secs", testInfo);
	}

	///////////////////////////////////////
	// TEST HELPERS
	///////////////////////////////////////

	public static class CheckWordCase extends Case {

		public String word = null;
		private int checkLevel = 1;
		public Boolean expectMisspelled = true;
		public String[] expSuggestions = null;

		public CheckWordCase(String _descr, String _word) {
			super(_descr, null);
			this.word = _word;
		}

		public CheckWordCase isCorrectlySpelled() {
			this.expectMisspelled = false;
			return this;
		}

		public CheckWordCase isMisSpelled() {
			this.expectMisspelled = true;
			return this;
		}

		public CheckWordCase usingLevel(int level) {
			this.checkLevel = level;
			return this;
		}

		public CheckWordCase expectSuggestions(String... _expSuggestions) {
			this.expSuggestions = _expSuggestions;
			isMisSpelled();
			return this;
		}
	}
}
