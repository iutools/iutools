package org.iutools.worddict;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.tm.TMEvaluator;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.iutools.script.TransCoder;
import org.iutools.worddict.MultilingualDict.WhatTerm;

import java.nio.file.Path;
import java.util.List;

import static org.iutools.concordancer.tm.TMEvaluator.matchTypes;

public class DictEvaluator {

	UserIO userIO = new UserIO();
	MultilingualDict dict = MultilingualDict.getInstance();
	ObjectMapper mapper = new ObjectMapper();

	public DictEvaluator() throws MultilingualDictException {
	}

	public DictEvaluationResults evaluate(Path glossaryPath) throws MultilingualDictException {
		return evaluate(glossaryPath, (Integer)null);
	}

	public DictEvaluationResults evaluate(Path glossaryPath, Integer firstN) throws MultilingualDictException {
		DictEvaluationResults results = new DictEvaluationResults();
		try {
			ObjectStreamReader reader =
				new ObjectStreamReader(glossaryPath.toFile());
			GlossaryEntry entry = (GlossaryEntry) reader.readObject();
			while (entry != null) {
				if (firstN != null && results.totalGlossaryEntries >= firstN) {
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

	protected void onNewGlossaryEntry(GlossaryEntry glossEntry, DictEvaluationResults results) throws MultilingualDictException {
		results.totalGlossaryEntries++;
		String iuTerm_roman = glossEntry.getTermInLang("iu_roman");
		String iuTerm_syll = TransCoder.ensureSyllabic(iuTerm_roman);
		String enTerm = glossEntry.getTermInLang("en");
		userIO.echo(results.totalGlossaryEntries+". iu:"+iuTerm_roman+" ("+iuTerm_syll+"), en:"+enTerm);
		MultilingualDictEntry wordEntry = dict.entry4word(iuTerm_roman);
		userIO.echo(1);
		try {
			WhatTerm whatIUFound = checkIUPresence(wordEntry, results);

			if (whatIUFound != null) {
				checkENSpotting(wordEntry, whatIUFound, enTerm, results);
			}
		} finally {
			userIO.echo(-1);
		}

	}

	private WhatTerm checkIUPresence(
		MultilingualDictEntry wordEntry, DictEvaluationResults results) throws MultilingualDictException {
		WhatTerm whatTerm = null;
		if (!wordEntry.origWordTranslations.isEmpty()) {
			whatTerm = WhatTerm.ORIGINAL;
		} else if (!wordEntry.relatedWordTranslations.isEmpty()) {
			try {
				userIO.echo("RELATED terms: "+mapper.writeValueAsString(wordEntry.relatedWords));
			} catch (JsonProcessingException e) {
				throw new MultilingualDictException(e);
			}
			whatTerm = WhatTerm.RELATED;
		}
		if (whatTerm != null) {
			userIO.echo(whatTerm+" IU term was FOUND");
			results.onIUPresent(whatTerm);
		}

		return whatTerm;
	}

	private void checkENSpotting(MultilingualDictEntry wordEntry,
		WhatTerm where, String enTerm, DictEvaluationResults results) {
		List<String> spottedTranslations = wordEntry.origWordTranslations;
		if (where == WhatTerm.RELATED) {
			spottedTranslations = wordEntry.relatedWordTranslations;
		}
		MatchType spottedSense = null;
		String spottedAs = null;
		for (String spotted: spottedTranslations) {
			Pair<MatchType, String> match = TMEvaluator.sameTerm(spotted, enTerm);
			if (spottedSense == null ||
			TMEvaluator.isMoreLenient(spottedSense, match.getLeft())) {
				spottedSense = match.getLeft();
				spottedAs = match.getRight();
			}
			if (spottedSense == MatchType.STRICT) {
				break;
			}
		}
		if (spottedSense != null) {
			userIO.echo(
				"EN translation was SPOTTED in "+where+
				" term, in sense="+spottedSense+" (as '"+spottedAs+"')");
			results.onENSpotting(spottedSense);
		}
	}


	private void printReport(DictEvaluationResults results) {
		userIO.echo("# Glossary entries: "+results.totalGlossaryEntries);

	}
}
