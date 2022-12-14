package org.iutools.worddict;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.tm.TMEvaluator;
import org.iutools.concordancer.tm.TMEvaluator.*;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.config.IUConfig;
import org.iutools.script.TransCoder;
import org.iutools.worddict.MachineGeneratedDict.WhatTerm;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MDictEvaluator {

	UserIO userIO = new UserIO();
	MachineGeneratedDict dict = new MachineGeneratedDict();
	ObjectMapper mapper = new ObjectMapper();

	public MDictEvaluator() throws MachineGeneratedDictException, TranslationMemoryException {
		init__DictEvaluator();
	}

	private void init__DictEvaluator() throws MachineGeneratedDictException {
	}

	/** Maximum number of seconds that the dictionary can spend trying to decompose
	 * the word.
	 */
	public MDictEvaluator setDecompMaxSecs(int maxSecs) {
		this.dict.setDecompMaxSecs(maxSecs);
		return this;
	}

	public MDictEvaluator setMinMaxPairs(Integer min, Integer max) throws MachineGeneratedDictException {
		dict.setMinMaxPairs(min, max);
		return this;
	}

	public MDictEvaluator setMaxTranslations(Integer max) throws MachineGeneratedDictException {
		dict.setMaxTranslations(max);
		return this;
	}

	public DictEvaluationResults evaluate(
		Path glossaryPath, Integer stopAfterN, Integer startingAt) throws MachineGeneratedDictException {

		// Since we are using the glossary words as cases for evaluation,
		// we don't want to allow the dictionary to use the glossaries for
		// producing translations, as that would be a form of "cheating".
		//
		dict.includeHumanTranslation = false;

		DictEvaluationResults results = new DictEvaluationResults();
		Pair<Integer,Integer> firstLast = minMaxEntryNums(stopAfterN, startingAt);
		Integer first = firstLast.getLeft();
		Integer last = firstLast.getRight();
		try {
			results.onEvaluationStart();
			ObjectStreamReader reader =
				new ObjectStreamReader(glossaryPath.toFile());
			int entryNum = 0;
			while (true) {
				GlossaryEntry entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				entryNum++;
				if (last != null && entryNum >= last) {
					// We reached the last entry to be evaluated
					break;
				}
				if (entryNum < first) {
					// We haven't yet reached the first entry to be evaluated
					continue;
				}
				onNewGlossaryEntry(entry, results);
			}
			results.onEvaluationEnd();
			printReport(results);
		} catch (Exception e) {
			throw new MachineGeneratedDictException(e);
		}

		return results;
	}

	private Pair<Integer, Integer> minMaxEntryNums(Integer firstN, Integer startingAt) {
		Integer first = 1;
		if (startingAt != null) {
			first = startingAt;
		}
		Integer last = null;
		if (firstN != null) {
			last = first + firstN;
		}
		return Pair.of(first, last);
	}

	protected void onNewGlossaryEntry(GlossaryEntry glossEntry, DictEvaluationResults results) throws MachineGeneratedDictException {
		results.totalGlossaryEntries++;
		String iuTerm_roman = glossEntry.firstTerm4Lang("iu_roman");
		String iuTerm_syll = TransCoder.ensureSyllabic(iuTerm_roman);
		String enTerm = glossEntry.firstTerm4Lang("en");
		userIO.echo(results.totalGlossaryEntries + ". iu:" + iuTerm_roman + " (" + iuTerm_syll + "), en:" + enTerm);
		userIO.echo(1);
		try {
			if (TMEvaluator.tokenize(iuTerm_roman).length > 1) {
				userIO.echo("SKIPPED (IU term contained more than one word)");
			} else {
				results.totalSingleWordIUEntries++;
				MDictEntry wordEntry = dict.entry4word(iuTerm_roman);
				WhatTerm whatIUFound = checkIUPresence(wordEntry, results);

				if (whatIUFound != null) {
					checkENSpotting(wordEntry, whatIUFound, enTerm, results);
				}
			}
		} finally {
			userIO.echo(-1);
		}
	}

	private WhatTerm checkIUPresence(
	MDictEntry wordEntry, DictEvaluationResults results) throws MachineGeneratedDictException {
		WhatTerm whatTerm = null;
		if (!wordEntry.bestTranslations.isEmpty()) {
			if (wordEntry.hasTranslationsForOriginalWord()) {
				whatTerm = WhatTerm.ORIGINAL;
			} else {
				whatTerm = WhatTerm.RELATED;
				try {
					userIO.echo("RELATED terms: " + mapper.writeValueAsString(wordEntry.relatedWords));
				} catch (JsonProcessingException e) {
					throw new MachineGeneratedDictException(e);
				}
			}
		}
		if (whatTerm != null) {
			userIO.echo(whatTerm+" IU term was FOUND");
			results.onIUPresent(whatTerm);
		}

		return whatTerm;
	}

	private void checkENSpotting(MDictEntry wordEntry,
		WhatTerm where, String enTerm, DictEvaluationResults results) throws MachineGeneratedDictException {
		List<String> spottedTranslations = wordEntry.bestTranslations;
		try {
			userIO.echo("All "+where+" translations: "+mapper.writeValueAsString(spottedTranslations));
			userIO.echo(spottedTranslations.size()+" translations spotted from "+wordEntry.totalBilingualExamples()+" sentence pairs");
		} catch (JsonProcessingException e) {
			throw new MachineGeneratedDictException(e);
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

	private void printReport(DictEvaluationResults results) throws MachineGeneratedDictException {
		printDictConfig();
		printStats(results);
		printTermEquivExplanation();
	}

	private void printDictConfig() throws MachineGeneratedDictException {
		try {
			userIO.echo("\nEvaluated dict:\n"+ mapper.writeValueAsString(dict)+"\n");
		} catch (JsonProcessingException e) {
			throw new MachineGeneratedDictException(e);
		}
	}

	private void printStats(DictEvaluationResults results) {
		userIO.echo("\n\n");
		userIO.echo("# entries in glossary");
		userIO.echo(1);
		try {
			userIO.echo("All: "+results.totalGlossaryEntries);
			userIO.echo("Single word IU terms: "+results.totalSingleWordIUEntries);
		} finally {
			userIO.echo(-1);
		}
		userIO.echo();
		userIO.echo("Entries with IU present");
		{
			userIO.echo(1);
			for (WhatTerm whatTerm: new WhatTerm[] {WhatTerm.ORIGINAL, WhatTerm.RELATED}) {
				userIO.echo(whatTerm + ": " + results.totalIUPresent(whatTerm));
			}
			userIO.echo(-1);
		}
		userIO.echo();
		userIO.echo("Avg secs per present IU entry: "+results.avgSecsPerEntryPresent);

		userIO.echo();
		userIO.echo("Entries with EN term SPOTTED");
		{
			userIO.echo(1);
			for (MatchType aSense: TMEvaluator.matchTypes()) {
				userIO.echo(
					"in sense "+aSense+": "+results.totalENSpotted(aSense)+
					" (cum: "+results.totalENSpotted_atLeastInSense(aSense)+")");
			}
			userIO.echo(-1);
		}
		userIO.echo();
		userIO.echo("EN spotting rates");
		{
			userIO.echo(1);
			for (MatchType aSense: TMEvaluator.matchTypes()) {
				userIO.echo("in sense "+aSense+": "+results.rateENSpotted(aSense));
			}
			userIO.echo(-1);
		}
	}

	private void printTermEquivExplanation() {
		userIO.echo("\n"+TMEvaluator.explainEquivSenses()+"\n");
	}

	public void main(String[] args) throws Exception {
		String glossaryPath = IUConfig.getIUDataPath("data/glossaries/wpGlossary.gloss.json");
		MDictEvaluator evaluator = new MDictEvaluator()
			.setMinMaxPairs(null, 100)
			.setMaxTranslations(10);

		Integer stopAfterN = null;
		Integer startingAtN = null;
		DictEvaluationResults results =
			evaluator.evaluate(Paths.get(glossaryPath), stopAfterN, startingAtN);
	}
}
