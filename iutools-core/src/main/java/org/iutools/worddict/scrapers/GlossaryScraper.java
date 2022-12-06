package org.iutools.worddict.scrapers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

public abstract class GlossaryScraper {

	public abstract void doScrape() throws GlossaryScraperException;


	protected String glossName = null;
	protected Path toJsonFile = null;
	protected PrintWriter writer = null;
	protected ObjectMapper mapper = new ObjectMapper();

	public GlossaryScraper(String _glossName, Path _toJsonFile) throws GlossaryScraperException {
		this.glossName = _glossName;
		this.toJsonFile = _toJsonFile;
		try {
			this.writer = new PrintWriter(toJsonFile.toString());
		} catch (FileNotFoundException e) {
			throw new GlossaryScraperException("Cannot open file for writing: "+toJsonFile, e);
		}
	}

	public void scrape() throws GlossaryScraperException {
		writer.println("// Source: "+glossName+"\n");
		writer.println("bodyEndMarker=NEW_LINE");
		writer.println("class=org.iutools.worddict.GlossaryEntry\n");

		doScrape();

		writer.close();
	}
}
