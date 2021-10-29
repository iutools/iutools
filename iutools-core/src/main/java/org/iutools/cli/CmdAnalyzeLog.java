package org.iutools.cli;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.loganalysis.EPA_Stats;
import org.iutools.loganalysis.LogAnalyzer;

import java.io.File;

public class CmdAnalyzeLog extends ConsoleCommand {

	File logFile = null;
	String reportType = "OVERVIEW";

	UserIO userIO = new UserIO();

	LogAnalyzer logAnalyzer = null;

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
		logAnalyzer = new LogAnalyzer(logFile.toPath());
		userIO.echo("Analysing log file: "+logFile+"...");
		logAnalyzer.analyze();
		if (reportType.equals("OVERVIEW")) {
			printOverviewReport();
		}
	}

	private void printOverviewReport() throws Exception {
		userIO.echo("\nOVERVIEW report\n");
		for (String cat : logAnalyzer.categories()) {
			printCategoryOverview(cat);
		}
	}

	private void printCategoryOverview(String category) throws Exception {
		userIO.echo(category);
		userIO.echo(1);
		try {
			EPA_Stats stats = logAnalyzer.stats4epa(category);
			userIO.echo(PrettyPrinter.print(stats));
		} finally {
			userIO.echo(-1);
		}
	}
}
