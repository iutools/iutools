package org.iutools.webservice.morphdict;

import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistryException;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.linguisticdata.*;
import org.iutools.morphemedict.MorphDictionaryEntry;
import org.iutools.morphemedict.MorphemeDictionary;
import org.iutools.morphemedict.MorphemeDictionaryException;
import org.iutools.morphemedict.MorphWordExample;
import org.iutools.webservice.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MorphemeDictEndpoint
	extends Endpoint<MorphemeDictInputs, MorphemeDictResult> {

	@Override
	protected MorphemeDictInputs requestInputs(String jsonRequestBody)
		throws ServiceException {
		return jsonInputs(jsonRequestBody, MorphemeDictInputs.class);
	}

	@Override
	public EndpointResult execute(MorphemeDictInputs inputs) throws ServiceException {

		Logger logger = LogManager.getLogger("org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint.execute");
		logger.trace("inputs= " + PrettyPrinter.print(inputs));
		MorphemeDictResult response = new MorphemeDictResult();

		if (inputs.isEmpty()) {
			throw new ServiceException("All morpheme specs were either null or empty");
		}

		String corpusName = inputs.corpusName;
		logger.trace("corpusName: " + corpusName);
		if (inputs.corpusName == null || inputs.corpusName.isEmpty()) {
			corpusName = null; // will use default corpus = Hansard2002
		}

		// Retrieve all words that match the wordPattern
		// and put the results in results.matchingWords
		MorphemeDictResult results =
			findExamples(inputs, corpusName);

		return results;
	}

	private MorphemeDictResult findExamples(
		MorphemeDictInputs inputs, String corpusName)
		throws MorphemeDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");

		tLogger.trace("invoked with inputs.canonicalForm="+inputs.canonicalForm +", inputs.nbExamples="+inputs.nbExamples);

		tLogger.trace("Creating the MorphemeDictionary instance");
		MorphemeDictResult results = new MorphemeDictResult();
		try {

			MorphemeDictionary morphExtractor = new MorphemeDictionary();

			tLogger.trace("Loading the corpus");
			CompiledCorpus compiledCorpus =
				new CompiledCorpusRegistry().getCorpus();
			morphExtractor.useCorpus(compiledCorpus);
			tLogger.trace("Using corpus of type="+compiledCorpus.getClass());

			int nbExamples = 20;
			if (inputs.nbExamples != null) {
				nbExamples = Integer.valueOf(inputs.nbExamples);
			}
			morphExtractor.setNbDisplayedWords(nbExamples);

			tLogger.trace("Finding words that contain the morpheme");
			Integer maxExamples = null;
			try {
				maxExamples = Integer.parseInt(inputs.nbExamples);
			} catch (Exception e) {
				// If the nbExamples input was not a string representation of an
				// integer, then just ignore it.
			}

			List<MorphDictionaryEntry> wordsForMorphemes =
				morphExtractor.search(inputs.canonicalForm, inputs.grammar, inputs.meaning,
					nbExamples);

			LinguisticData linguisticData = LinguisticData.getInstance();
			tLogger.trace("wordsForMorphemes: "+wordsForMorphemes.size());
			Iterator<MorphDictionaryEntry> itWFM = wordsForMorphemes.iterator();
			while (itWFM.hasNext()) {
				MorphDictionaryEntry w = itWFM.next();
				String morphID = w.morphemeWithId;
				if (linguisticData.getMorpheme(morphID).isComposite()) {
					// We only list the non-composite morphemes
					continue;
				}
				String morphMeaning = Morpheme.getMorpheme(morphID).englishMeaning;
				tLogger.trace("morphID: "+morphID+", morphMeaning: "+morphMeaning);
				MorphemeHumanReadableDescr morphDescr =
					new MorphemeHumanReadableDescr(morphID, morphMeaning);
				results.matchingMorphemes.add(morphDescr);

				List<MorphWordExample> wordsAndFreqs = w.words;
				tLogger.trace("wordsAndFreqs: "+wordsAndFreqs.size());
				List<String> words = new ArrayList<String>();
				for (MorphWordExample example : wordsAndFreqs) {
					tLogger.trace("example.word: "+example.word);
					words.add(example.word);
				}
				results.examplesForMorpheme.put(
					w.morphemeWithId, words.toArray(new String[0]));
			}
		} catch (MorphemeDictionaryException | CompiledCorpusException | IOException | MorphemeException | LinguisticDataException | CompiledCorpusRegistryException e) {
			throw new MorphemeDictException(e);
		}
		tLogger.trace("end of method");
		return results;
	}

	private MorphemeDictResult findExamples_NEW(
			MorphemeDictInputs inputs, String corpusName)
			throws MorphemeDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");

		tLogger.trace("invoked with inputs.canonicalForm="+inputs.canonicalForm +", inputs.nbExamples="+inputs.nbExamples);

		tLogger.trace("Creating the MorphemeDictionary instance");
		MorphemeDictResult results = new MorphemeDictResult();
		try {

			MorphemeDictionary morphExtractor = new MorphemeDictionary();

			tLogger.trace("Loading the corpus");
			CompiledCorpus compiledCorpus =
				new CompiledCorpusRegistry().getCorpus();
			morphExtractor.useCorpus(compiledCorpus);
			tLogger.trace("Using corpus of type="+compiledCorpus.getClass());

			int nbExamples = 20;
			if (inputs.nbExamples != null) {
				nbExamples = Integer.valueOf(inputs.nbExamples);
			}
			morphExtractor.setNbDisplayedWords(nbExamples);

			tLogger.trace("Finding words that contain the morpheme");
			Integer maxExamples = null;
			try {
				maxExamples = Integer.parseInt(inputs.nbExamples);
			} catch (Exception e) {
				// If the nbExamples input was not a string representation of an
				// integer, then just ignore it.
			}

			List<MorphDictionaryEntry> wordsForMorphemes =
				morphExtractor.search(inputs.canonicalForm, maxExamples);

			LinguisticData linguisticData = LinguisticData.getInstance();
			tLogger.trace("wordsForMorphemes: "+wordsForMorphemes.size());
			Iterator<MorphDictionaryEntry> itWFM = wordsForMorphemes.iterator();
			while (itWFM.hasNext()) {
				MorphDictionaryEntry w = itWFM.next();
				String morphID = w.morphemeWithId;
				if (linguisticData.getMorpheme(morphID).isComposite()) {
					// We only list the non-composite morphemes
					continue;
				}
				String morphMeaning = Morpheme.getMorpheme(morphID).englishMeaning;
				tLogger.trace("morphID: "+morphID+", morphMeaning: "+morphMeaning);
				MorphemeHumanReadableDescr morphDescr =
					new MorphemeHumanReadableDescr(morphID, morphMeaning);
				results.matchingMorphemes.add(morphDescr);

				List<MorphWordExample> wordsAndFreqs = w.words;
				tLogger.trace("wordsAndFreqs: "+wordsAndFreqs.size());
				List<String> words = new ArrayList<String>();
				for (MorphWordExample example : wordsAndFreqs) {
					tLogger.trace("example.word: "+example.word);
					words.add(example.word);
				}
				results.examplesForMorpheme.put(
					w.morphemeWithId, words.toArray(new String[0]));
			}
		} catch (MorphemeDictionaryException | CompiledCorpusException | IOException | MorphemeException | LinguisticDataException | CompiledCorpusRegistryException e) {
			throw new MorphemeDictException(e);
		}
		tLogger.trace("end of method");
		return results;
	}

}
