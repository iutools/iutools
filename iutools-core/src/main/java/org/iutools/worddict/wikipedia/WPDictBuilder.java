package org.iutools.worddict.wikipedia;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WPDictBuilder {

	public static void main(String[] args) throws Exception {
		new WPDictBuilder().run(args);
	}

	public WPDictBuilder() {

	}

	public void run(String[] args) throws WPException {
		Map<String,String> translations = parseWikiDataResponse(
			"<api success=\"1\">\n" +
			"<entities>\n" +
			"<entity type=\"item\" id=\"Q74560\">\n" +
			"<labels>\n" +
			"<label language=\"en\" value=\"spermatozoon\"/>\n" +
			"</labels>\n" +
			"</entity>\n" +
			"</entities>\n" +
			"</api>");



//		String iuWord = args[0];
//		String enWord = findEnWord(iuWord);
//		System.out.println("iuWord: "+iuWord);
//		System.out.println("xml of en wikidata: "+enWord);
	}

//	private String parseEnWord(String xml) throws WPException {
//
//		try {
//			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
//			Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
//			Element apiNode = doc.getDocumentElement();
//
//			return "BLAH";
//		} catch (SAXException | IOException | ParserConfigurationException e) {
//			throw new WPException(e);
//		}
//
//	}

	private String findEnWord(String iuWord) throws WPException {
		try {
			iuWord = URLEncoder.encode(iuWord, String.valueOf(StandardCharsets.UTF_8));
			URL url = new URL(
				"https://www.wikidata.org/w/api.php?action=wbgetentities&sites=iuwiki&titles="+
				iuWord+"&languages=en&props=labels&format=xml");
			return retrieveURL(url);
		} catch (MalformedURLException | UnsupportedEncodingException e) {
			throw new WPException(e);
		}
	}

	public String retrieveURL(URL url) throws WPException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();

			InputStream stream = url.openStream();
			Document doc = docBuilder.parse(stream);
			Element apiElement = doc.getDocumentElement();
			NodeList labelsNodes = doc.getElementsByTagName("labels");

			Element label = (Element)doc.getElementsByTagName("labels").item(0);
			String enWord = label.getAttribute("value");
			return enWord;
		} catch (ParserConfigurationException | IOException | SAXException e) {
			throw new WPException(e);
		}
	}

	public Map<String,String> parseWikiDataResponse(String xml) throws WPException {
		try {
			Map<String,String> translations = new HashMap<String,String>();

			// Instantiate the Factory
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

			// optional, but recommended
			// process XML securely, avoid attacks like XML External Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parse XML string
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new InputSource(new StringReader(xml)));

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
					System.out.println("lang=" + lang + ", value=" + value);
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
