package org.iutools.concordancer.tm;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.script.TransCoder;
import org.iutools.worddict.GlossaryEntry;

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
		String term_roman = entry.getTermInLang("iu_roman");
		String term_syll = TransCoder.ensureSyllabic(term_roman);
		results.totalEntries++;
		userIO.echo(results.totalEntries+". iu:"+term_roman+" ("+term_syll+"), en:"+entry.getTermInLang("en")+" ...");
		evaluateGlossaryTerm(entry, results);
	}


	protected void evaluateGlossaryTerm(GlossaryEntry glossEntry, EvaluationResults results) throws Exception {
		userIO.echo(1);
		try {
			String enTerm = glossEntry.getTermInLang("en").toLowerCase();
			String iuTerm_syll = glossEntry.getTermInLang("iu_syll").toLowerCase();
			AlignmentsSummary algnSummary =
				analyzeAlignments(iuTerm_syll, enTerm);

			if (algnSummary.iuTermPresent) {
				userIO.echo("IU term was present");
				results.totalIUPresent_Orig++;
			}
			if (algnSummary.enTermPresent_Sense != null) {
				userIO.echo("EN term was PRESENT in the sense "+algnSummary.enTermPresent_Sense+
					": "+algnSummary.enTermPresent_occur);
				results.enPresent_Histogram.updateFreq(algnSummary.enTermPresent_Sense);
			}
			if (algnSummary.enTermSpotted_Sense != null) {
				userIO.echo("EN term was SPOTTED in the sense "+algnSummary.enTermSpotted_Sense+
				": "+algnSummary.enTermSpotted_occur);
				results.enPresent_Histogram.updateFreq(algnSummary.enTermPresent_Sense);
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
				if (alignmentContainsIU(algn, iuTerm_syll)) {
					analysis.iuTermPresent = true;
					totalAlignments++;
					Pair<MatchType,String> match = findENTermInAlignment(algn, enTerm);
					if (analysis.enTermPresent_Sense == null ||
						isMoreLenient(analysis.enTermPresent_Sense, match.getLeft())) {
						analysis.enTermPresent_Sense = match.getLeft();
						analysis.enTermPresent_occur = match.getRight();
						attemptSpotting = true;
					}

					if (attemptSpotting) {
						match = checkEnTermSpotting(algn, iuTerm_syll, enTerm);

						if (analysis.enTermSpotted_Sense == null ||
							isMoreLenient(analysis.enTermSpotted_Sense, match.getLeft())) {
							analysis.enTermSpotted_Sense = match.getLeft();
							analysis.enTermSpotted_occur = match.getRight();
						}
					}
				}
			}

			return analysis;
		} catch (TranslationMemoryException e) {
			throw new TranslationMemoryException(e);
		}
	}

	private Pair<MatchType,String> checkEnTermSpotting(
		Alignment_ES algn, String iuTerm_syll, String enTerm) throws TranslationMemoryException {
		Pair<MatchType,String> match = Pair.of(null, null);
		try {
			Map<String, String> spotting =
				new WordSpotter(algn.sentencePair("iu", "en"))
					.spot("iu", iuTerm_syll);
			String spottedEn = spotting.get("en");
			if (spottedEn != null) {
				match = findTerm(enTerm, spottedEn);
			}
		} catch (WordSpotterException e) {
			throw new TranslationMemoryException(e);
		}

		return match;
	}


	private boolean alignmentContainsIU(
		Alignment_ES algn, String expIUTerm_syll) throws TranslationMemoryException {
		boolean answer = false;
		String sentence = algn.sentence4lang("iu");
		if (sentence != null) {
			sentence = sentence.toLowerCase();
			if (sentence.indexOf(expIUTerm_syll.toLowerCase()) >= 0) {
				answer = true;
			}
		}

		return answer;
	}


	private Pair<MatchType, String> findENTermInAlignment(Alignment_ES algn, String enTerm) {
		String enSentence = algn.sentence4lang("en");
		Pair<MatchType, String> match = findTerm(enTerm, enSentence);
		return match;
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

	public Pair<MatchType, String> findTerm(String term, String inText) {
		String[] termTokens = tokenize(term);
		String[] textTokens = tokenize(inText);
		String occFound = null;
		MatchType typeFound = null;
		for (int ii=0; ii < textTokens.length; ii++) {
			if (typeFound == MatchType.STRICT) {
				break;
			}
			int maxPos = Math.min(ii+5,textTokens.length);
			for (int jj=maxPos; jj >= ii; jj--) {
				String[] textTermTokens = Arrays.copyOfRange(textTokens, ii, jj);
				Pair<MatchType,String> match = sameTerm(termTokens, textTermTokens);
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
		MatchType enTermPresent_Sense = null;
		String enTermPresent_occur = null;
		MatchType enTermSpotted_Sense = null;
		String enTermSpotted_occur = null;
		Set<String> allTranslations = new HashSet<String>();

		//		boolean enTranslPresent_strict = false;
//		boolean enTranslPresent_lenient = false;
//		Set<String> enTranslPresent_lenient_matches = new HashSet<String>();
//		boolean enTranslSpotted_strict = false;
//		boolean enTranslSpotted_lenient = false;
//		Set<String> enTranslSpotted_lenient_matches = new HashSet<String>();
//		boolean enTranslSpotted_lenientoverlap = false;
	}

	public static enum MatchType {LENIENT_OVERLAP, LENIENT, STRICT};

	public static boolean isMoreLenient(MatchType sense1, MatchType sense2) {
		boolean answer = false;
		if (sense1 != null && sense2 != null) {
			answer = (sense1.ordinal() < sense2.ordinal());
		}
		return answer;
	}
}

