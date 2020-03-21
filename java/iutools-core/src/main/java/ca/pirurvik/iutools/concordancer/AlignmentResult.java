package ca.pirurvik.iutools.concordancer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AlignmentResult {
	
	Map<String,String> pagesContent = new HashMap<String,String>();
	Map<String,URL> pagesURL = new HashMap<String,URL>();

	public List<Alignment> getAligments() {
		List<Alignment> alignments = new ArrayList<Alignment>();
		return alignments;
	}

	public void setPageContent(String lang, String text) {
		pagesContent.put(lang, text);
	}
	
	@JsonIgnore
	public String getPageContent(String lang) {
		String content = pagesContent.get(lang);
		return content;
	}

	@JsonIgnore
	public URL getPageURL(String lang) throws Exception {
		URL url = pagesURL.get(lang);
		return url;
	}

	public void setPageURL(String lang, URL url) {
		pagesURL.put(lang, url);
	}

}
