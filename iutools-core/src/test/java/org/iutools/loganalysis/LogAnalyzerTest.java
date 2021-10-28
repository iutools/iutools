package org.iutools.loganalysis;

import ca.nrc.testing.RunOnCases;
import ca.nrc.testing.RunOnCases.*;
import ca.nrc.testing.TestDirs;
import org.junit.jupiter.api.Assertions;
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
		EPA_Stats spellActionStats = analyzer.actionStats("MORPHEME_SEARCH");

		// This gets stats about the 'morpheme_dictioanary' *END POINT*
		EPA_Stats spellEndpointStats = analyzer.endpointStats("morpheme_dictioanary");
	}


	///////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////

	@Test
	public void test__LogAnalyzer__morphdict() throws Exception {
		Path logFile = testDirs.inputsFile("samplelogs/samplelog_morphdict.txt");
		LogAnalyzer analyzer = new LogAnalyzer(logFile);
		analyzer.analyze();
		EPA_Stats spellActionStats = analyzer.actionStats("MORPHEME_SEARCH");
		new AssertEPA_Sats(spellActionStats)
			.frequencyIs(3)
//			.avgMSecsIs(999.0)
			;

		// This gets stats about the 'morpheme_dictioanary' *END POINT*
		EPA_Stats spellEndpointStats = analyzer.endpointStats("morpheme_dictioanary");
	}

	@Test
	public void test__actionForLine__SeveralCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("webservice.user_action", sampleLine__MORPHEME_SEARCH__START,
				"MORPHEME_SEARCH"),
		};

		Consumer<Case> runner = (aCase) -> {
			String line = (String) aCase.data[0];
			String expAction = (String) aCase.data[1];
			String gotAction = LogAnalyzer.actionForLine(line);
			Assertions.assertEquals(expAction, gotAction,
				"Action not as expected for log line: '"+line+"'");
		};

		new RunOnCases(cases, runner)
			.run();
	}

	@Test
	public void test__parseLine__VariousCases() throws Exception {
		Case[] cases = new Case[] {
			new Case("MORPHEME_SEARCH START", sampleLine__MORPHEME_SEARCH__START,
				new UserActionLine()
					.setPhase("START")),

			new Case("MORPHEME_SEARCH END", sampleLine__MORPHEME_SEARCH__END,
				new UserActionLine()
					.setPhase("END")
					.setElapsedMSecs(521)
				),

			new Case("morpheme_dictionary START", sampleLine__morpheme_dictionary__START,
				new EndpointLine()
					.setPhase("START")),

			new Case("morpheme_dictionary END", sampleLine__morpheme_dictionary__END,
				new EndpointLine()
					.setPhase("END")
					.setElapsedMSecs(521)
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
//			.onlyCaseNums(4)
			.run();
	}
}
