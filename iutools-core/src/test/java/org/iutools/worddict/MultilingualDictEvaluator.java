package org.iutools.worddict;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.WordSpotterException;

import java.nio.file.Path;
import java.util.*;

/** Use this class to evaluate the accuracy and coverage the MultilingualWordDict
 * on terms from a glossary.
 */
public class MultilingualDictEvaluator {

	UserIO userIO = new UserIO();
	ObjectMapper mapper = new ObjectMapper();

	public EvaluationResults evaluate(Path glossaryFile, Integer firstN)
		throws MultilingualDictException {
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
			throw new MultilingualDictException(e);
		}

		return results;
	}


	private void printReport(EvaluationResults results) {
//		System.out.println("\n\n");
//		System.out.println("# entries in glossary         : "+results.totalEntries);
//		System.out.println("# alignments in hansard       : "+results.totalEntriesPresent);
//		System.out.println("# CORRECTLY spotted           : "+results.totalCorrectSpotOrig);
//		System.out.println("# PARTIALLY correctly spotted : "+results.totalPartiallyCorrectSpotOrig);

//		double gotCompleteAccuracy = 0.0;
//		double gotPartialAccuracy = 0.0;
//		if (results.totalEntriesPresent > 0) {
//			gotCompleteAccuracy = 1.0 * results.totalCorrectSpotOrig / results.totalEntriesPresent;
//			gotPartialAccuracy = 1.0 * results.totalPartiallyCorrectSpotOrig / results.totalEntriesPresent;
//		}
//		System.out.println("Spotting accuracy");
//		System.out.println("  COMPLETE spotting : "+gotCompleteAccuracy);
//		System.out.println("  PARTIAL  spotting : "+gotPartialAccuracy);
	}

	private void onNewGlossaryEntry(GlossaryEntry entry, EvaluationResults results) throws Exception {
		String term = entry.getTermInLang("iu_roman");
		results.totalEntries++;
		userIO.echo(results.totalEntries+". iu:"+term+", en:"+entry.getTermInLang("en")+" ...");
		boolean termPresent = true;

		evaluateGlossaryTerm(entry, results);
	}


	private void evaluateGlossaryTerm(GlossaryEntry glossEntry, EvaluationResults results) throws Exception {
		userIO.echo(1);
		try {
			String enTerm = glossEntry.getTermInLang("en").toLowerCase();
			String iuTerm_roman = glossEntry.getTermInLang("iu_roman").toLowerCase();
			String iuTerm_syll = glossEntry.getTermInLang("iu_syll").toLowerCase();
			MultilingualDictEntry dictEntry =
				MultilingualDict.getInstance().entry4word(iuTerm_syll);

			userIO.echo("ORIG word translations: "+
				mapper.writeValueAsString(dictEntry.origWordTranslations));
			userIO.echo("RELATED word translations: "+
				mapper.writeValueAsString(dictEntry.relatedWordTranslations));

			boolean someOrigTranslationsFound = !dictEntry.origWordTranslations.isEmpty();
			boolean someRelatedTranslationsFound = !dictEntry.origWordTranslations.isEmpty();

			if (someOrigTranslationsFound) {
				results.totalIUPresent_Orig++;
			} else if (someRelatedTranslationsFound) {
				results.totalRelatedIUPresent++;
			}

			String origExactMatch = null;
			String origPartialMatch = null;
			if (someOrigTranslationsFound) {
				Map<String,String> found =
					lookForCorrectTranslation(enTerm, dictEntry.origWordTranslations);
				origExactMatch = found.get("exact");
				origPartialMatch = found.get("partial");
			}

			String relatedExactMatch = null;
			String relatedPartialMatch = null;
			if (someRelatedTranslationsFound) {
				Map<String,String> found =
					lookForCorrectTranslation(enTerm, dictEntry.origWordTranslations);
				relatedExactMatch = found.get("exact");
				relatedPartialMatch = found.get("partial");
			}

			String anyExactMatch = null;
			String anyPartialMactch = null;
			if (someOrigTranslationsFound || someRelatedTranslationsFound) {
				Map<String,String> found =
					lookForCorrectTranslation(enTerm, dictEntry.allTranslations());
				anyExactMatch = found.get("exact");
				anyPartialMactch = found.get("partial");
			}

			if (origExactMatch != null) {
				results.totalENSpotted_Strict++;
				userIO.echo("Found EXACT translation for ORIG term: "+origExactMatch);
			} else if (origPartialMatch != null) {
				results.totalENSpotted_Lenient++;
				userIO.echo("Found PARTIAL translation for ORIG term: "+origPartialMatch);
			}
			if (relatedExactMatch != null) {
				results.totalExactSpotRelated++;
				userIO.echo("Found EXACT translation for RELATED term: "+relatedExactMatch);
			} else if (relatedPartialMatch != null) {
				results.totalPartialSpotRelated++;
				userIO.echo("Found PARTIAL translation for RELATED term: "+relatedPartialMatch);
			}
			if (anyExactMatch != null) {
				results.totalExactSpotAny++;
			} else if (anyPartialMactch != null) {
				results.totalExactSpotAny++;
			}
		} finally {
			userIO.echo(-1);
		}
	//		if (!dictEntry.relatedWordTranslations.isEmpty()) {
	//			results.totalRelatedIUPresent++;
	//			Map<String,Boolean> found =
	//			lookForCorrectTranslation(enTerm, dictEntry.origWordTranslations);
	//		}

	//
	//
	//		Integer outcome = null;
	//		Iterator<Alignment_ES> alignmentIter =
	//		new TranslationMemory().searchIter("iu", iuTerm_roman, "en");
	//		if (!alignmentIter.hasNext()) {
	//			// Did not find any sentence pairs with the IU term
	//			outcome = 0;
	//		}
	//		if (outcome == null) {
	//			// At the very least, we found some pairs that contained the IU term.
	//			outcome = 1;
	//			final int MAX_PAIRS = 100;
	//			int totalPairs = 1;
	//			while (alignmentIter.hasNext() && totalPairs <= MAX_PAIRS) {
	//				totalPairs++;
	//				Alignment_ES alignment = alignmentIter.next();
	//				SentencePair bilingualAlignment = null;
	//				bilingualAlignment = alignment.sentencePair("iu", "en");
	//				if (bilingualAlignment.getText("en").toLowerCase().contains(enTerm)) {
	//					// At the very least, we found a pair that contains both the
	//					// IU and EN terms.
	//					outcome = 2;
	//				}
	//				if (bilingualAlignment.hasWordLevel()) {
	//					Map<String, String> spottings =
	//					new WordSpotter(bilingualAlignment).spot("iu", iuTerm_syll);
	//					String enSpotting = spottings.get("en");
	//					if (enSpotting != null) {
	//						enSpotting = enSpotting.toLowerCase();
	//						translationsFound.add(enSpotting);
	//						if (enSpotting.equals(enTerm)) {
	//							// At least one of the spotted translations is correct
	//							outcome = 4;
	//						} else if (partiallyOverlap(enSpotting, enTerm)) {
	//							// At least one of the spotted translations partially
	//							// overlaps with the correct term.
	//							outcome = 3;
	//						}
	//					}
	//				}
	//			}
	//		}
	//
	//		return Pair.of(outcome, translationsFound);
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


	private int evaluateGlossaryTerm(GlossaryEntry entry, Iterator<Alignment_ES> alignments) throws WordSpotterException, JsonProcessingException, MultilingualDictException {
		boolean answer = false;
		String enTerm = entry.getTermInLang("en").toLowerCase();
		String iuTerm = entry.getTermInLang("iu_roman").toLowerCase();
		int outcome = 0;

		List<String> enTranslations =
		MultilingualDict.getInstance().entry4word(iuTerm, "iu", false, MultilingualDictEntry.Field.TRANSLATIONS).origWordTranslations;
		if (!enTranslations.isEmpty()) {
			// At the very least we found some alignments for the iu term
			outcome = 1;
			for (String spottedEn : enTranslations) {
				spottedEn = spottedEn.toLowerCase();
				if (spottedEn.equals(enTerm)) {
					// We found an exact spotting
					outcome = 3;
					break;
				} else if (spottedEn.contains(enTerm) || enTerm.contains(spottedEn)) {
					// We found a spotting with partial overlap
					outcome = 2;
				}
			}
		}


//		final int MAX_TRIES = 20;
//		int count = 0;
//		Set<String> enTermsFound = new HashSet<String>();
//		while (alignments.hasNext()) {
//			count++;
//			if (count > MAX_TRIES) {
//				break;
//			}
//			Alignment_ES anAlg = alignments.next();
//			String enSent = anAlg.sentence4lang("en");
//			if (enSent.toLowerCase().contains(enTerm)) {
//				// At the very least, we have an alignment that contains the
//				// iu term and its en translation
//				outcome = 1;
//				Map<String, String> spottings =
//					new WordSpotter(anAlg.sentencePair("iu", "en")).spot("iu", iuTerm);
//				String spottedEn = spottings.get("en");
//				if (spottedEn != null) {
//					enTermsFound.add(spottedEn);
//					spottedEn = spottedEn.toLowerCase();
//					if (spottedEn.equals(enTerm)) {
//						// We found an exact spotting
//						outcome = 3;
//						break;
//					} else if (spottedEn.contains(enTerm) || enTerm.contains(spottedEn)) {
//						// We found a spotting with partial overlap
//						outcome = 2;
//					} else {
//						int x = 0;
//					}
//				}
//			}
//		}
		System.out.println("  outcome="+outcome+", translations found: "+new ObjectMapper().writeValueAsString(enTranslations));
		return outcome;
	}

}
