package org.iutools.concordancer.tm;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.worddict.GlossaryEntry;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TMEvaluator {
	UserIO userIO = new UserIO();
	TranslationMemory tm = new TranslationMemory();
	ObjectMapper mapper = new ObjectMapper();
	int MAX_ALIGNMENTS = 100;

	public EvaluationResults evaluate(Path glossaryFile, Integer firstN) throws TranslationMemoryException {
		EvaluationResults results = new EvaluationResults();
		try {
			ObjectStreamReader reader =
				new ObjectStreamReader(glossaryFile.toFile());
			GlossaryEntry entry = (GlossaryEntry) reader.readObject();
			while (entry != null) {
				if (firstN != null && results.totalEntries >= firstN) {
					break;
				}
				onNewGlossaryEntry(entry, results);
				entry = (GlossaryEntry) reader.readObject();
			}
			printReport(results);
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		return results;
	}

	private void onNewGlossaryEntry(GlossaryEntry entry, EvaluationResults results) throws Exception {
		String term = entry.getTermInLang("iu_roman");
		results.totalEntries++;
		userIO.echo(results.totalEntries+". iu:"+term+", en:"+entry.getTermInLang("en")+" ...");
		evaluateGlossaryTerm(entry, results);
	}


	private void evaluateGlossaryTerm(GlossaryEntry glossEntry, EvaluationResults results) throws Exception {
		userIO.echo(1);
		try {
			String enTerm = glossEntry.getTermInLang("en").toLowerCase();
			String iuTerm_roman = glossEntry.getTermInLang("iu_roman").toLowerCase();
			String iuTerm_syll = glossEntry.getTermInLang("iu_syll").toLowerCase();
			AlignmentsSummary algnSummary =
				analyzeAlignments(iuTerm_syll, enTerm);

			if (algnSummary.iuTermPresent) {
				userIO.echo("IU term was present in the STRICT sense");
				results.totalIUPresent_Orig++;
			}
			if (algnSummary.enTranslPresent_strict) {
				results.totalENPresent_Strict++;
				userIO.echo("EN translation was PRESENT in the STRICT sense");
			}
			if (algnSummary.enTranslPresent_lenient) {
				userIO.echo("EN translation was PRESENT in the LENIENT sense");
				if (!algnSummary.enTranslPresent_lenient_matches.isEmpty()) {
					userIO.echo("   ** LENIENT occurences="+mapper.writeValueAsString(algnSummary.enTranslPresent_lenient_matches));
				}
				results.totalENPresent_Lenient++;
			}

			if (algnSummary.enTranslSpotted_strict) {
				userIO.echo("EN translation was SPOTTED in the STRICT sense");
				results.totalENSpotted_Strict++;
			}

			if (algnSummary.enTranslSpotted_lenient) {
				userIO.echo("EN translation was SPOTTED in the LENIENT sense");
				if (!algnSummary.enTranslSpotted_lenient_matches.isEmpty()) {
					userIO.echo("   ** LENIENT occurences="+mapper.writeValueAsString(algnSummary.enTranslSpotted_lenient_matches));
				}
				results.totalENSpotted_Lenient++;
			}
		} finally {
			userIO.echo(-1);
		}
	}

	private AlignmentsSummary analyzeAlignments(String iuTerm_syll, String enTerm) throws TranslationMemoryException {
		try {
			AlignmentsSummary analysis = new AlignmentsSummary();
			iuTerm_syll = iuTerm_syll.toLowerCase();
			enTerm = enTerm.toLowerCase();
			Iterator<Alignment_ES> algsIter = tm.searchIter("iu", iuTerm_syll, "en");
			int totalAlignments = 0;
			while (algsIter.hasNext() && totalAlignments < MAX_ALIGNMENTS) {
				boolean attemptSpotting = false;
				Alignment_ES algn = algsIter.next();
				if (alignmentContains(algn, "iu", iuTerm_syll)) {
					analysis.iuTermPresent = true;
					totalAlignments++;
					if (alignmentContains(algn, "en", enTerm)) {
						analysis.enTranslPresent_strict = true;
						attemptSpotting = true;
					} else {
						String found = findTextInAlignment(algn, "en", enTerm, true);
						if (found != null) {
							attemptSpotting = true;
							analysis.enTranslPresent_lenient = true;
							analysis.enTranslPresent_lenient_matches.add(found);
						}
					}
					if (attemptSpotting) {
						SpottingCheck spottingStatus =
							checkEnTermSpotting(algn, iuTerm_syll, enTerm);
						if (spottingStatus.spottedStrict) {
							analysis.enTranslSpotted_strict = true;
						}
						if (null != spottingStatus.spottedLenient) {
							analysis.enTranslSpotted_lenient = true;
							analysis.enTranslSpotted_lenient_matches.add(spottingStatus.spottedLenient);
						}
						if (null != spottingStatus.spottedLenientOverlap) {
							analysis.enTranslSpotted_lenientoverlap = true;
						}
					}

					if (analysis.enTranslPresent_strict) {
						// Strict presence implies lenient as well
						analysis.enTranslPresent_lenient = true;
					}

					if (analysis.enTranslSpotted_strict) {
						// Strict spotting implies lenient as well
						analysis.enTranslSpotted_lenient = true;
					}

					if (analysis.enTranslSpotted_strict) {
						// We found at least one alignment where IU term is present
						// and EN translation was spotted in the STRICT sense.
						// No need to go further.
						break;
					}
				}
			}

			return analysis;
		} catch (TranslationMemoryException e) {
			throw new TranslationMemoryException(e);
		}
	}

	private SpottingCheck checkEnTermSpotting(
		Alignment_ES algn, String iuTerm_syll, String enTerm) throws TranslationMemoryException {
		SpottingCheck check = new SpottingCheck();
//		boolean spottedStrict = false;
//		String spottedLenient = null;
//		String spottedLenientOverlap = null;
		try {
			Map<String, String> spotting =
				new WordSpotter(algn.sentencePair("iu", "en"))
					.spot("iu", iuTerm_syll);
			String spottedEn = spotting.get("en");
			if (spottedEn != null) {
				spottedEn = spottedEn.replaceAll("\\s+\\.\\.\\.\\s+", " ");
				if (spottedEn.equals(enTerm)) {
					check.spottedStrict = true;
				}
				check.spottedLenient = findText(spottedEn, enTerm, true);
				if (check.spottedLenient == null) {
					check.spottedLenient = findText(enTerm, spottedEn, true);
				}
				check.spottedLenientOverlap = partialOverlap(enTerm, spottedEn, true);
//				if (null != partialOverlap(enTerm, spottedEn, true)) {
//					check.spottedLenientOverlap = true;
//				}
			}
		} catch (WordSpotterException e) {
			throw new TranslationMemoryException(e);
		}

		return check;
	}


	private boolean alignmentContains(
		Alignment_ES algn, String lang, String expText) throws TranslationMemoryException {
		return alignmentContains(algn, lang, expText, (Boolean)null);
	}

	private boolean alignmentContains(
		Alignment_ES algn, String lang, String expText, Boolean lenient) throws TranslationMemoryException {
		String found = findTextInAlignment(algn, lang, expText, lenient);
		boolean answer = (found != null);
		return answer;
	}


	private String findTextInAlignment(
		Alignment_ES algn, String lang, String expText, Boolean lenient) throws TranslationMemoryException {
		if (lenient == null) {
			lenient = false;
		}
		if (lang.equals("iu")) {
			lenient = false;
		}

		String sentence = algn.sentence4lang(lang);
		String found = null;
		if (sentence != null) {
			found = findText(expText, sentence, lenient);
		}
		return found;
	}

	private static String truncateWord(String word) {
		final int MAX_LEN = 5;
		if (word.length() > MAX_LEN) {
			word = word.substring(0, MAX_LEN);
		}
		if (! word.matches("^[^a-zA-Z\\-\\d]+$")) {
			word += "*";
		};
		return word;
	}

	private String[] truncateWords(String[] words, Boolean lenient) {
		for (int ii=0; ii < words.length; ii++) {
			words[ii] = truncateWord(words[ii]);
		}
		return words;
	}


	protected static String[] lemmatizeWords(String[] words) {
		String[] lemmatized = new String[words.length];
		for (int ii=0; ii <  lemmatized.length; ii++) {
			lemmatized[ii] = truncateWord(words[ii]);
		}
		return lemmatized;
	}


	protected String lemmatizeWord(String word) {
		return lemmatizeWord(word, (Boolean)null);
	}

	protected String lemmatizeWord(String word, Boolean last) {
		if (last == null) {
			last = false;
		}
		word = truncateWord(word);

		String regexp = word;
		if (!last) {
			word += "[a-zA-Z\\-]*";
		}
		return regexp;
	}

	protected String lemmatizePhrase(String phrase) {
		String[] tokens = phrase.split("[^a-zA-Z\\-]+");
		for (int ii=0; ii < tokens.length; ii++) {
			boolean last = false;
			if (ii == tokens.length - 1) {
				last = true;
			}
			tokens[ii] = lemmatizeWord(tokens[ii], last);
		}

		String regexp = StringUtils.join(tokens, "[^a-zA-Z\\-]+");
		return regexp;
	}

	public Pair<MatchType, String> findTerm(String term, String inText) {
		String[] termTokens = tokenize(term);
		String[] textTokens = tokenize(inText);
		String occFound = null;
		MatchType typeFound = null;
		for (int ii=0; ii < textTokens.length; ii++) {
			if (typeFound == MatchType.STRICT) {
				break;
			}
			int maxPos = Math.min(ii+5,textTokens.length-1);
			for (int jj=maxPos; jj >= ii; jj--) {
				String[] textTermTokens = Arrays.copyOfRange(textTokens, ii, jj);
				Pair<MatchType,String> match = sameTerm(termTokens, textTokens);
				if (typeFound == null || isMoreLenient(typeFound, match.getLeft())) {
					typeFound = match.getLeft();
					occFound = match.getRight();
				}
				if (typeFound == MatchType.STRICT) {
					break;
				}
			}
		}

		return Pair.of(typeFound, occFound);
	}

	public static Pair<MatchType, String> sameTerm(String[] term1Toks, String[] term2Toks) {
		MatchType matchType = null;
		String overlap = null;
		String term1 = String.join(" ", term1Toks);
		if (term1.equals(String.join(" ", term2Toks))) {
			matchType = MatchType.STRICT;
			overlap = term1;
		} else {
			String[] term1LemToks = lemmatizeWords(term1Toks);
			String[] term2LemToks = lemmatizeWords(term2Toks);
			// Check if all the lemmatized tokens are the same
			if (term1LemToks.length == term2LemToks.length) {
				boolean same = true;
				for (int ii=0; ii < term1LemToks.length; ii++) {
					if (!term1LemToks[ii].equals(term2LemToks[ii])) {
						same = false;
						break;
					}
				}
				if (same) {
					matchType = MatchType.LENIENT;
					overlap = String.join(" ", term1LemToks);
				} else {
					// Check if the two terms share at least one lemmatized token
					String commonToken = null;
					for (int ii=0; ii < term1LemToks.length; ii++) {
						if (commonToken != null) break;
						for (int jj=0; jj < term2LemToks.length; jj++) {
							if (term1LemToks[ii].equals(term2LemToks[jj])) {
								commonToken = term1LemToks[ii];
								break;
							}
						}
					}
					if (commonToken != null) {
						matchType = MatchType.LENIENT_OVERLAP;
						overlap = commonToken;
					}
				}
			}
		}

		return Pair.of(matchType, overlap);
	}

	public static String[] tokenize(String text) {
		return text.split("\\s+");
	}


	public String findText(String findWhat, String text, Boolean lenient) throws TranslationMemoryException {
		if (lenient == null) {
			lenient = false;
		}
		if (lenient) {
			findWhat = removeRegexpChars(findWhat);
			text = removeRegexpChars(text);
		}
		String found = null;
		if (lenient) {
			found = findText_Lenient(findWhat, text);
		} else {
			found = findText_Strict(findWhat, text);
		}

		return found;
	}

	private String removeRegexpChars(String text) {
		text = text.replaceAll("[^a-zA-Z\\-\\s\\d]+", "");
		return text;
	}

	private String findText_Lenient(String what, String inText) {
		String found = null;
		String regexp = lemmatizePhrase(what);
		Matcher matcher =
			Pattern.compile(regexp, Pattern.CASE_INSENSITIVE).matcher(inText);

		if (matcher.find()) {
			found = matcher.group();
		}
		return found;
	}

	private String findText_Strict(String what, String inText) {
		String found = null;
		int pos = inText.indexOf(what);
		if (pos >= 0) {
			found = what;
		}
		return found;
	}

	public String partialOverlap(String str1, String str2, Boolean lenient) {
		if (lenient == null) {
			lenient = false;
		}
		String[] tokens1 = str1.split("\\s+");
		String[] tokens2 = str2.split("\\s+");

		if (lenient) {
			tokens1 = truncateWords(tokens1, lenient);
			tokens2 = truncateWords(tokens2, lenient);
		}

		String overlap = null;

		for (int ii=0; ii < tokens1.length; ii ++) {
			if (overlap != null) {break;}
			String tok1 = tokens1[ii];
			if (tok1.length() <= 3) {continue;}
			if (lenient) {
				tok1 = truncateWord(tok1);
			}
			for (int jj=0; jj < tokens2.length; jj ++) {
				if (overlap != null) {break;}
				String tok2 = tokens2[jj];
				if (tok2.length() <= 3) {continue;}
				if (lenient) {
					tok2 = truncateWord(tok2);
				}

				if (tok1.equals(tok2)) {
					overlap = tok1;
				}
			}
		}

		return overlap;
	}

	private void printReport(EvaluationResults results) {
		userIO.echo("\n\n");
		userIO.echo("# entries in glossary         : "+results.totalEntries);
		userIO.echo();
		userIO.echo("Presence of IU term and EN translation in Hansard sentence pairs");
		{
			userIO.echo(1);
			userIO.echo("IU term");
			{
				userIO.echo(1);
				userIO.echo("found="+ results.totalIUPresent_Orig +"; absent="+results.totalIUAbsentOrig());
				userIO.echo(-1);
			}
			userIO.echo("EN translation (only for the "+results.totalIUPresent_Orig +" IU terms that appear in TM) ");
			{
				userIO.echo(1);
				userIO.echo(
					"FOUND: strict="+results.totalENPresent_Strict +"; "+
					"lenient="+results.totalENPresent_Lenient);
				userIO.echo(
					"ABSENT: strict="+results.totalENAbsent_Orig_Strict()+"; "+
					"lenient="+results.totalENAbsent_Orig_Lenient());
				userIO.echo(-1);
			}
			userIO.echo(-1);
		}
		userIO.echo("Correct spottings of the EN translation");
		{
			userIO.echo(1);
			userIO.echo("Absolute #: strict="+results.totalENSpotted_Strict+"; lenient="+results.totalENSpotted_Lenient);
			userIO.echo("rates: strict="+results.rateENSpotted_Strict()+"; lenient="+results.rateENSpotted_Lenient());
			userIO.echo(-1);
		}
	}

	public static class AlignmentsSummary {
		boolean  iuTermPresent= false;
		boolean enTranslPresent_strict = false;
		boolean enTranslPresent_lenient = false;
		Set<String> allTranslations = new HashSet<String>();
		Set<String> enTranslPresent_lenient_matches = new HashSet<String>();
		boolean enTranslSpotted_strict = false;
		boolean enTranslSpotted_lenient = false;
		Set<String> enTranslSpotted_lenient_matches = new HashSet<String>();
		boolean enTranslSpotted_lenientoverlap = false;
	}

	public static class SpottingCheck {
		Boolean spottedStrict = false;
		String spottedLenient = null;
		String spottedLenientOverlap = null;
	}

	public static enum MatchType {LENIENT_OVERLAP, LENIENT, STRICT};

	public static boolean isMoreLenient(MatchType sense1, MatchType sense2) {
		boolean answer = (sense1.ordinal() < sense2.ordinal());
		return answer;
	}
}

