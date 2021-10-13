package org.iutools.loganalysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;

/**
 * This class is used to analyze the IUTools app tomcat logs
 */
public class LogAnalyzer {

	public Path logFile = null;
	BufferedReader reader = null;

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

	public EPA_Stats actionStats(String spell) {
		return null;
	}

	public EPA_Stats endpointStats(String spell) {
		return null;
	}
}
