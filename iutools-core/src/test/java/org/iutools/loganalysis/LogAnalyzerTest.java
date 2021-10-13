package org.iutools.loganalysis;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;

public class LogAnalyzerTest {

	private static String sampleLogContent =
		"blah"
		;

	///////////////////////////////////
	// DOCUMENTATION TESTS
	///////////////////////////////////


	@Test
	public void test__LogAnalyzer__Synopsis() throws Exception {
		// You can create a LogAnalyzer by feeding it either the path to a
		// log file, or a buffered reader to it.
		// For the purposes of this test, we use a BufferedReader.
		BufferedReader reader = new BufferedReader(new StringReader(sampleLogContent));
		LogAnalyzer analyzer = new LogAnalyzer(reader);

		// Analyze the log
		analyzer.analyze();

		// Get some stats about the various actions and endpoints
		// This gets stats about the 'SPELL' *ACTION*
		EPA_Stats spellActionStats = analyzer.actionStats("SPELL");

		// This gets stats about the 'spell' *END POINT*
		EPA_Stats spellEndpointStats = analyzer.endpointStats("SPELL");
	}

}
