package org.iutools.worddict.scrapers;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.iutools.worddict.GlossaryEntry;
import org.iutools.worddict.GlossaryException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TusaalangaScraper extends GlossaryScraper {

	private HtmlCleaner htmlCleaner = new HtmlCleaner();

	private final Path htmlFilesDir;

	public TusaalangaScraper(Path _htmlFilesDir, Path _toJsonFile) throws GlossaryScraperException {
		super("tusaalanga", _toJsonFile);
		this.htmlFilesDir = _htmlFilesDir;
	}

	@Override
	public void doScrape() throws GlossaryScraperException {
		File[] directoryListing = htmlFilesDir.toFile().listFiles();
		Pattern filePattern = Pattern.compile("tusaalanga\\-(AIVILINGMIUTUT|PAALLIRMIUTUT|NATTILINGMIUTUT|QIKIQTAALUK|NORTH-QIKIQTAALUK|INUINNAQTUN|NUNATSIAVUMMIUTUT)\\.html");
		for (File aFile: directoryListing) {
			String fileName = aFile.getName();
			Matcher matcher = filePattern.matcher(fileName);
			String dialectName = null;
			if (matcher.find()) {
				dialectName = matcher.group(1);
			} else {
				continue;
			}
			parseHtmlFile(aFile, dialectName);
		}
	}

	private void parseHtmlFile(File aFile, String dialectName) throws GlossaryScraperException {
		try {
			URI uri = aFile.toURI();
			TagNode rootNode = htmlCleaner.clean(uri.toURL());
			TagNode glossTableNode = rootNode.findElementByAttValue("id", "glossary-table", true, true);
			TagNode[] terms = glossTableNode.getElementsByName("tr", true);
			for (TagNode termNode: terms) {
				String romanizedTerm = termNode.findElementByAttValue("id", "romanized", true, true)
					.getText().toString();
				String syllTerm = termNode.findElementByAttValue("id", "syllabic", true, true)
					.getText().toString();
				String enTerm = termNode.findElementByAttValue("id", "term", true, true)
					.getText().toString();
				GlossaryEntry entry = new GlossaryEntry()
					.setTermInLang("en", enTerm)
					.setTermInLang("iu_syll", syllTerm)
					.setTermInLang("iu_roman", romanizedTerm)
					.setDialects(dialectName)
					.setSource(glossName)
					.setReference("https://tusaalanga.ca/glossary")
					;

				String entryJson = mapper.writeValueAsString(entry);
				writer.println(entryJson);

				int x = 1;
			}

		} catch (IOException | GlossaryException e) {
			throw new GlossaryScraperException(e);
		}
	}
}
