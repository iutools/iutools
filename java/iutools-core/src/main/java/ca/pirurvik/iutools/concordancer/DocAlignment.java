package ca.pirurvik.iutools.concordancer;

import java.beans.Transient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DocAlignment {
	
	public boolean success = false;
	public List<String> problemsEncountered = new ArrayList<String>();
	List<Alignment> alignments = new ArrayList<Alignment>();
	Map<String,String> pagesContent = new HashMap<String,String>();
	Map<String,URL> pagesURL = new HashMap<String,URL>();

	
	public DocAlignment() {
		init_DocAlignment(null, null);		
	}
	
	public DocAlignment(String lang1, String lang2) {
		init_DocAlignment(lang1, lang2);
	}

	private void init_DocAlignment(String lang1, String lang2) {
		if (lang1 != null) {
			pagesContent.put(lang1, null);
			pagesURL.put(lang1, null);
		}
		if (lang2 != null) {
			pagesContent.put(lang2, null);
			pagesURL.put(lang2, null);
		}
	}
	
	@Transient
	public Set<String> getLanguages() {
		Set<String> langs = pagesContent.keySet();
		return langs;
	}

	public List<Alignment> getAligments() {
		return alignments;
	}
	
	public DocAlignment addAlignment(Alignment alignment) {
		alignments.add(alignment);
		return this;
	}

	public DocAlignment setPageContent(String lang, String text) {
		pagesContent.put(lang, text);
		return this;
	}
	
	@JsonIgnore
	public String getPageContent(String lang) {
		String content = pagesContent.get(lang);
		return content;
	}

	@JsonIgnore
	public URL getPageURL(String lang)  {
		URL url = pagesURL.get(lang);
		return url;
	}

	public DocAlignment setPageURL(String lang, URL url) {
		pagesURL.put(lang, url);
		return this;
	}

	public DocAlignment addProblem(String errMess) {
		problemsEncountered.add(errMess);
		return this;
	}

	public DocAlignment setSuccess(boolean _success) {
		this.success = _success;
		return this;
	}
}
