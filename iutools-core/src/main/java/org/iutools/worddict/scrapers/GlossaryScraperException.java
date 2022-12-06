package org.iutools.worddict.scrapers;


public class GlossaryScraperException extends Exception {
	public GlossaryScraperException(String mess, Exception e) {
		super(mess, e);
	}

	public GlossaryScraperException(Exception e) {
		super(e);
	}

}
