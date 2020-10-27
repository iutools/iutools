package ca.pirurvik.iutools.webservice;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.pirurvik.iutools.corpus.*;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.data.Morpheme;
import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.morphemesearcher.MorphSearchResults;
import ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher;
import ca.pirurvik.iutools.morphemesearcher.ScoredExample;

public class OccurenceSearchEndpoint extends HttpServlet {
			
	public OccurenceSearchEndpoint() {
		this.initialize();
	};

	protected void initialize() {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();		
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.OccurenceSearchEndpoint.doPost");
		tLogger.trace("invoked");
		tLogger.trace("request URI= "+request.getRequestURI());
		
		String jsonResponse = null;
		
		OccurenceSearchInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, OccurenceSearchInputs.class);
			tLogger.trace("inputs= "+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.OccurenceSearchEndpoint.writeJsonResponse");
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		tLogger.trace("Returning json="+json);
	}

	public OccurenceSearchResponse executeEndPoint(OccurenceSearchInputs inputs) throws Exception {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.webservice.OccurenceSearchEndpoint.executeEndPoint");
		logger.trace("inputs= " + PrettyPrinter.print(inputs));
		OccurenceSearchResponse results = new OccurenceSearchResponse();

		if (inputs.wordPattern == null || inputs.wordPattern.isEmpty()) {
			throw new SearchEndpointException("Word pattern was empty or null");
		}

		String corpusName = inputs.corpusName;
		logger.trace("corpusName: " + corpusName);
		if (inputs.corpusName == null || inputs.corpusName.isEmpty()) {
			corpusName = null; // will use default corpus = Hansard2002
		}

		// Retrieve all words that match the wordPattern
		// and put the results in results.matchingWords
		HashMap<String, MorphemeSearchResult> wordsForMorphemes =
			getOccurrences(inputs, corpusName);
		results.matchingWords = wordsForMorphemes;

		return results;
	}

	private HashMap<String,MorphemeSearchResult> getOccurrences(
			OccurenceSearchInputs inputs, String corpusName)
			throws OccurenceSearchEndpointException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");
		
		tLogger.trace("invoked with inputs.wordPattern="+inputs.wordPattern+", inputs.nbExamples="+inputs.nbExamples);

		tLogger.trace("Creating the MorphemeSearcher instance");
		try {

			MorphemeSearcher morphExtractor = new MorphemeSearcher();

			tLogger.trace("Loading the corpus");
			CompiledCorpus compiledCorpus =
				new CompiledCorpus_ES(CompiledCorpusRegistry.defaultESCorpusName);
			morphExtractor.useCorpus(compiledCorpus);
			tLogger.trace("Using corpus of type="+compiledCorpus.getClass());

			int nbExamples = Integer.valueOf(inputs.nbExamples);
			morphExtractor.setNbDisplayedWords(nbExamples);

			tLogger.trace("Finding words that contain the morpheme");
			List<MorphSearchResults> wordsForMorphemes =
				morphExtractor.wordsContainingMorpheme(inputs.wordPattern);
			tLogger.trace("wordsForMorphemes: "+wordsForMorphemes.size());
			HashMap<String,MorphemeSearchResult> results = new HashMap<String,MorphemeSearchResult>();
			MorphemeSearcher.WordFreqComparator comparator = morphExtractor.new WordFreqComparator();
			Iterator<MorphSearchResults> itWFM = wordsForMorphemes.iterator();
			while (itWFM.hasNext()) {
				MorphSearchResults w = itWFM.next();
				tLogger.trace("morphemeWithId: "+w.morphemeWithId);
				String meaningOfMorpheme = Morpheme.getMorpheme(w.morphemeWithId).englishMeaning;
				tLogger.trace("meaningOfMorpheme: "+meaningOfMorpheme);
				List<ScoredExample> wordsAndFreqs = w.words;
				tLogger.trace("wordsAndFreqs: "+wordsAndFreqs.size());
				ScoredExample[] wordsFreqsArray = wordsAndFreqs.toArray(new ScoredExample[] {});
				List<String> words = new ArrayList<String>();
				List<Double> wordScores = new ArrayList<Double>();
				for (ScoredExample example : wordsFreqsArray) {
					tLogger.trace("example.word: "+example.word);
					words.add(example.word);
					wordScores.add(example.score);
				}
				MorphemeSearchResult morpheSearchResult = new MorphemeSearchResult(meaningOfMorpheme,words,wordScores);
				results.put(w.morphemeWithId, morpheSearchResult);
			}

			tLogger.trace("end of method");

			return results;

		} catch (Exception e) {
			throw new OccurenceSearchEndpointException(e);
		}

	}


}
