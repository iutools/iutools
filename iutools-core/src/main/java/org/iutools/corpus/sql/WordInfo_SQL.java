package org.iutools.corpus.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.sql.Row;
import org.iutools.text.ngrams.NgramCompiler;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.*;

/**
 * Subclass of WordInfo that can be stored in SQL database
 */
public class WordInfo_SQL extends WordInfo {
	/**
	 * ngrams contained in the word
	 */
	private String wordNgrams = null;

	/**
	 * Decomposition sample in the form of a JSON string.
	 */
	public String decompositionsSampleJSON = null;

	/**
	 * This field exists to support an efficient way in SQL to find all the
	 * words that contain a particular morpheme ID like 'taku/1v'.
	 *
	 * The field lists all the morpheme ngrams for that word.
	 * The morphemes in the ngrams correspond to full IDs (ex: 'taku/1v'), but
	 * they are rendered in a form like (ex: 'taku_1vv') so that the SQL indexer
	 * won't split them into more than one word (ex: 'taku/1vv' -> taku, 1vv).
	 */
	private String morphemeNgrams = null;

	/**
	 * This field exists to support an efficient way in SQL to find all the
	 * words that contain any morpheme whose written form is 'taku'.
	 *
	 * The field lists all the morpheme ngrams for that word.
	 * The morphemes in the ngrams correspond only to the written form form
	 * of the morphemes (ex: 'taku' as opposed to 'taku/1v')
	 */
	private String morphemeNgramsWrittenForms = null;

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


	public String getMorphemeNgramsWrittenForms() throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.WordInfo.getMorphemeNgramsWrittenForms");
		if (morphemeNgramsWrittenForms == null) {
			String[] decomp = topDecomposition();
			morphemeNgramsWrittenForms = computeMorphNgrams(true, decomp);
		}
		return morphemeNgramsWrittenForms;
	}

	protected static String computeMorphNgrams(String... morphemesArr) throws CompiledCorpusException {
		return computeMorphNgrams((Boolean)null, morphemesArr);
	}

	protected static String computeMorphNgrams(Boolean writtenFormsOnly, String... morphemesArr) throws CompiledCorpusException {
		if (writtenFormsOnly == null) {
			writtenFormsOnly = false;
		}
		String ngrams = "";
		if (morphemesArr != null) {
			if (morphemesArr.length > 0 && morphemesArr[morphemesArr.length-1].equals("\\")) {
				// We used to put a \ at the end of decomps. Remove it in case we
				// process one of those "legacy" decomps.
				morphemesArr = Arrays.copyOfRange(morphemesArr, 0, morphemesArr.length-1);
			}
			boolean startsWithCaret = (morphemesArr.length > 0 && morphemesArr[0].equals("^"));
			boolean endsWithDollar = (morphemesArr.length > 0 && morphemesArr[morphemesArr.length-1].equals("$"));
			if (writtenFormsOnly) {
				morphemesArr = writtenFormsForMorphemes(morphemesArr);
			}
			List<String> morphemes = new ArrayList<String>();
			if (!startsWithCaret) {
				// Add ^ at start of the morpheme lists to signify its start
				morphemes.add("^");
			}
			Collections.addAll(morphemes, morphemesArr);
			if (!endsWithDollar) {
				// Add $ at the end of the morphemes list to signify its end
				morphemes.add("$");
			}
			boolean isFirst = true;
			for (int start=0; start < morphemes.size(); start++) {
				for (int end = start; end < morphemes.size(); end++) {
					List<String> ngramMorphemes = morphemes.subList(start, end+1);
					String ngramAsStr = formatNgramAsSearchableString(writtenFormsOnly, ngramMorphemes);
					if (ngramAsStr.matches("^(BEG|END|BEG__END)$")) {
						continue;
					}
//					for (int ii=0; ii < ngramMorphemes.length; ii++) {
//						String morphID = ngramMorphemes[ii];
//						if (morphID.equals("\\")) {
//							continue;
//						}
//						morphID = morphID.replaceAll("/", "_");
//						morphID = morphID.replaceAll("[{}\\-]", "");
//						if (ii > 0) {
//							ngrams += "-";
//						}
//						ngrams += morphID;
//					}
					if (!isFirst) {
						ngrams += " ";
					}
					ngrams += ngramAsStr;
					isFirst = false;
				}
			}
		}
		return ngrams;
	}

	private static String[] writtenFormsForMorphemes(String[] morphemes) throws CompiledCorpusException {
		String[] morphemesWritten = null;
		if (morphemes != null) {
			morphemesWritten =new String[morphemes.length];
			for (int ii=0; ii < morphemes.length; ii++) {
				String origMorph = morphemes[ii];
				morphemesWritten[ii] = origMorph;
				if (!origMorph.equals("^") && !origMorph.equals("$")) {
					morphemesWritten[ii] = origMorph;
				} else {
					try {
						morphemesWritten[ii] = Morpheme.splitMorphID(origMorph).getLeft();
					} catch (MorphemeException e) {
						throw new CompiledCorpusException(e);
					}
				}
			}
		}
		return morphemesWritten;
	}

	public static String formatNgramAsSearchableString(String joinedMorphemes) {
		String[] morphemes = joinedMorphemes.split("\\s+");
		return formatNgramAsSearchableString(morphemes);
	}

	public static String formatNgramAsSearchableString(String[] morphemes) {
		List<String> morphemesLst = new ArrayList<String>();
		Collections.addAll(morphemesLst, morphemes);
		return formatNgramAsSearchableString((Boolean)null, morphemesLst);
	}

	public static String formatNgramAsSearchableString(
		Boolean writtenFormsOnly, List<String> ngramMorphemes) {
		if (writtenFormsOnly == null) {
			writtenFormsOnly = false;
		}
		String ngramStr = "";
		if (ngramMorphemes != null) {
			boolean isFirst = true;
			for (int ii = 0; ii < ngramMorphemes.size(); ii++) {
				String morphID = ngramMorphemes.get(ii);
				if (writtenFormsOnly) {
					morphID = morphID.replaceAll("/.*$", "");
				}
				String morphStr = null;
				if (morphID.equals("\\")) {
					continue;
				} else if (morphID.equals("^")) {
					morphStr = "BEG";
				} else if (morphID.equals("$")) {
					morphStr = "END";
				} else {
					morphStr = morphID;
					morphStr = morphStr.replaceAll("[/\\-]", "_");
					morphStr = morphStr.replaceAll("^\\^", "BEG__");
					morphStr = morphStr.replaceAll("\\$$", "__END");
					morphStr = morphStr.replaceAll("[{}\\s]", "");
				}
				if (ii > 0) {
					ngramStr += "__";
				}
				ngramStr += morphStr;
			}
		}
		return ngramStr;
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
			jsonObj.remove("wordCharsSpaceConcatenated");
			jsonObj.remove("decompositionsSample");
			row = new Row(jsonObj, new WordInfoSchema().tableName, new WordInfoSchema().idColumnName);
		} catch (JsonProcessingException e) {
			throw new CompiledCorpusException(e);
		}

		return row;
	}
}
