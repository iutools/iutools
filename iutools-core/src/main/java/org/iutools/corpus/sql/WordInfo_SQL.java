package org.iutools.corpus.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.sql.Row;
import org.iutools.text.ngrams.NgramCompiler;
import org.json.JSONObject;

import java.util.*;

/**
 * Subclass of WordInfo that can be stored in SQL database
 */
public class WordInfo_SQL extends WordInfo {
//	/**
//	 * This representation of the word allows SQL to efficiently search for
//	 * ngrams in the word.
//	 */
//	private String wordSQLSearchable = null;

	/**
	 * ngrams contained in the word
	 */
	private String wordNgrams = null;

	/**
	 * Decomposition sample in the form of a JSON string.
	 */
	public String decompositionsSampleJSON = null;

	/**
	 * This field lists all the morpheme ngrams for the word. It makes it possible for SQL
	 * to rapidly find words that contain a particular ngram.
	 */
	private String morphemeNgrams = null;

	ObjectMapper _mapper = null;

	public WordInfo_SQL() {
		super();
	}

	public WordInfo_SQL(String word) {
		super(word);
	}

	public WordInfo_SQL(WordInfo winfo) throws CompiledCorpusException {
		super();
		word = winfo.word;
		frequency = winfo.frequency;
		setDecompositions(winfo.decompositionsSample, winfo.totalDecompositions);
	}

	public NgramCompiler ngramsCompiler() {
		return new NgramCompiler(3, true);
	}

	public ObjectMapper mapper() {
		if (_mapper == null) {
			_mapper = new ObjectMapper();
		}
		return _mapper;
	}

	@Override
	public WordInfo setDecompositions(String[][] sampleDecomps, Integer totalDecomps) throws CompiledCorpusException {
		super.setDecompositions(sampleDecomps, totalDecomps);
		try {
			decompositionsSampleJSON = mapper().writeValueAsString(sampleDecomps);
			this.decompositionsSampleJSON = decompositionsSampleJSON;
		} catch (JsonProcessingException e) {
			throw new CompiledCorpusException(e);
		}
		return this;
	}

//	public String getWordSQLSearchable() {
//		if (wordSQLSearchable == null && word != null) {
//			String[] wordChars = word.split("");
//			for (int ii=0; ii < wordChars.length; ii++) {
//				String ch = wordChars[ii];
//				if (!ch.matches("[$^]")) {
//					// We triple each character so they won't be considered as stop words
//					// by the SQL full text indexer.
//					for (int jj = 0; jj < 2; jj++) {
//						wordChars[ii] += ch;
//					}
//				}
//			}
//			// We add BEGIN and END keywords so we can search for words that start
//			// or end with a particular ngram.
//			wordSQLSearchable =
//				"BEGIN " + String.join(" ", wordChars) + " END";
//		}
//		return wordSQLSearchable;
//	}

	/**
	 * This field lists all the character ngrams for the word. It makes it possible for SQL
	 * to rapidly find words that contain a particular ngram.
	 */
	public String getWordNgrams() {
		Logger logger = LogManager.getLogger("org.iutools.corpus.WordInfo.getWordNgrams");
		if (wordNgrams == null && word != null) {
			NgramCompiler compiler = ngramsCompiler();
			Set<String> ngramsSet = compiler.compile(word);
			List<String> ngrams = new ArrayList<String>(ngramsSet);
			Collections.sort(ngrams);
			wordNgrams = "";
			for (String ngram: ngrams) {
				logger.trace("ngram="+ngram);
				ngram = compiler.replaceCaretAndDollar(ngram);
				logger.trace("After replacing caret and dollar, ngram="+ngram);
				wordNgrams += ngram+" ";
			}
		}
		return wordNgrams;
	}

	/**
	 * This field lists all the morpheme ngrams for the word. It makes it possible for SQL
	 * to rapidly find words that contain a particular ngram.
	 */
	public String getMorphemeNgrams() throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.WordInfo.getMorphemeNgrams");
		if (morphemeNgrams == null) {
			String[] decomp = topDecomposition();
			morphemeNgrams = computeMorphNgrams(decomp);
		}
		return morphemeNgrams;
	}

	protected static String computeMorphNgrams(String[] morphemes) throws CompiledCorpusException {
		String ngrams = "";
		if (morphemes != null) {
			if (morphemes.length > 0 && morphemes[morphemes.length-1].equals("\\")) {
				// We used to put a \ at the end of decomps. Remove it in case we
				// process one of those "legacy" decomps.
				morphemes = Arrays.copyOfRange(morphemes, 0, morphemes.length-1);
			}

			for (int start=0; start < morphemes.length; start++) {
				for (int end = start; end < morphemes.length; end++) {
					String[] ngramMorphemes = Arrays.copyOfRange(morphemes, start, end+1);
					int x = 1;
					for (int ii=0; ii < ngramMorphemes.length; ii++) {
						String morphID = ngramMorphemes[ii];
						if (morphID.equals("\\")) {
							continue;
						}
						morphID = morphID.replaceAll("/", "_");
						morphID = morphID.replaceAll("[{}\\-]", "");
						if (ii > 0) {
							ngrams += "-";
						}
						ngrams += morphID;
					}
					ngrams += " ";
				}
			}
		}
		return ngrams;
	}

	public Row toSQLRow() throws CompiledCorpusException {
		Row row = null;
		try {
			String jsonStr =  mapper().writeValueAsString(this);
			JSONObject jsonObj = new JSONObject(jsonStr);
			jsonObj.remove("_detect_language");
			jsonObj.remove("content");
			jsonObj.remove("creationDate");
			jsonObj.remove("additionalFields");
			jsonObj.remove("id");
			jsonObj.remove("idWithoutType");
			jsonObj.remove("lang");
			jsonObj.remove("longDescription");
			jsonObj.remove("morphemesSpaceConcatenated");
			jsonObj.remove("shortDescription");
			jsonObj.remove("type");
//			jsonObj.remove("topDecompositionStr");
			jsonObj.remove("wordCharsSpaceConcatenated");
			jsonObj.remove("decompositionsSample");
//			String decompsSampleStr = mapper().writeValueAsString(jsonObj.get("decompositionsSample"));
//
//			jsonObj.remove("decompositionsSample");
//			jsonObj.put("decompositionsSample", decompsSampleStr);

			row = new Row(jsonObj, new WordInfoSchema().tableName, new WordInfoSchema().idColumnName);
		} catch (JsonProcessingException e) {
			throw new CompiledCorpusException(e);
		}

		return row;
	}
}
