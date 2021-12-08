package org.iutools.concordancer.tm;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.SentencePair;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.script.TransCoder;
import org.iutools.worddict.GlossaryEntry;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.nio.file.Path;
import java.util.*;

public class TMEvaluator {
	UserIO userIO = new UserIO();
	TranslationMemory tm = new TranslationMemory();
	ObjectMapper mapper = new ObjectMapper();
	int MAX_ALIGNMENTS = 100;

	public TMEvaluator setVerbosity(UserIO.Verbosity level) {
		userIO.setVerbosity(level);
		return this;
	}

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


	protected void evaluateGlossaryTerm(
		GlossaryEntry glossEntry, EvaluationResults results) throws Exception {
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
				results.onEnPresent(algnSummary.enTermPresent_Sense);
			}

			if (algnSummary.spottingAttempted) {
				userIO.echo("All translations found: "+mapper.writeValueAsString(algnSummary.allTranslations));

			}
			if (algnSummary.enTermSpotted_Sense != null) {
				userIO.echo("EN term was SPOTTED in the sense "+algnSummary.enTermSpotted_Sense+
					": "+algnSummary.enTermSpotted_occur);
				results.onEnSpotted(algnSummary.enTermSpotted_Sense);
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
					if (null != match.getLeft()) {
						attemptSpotting = true;
					}
					if (analysis.enTermPresent_Sense == null ||
						isMoreLenient(analysis.enTermPresent_Sense, match.getLeft())) {
						analysis.enTermPresent_Sense = match.getLeft();
						analysis.enTermPresent_occur = match.getRight();
					}

					if (attemptSpotting) {
						analysis.spottingAttempted = true;
						Triple<String,MatchType,String> spottingInfo
							= checkEnTermSpotting(algn, iuTerm_syll, enTerm);
						analysis.allTranslations.add(spottingInfo.getLeft());
						if (analysis.enTermSpotted_Sense == null ||
							isMoreLenient(analysis.enTermSpotted_Sense, match.getLeft())) {
							analysis.enTermSpotted_Sense = spottingInfo.getMiddle();
							analysis.enTermSpotted_occur = spottingInfo.getRight();
						}
					}
				}
			}

			return analysis;
		} catch (TranslationMemoryException e) {
			throw new TranslationMemoryException(e);
		}
	}

	private Triple<String,MatchType,String> checkEnTermSpotting(
		Alignment_ES algn, String iuTerm_syll, String enTerm) throws TranslationMemoryException {
		Logger logger = Logger.getLogger("org.iutools.concordancer.tm.TMEvaluator.checkEnTermSpotting");
		MatchType matchType = null;
		String spottedEN = null;
		String common = null;
		try {
			logger.trace("Spotting translation for iuTerm_syll="+iuTerm_syll);
			SentencePair pair = algn.sentencePair("iu", "en");
			Map<String, String> spotting =
				new WordSpotter(pair)
					.spot("iu", iuTerm_syll);
			logger.trace("** DONE Spotting translation for iuTerm_syll="+iuTerm_syll);
			spottedEN = spotting.get("en");
			if (spottedEN != null) {
				Pair<MatchType,String> match = sameTerm(enTerm, spottedEN);
				matchType = match.getLeft();
				common = match.getRight();
			}
		} catch (WordSpotterException e) {
			throw new TranslationMemoryException(e);
		}

		return Triple.of(spottedEN, matchType, common);
	}


	private boolean alignmentContainsIU(
		Alignment_ES algn, String expIUTerm_syll) throws TranslationMemoryException {
		Logger logger = Logger.getLogger("org.iutools.concordancer.tm.TMEvaluator.alignmentContainsIU");
		boolean answer = false;
		String sentence = algn.sentence4lang("iu");
		logger.trace("expIUTerm_syll="+expIUTerm_syll+", sentence="+sentence);

		if (sentence != null) {
			sentence = sentence.toLowerCase();
			if (sentence.indexOf(expIUTerm_syll.toLowerCase()) >= 0) {
				answer = true;
			}
		}

		logger.trace("returning answer="+answer);
		return answer;
	}


	private Pair<MatchType, String> findENTermInAlignment(Alignment_ES algn, String enTerm) throws TranslationMemoryException {
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

	public Pair<MatchType, String> findTerm(String term, String inText) throws TranslationMemoryException {
		Logger logger = Logger.getLogger("org.iutools.concordancer.tm.TMEvaluator.findTerm");
		try {
			String[] termTokens = tokenize(term);
			String[] textTokens = tokenize(inText);
			if (logger.isTraceEnabled()) {
				logger.trace("termTokens=" + mapper.writeValueAsString(termTokens));
			}
			String occFound = null;
			MatchType typeFound = null;
			for (int ii = 0; ii < textTokens.length; ii++) {
				if (typeFound == MatchType.STRICT) {
					break;
				}
				int maxPos = Math.min(ii + 5, textTokens.length);
				for (int jj = maxPos; jj >= ii; jj--) {
					String[] textTermTokens = Arrays.copyOfRange(textTokens, ii, jj);
					if (logger.isTraceEnabled()) {
						logger.trace("textTermTokens=" + mapper.writeValueAsString(textTermTokens));
					}
					Pair<MatchType, String> match = sameTerm(termTokens, textTermTokens);
					if (typeFound == null || isMoreLenient(typeFound, match.getLeft())) {
						typeFound = match.getLeft();
						occFound = match.getRight();
						logger.trace("Now, typeFound="+typeFound+", occFound="+occFound);
					}
					if (typeFound == MatchType.STRICT) {
						break;
					}
				}
			}
			return Pair.of(typeFound, occFound);
		} catch (JsonProcessingException e) {
			throw new TranslationMemoryException(e);
		}

	}

	public static Pair<MatchType, String> sameTerm(String term1, String term2) {
		String[] term1Tokens = tokenize(term1);
		String[] term2Tokens = tokenize(term2);
		return sameTerm(term1Tokens, term2Tokens);
	}

	public static Pair<MatchType, String> sameTerm(String[] term1Toks, String[] term2Toks) {
		Logger logger = Logger.getLogger("org.iutools.concordancer.tm.TMEvaluator.sameTerm");
		MatchType matchType = null;
		String overlap = null;
		String term1 = String.join(" ", term1Toks).toLowerCase();
		String term2 = String.join(" ", term2Toks).toLowerCase();
		logger.trace("term1="+term1+", term2="+term2);
		if (term1.equals(term2)) {
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
		MatchType[] types = matchTypes();
//		ArrayUtils.reverse(types);
		userIO.echo("\n\n");
		userIO.echo("# entries in glossary: "+results.totalEntries);
		{
			userIO.echo(1);
			userIO.echo("IU term PRESENT: "+results.totalIUPresent_Orig);
			userIO.echo("EN term PRESENT:");
			{
				userIO.echo(1);
				for (MatchType type: types) {
					userIO.echo(type+": "+results.totalEnPresent_inSense(type)+
					" (cum: "+results.totalEnPresent_atLeastInSense(type)+")");
				}
				userIO.echo(-1);
			}
			userIO.echo("EN translation SPOTTED:");
			{
				userIO.echo(1);
				for (MatchType type: types) {
					userIO.echo(type+": "+results.totalEnSpotted_inSense(type)+
					" (cum: "+results.totalEnSpotted_atLeastInSense(type)+")");
				}
				userIO.echo(-1);
			}
		}
		userIO.echo("");
		userIO.echo("Translation Spotting rates");
		{
			userIO.echo(1);
			for (MatchType type: types) {
				userIO.echo(type+": "+results.rateENSpotted_inSense(type));
			}
			userIO.echo(-1);
		}

	}

	public static class AlignmentsSummary {
		boolean  iuTermPresent= false;
		MatchType enTermPresent_Sense = null;
		String enTermPresent_occur = null;
		MatchType enTermSpotted_Sense = null;
		String enTermSpotted_occur = null;
		boolean spottingAttempted = false;
		Set<String> allTranslations = new HashSet<String>();
	}

	public static enum MatchType {LENIENT_OVERLAP, LENIENT, STRICT};

	public static MatchType[] matchTypes() {
		return matchTypes(null);
	}

	public static MatchType[] matchTypes(Boolean strictFirst) {
		if (strictFirst == null) {
			strictFirst = true;
		}
		MatchType[] types = MatchType.values();
		if (strictFirst) {
			ArrayUtils.reverse(types);
		}
		return types;
	}


	public static boolean isMoreLenient(MatchType sense1, MatchType sense2) {
		boolean answer = false;
		if (sense1 != null && sense2 != null) {
			answer = (sense1.ordinal() < sense2.ordinal());
		}
		return answer;
	}
}

