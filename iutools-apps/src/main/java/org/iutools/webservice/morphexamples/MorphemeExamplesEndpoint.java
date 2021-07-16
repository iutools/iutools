package org.iutools.webservice.morphexamples;

import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;
import org.iutools.morphemesearcher.MorphSearchResults;
import org.iutools.morphemesearcher.MorphemeSearcher;
import org.iutools.morphemesearcher.ScoredExample;
import org.iutools.webservice.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MorphemeExamplesEndpoint
	extends Endpoint<MorphemeExamplesInputs, MorphemeExamplesResult> {

	@Override
	protected MorphemeExamplesInputs requestInputs(HttpServletRequest request)
		throws ServiceException {
		return jsonInputs(request, MorphemeExamplesInputs.class);
	}

	@Override
	public EndpointResult execute(MorphemeExamplesInputs inputs) throws ServiceException {

		Logger logger = Logger.getLogger("org.iutools.webservice.morphexamples.MorphemeExamplesEndpoint.execute");
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
		Logger tLogger = Logger.getLogger("org.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");

		tLogger.trace("invoked with inputs.wordPattern="+inputs.wordPattern+", inputs.nbExamples="+inputs.nbExamples);

		tLogger.trace("Creating the MorphemeSearcher instance");
		MorphemeExamplesResult results = new MorphemeExamplesResult();
		try {

			MorphemeSearcher morphExtractor = new MorphemeSearcher();

			tLogger.trace("Loading the corpus");
			CompiledCorpus compiledCorpus =
				new CompiledCorpus(CompiledCorpusRegistry.defaultCorpusName);
			morphExtractor.useCorpus(compiledCorpus);
			tLogger.trace("Using corpus of type="+compiledCorpus.getClass());

			int nbExamples = 20;
			if (inputs.nbExamples != null) {
				nbExamples = Integer.valueOf(inputs.nbExamples);
			}
			morphExtractor.setNbDisplayedWords(nbExamples);

			tLogger.trace("Finding words that contain the morpheme");
			List<MorphSearchResults> wordsForMorphemes =
				morphExtractor.wordsContainingMorpheme(inputs.wordPattern);

			tLogger.trace("wordsForMorphemes: "+wordsForMorphemes.size());
			Iterator<MorphSearchResults> itWFM = wordsForMorphemes.iterator();
			while (itWFM.hasNext()) {
				MorphSearchResults w = itWFM.next();
				String morphID = w.morphemeWithId;
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
		} catch (Exception e) {
			throw new MorphemeExamplesException(e);
		}
		tLogger.trace("end of method");
		return results;
	}
}
