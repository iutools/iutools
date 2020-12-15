package org.iutools.documents;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.config.IUConfig;
import ca.nrc.config.ConfigException;
import junit.framework.TestCase;

public class NRC_PDFDocumentTest extends TestCase {

	public void testGetContents() throws ConfigException {
		String pdfURLName = "file:///"+IUConfig.getIUDataPath()+"/src/test/A-03763i_90.pdf";
		try {
			NRC_PDFDocument doc = new NRC_PDFDocument(pdfURLName);
			String contents = doc.getContents();
			String[] fontNames = doc.getAllFontsNames();
			doc.close();
//			System.out.println("font names: "+Arrays.toString(fontNames));
//			System.out.println(contents);
			String targetPattern = "Z\\?m4f5\\s+WJmJ5 g4yCsti4\\s+kNo8i Z\\?m4fi9l Wp5yC6t4f5,\\s+";
			Pattern p = Pattern.compile(targetPattern);
			Matcher mp = p.matcher(contents);
			assertTrue("Mauvais résultat",mp.find());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
