package org.iutools.loganalysis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogLineTracer {
	private static Logger logger = LogManager.getLogger("org.iutools.loganalysis.LogLineTracer");

	private LogLine line;
	private String linePattern;
	private String who;

	public LogLineTracer(String _who, LogLine _line, String _linePattern) {
		line = _line;
		linePattern = _linePattern;
		who = _who;
	}

	public void trace(String mess) {
		if (logger.isTraceEnabled() &&
			(line.text.contains(linePattern))) {
			logger.trace("(who="+who+", line.key="+line.category()+", line="+line.text+"):\n   "+mess);
		}
	}

	public boolean isEnabled() {
		boolean enabled =
			logger.isTraceEnabled() && line.text.contains(linePattern);
		return enabled;
	}
}
