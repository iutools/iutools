package ca.pirurvik.iutools.webservice;

import java.net.URL;

public class GistTextInputs extends ServiceInputs {
	
	public String word = null;
	
	public GistTextInputs() {}

	public GistTextInputs(String _word) {
		this.word = _word;
	}
	
	public URL inputURL() {
		URL url = null;
		try {
			new URL(word);
		} catch (Exception e) {
			// If an exception is raised, it means that 
			// the text was NOT a URL. Just leave the 
			// url to null
		}
		
		return url;
	}
}
