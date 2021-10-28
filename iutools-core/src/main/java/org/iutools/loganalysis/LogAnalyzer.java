package org.iutools.loganalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to analyze the IUTools app tomcat logs
 */
public class LogAnalyzer {

	public Path logFile = null;
	BufferedReader reader = null;

	static Pattern pattActionStart = Pattern.compile("\"_action\":\"([^\"]+)\",\"_phase\":\"START\"");

	public LogAnalyzer(Path _logFile) throws LogAnalyzerException {
		init_LogAnalyzer(_logFile);
	}

	public LogAnalyzer(BufferedReader _reader) throws LogAnalyzerException {
		init_LogAnalyzer(_reader);
	}

	private void init_LogAnalyzer(Path _logFile) throws LogAnalyzerException {
		this.logFile = _logFile;
		BufferedReader _reader;
		try {
			_reader = new BufferedReader(new FileReader(logFile.toFile()));
		} catch (FileNotFoundException e) {
			throw new LogAnalyzerException(
				"Could not open log file for analysis: "+logFile,
				e);
		}
		init_LogAnalyzer(_reader);
	}

	private void init_LogAnalyzer(BufferedReader _reader) throws LogAnalyzerException {
		this.reader = _reader;
	}

	public void analyze() {
	}

	public EPA_Stats actionStats(String action) throws LogAnalyzerException {
		EPA_Stats stats = new EPA_Stats();
		while (true) {
			try {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				String lineAction = actionForLine(line);
				if (lineAction != null && lineAction.equals(action)) {
					stats.frequency++;
				}
			} catch (IOException e) {
				throw new LogAnalyzerException(e);
			}
		}
		return stats;
	}

	protected static String actionForLine(String line) {
		Matcher matcher = pattActionStart.matcher(line);
		String action = null;
		if (matcher.find()) {
			action = matcher.group(1);
		}
		return action;
	}

	public EPA_Stats endpointStats(String spell) {
		return null;
	}
}
