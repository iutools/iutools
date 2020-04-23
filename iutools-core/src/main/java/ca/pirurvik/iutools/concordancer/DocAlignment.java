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
	
	/** 
	 * Signals specific problems encountered during alignment of the page.
	 * @author desilets
	 *
	 */
	public static enum Problem {
		FETCHING_INPUT_URL, 
		GUESSING_LANGUAGE_OF_INPUT_URL,		
		
		FETCHING_CONTENT_OF_OTHER_LANG_PAGE, 
		
		ALIGNING_SENTENCES
	};
	
	public boolean success = false;
	public Map<Problem,Exception> problemsEncountered = 
		new HashMap<Problem,Exception>();
	
	private Map<String,List<String>> _pageSentences = 
		new HashMap<String,List<String>>();
	
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
	
	public List<String> getPageSentences(String lang) {
		List<String> sentences = _pageSentences.get(lang);
		return sentences;
	}
	
	public DocAlignment setPageSentences(String lang, List<String> sentences) {
		_pageSentences.put(lang, sentences);
		return this;
	}

	public void raiseProblem(Problem problem, String mess) {
		raiseProblem(problem, new Exception(mess));
		
	}
	
	public DocAlignment raiseProblem(Problem descr, Exception e) {
		problemsEncountered.put(descr, e);
		return this;
	}

	public DocAlignment setSuccess(boolean _success) {
		this.success = _success;
		return this;
	}

	public boolean hasContentForBothLanguages() {
		int numWithContent = 0;
		for (String lang: getLanguages()) {
			if (getPageContent(lang) != null) {
				numWithContent++;
			}
		}
		boolean answer = (numWithContent == 2);
		
		return answer;
	}

	public boolean encounteredSomeProblems() {
		return !problemsEncountered.isEmpty();
	}
	
	public boolean encounteredProblem(Problem problem) {
		return problemsEncountered.containsKey(problem);
	}
	
	public boolean bothLangsContentFetched() {
		boolean bothFetched = true;
		for (String lang: getLanguages()) {
			if (getPageContent(lang) == null) {
				bothFetched = false;
				break;
			}
		}
		
		return bothFetched;
	}
}
