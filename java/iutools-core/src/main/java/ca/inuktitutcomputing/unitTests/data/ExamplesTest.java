package ca.inuktitutcomputing.unitTests.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import junit.framework.TestCase;
import ca.inuktitutcomputing.data.Examples;

public class ExamplesTest extends TestCase {
	
	public void test_locateFile() {
		System.out.println("classpath: "+System.getProperty("java.class.path"));
		Examples ex = new Examples();
		String filename = ex.filename;
		String filePath = ex.locateFile(filename);
		System.out.println("filePath: "+filePath);
	}

	/*public void test_getExampleStream() {

		BufferedReader r = null;
		Examples ex = new Examples();
		InputStream is = ex.getExampleStream();

		if (is != null) {
			InputStreamReader isr;
			try {
				isr = new InputStreamReader(is, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				isr = new InputStreamReader(is);
			}
			r = new BufferedReader(isr);
		}

		Hashtable examples = new Hashtable();
		boolean eof = false;

		while (r != null && !eof) {
			String line = null;
			try {
				line = r.readLine();
			} catch (IOException e) {
			}
			if (line == null)
				eof = true;
			else {
				System.out.println("line: " + line);
			}
		}
	}*/

}
