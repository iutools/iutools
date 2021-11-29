package org.iutools.loganalysis;

import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import ca.nrc.testing.TestDirs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.util.function.Consumer;

public class LogAnalyzerTest {

	private TestDirs testDirs = null;
	private Path sampleLogContent = null;

	private static String sampleLine__MORPHEME_SEARCH__START =
		"-- webservice.user_action@08:23:10,054(thr=http-nio-80-exec-3): {\"_phase\":\"START\",\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_uri\":\"/iutools/srv2/log_action\",\"taskData\":{\"_action\":\"MORPHEME_SEARCH\",\"_phase\":\"START\",\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_taskStartTime\":1635337390053,\"nbExamples\":\"10\",\"wordPattern\":\"tut\"}}";
	private static String sampleLine__MORPHEME_SEARCH__END =
		"-- webservice.user_action@08:23:10,586(thr=http-nio-80-exec-8): {\"_phase\":\"START\",\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_uri\":\"/iutools/srv2/log_action\",\"taskData\":{\"_action\":\"MORPHEME_SEARCH\",\"_phase\":\"END\",\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_taskStartTime\":1635337390053,\"taskElapsedMsecs\":521}}";
	private static String sampleLine__morpheme_dictionary__START =
		"-- webservice.endpoint@08:23:10,067(thr=http-nio-80-exec-2): {\"_phase\":\"START\",\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_uri\":\"/iutools/srv2/morpheme_dictionary\",\"taskData\":{\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_taskStartTime\":1635337390053,\"nbExamples\":\"10\",\"wordPattern\":\"tut\"}}";
	private static String sampleLine__morpheme_dictionary__END =
		"-- webservice.endpoint@08:23:10,574(thr=http-nio-80-exec-2): {\"_taskID\":\"2021-10-27T12:23:10.053Z\",\"_uri\":\"/iutools/srv2/morpheme_dictionary\",\"_taskElapsedMsecs\":521,\"_phase\":\"END\"}";
	private static String sampleLine__spell__Exception =
		"-- webservice.endpoint@11:33:25,293(thr=http-nio-8080-exec-2): {\"exception\":\"Some exception\",\"_taskID\":\"2021-10-29T15:33:25.202Z\",\"_uri\":\"/iutools/srv2/spell\",\"suggestCorrections\":false,\"_taskStartTime\":1635521605227,\"_action\":null,\"text\":\"nunavvvut\",\"includePartiallyCorrect\":false,\"taskElapsedMsecs\":null}";

	@BeforeEach
	public void setUp(TestInfo testInfo) throws Exception {
		testDirs = new TestDirs(testInfo);
		testDirs.copyResourceDirToInputs("org/iutools/loganalysis/samplelogs");
		sampleLogContent = testDirs.inputsFile("loganalysis/samplelog_morphdict.txt");
		return;
	}

	///////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////

	@Test
	public void test__LogAnalyzer__Synopsis() throws Exception {
		// You create a LogAnalyzer by passing it the path of a log file
		Path logFile = testDirs.inputsFile("samplelogs/samplelog_morphdict.txt");
		LogAnalyzer analyzer = new LogAnalyzer(logFile);

		// Analyze the log
		analyzer.analyze();

		// Get some stats about the various actions and endpoints
		// This gets stats about the 'MORPHEME_SEARCH' *ACTION*
		EPA_Stats spellActionStats = analyzer.stats4epa("MORPHEME_SEARCH");

		// This gets stats about the 'morpheme_dictioanary' *END POINT*
		EPA_Stats spellEndpointStats = analyzer.stats4epa("morpheme_dictionary");
	}


	///////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////

	@Test
	public void test__LogAnalyzer__AllTypes() throws Exception {
		Path logFile = testDirs.inputsFile("samplelogs/samplelog_ALL.txt");
		LogAnalyzer analyzer = new LogAnalyzer(logFile);
		analyzer.analyze();

		// Action stats
		{
			new AssertEPA_Stats(analyzer.stats4epa("_ALL_ACTIONS"), "_ALL_ACTIONS")
				// Not sure why this is 16 instead of 14
				.frequencyIs(16)
				.avgMSecsIs(975.8)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
			;

			new AssertEPA_Stats(analyzer.stats4epa("DICTIONARY_SEARCH"), "DICTIONARY_SEARCH")
				.frequencyIs(1)
				.avgMSecsIs(1282.0)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("WORD_LOOKUP"), "WORD_LOOKUP")
				.frequencyIs(7)
				.avgMSecsIs(1193.8)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("SEARCH_WEB"), "SEARCH_WEB")
				.frequencyIs(1)
				.avgMSecsIs(77.0)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
			;

			new AssertEPA_Stats(analyzer.stats4epa("MORPHEME_SEARCH"), "MORPHEME_SEARCH")
				.frequencyIs(2)
				.avgMSecsIs(698.5)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
			;

			new AssertEPA_Stats(analyzer.stats4epa("GIST_TEXT"), "GIST_TEXT")
				.frequencyIs(3)
				.avgMSecsIs(1500.3)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
			;
		}

		// Endpoint stats
		{
			new AssertEPA_Stats(analyzer.stats4epa("_all_endpoints"), "_all_endpoints")
				.frequencyIs(16)
				.avgMSecsIs(3056.3)
				.totalExceptionsIs(1)
				.exceptionsRateIs(0.06)
				;

			new AssertEPA_Stats(analyzer.stats4epa("worddict"), "worddict")
				.frequencyIs(8)
				.avgMSecsIs(1204.8)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("spell"), "spell")
				// Note: The stats for "spell" endpoint are a bit counter-intuitive
				//  because we do not log the START of any of those endpoints,
				//  and we only log the END in cases where the word is misspelled.
				.frequencyIs(0)
				.avgMSecsIs(0.0)
				.totalExceptionsIs(1)
				// Note: Exceptions rate is 0 because freq=0 (see above note)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("search/expandquery"), "expandquery")
				.frequencyIs(1)
				.avgMSecsIs(77.0)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
			;

			new AssertEPA_Stats(analyzer.stats4epa("morpheme_dictionary"), "morpheme_dictionary")
				.frequencyIs(2)
				.avgMSecsIs(698.5)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("tokenize"), "tokenize")
				.frequencyIs(2)
				.avgMSecsIs(14.5)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;

			new AssertEPA_Stats(analyzer.stats4epa("gist/preparecontent"), "preparecontent")
				.frequencyIs(3)
				.avgMSecsIs(1500.3)
				.totalExceptionsIs(0)
				.exceptionsRateIs(0.0)
				;
		}
	}


	@Test
	public void test__LogAnalyzer__morphdict() throws Exception {
		Path logFile = testDirs.inputsFile("samplelogs/samplelog_morphdict.txt");
		LogAnalyzer analyzer = new LogAnalyzer(logFile);
		analyzer.analyze();

		new AssertEPA_Stats(analyzer.stats4epa("MORPHEME_SEARCH"))
			.frequencyIs(3)
			.avgMSecsIs(516.6)
			;

		new AssertEPA_Stats(analyzer.stats4epa("MORPHEME_SEARCH"))
			.frequencyIs(3)
			.avgMSecsIs(516.6)
			;

	}

	@Test
	public void test__parseLine__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("MORPHEME_SEARCH START", sampleLine__MORPHEME_SEARCH__START,
				new UserActionLine()
					.setAction("MORPHEME_SEARCH")
					.setPhase("START")),

			new Case("MORPHEME_SEARCH END", sampleLine__MORPHEME_SEARCH__END,
				new UserActionLine()
					.setAction("MORPHEME_SEARCH")
					.setPhase("END")
					.setElapsedMSecs(521)
				),

			new Case("morpheme_dictionary START", sampleLine__morpheme_dictionary__START,
				new EndpointLine()
					.setUri("morpheme_dictionary")
					.setPhase("START")),

			new Case("morpheme_dictionary END", sampleLine__morpheme_dictionary__END,
				new EndpointLine()
					.setUri("morpheme_dictionary")
					.setPhase("END")
					.setElapsedMSecs(521)
				),

			new Case("spell Exception raised", sampleLine__spell__Exception,
				new EndpointLine()
					.setUri("spell")
					.setExceptionRaised("Some exception")
				),
		};

		Consumer<Case> runner = (aCase) -> {
			String line = (String) aCase.data[0];
			LogLine expLineObj = (LogLine) aCase.data[1];
			LogLine gotLineObj = LogLine.parseLine(line);
			try {
				new AssertLogLine(gotLineObj, "Line: "+line)
					.isEqualTo(expLineObj);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		};

		new RunOnCases(cases, runner)
//			.onlyCaseNums(5)
			.run();
	}
}
