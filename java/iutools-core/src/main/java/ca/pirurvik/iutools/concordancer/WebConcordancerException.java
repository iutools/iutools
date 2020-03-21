package ca.pirurvik.iutools.concordancer;

import ca.nrc.data.harvesting.PageHarvesterException;

public class WebConcordancerException extends Exception {

	public WebConcordancerException(String mess, Exception e) {
		super(mess, e);
	}

	public WebConcordancerException(String mess) {
		super(mess);
	}

	public WebConcordancerException(Exception e) {
		super(e);
	}
}
