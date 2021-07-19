package org.iutools.concordancer;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.json.PrettyPrinter;
import org.junit.Assert;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import org.junit.jupiter.api.Assertions;

public class DocAlignmentAsserter {
	private static final DocAlignment pageAlignment = null;

	String baseMessage = "";
	DocAlignment gotDocAlignment = null;
	
	
	public static DocAlignmentAsserter assertThat(
			DocAlignment pageAligment) {
		DocAlignmentAsserter asserter =  
			new DocAlignmentAsserter(pageAligment);
		return asserter;
	}

	public static DocAlignmentAsserter assertThat(
			DocAlignment pageAligment,  String _baseMess) {
		DocAlignmentAsserter asserter = 
			new DocAlignmentAsserter(pageAligment, _baseMess);
		return asserter;
	}
	
	public DocAlignmentAsserter(DocAlignment pageAligment) {
		this.gotDocAlignment = pageAligment;
	}

	public DocAlignmentAsserter(
			DocAlignment _pageAligment, String _baseMess) {
		init__AlignmentResultAsserter(_pageAligment, _baseMess);
	}

	private void init__AlignmentResultAsserter(
			DocAlignment _pageAligment, String _baseMess) {
		this.baseMessage = _baseMess;
		this.gotDocAlignment = _pageAligment;
	}

	public void containsAlignment(SentencePair expAlignment) {
		
		boolean found = false;
		String errMess =
			"Alignments did not contain an expected alignment.\n"+
			"Expected: "+expAlignment.toString()+"\n"+
			"Got:\n";
				
		for (SentencePair anAlignment: gotDocAlignment.alignmentsAll) {
			errMess += "  "+anAlignment.toString()+"\n";
			if (anAlignment.toString().equals(expAlignment.toString())) {
				found = true;
				break;
			}
		}
		
		Assert.assertTrue(errMess, found);			
	}


	public DocAlignmentAsserter didNotEncounterProblems() {
		String message =
			baseMessage+"\n"+
			"SentencePair should NOT have encountered problems but it did.\n"+
			"Problems were:\n  "+gotDocAlignment.problems2str("\n  ");
		Assert.assertFalse(message, gotDocAlignment.encounteredSomeProblems());
		return this;
	}	
	
	public void alignmentsEqual(String mess, String lang1, String lang2, 
			Pair<String, String>[] expAlPairs) throws Exception {

		String[] expAlStrs = new String[expAlPairs.length];
		for (int ii=0; ii < expAlPairs.length; ii++) {
			expAlStrs[ii] = 
					"(" +
					lang1 + ":" + expAlPairs[ii].getFirst() + 
					" <--> " +
					lang2 + ":" + expAlPairs[ii].getSecond() + 
					")";
		}
		List<SentencePair> gotAlList = this.gotDocAlignment.getAligments();
		String[] gotAlStrs = new String[gotAlList.size()];
		for (int ii=0; ii < gotAlList.size(); ii++) {
			gotAlStrs[ii] = gotAlList.get(ii).toString();
		}
		
		AssertObject.assertDeepEquals(
				"Alignments texts were not as expected.", 
				expAlStrs, gotAlStrs);
	}

	public DocAlignmentAsserter urlForLangEquals(
				String lang, URL expURL) throws Exception {
		URL gotURL = gotDocAlignment.getPageURL(lang);
		AssertString.assertStringEquals(
				"URL of the "+lang+" page was not as expected.",
				expURL.toString(), gotURL.toString());;
		return this;
	}

	public DocAlignmentAsserter mainTextContains(String lang, String expText) {
		String gotText = gotDocAlignment.getPageMainText(lang);
		AssertString.assertStringContains(
		baseMessage+"\nPlain text of the MAIN page content for lang="+lang+" was not as expected",
		gotText, expText);
		return this;
	}

	public DocAlignmentAsserter completeTextContains(String lang, String expText) {
		String gotText = gotDocAlignment.getPageText(lang);
		AssertString.assertStringContains(
				baseMessage+"\nPlain text of the COMPLETE page content for lang="+lang+" was not as expected",
				gotText, expText);	
		return this;
	}

	public DocAlignmentAsserter pageTextIsNotHtml(String... langs) {

		Pattern pattHtmlTag = Pattern.compile("(</([^>])>)");

		for (String aLang: langs) {
			for (String text : new String[]{
			gotDocAlignment.getPageText(aLang),
			gotDocAlignment.getPageMainText(aLang)}) {
				if (text != null) {
					Matcher matcher = pattHtmlTag.matcher(text);
					if (matcher.find() && matcher.group(0).length() < 20) {
						Assert.fail(
						"Text for language " + aLang + " contained HTML tags\n" +
						"First tag found: " + matcher.group(0));
					}
				}
			}
		}
		return this;
	}

	public DocAlignmentAsserter hasNoContentForLang(String lang) {
		String content = gotDocAlignment.getPageText(lang);
		Assert.assertEquals(
			"SentencePair should not have had content for language "+lang,
			null, content);
		
		return this;
	}

	public DocAlignmentAsserter containsSentenceStartingWith(String lang, String expSent) {
		String errMess = 
			"Could not find sentences for lang="+lang+": "+expSent+"\n"+
			"Sentences were:\n"
			;
		boolean found = false;
		for (String gotSent: gotDocAlignment.getPageSentences(lang)) {
			if (gotSent.startsWith(expSent)) {
				found = true;
				break;
			}
			errMess += "   '"+gotSent+"'\n";
		}
		
		Assert.assertTrue(errMess, found);
		
		return this;
	}
	
	public DocAlignmentAsserter encounteredProblems(DocAlignment.Problem expProblem) {
		return encounteredProblems(new DocAlignment.Problem[] {expProblem});
	}

	
	public DocAlignmentAsserter encounteredProblems(DocAlignment.Problem[] expProblems) {
		
		Iterator<DocAlignment.Problem> probIter = 
				gotDocAlignment.problemsEncountered.keySet().iterator();
		
			String encounteredProbsMess = 
				"'\nProblems actually encountered were:\n   "+
				StringUtils.join(probIter, "\n   ")+"\n";
		
		for (DocAlignment.Problem probDescr: expProblems) {
			String errMess = 
				baseMessage+"\nShould have encountered problem '"+probDescr+
				encounteredProbsMess;

			Assert.assertTrue(
				errMess,
				gotDocAlignment.problemsEncountered.get(probDescr) != null);
		}
		return this;
	}

	private DocAlignment.Problem[] toProblemObjects(String[] problemsStr) {
		DocAlignment.Problem[] problems = new DocAlignment.Problem[problemsStr.length];
		for (int ii=0; ii < problemsStr.length; ii++) {
			DocAlignment.Problem iithProblem = DocAlignment.Problem.valueOf(problemsStr[ii]);
			problems[ii] = iithProblem;
		}
			
		return problems;
	}

	public DocAlignmentAsserter providesValuesFor(WebConcordancer.AlignOptions... what) {
		for (WebConcordancer.AlignOptions field: what) {
			Object fieldValue = value4field(field);
			Assertions.assertTrue(
				null != fieldValue,
			baseMessage+"\nThe "+field+" should NOT have been null");

			if (fieldValue instanceof Collection) {
				Assertions.assertFalse(
					((Collection<?>) fieldValue).isEmpty(),
					baseMessage+"\nThe "+field+" should NOT have been empty");
			}

			if (fieldValue instanceof Map) {
				Map<?,?> fieldValueMap = (Map)fieldValue;
				Assertions.assertFalse(
					fieldValueMap.isEmpty(),
					baseMessage+"\nThe "+field+" should NOT have been empty");
			}

			if (fieldValue instanceof Map) {
				Map<String,Object> fieldLangsMap = (Map<String,Object>)fieldValue;
				String mess = baseMessage+"\nThe "+field+" map should have non-null entries for all languages.\n";
				for (String lang: fieldLangsMap.keySet()) {
					mess +=
						"Entry for language "+lang+" was null.\n"+
						"Field value was:\n"+PrettyPrinter.print(fieldValue);
					Assertions.assertTrue(
						null != fieldLangsMap.get(lang),
						mess
					);
				}
			}
		}

		return this;
	}

	public DocAlignmentAsserter doesNotProvideValuesFor(WebConcordancer.AlignOptions... what) {
		for (WebConcordancer.AlignOptions field: what) {
			Object fieldValue = value4field(field);
			if (fieldValue instanceof List) {
				Assertions.assertTrue(
					fieldValue == null ||
						((List) fieldValue).isEmpty(),
					baseMessage+"\nField "+field+" should have been empty but it was\n"+
						PrettyPrinter.print(fieldValue)
				);
			} else if (fieldValue instanceof Map) {
				Map<String,Object> fieldLangsMap = (Map<String,Object>)fieldValue;
				String mess = baseMessage+"\nThe "+field+" map should have null entries for all languages.\n";
				for (String lang: fieldLangsMap.keySet()) {
					mess +=
						"Entry for language "+lang+" was not not null.\n"+
						"Field value was:\n"+PrettyPrinter.print(fieldValue);
					Assertions.assertTrue(
						null == fieldLangsMap.get(lang),
						mess
					);
				}
			} else {
				throw new RuntimeException("Field "+field+" was of a type that could not be checked.\nType was: "+fieldValue.getClass());
			}
		}
		return this;
	}

	private Object value4field(WebConcordancer.AlignOptions field) {
		Object value = null;
		if (field == WebConcordancer.AlignOptions.ALIGNED_SENTENCES) {
			value = new ArrayList<SentencePair>();
			List<SentencePair> alignedSentences =
				docAlignment().getAligments(DocAlignment.PageSection.ALL);
			((List<SentencePair>)value).addAll(alignedSentences);
			alignedSentences =
				docAlignment().getAligments(DocAlignment.PageSection.MAIN);
			((List<SentencePair>)value).addAll(alignedSentences);
		} else if (field == WebConcordancer.AlignOptions.ALL_TEXT) {
			value = docAlignment().pagesTextHash();
		} else if (field == WebConcordancer.AlignOptions.MAIN_TEXT) {
			value = docAlignment().pagesMainTextHash();
		} else if (field == WebConcordancer.AlignOptions.HTML) {
			value = docAlignment().pagesHtmlHash();
		} else {
			throw new RuntimeException("Cannot get value of field "+field);
		}

		return value;
	}

	DocAlignment docAlignment() {
		return (DocAlignment) gotDocAlignment;
	}
}
