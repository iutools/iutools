package org.iutools.concordancer.tm;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.worddict.EvaluationResults;
import org.iutools.worddict.GlossaryEntry;

import java.nio.file.Path;
import java.util.*;

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
			List<Alignment_ES>[] alignments =
				fetchAlignments(iuTerm_syll, enTerm);
			List<Alignment_ES> algs_bothSides_strict = alignments[0];
			List<Alignment_ES> algs_bothSides_lenient = alignments[1];
			List<Alignment_ES> algs_iuside_only = alignments[2];
			results.totalIUPresentOrig +=
				algs_bothSides_strict.size() + algs_bothSides_lenient.size() +
				algs_iuside_only.size();
			if (!algs_bothSides_strict.isEmpty()) {
				results.totalENPresent_Orig_Strict++;
				results.totalENPresent_Orig_Lenient++;
			}
			if (!algs_bothSides_lenient.isEmpty()) {
				results.totalENPresent_Orig_Lenient++;
			}
		} finally {
			userIO.echo(-1);
		}
	}

	private List<Alignment_ES>[] fetchAlignments(String iuTerm_syll, String enTerm) throws TranslationMemoryException {
		try {
			iuTerm_syll = iuTerm_syll.toLowerCase();
			enTerm = enTerm.toLowerCase();
			Iterator<Alignment_ES> algsIter = tm.searchIter("iu", iuTerm_syll, "en");
			List<Alignment_ES> iuOnly = new ArrayList<Alignment_ES>();
			List<Alignment_ES> iuAndEn_strict = new ArrayList<Alignment_ES>();
			List<Alignment_ES> iuAndEn_lenient = new ArrayList<Alignment_ES>();
			int totalAlignments = 0;
			while (algsIter.hasNext() && totalAlignments < MAX_ALIGNMENTS) {
				Alignment_ES algn = algsIter.next();
				if (alignmentContains(algn, "iu", iuTerm_syll)) {
					totalAlignments++;
					if (alignmentContains(algn, "en", enTerm)) {
						iuAndEn_strict.add(algn);
					} else if (alignmentContains(algn, "en", enTerm, true)){
						iuAndEn_lenient.add(algn);
					} else {
						iuOnly.add(algn);
					}
				}
			}

			List<Alignment_ES>[] results = new List[] {
				iuAndEn_strict, iuAndEn_lenient, iuOnly
			};
			return results;
		} catch (TranslationMemoryException e) {
			throw new TranslationMemoryException(e);
		}
	}

	private boolean alignmentContains(
		Alignment_ES algn, String lang, String expText) {
		return alignmentContains(algn, lang, expText, (Boolean)null);
	}

	private boolean alignmentContains(
		Alignment_ES algn, String lang, String expText, Boolean lenient) {
		if (lenient == null) {
			lenient = false;
		}
		expText = expText.toLowerCase();
		if (lenient) {
			expText = lemmatize(expText);
		}
		String sentence = algn.sentence4lang(lang);
		boolean answer = false;
		if (sentence != null && sentence.toLowerCase().contains(expText)) {
			answer = true;
		}
		return answer;
	}

	private String lemmatize(String term) {

		term = StringUtils.abbreviate(term, 8);
		term = term.replaceAll("\\.\\.\\.$", "");
		return term;
	}


	private Map<String, String> lookForCorrectTranslation(
		String enTerm, List<String> translations) {
		Map<String,String> found = new HashMap<String,String>();
		found.put("exact", null);
		found.put("partial", null);
		for (String aTranslation: translations) {
			if (aTranslation.equals(enTerm)) {
				found.put("exact", aTranslation);
				found.put("partial", aTranslation);
				break;
			} else if (partiallyOverlap(aTranslation, enTerm)) {
				found.put("partial", aTranslation);
			}
		}

		return found;
	}

	private boolean partiallyOverlap(String str1, String str2) {
		String[] tokens1 = str1.split("\\s+");
		String[] tokens2 = str2.split("\\s+");

		boolean overlap = false;

		for (String tok1: tokens1) {
			if (overlap) {break;}
			if (tok1.length() <= 3) {continue;}
			for (String tok2: tokens2) {
				if (overlap) {break;}
				if (tok2.length() <= 3) {continue;}

				if (sameRoot(tok1, tok2)) {
					overlap = true;
				}
			}
		}


		return overlap;
	}

	private boolean sameRoot(String tok1, String tok2) {
		tok1 = StringUtils.abbreviate(tok1, 5);
		tok2 = StringUtils.abbreviate(tok2, 5);
		boolean answer = (tok1.equals(tok2));
		return answer;
	}

	private void printReport(EvaluationResults results) {
		userIO.echo("\n\n");
		userIO.echo("# entries in glossary         : "+results.totalEntries);
		userIO.echo();
		userIO.echo("Presence of IU term and EN translation in Hansard sentence pairs");
		{
			userIO.echo(1);
			userIO.echo("*BOTH* IU and EN terms");
			{
				userIO.echo(1);
				userIO.echo(
					"strict="+results.totalENPresent_Orig_Strict+"; "+
					"lenient="+results.totalENPresent_Orig_Lenient);
				userIO.echo(-1);
			}
			userIO.echo("*ONLY* IU term");
			{
				userIO.echo(1);
				userIO.echo("strict="+results.totalOnlyIUPresentOrig());
				userIO.echo(-1);
			}
			userIO.echo(-1);
		}
//		userIO.echo("# CORRECTLY spotted           : "+results.totalCorrectSpotOrig);
//		userIO.echo("# PARTIALLY correctly spotted : "+results.totalPartiallyCorrectSpotOrig);

//		double gotCompleteAccuracy = 0.0;
//		double gotPartialAccuracy = 0.0;
//		if (results.totalEntriesPresent > 0) {
//			gotCompleteAccuracy = 1.0 * results.totalCorrectSpotOrig / results.totalEntriesPresent;
//			gotPartialAccuracy = 1.0 * results.totalPartiallyCorrectSpotOrig / results.totalEntriesPresent;
//		}
//		userIO.echo("Spotting accuracy");
//		userIO.echo("  COMPLETE spotting : "+gotCompleteAccuracy);
//		userIO.echo("  PARTIAL  spotting : "+gotPartialAccuracy);
	}

}

