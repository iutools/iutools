package org.iutools.cli;

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
		printAggregateCategoriesReport();

		echo("STATS FOR EACH ACTION OR ENDPOINT/n");
		echo(1);
		try {
			for (String cat : logAnalyzer.categories()) {
				if (!cat.startsWith("_")) {
					printCategoryOverview(cat);
				}
			}
		} finally {
			echo(-1);
		}
	}

	private void printAggregateCategoriesReport() throws Exception {
		echo("SUMMARY OF ALL ACTIONS AND ENDPOINTS/n");
		echo(1);
		try {
			printCategoryOverview("_ALL_ACTIONS");
			printCategoryOverview("_all_endpoints");
		} finally {
			echo(-1);
		}
	}

	private void printCategoryOverview(String category) throws Exception {
		userIO.echo(category);
		userIO.echo(1);
		try {
			EPA_Stats stats = logAnalyzer.stats4epa(category);
			echo("Frequency                : "+stats.frequency);
			echo("Elapsed secs (avg/total) : "+
				decim2(stats.avgSecs())+" ("+decim2(stats.totalElapsedSecs())+")");
			echo("Exceptions (avg/total)   : "+
				decim2(stats.exceptionsRate())+" ("+stats.totalExceptions+")");
			echo();
		} finally {
			echo(-1);
		}
	}

	private String decim2(double num) {
		return String.format("%.2f", num);
	}
}
