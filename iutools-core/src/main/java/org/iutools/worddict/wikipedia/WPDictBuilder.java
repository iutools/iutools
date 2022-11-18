package org.iutools.worddict.wikipedia;

import ca.nrc.json.PrettyPrinter;
import org.iutools.script.TransCoder;
import org.iutools.worddict.GlossaryEntry;
import org.iutools.worddict.GlossaryException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WPDictBuilder {

	FileWriter dictFileWriter = null;

	Set<String> iuAlreadySeen = new HashSet<String>();

	public static void main(String[] args) throws Exception {
		new WPDictBuilder().run(args);
	}

	public WPDictBuilder() {

	}

	public void run(String[] args) throws WPException, IOException, GlossaryException {
		if (args.length != 2) {
			usage("Wrong number of arguments");
		}
		Path wpDumpFile = Paths.get(args[0]);
		Path wpDictFile = Paths.get(args[1]);
		dictFileWriter = new FileWriter(wpDictFile.toFile());
		printHeaders();
		
		List<String> iuWords = parseIUWikipediaDump(wpDumpFile);
		int totalWords = 0;
		for (String iuWord: iuWords) {
			if (TransCoder.textScript(iuWord) != TransCoder.Script.SYLLABIC) {
				continue;
			}
			if (iuWord.matches("^\\d*$")) {
				continue;
			}
			if (iuAlreadySeen.contains(iuWord)) {
				continue;
			}
			String enWord = findEnWord(iuWord);
			if (enWord == null) {
				continue;
			}
			printEntry(iuWord, enWord);
			totalWords++;
		}

		echo("Total words = "+totalWords);

		dictFileWriter.close();

		return;
	}

	private void printHeaders() throws WPException {
		try {
			dictFileWriter.write(
				"bodyEndMarker=BLANK_LINE\n"+
				"class=org.iutools.worddict.GlossaryEntry\n\n"
			);
		} catch (IOException e) {
			throw new WPException(e);
		}
	}

	private void printEntry(String iuWord, String enWord) throws WPException, IOException, GlossaryException {
		String[] iuTokens = iuWord.split(":");
		String[] enTokens = enWord.split(":");
		if (iuTokens.length != enTokens.length) {
			throw new WPException(
				"The IU and EN terms did not contain the same number of tokens\n"+
				"iu="+iuTokens.length+", en="+enTokens.length
				);
		}
		for (int ii=0; ii < iuTokens.length; ii++) {
			echo(iuTokens[ii]+":"+enTokens[ii]);
			GlossaryEntry entry = new GlossaryEntry()
				.setTermInLang("en", enTokens[ii])
				.setTermInLang("iu_syll", TransCoder.ensureSyllabic(iuTokens[ii]))
				.setTermInLang("iu_roman", TransCoder.ensureRoman(iuTokens[ii]))
				;

			String jsonEntry = PrettyPrinter.print(entry);
			dictFileWriter.write(jsonEntry+"\n\n");
			dictFileWriter.flush();
		}
		return;
	}

	private void echo(String mess) {
		System.out.println(mess);
	}

	private void usage(String mess) {
		if (mess != null) {
			System.out.println("** ERROR: "+mess+"\n");
		}
		System.out.println(
			"Usage WPDictBuilder wp-dump-file dict-json-file"
		);
		System.exit(1);

	}

	private String findEnWord(String iuWord) throws WPException {
		try {
			String iuWordEncoded = URLEncoder.encode(iuWord, String.valueOf(StandardCharsets.UTF_8));
			URL url = new URL(
				"https://www.wikidata.org/w/api.php?action=wbgetentities&sites=iuwiki&titles="+
				iuWordEncoded+"&languages=en&props=labels&format=xml");


			InputStream stream = url.openStream();
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream, StandardCharsets.UTF_8));
			Map<String,String> translations = parseWikiDataResponse(reader);

			return translations.get("en");
		} catch (IOException e) {
			throw new WPException(e);
		}
	}

	public Map<String,String> parseWikiDataResponse(String xml) throws WPException {
		Reader reader = new StringReader(xml);
		return parseWikiDataResponse(reader);
	}


	public Map<String,String> parseWikiDataResponse(Reader xmlReader) throws WPException {
		try {
			Map<String,String> translations = new HashMap<String,String>();

			// Instantiate the Factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parse XML string
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(xmlReader));

			// optional, but recommended
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();


			NodeList nodeLstLabel = doc.getElementsByTagName("label");
			for (int ii = 0; ii < nodeLstLabel.getLength(); ii++) {
				Node nodeLabel = nodeLstLabel.item(ii);
				short nodeType = nodeLabel.getNodeType();
				if (nodeType == Node.ELEMENT_NODE) {

					Element element = (Element) nodeLabel;

					String lang = element.getAttribute("language");
					String value = element.getAttribute("value");
					translations.put(lang, value);
				}
			}
			return translations;

		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new WPException(e);
		}
	}

	public List<String> parseIUWikipediaDump(String xmlDump) throws WPException {
		BufferedReader reader = new BufferedReader(new StringReader(xmlDump));
		return parseIUWikipediaDump(reader);
	}

	public List<String> parseIUWikipediaDump(Path xmlDumpFile) throws WPException {

		BufferedReader reader = null;
		try {
			FileReader freader = new FileReader(xmlDumpFile.toFile());
			reader = new BufferedReader(freader);
			return parseIUWikipediaDump(reader);
		} catch (FileNotFoundException e) {
			throw new WPException(e);
		}
	}

	private List<String> parseIUWikipediaDump(BufferedReader reader) throws WPException {
		List<String> words = new ArrayList<String>();
		Pattern pattTitleLine = Pattern.compile("^\\s*<title>\\s*([^<]*?)\\s*</title>\\s*$");
		try {
			String line = reader.readLine();
			while (line != null) {
				Matcher matcher = pattTitleLine.matcher(line);
				if (matcher.matches()) {
					words.add(matcher.group(1));
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			throw new WPException(e);
		}
		return words;
	}
}
