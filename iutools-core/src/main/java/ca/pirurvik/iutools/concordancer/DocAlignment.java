package ca.pirurvik.iutools.concordancer;

import java.beans.Transient;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DocAlignment {

	/**
	 * Indicates if we are interested in the complete page or its
	 * main part (excluding things like banners, menus, advertisement).
	 */
	public static enum PageSection {ALL, MAIN}

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
	
	Map<String,List<String>> _pageSentences =
		new HashMap<String,List<String>>();

	Map<String,List<String>> _pageMainSentences =
		new HashMap<String,List<String>>();

	
	public List<Alignment> alignmentsAll = new ArrayList<Alignment>();
	public List<Alignment> alignmentsMain = new ArrayList<Alignment>();
	public Map<String,String> pagesAllText = new HashMap<String,String>();
	public Map<String,String> pagesMainText = new HashMap<String,String>();
	public Map<String,URL> pagesURL = new HashMap<String,URL>();
	public Map<String,String> pagesHtml = new HashMap<String,String>();

	public DocAlignment() {
		init_DocAlignment(null, null);		
	}
	
	public DocAlignment(String... langs) {
		init_DocAlignment(langs);
	}

	private void init_DocAlignment(String... langs) {
		for (String lang: langs) {
			if (lang != null) {
				pagesAllText.put(lang, null);
				pagesMainText.put(lang, null);
				pagesURL.put(lang, null);
			}
		}
	}
	
	@Transient
	public Set<String> getLanguages() {
		Set<String> langs = pagesAllText.keySet();
		return langs;
	}

	@JsonIgnore
	public List<Alignment> getAligments() {
		return getAligments((PageSection)null);
	}

	@JsonIgnore
	public List<Alignment> getAligments(PageSection pageSection) {
		if (pageSection == null) {
			pageSection = PageSection.ALL;
		}
		List<Alignment> alignments = null;
		if (pageSection == PageSection.ALL) {
			alignments = alignmentsAll;
		} else {
			alignments = alignmentsMain;
		}
		return alignments;
	}

	public DocAlignment addAlignment(Alignment alignment) {
		return addAlignment((PageSection)null, alignment);
	}

	public DocAlignment addAlignment(PageSection pageSection, Alignment alignment) {
		getAligments(pageSection).add(alignment);
		return this;
	}

	public Map<String,String> pagesTextHash() {
		return pagesAllText;
	}

	public DocAlignment setPageText(String lang, String text)
		throws DocAlignmentException {
		if (lang == null || !this.getLanguages().contains(lang)) {
			throw new DocAlignmentException(
			"Trying to set COMPLETE text for a page in unexpected language: "+lang);
		}
		pagesAllText.put(lang, text);

		return this;
	}

	@JsonIgnore
	public String getPageText(String lang) {
		String content = pagesAllText.get(lang);
		return content;
	}


	public Map<String,String> pagesMainTextHash() {
		return pagesMainText;
	}

	public DocAlignment setPageMainText(String lang, String text)
		throws DocAlignmentException {
		if (lang == null || !this.getLanguages().contains(lang)) {
			throw new DocAlignmentException(
			"Trying to set MAIN text for a page in unexpected language: "+lang);
		}
		pagesMainText.put(lang, text);

		return this;
	}

	@JsonIgnore
	public String getPageMainText(String lang) {
		return pagesMainText.get(lang);
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

	protected Map<String,List<String>> pageSentencesMap() {
		return _pageSentences;
	}

	public List<String> getPageSentences(String lang) {
		List<String> sentences = _pageSentences.get(lang);
		return sentences;
	}
	
	public DocAlignment setPageSentences(String lang, List<String> sentences) {
		_pageSentences.put(lang, sentences);
		return this;
	}

	public DocAlignment setPageMainSentences(String lang, List<String> sentences) {
		_pageMainSentences.put(lang, sentences);
		return this;
	}

	public Map<String,String> pagesHtmlHash() {
		return pagesHtml;
	}


	public void setPageHtml(String urlLang, String html) {
		pagesHtml.put(urlLang, html);
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

	public  boolean hasTextForBothLanguages(PageSection pageSection) {
		int numWithContent = 0;

		Map<String, String> text4Lang = null;
		if (pageSection == PageSection.MAIN) {
			text4Lang = this.pagesMainTextHash();
		} else {
			text4Lang = this.pagesTextHash();
		}
		for (String lang: getLanguages()) {
			if (text4Lang.get(lang) != null) {
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
			if (getPageText(lang) == null) {
				bothFetched = false;
				break;
			}
		}
		
		return bothFetched;
	}

	public String problems2str() {
		return problems2str("\n");
	}

	public String problems2str(String delimiter) {
		String allprobs = "";
		for (Map.Entry<Problem, Exception> aProblem:
			problemsEncountered.entrySet()) {
			allprobs += "\n   "+aProblem.getKey()+": "+
				aProblem.getValue().getMessage();
		}
		return allprobs;
	}
}
