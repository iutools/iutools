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

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.nunhansearch.ProcessQuery;
import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
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
//			List<Pair<String,List<String>>> wordsForMorphemes = getOccurrences(inputs);
		HashMap<String, MorphemeSearchResult> wordsForMorphemes = getOccurrences(inputs, corpusName);
//			Gson gson = new Gson();
//			String str = gson.toJson(wordsForMorphemes, HashMap.class);
//			logger.trace("results in json: "+str);
//			results.matchingWords = str;
		results.matchingWords = wordsForMorphemes;

		return results;
	}

	/*private CompiledCorpus getCorpus(String corpusName) throws ConfigException, CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusName);
		return corpus;
	}*/

	private HashMap<String,MorphemeSearchResult> getOccurrences(OccurenceSearchInputs inputs, String corpusName) 
			throws SearchEndpointException, ConfigException, CompiledCorpusRegistryException, 
					IOException, Exception {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.webservice.OccurenceSearchEndpoint.getOccurrences");
		MorphemeSearcher morphExtractor = new MorphemeSearcher();
		//CompiledCorpus compiledCorpus = CompiledCorpusRegistry.getCorpus(corpusName);
		CompiledCorpus compiledCorpus = CompiledCorpusRegistry.getCorpus(corpusName);
		morphExtractor.useCorpus(compiledCorpus);
		int nbExamples = Integer.valueOf(inputs.nbExamples);
		morphExtractor.setNbDisplayedWords(nbExamples);
				
		List<MorphemeSearcher.Words> wordsForMorphemes = morphExtractor.wordsContainingMorpheme(inputs.wordPattern);
		logger.debug("wordsForMorphemes: "+wordsForMorphemes.size());
		HashMap<String,MorphemeSearchResult> results = new HashMap<String,MorphemeSearchResult>();
		MorphemeSearcher.WordFreqComparator comparator = morphExtractor.new WordFreqComparator();
		Iterator<MorphemeSearcher.Words> itWFM = wordsForMorphemes.iterator();
		while (itWFM.hasNext()) {
			MorphemeSearcher.Words w = itWFM.next();
			logger.debug("morphemeWithId: "+w.morphemeWithId);
			String meaningOfMorpheme = Morpheme.getMorpheme(w.morphemeWithId).englishMeaning;
			logger.debug("meaningOfMorpheme: "+meaningOfMorpheme);
			List<ScoredExample> wordsAndFreqs = w.words;
			logger.debug("wordsAndFreqs: "+wordsAndFreqs.size());
			ScoredExample[] wordsFreqsArray = wordsAndFreqs.toArray(new ScoredExample[] {});
//			Arrays.sort(wordsFreqsArray, comparator);
			List<String> words = new ArrayList<String>();
			List<Double> wordScores = new ArrayList<Double>();
			for (ScoredExample example : wordsFreqsArray) {
				logger.debug("example.word: "+example.word);
				words.add(example.word);
				wordScores.add(example.score);
			}
			MorphemeSearchResult morpheSearchResult = new MorphemeSearchResult(meaningOfMorpheme,words,wordScores);
			results.put(w.morphemeWithId, morpheSearchResult);
		}
	
		logger.debug("end of method");
		return results;
	}


}
