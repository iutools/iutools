package org.iutools.concordancer;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StderrCapture {
	private static PrintStream oldStderr = null;
	static ByteArrayOutputStream baos = null;
	static PrintStream ps = null;

	public static void startCapturing() {
		if (oldStderr == null) {
			 oldStderr = System.out;
			 baos = new ByteArrayOutputStream();
			 ps = new PrintStream(baos);
			 System.setErr(ps);
		}
	}

	public static String stopCapturing() {
		String output = "";
		if (oldStderr != null) {
			System.out.flush();
			System.setOut(oldStderr);
			oldStderr = null;
			output = baos.toString();
		}

		return output;
	}

}
