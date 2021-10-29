package org.iutools.loganalysis;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This class is used to analyze the IUTools app tomcat logs
 */
public class LogAnalyzer {

	public Path logFile = null;
	BufferedReader reader = null;

	Map<String,EPA_Stats> epaStats = new HashMap<String,EPA_Stats>();

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

	public void analyze() throws LogAnalyzerException {
		Logger tLogger = Logger.getLogger("org.iutools.loganalysis.LogAnalyzer.analyze");
		while (true) {
			try {
				String line = reader.readLine();
				tLogger.trace("Looking at line="+line);
				if (line == null) {
					break;
				}
				LogLine lineObj = LogLine.parseLine(line);
				if (lineObj != null) {
					updateGenericStats(lineObj);
					if (lineObj instanceof UserActionLine) {
						onUserActionLine((UserActionLine) lineObj);
					} else if (lineObj instanceof EndpointLine) {
						onEndpointLine((EndpointLine) lineObj);
					}
				}
			} catch (IOException e) {
				throw new LogAnalyzerException(e);
			}
		}
		return;
	}

	private void updateGenericStats(LogLine line) {
		String key = null;
		if (line instanceof UserActionLine) {
			key = ((UserActionLine) line).action;
		} else if (line instanceof EndpointLine) {
			key = ((EndpointLine)line).uri;
		}
		if (!epaStats.containsKey(key)) {
			epaStats.put(key, new EPA_Stats());
		}
		EPA_Stats stats = this.epaStats.get(key);
		if (line.phase.equals("START")) {
			stats.frequency++;
		} else if (line.phase.equals("END")) {
			if (line.elapsedMSecs != null) {
				stats.totalElapsedMSecs += line.elapsedMSecs;
			}
		}
	}

	private void onUserActionLine(UserActionLine line) {
	}

	private void onEndpointLine(EndpointLine lineObj) {
	}

	public EPA_Stats stats4epa(String epaName) throws LogAnalyzerException {
		if (!epaStats.containsKey(epaName)) {
			throw new LogAnalyzerException("Unknown Action or Endpoint name: "+epaName);
		}
		EPA_Stats stats = epaStats.get(epaName);
		return stats;
	}
}
