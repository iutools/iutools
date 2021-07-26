package org.iutools.concordancer.tm;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;
import ca.nrc.testing.Asserter;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssertWordSpotter extends Asserter<WordSpotter> {
	public AssertWordSpotter(WordSpotter _gotObject) {
		super(_gotObject);
	}

	public AssertWordSpotter(WordSpotter _gotObject, String mess) {
		super(_gotObject, mess);
	}

	protected WordSpotter spotter() {
		return (WordSpotter)gotObject;
	}

	public AssertWordSpotter producesHighlights(
		String l1, String l1Expr, String expL1Highlight, String expL2Highlight)
		throws Exception {
		String caseDescr = "l1="+l1+", l1Expr="+l1Expr;
		String startTag = "<strong>";
		String endTag = "</strong>";
		Map<String, String> highlights = spotter().higlight(l1, l1Expr, "strong");

		Pattern pattHighlight = Pattern.compile(startTag+"([\\s\\S]*?)"+endTag);

		// Check the highlighted expressions for both languages
		for (String[] oneSide: new String[][]
			{
				new String[] {l1, expL1Highlight},
				new String[]{spotter().pair.otherLangThan(l1), expL2Highlight}
			}) {
			String lang = oneSide[0];
			String expHighlights = oneSide[1];
			String text = highlights.get(lang);
			Set<String> gotHighlightsSet = new HashSet<String>();
			Matcher matcher = pattHighlight.matcher(text);
			while (matcher.find()) {
				gotHighlightsSet.add(matcher.group(1));
			}
			Set<String> expHighlightsSet = new HashSet<String>();
			expHighlightsSet.add(expHighlights);

			AssertObject.assertDeepEquals(
				baseMessage+"\nHighlights were wrong for language "+lang+
				"(case descr: "+caseDescr+")",
				expHighlightsSet, gotHighlightsSet);
		}

		// Make sure the highlighting did not change sentences on either side
		for (String lang: new String[] {l1, spotter().pair.otherLangThan(l1)}) {
			String textNoHighlights = spotter().pair.getText(lang);
			String textWithHighlights = highlights.get(lang);
			String textWithHighlightsStripped = textWithHighlights.replaceAll(pattHighlight.pattern(), "$1");
			AssertString.assertStringEquals(
				"Highlighting seems to have done more than just inserting highlights.",
				textNoHighlights, textWithHighlightsStripped
			);
		}

		return this;
	}
}
