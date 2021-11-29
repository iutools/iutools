package org.iutools.worddict.wikipedia;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WPDictBuilder {

	public static void main(String[] args) throws Exception {
		new WPDictBuilder().run(args);
	}

	public WPDictBuilder() {

	}

	public void run(String[] args) throws WPException {
		String enWord = parseWikiDataResponse(
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

	public String parseWikiDataResponse(String xml) throws WPException {
		try {
			String translation = "";
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = dbf.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));
			Element apiNode = doc.getDocumentElement();

			return translation;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new WPException(e);
		}
	}
}
