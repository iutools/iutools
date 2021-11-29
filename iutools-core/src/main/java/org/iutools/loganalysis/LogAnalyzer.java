package org.iutools.loganalysis;

import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
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

	public List<String> categories() {
		List<String> cats = new ArrayList<String>();
		cats.addAll(epaStats.keySet());
		Collections.sort(cats, (c1, c2) -> c1.compareTo(c2));

		return cats;
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
		LogLineTracer tracer = new LogLineTracer("updateGenericStats", line, "webservice.user_action");

		EPA_Stats categStats = this.singleCategStats4line(line);
		EPA_Stats allCatStats = this.allCategStats4line(line);

		if (tracer.isEnabled()) {
			tracer.trace(
				"** On entry\n"+
				"categStats="+PrettyPrinter.print(categStats)+"\n"+
				"allCatStats="+PrettyPrinter.print(allCatStats));
		}
		if (line.phase != null) {
			if (line.phase.equals("START")) {
				categStats.onStart();
				allCatStats.onStart();
			} else if (line.phase.equals("END")) {
				categStats.onEnd(line.elapsedMSecs);
				allCatStats.onEnd(line.elapsedMSecs);
			}
		}
		if (line.exceptionRaised != null && !line.exceptionRaised.isEmpty()) {
			categStats.totalExceptions++;
			allCatStats.totalExceptions++;
		}

		if (tracer.isEnabled()) {
			tracer.trace(
			"** On exit\n"+
			"categStats="+PrettyPrinter.print(categStats)+"\n"+
			"allCatStats="+PrettyPrinter.print(allCatStats));
		}
		return;
	}

	private EPA_Stats singleCategStats4line(LogLine line) {
		String key = line.category();
		if (!epaStats.containsKey(key)) {
			epaStats.put(key, new EPA_Stats(key));
		}
		EPA_Stats categStats = this.epaStats.get(key);
		return categStats;
	}

	private EPA_Stats allCategStats4line(LogLine line) {
		String key = "_ALL_ACTIONS";
		if (line instanceof EndpointLine) {
			key = "_all_endpoints";
		}
		if (!epaStats.containsKey(key)) {
			epaStats.put(key, new EPA_Stats(key));
		}
		EPA_Stats categStats = this.epaStats.get(key);
		return categStats;
	}

	private void onUserActionLine(UserActionLine line) {
	}

	private void onEndpointLine(EndpointLine lineObj) {
	}

	public EPA_Stats stats4epa(String epaName) throws LogAnalyzerException {
		EPA_Stats stats = new EPA_Stats(epaName);
		if (epaStats.containsKey(epaName)) {
			stats = epaStats.get(epaName);
		}
		return stats;
	}
}
