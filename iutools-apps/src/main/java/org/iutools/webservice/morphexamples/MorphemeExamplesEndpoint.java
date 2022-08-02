package org.iutools.webservice.morphexamples;

import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.linguisticdata.*;
import org.iutools.morphemedict.MorphDictionaryEntry;
import org.iutools.morphemedict.MorphemeDictionary;
import org.iutools.morphemedict.MorphemeDictionaryException;
import org.iutools.morphemedict.ScoredExample;
import org.iutools.webservice.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MorphemeExamplesEndpoint
	extends Endpoint<MorphemeExamplesInputs, MorphemeExamplesResult> {

	@Override
	protected MorphemeExamplesInputs requestInputs(String jsonRequestBody)
		throws ServiceException {
		return jsonInputs(jsonRequestBody, MorphemeExamplesInputs.class);
	}

	@Override
	public EndpointResult execute(MorphemeExamplesInputs inputs) throws ServiceException {

		Logger logger = LogManager.getLogger("org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint.execute");
		logger.trace("inputs= " + PrettyPrinter.print(inputs));
		MorphemeExamplesResult response = new MorphemeExamplesResult();

		if (inputs.wordPattern == null || inputs.wordPattern.isEmpty()) {
			throw new MorphemeExamplesException("Word pattern was empty or null");
		}

		String corpusName = inputs.corpusName;
		logger.trace("corpusName: " + corpusName);
		if (inputs.corpusName == null || inputs.corpusName.isEmpty()) {
			corpusName = null; // will use default corpus = Hansard2002
		}

		// Retrieve all words that match the wordPattern
		// and put the results in results.matchingWords
		MorphemeExamplesResult results =
			findExamples(inputs, corpusName);

		return results;
	}

	private MorphemeExamplesResult findExamples(
		MorphemeExamplesInputs inputs, String corpusName)
			throws MorphemeExamplesException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");

		tLogger.trace("invoked with inputs.wordPattern="+inputs.wordPattern+", inputs.nbExamples="+inputs.nbExamples);

		tLogger.trace("Creating the MorphemeDictionary instance");
		MorphemeExamplesResult results = new MorphemeExamplesResult();
		try {

			MorphemeDictionary morphExtractor = new MorphemeDictionary();

			tLogger.trace("Loading the corpus");
			CompiledCorpus_ES compiledCorpus =
				new CompiledCorpus_ES(CompiledCorpusRegistry.defaultCorpusName);
			morphExtractor.useCorpus(compiledCorpus);
			tLogger.trace("Using corpus of type="+compiledCorpus.getClass());

			int nbExamples = 20;
			if (inputs.nbExamples != null) {
				nbExamples = Integer.valueOf(inputs.nbExamples);
			}
			morphExtractor.setNbDisplayedWords(nbExamples);

			tLogger.trace("Finding words that contain the morpheme");
			List<MorphDictionaryEntry> wordsForMorphemes =
				morphExtractor.search(inputs.wordPattern);

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

				List<ScoredExample> wordsAndFreqs = w.words;
				tLogger.trace("wordsAndFreqs: "+wordsAndFreqs.size());
				List<String> words = new ArrayList<String>();
				for (ScoredExample example : wordsAndFreqs) {
					tLogger.trace("example.word: "+example.word);
					words.add(example.word);
				}
				results.examplesForMorpheme.put(
					w.morphemeWithId, words.toArray(new String[0]));
			}
		} catch (MorphemeDictionaryException | CompiledCorpusException | IOException | MorphemeException | LinguisticDataException e) {
			throw new MorphemeExamplesException(e);
		}
		tLogger.trace("end of method");
		return results;
	}
}
