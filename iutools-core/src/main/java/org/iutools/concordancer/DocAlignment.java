package org.iutools.concordancer;

import java.beans.Transient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class is used to store information about the different language
 * versions of a same document.
 */
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
	public Map<Problem,Throwable> problemsEncountered =
		new HashMap<Problem,Throwable>();
	
	Map<String,List<String>> _pageSentences =
		new HashMap<String,List<String>>();

	Map<String,List<String>> _pageMainSentences =
		new HashMap<String,List<String>>();

	
	public List<SentencePair> alignmentsAll = new ArrayList<SentencePair>();
	public List<SentencePair> alignmentsMain = new ArrayList<SentencePair>();
	public Map<String,String> pagesAllText = new HashMap<String,String>();
	public Map<String,String> pagesMainText = new HashMap<String,String>();
	public Map<String,String> pagesID = new HashMap<String,String>();
	public Map<String,String> pagesRawContent = new HashMap<String,String>();

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
				pagesID.put(lang,null);
//				pagesURL.put(lang, null);
			}
		}
	}
	
	@Transient
	public Set<String> languages() {
		Set<String> langs = pagesAllText.keySet();
		return langs;
	}

	@JsonIgnore
	public List<SentencePair> getAligments() {
		return getAligments((String)null, (String)null, (PageSection)null);
	}

	@JsonIgnore
	public List<SentencePair> getAligments(String lang1, String lang2) {
		return getAligments(lang1, lang2, (PageSection)null);
	}

	@JsonIgnore
	public List<SentencePair> getAligments(PageSection pageSection) {
		return getAligments((String)null, (String)null, pageSection);
	}

	@JsonIgnore
	public List<SentencePair> getAligments(String lang1, String lang2, PageSection pageSection) {
		if (pageSection == null) {
			pageSection = PageSection.ALL;
		}
		List<SentencePair> alignments = null;
		if (pageSection == PageSection.ALL) {
			alignments = alignmentsAll;
		} else {
			alignments = alignmentsMain;
		}
		return alignments;
	}

	public DocAlignment addAlignment(AlignmentSpec alignment) {
		return addAlignment((PageSection)null, alignment);
	}

	public DocAlignment addAlignment(PageSection pageSection, AlignmentSpec alignment) {
		// TODO: Implement this
		return this;
	}

	public DocAlignment addAlignment(SentencePair alignment) {
		return addAlignment((PageSection)null, alignment);
	}

	public DocAlignment addAlignment(PageSection pageSection, SentencePair alignment) {
		getAligments(pageSection).add(alignment);
		return this;
	}

	public Map<String,String> pagesTextHash() {
		return pagesAllText;
	}

	public DocAlignment setPageText(String lang, String text)
		throws DocAlignmentException {
		if (lang == null || !this.languages().contains(lang)) {
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
		if (lang == null || !this.languages().contains(lang)) {
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

	public DocAlignment setPageID(String lang, String id) {
		pagesID.put(lang, id);
		return this;
	}

	public String getPageID(String lang) {
		return pagesID.get(lang);
	}

	public DocAlignment setPageURL(String lang, String url) {
		return setPageID(lang, url.toString());
	}

	public DocAlignment setPageURL(String lang, URL url) {
		pagesID.put(lang, url.toString());
		return this;
	}

	@JsonIgnore
	public URL getPageURL(String lang) throws DocAlignmentException {
		String urlStr = null;
		URL url = null;
		try {
			urlStr = pagesID.get(lang);
			if (urlStr != null) {
				url = new URL(urlStr);
			}
		} catch (MalformedURLException e) {
			throw new DocAlignmentException(
			"ID for lang: "+lang+" is not a URL: "+urlStr, e);
		}
		return url;
	}

	protected Map<String,List<String>> pageSentencesMap() {
		return _pageSentences;
	}

	public List<String> getPageSentences(String lang) {
		List<String> sentences = _pageSentences.get(lang);
		return sentences;
	}

	public DocAlignment setPageSentences(String lang, String... sentences) {
		List<String> sentsList = new ArrayList<String>();
		Collections.addAll(sentsList, sentences);
		setPageSentences(lang, sentsList);
		return this;
	}

	public DocAlignment setPageSentences(String lang, List<String> sentences) {
		_pageSentences.put(lang, sentences);
		return this;
	}

	public DocAlignment setPageMainSentences(String lang, String... sentences) {
		List<String> sentsList = new ArrayList<String>();
		Collections.addAll(sentsList, sentences);
		setPageMainSentences(lang, sentsList);
		return this;
	}

	public DocAlignment setPageMainSentences(String lang, List<String> sentences) {
		_pageMainSentences.put(lang, sentences);
		return this;
	}

	public List<String> getPageMainSentences(String lang) {
		List<String> mainSentences = _pageMainSentences.get(lang);
		return mainSentences;
	}

	public Map<String,String> pagesHtmlHash() {
		return pagesRawContent;
	}


	public DocAlignment setPageRawContent(String urlLang, String html) {
		pagesRawContent.put(urlLang, html);
		return this;
	}

	public String getPageRawContent(String lang) {
		return pagesRawContent.get(lang);
	}

	public void raiseProblem(Problem problem, String mess) {
		raiseProblem(problem, new Exception(mess));
	}
	
	public DocAlignment raiseProblem(Problem descr, Throwable e) {
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
		for (String lang: languages()) {
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
		for (String lang: languages()) {
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
		for (Map.Entry<Problem, Throwable> aProblem:
			problemsEncountered.entrySet()) {
			allprobs += "\n   "+aProblem.getKey()+": "+
				aProblem.getValue().getMessage();
		}
		return allprobs;
	}
}
