package org.iutools.cli;

import ca.nrc.data.file.FileGlob;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.Alignment_ES;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cmd_tmx2tmjson extends ConsoleCommand {
	Map<String,String> urlIndex = new HashMap<String,String>();
	List<String> topics = null;

	static Pattern pattTmxFile = Pattern.compile("/([^/]*)_en_ENG-IUK_BT\\.tmx$");
	static Pattern pattURL = Pattern.compile("/([^/]*)$");
	static Pattern pattRawLang = Pattern.compile("^(iu|en|fr)");

	public Cmd_tmx2tmjson(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Convert a TMX file to iutool's tm.json format.";
	}

	@Override
	public void execute() throws Exception {
		File tmxDir = getInputDir();
		Pattern pattFile = getFileRegexp();
		topics = getTopics(true);
		File urlListFile = new File(tmxDir, "../../clean_gov_nu_ca.urls.txt");
		try (FileWriter tmjsonFileWriter = init_tmjsonFile(getOutputFile())) {
			readURLsFile(urlListFile);
			Path tmJsonFPath = getOutputFile();
			File[] files = FileGlob.listFiles(tmxDir.toString() + "/*.tmx");
			for (File tmxFile : files) {
				if (pattFile != null && !pattFile.matcher(tmxFile.toString()).find()) {
					continue;
				}
				convertTMXFile(tmxFile, tmjsonFileWriter);
			}
		}
	}

	private FileWriter init_tmjsonFile(Path outputFile)
		throws ConsoleException {
		FileWriter fwriter = null;
		try {
			fwriter = new FileWriter(outputFile.toFile());
			fwriter.write(
				"bodyEndMarker=BLANK_LINE\n" +
				"class=org.iutools.concordancer.Alignment_ES\n\n");
		} catch (IOException e) {
			throw new ConsoleException(e);
		}
		return fwriter;
	}

	private void readURLsFile(File urlListFile) throws ConsoleException {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(urlListFile));
			String url = reader.readLine();
			while (url != null) {
				onNewURL(url);
				url = reader.readLine();
			}
		} catch (IOException e) {
			throw new ConsoleException(e);
		}

		echo("URL index:\n", UserIO.Verbosity.Level5);
		echo(1);
		try {
			for (String fname: urlIndex.keySet()) {
				echo(fname+" --> "+urlIndex.get(fname));
			}
		} finally {
			echo(-1);
		}
		return;
	}

	void onNewURL(String url) throws ConsoleException {
		String fileName = urlFileName(url);
		if (urlIndex.keySet().contains(fileName)) {
			// We have already encountered a url with that file name.
			// Add a number to it to distinguish from the other
			int fileNum = 1;
			while (true) {
				if (!urlIndex.keySet().contains(fileName+"_"+fileNum)) {
					break;
				}
				fileNum++;
			}
			fileName = fileName+"_"+fileNum;
		}
		urlIndex.put(fileName, url);
	}

	protected String urlFileName(String url) throws ConsoleException {
		String fileName = null;
		Matcher matcher = pattURL.matcher(url);
		if (!matcher.find()) {
			throw new ConsoleException("Could not parse URL "+url);
		} else {
			fileName = matcher.group(1);
		}
		return fileName;
	}


	private void convertTMXFile(File tmxFile, FileWriter tmjsonFileWriter)
		throws ConsoleException {
		echo("Converting file: "+tmxFile);
		URL url = url4tmxfile(tmxFile);
		String webDomain = url.getHost();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(tmxFile);
			doc.getDocumentElement().normalize();
			NodeList list = doc.getElementsByTagName("tu");

			for (int pairNum = 0; pairNum < list.getLength(); pairNum++) {
				Node node = list.item(pairNum);
				Alignment_ES alignment =
					new Alignment_ES(url.toString(), pairNum)
						.setWebDomain(webDomain)
						.setTopics(topics);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					NodeList tuvElements = element.getElementsByTagName("tuv");
					for (int ii=0; ii < tuvElements.getLength(); ii++) {
						Element tuv = (Element)tuvElements.item(ii);
						String lang = tuv.getAttribute("xml:lang");
						lang = parseRawLang(lang);
						NodeList segElts = tuv.getElementsByTagName("seg");
						String text = segElts.item(0).getTextContent();
						text = substituteProblematicChars(text);
						alignment.setSentence(lang, text);
					}
					String alignJson = jsonifyAlignment(alignment);
					validateAlignJson(alignJson);

					tmjsonFileWriter.write(alignJson);
				}
			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ConsoleException(e);
		}
	}

	private String jsonifyAlignment(Alignment_ES alignment) {
		Set<String> ignoreFields = new HashSet<String>();

		Collections.addAll(ignoreFields,
			new String[] {"_detect_language", "additionalFields", "content",
				"creationDate", "lang", "shortDescription"}
		);
		String json = PrettyPrinter.print(alignment, ignoreFields)+"\n\n";
		return json;
	}

	private String substituteProblematicChars(String text) {
		text = text.replaceAll("[○•]", "-");
		return text;
	}

	private void validateAlignJson(String alignJson) {
		String oStream =
			"bodyEndMarker=BLANK_LINE\n" +
			"class=org.iutools.concordancer.Alignment_ES\n" +
			"\n" +
			alignJson+"\n"
			;
		try {
			new ObjectStreamReader(oStream).readObject();
		} catch (Exception e) {
			echo("INVALID ALIGNMENT JSON:\nException: "+e.getMessage()+"\n"+
				"JSON:\n"+alignJson);
		}
	}

	private String parseRawLang(String rawLang) throws ConsoleException {
		String lang = null;
		Matcher matcher = pattRawLang.matcher(rawLang);
		if (!matcher.find()) {
			throw new ConsoleException("Coulrawd not parse raw langage: "+rawLang);
		} else {
			lang = matcher.group(1);
		}
		return lang;
	}

	protected URL url4tmxfile(File tmxFile) throws ConsoleException {
		URL url = null;

		String fnameKey = tmxFileKey(tmxFile);
		if (!urlIndex.containsKey(fnameKey)) {
			throw new ConsoleException("Could not determine URL of tmx file "+tmxFile.toString()+" (file key: "+fnameKey+")");
		}
		try {
			url = new URL(urlIndex.get(fnameKey));
		} catch (MalformedURLException e) {
			throw new ConsoleException(e);
		}
		return url;
	}

	protected String tmxFileKey(File tmxFile) throws ConsoleException {
		String fnameKey = null;
		Matcher matcher = pattTmxFile.matcher(tmxFile.toString());
		if (!matcher.find()) {
			throw new ConsoleException("Could not parse tmx file: " + tmxFile);
		} else {
			fnameKey = matcher.group(1);
			fnameKey = fnameKey.replaceAll("\\.(\\d+)$", "_$1");
		}
		return fnameKey;
	}


}