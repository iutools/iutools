package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.worddict.scrapers.GlossaryScraper;
import org.iutools.worddict.scrapers.TusaalangaScraper;

import java.io.File;
import java.nio.file.Path;

public class CmdScrapeGlossary extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Scrape a glossary from the web or downloaded HTML files.";
	}

	public CmdScrapeGlossary(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		String glossName = getGlossaryName();
		Path outputFile = getOutputFile();
		GlossaryScraper scraper = null;
		if (glossName.equals("tusaalunga")) {
			File htmlFilesDir = getInputDir(true);
			if (!htmlFilesDir.isDirectory()) {
				usageBadOption(OPT_INPUT_DIR, "Must be an existing directory");
			}
			scraper = new TusaalangaScraper(htmlFilesDir.toPath(), outputFile);
		}

		scraper.scrape();
	}
}
