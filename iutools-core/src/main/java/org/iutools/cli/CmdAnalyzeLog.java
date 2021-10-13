package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.loganalysis.LogAnalyzer;
import org.iutools.loganalysis.LogAnalyzerException;

import java.io.File;

public class CmdAnalyzeLog extends ConsoleCommand {

	File logFile = null;
	String reportType = "OVERVIEW";

	public CmdAnalyzeLog(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Analyze the IUTools application log";
	}

	@Override
	public void execute() throws Exception {
		logFile = getLogFile();
		reportType = getLogReportType();
		run();
	}

	private void run() throws LogAnalyzerException {
		echo(reportType+" REPORT\n");
		echo(1);
		try {
			echo("Log file: "+logFile);
		} finally {
			echo(-1);
		}
		new LogAnalyzer(logFile.toPath());
	}
}
