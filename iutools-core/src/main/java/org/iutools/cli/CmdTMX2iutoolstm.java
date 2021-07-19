package org.iutools.cli;

import ca.nrc.data.file.FileGlob;
import ca.nrc.ui.commandline.CommandLineException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class CmdTMX2iutoolstm extends ConsoleCommand {
	public CmdTMX2iutoolstm(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Convert a TMX file to iutool's tm.json format.";
	}

	@Override
	public void execute() throws Exception {
		File tmxDir = getInputDir();
		Path tmJsonFPath = getOutputFile();
		File[] files = FileGlob.listFiles(tmxDir.toString() + "/*.json");
		for (File tmxFile : files) {
			convertTMXFile(tmxFile);
		}
	}


	private void convertTMXFile(File tmxFile) throws ConsoleException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(tmxFile);
			doc.getDocumentElement().normalize();

			System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
			System.out.println("------");

			// get <staff>
			NodeList list = doc.getElementsByTagName("tu");

			for (int temp = 0; temp < list.getLength(); temp++) {

				Node node = list.item(temp);

			}

		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ConsoleException(e);
		}
	}

}