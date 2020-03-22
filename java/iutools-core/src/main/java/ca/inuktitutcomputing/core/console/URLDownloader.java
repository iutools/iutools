package ca.inuktitutcomputing.core.console;

import ca.nrc.data.harvesting.PageHarvester_Barebones;
import ca.nrc.data.harvesting.PageHarvesterException;

public class URLDownloader {

	public static void main(String[] args) throws PageHarvesterException {
		PageHarvester_Barebones harvester = new PageHarvester_Barebones();
		String url = args[0];
		System.out.println("Harvesting content of url: "+url);
		harvester.harvestSinglePage(url);
		String text = harvester.getText();
		System.out.println("Text is:\n\n"+text);
		String html = harvester.getHtml();
		System.out.println("\n============\nhtml is:\n\n"+html);
	}

}
