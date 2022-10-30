package org.iutools.corpus.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.sql.Row2Pojo;
import org.iutools.text.ngrams.NgramCompiler;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.*;

/** Convert JSONObject to a WordInfo object */
public class Row2WordInfo extends Row2Pojo<WordInfo> {
	private ObjectMapper mapper = new ObjectMapper();

	public Row2WordInfo() {
		super(new WordInfoSchema(), new WordInfo());
	}

	@Override
	public void convertPojoAttributes(WordInfo winfo, JSONObject row) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.Row2WordInfo.convertPojoAttributes");

		removeColumn(row, "wordCharsSpaceConcatenated");
		removeColumn(row, "morphemesSpaceConcatenated");

		// The SQL row stores the decompositionSample as a JSON string
		Object decompositionSample =
			removeColumn(row, "decompositionsSample");
		String decompositionsSampleJSON = "null";
		if (decompositionSample != null) {
			decompositionsSampleJSON = decompositionSample.toString();
		}
		row.put("decompositionsSampleJSON", decompositionsSampleJSON);

		row.put("wordNgrams", wordNgrams(winfo.word));
		row.put("morphemeNgrams", morphemeNgrams(winfo.topDecomposition()));
		row.put("morphemeNgramsWrittenForms",
			morphemeNgramsWrittenForms(winfo.topDecomposition()));

		return;
	}

	@Override
	public WordInfo toPOJO(JSONObject row) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.sql.Row2WordInfo.convertRow2Pojo");
		WordInfo winfo = null;

		try {
			// Remove SQL columns that are not attributes of WordInfo
			removeColumn(row, "noid");
			removeColumn(row, "corpusName");
			removeColumn(row, "wordNgrams");
			removeColumn(row, "morphemeNgrams");
			removeColumn(row, "morphemeNgramsWrittenForms");
			String decompositionsSampleJSON =
				(String) removeColumn(row, "decompositionsSampleJSON");

			// Create the WordInfo from the remaining fields
			String jsonStr = row.toString();
			winfo = mapper.readValue(jsonStr, WordInfo.class);

			// Set some fields whose values are derived from the removed SQL columns
			String[][] decompsSample = deserializeDecompositionsSample(decompositionsSampleJSON);
			winfo.setDecompositions(decompsSample, winfo.totalDecompositions);
		} catch (JsonProcessingException| CompiledCorpusException e) {
			throw new SQLException("Error converting SQL row to WordInfo instance", e);
		}

		return winfo;
	}

	/**
	 * Returns a string that lists all the character ngrams for the word. This
	 * makes it possible for SQL to rapidly find words that contain a particular ngram.
	 */
	public static String wordNgrams(String word) {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.Row2WordInfo.wordNgrams");
		String ngramsStr = "";
		if (word != null) {
			NgramCompiler compiler = ngramsCompiler();
			Set<String> ngramsSet = compiler.compile(word);
			List<String> ngramsLst = new ArrayList<String>(ngramsSet);
			Collections.sort(ngramsLst);
			ngramsStr = "";
			for (String ngram: ngramsLst) {
				logger.trace("ngram="+ngram);
				ngram = compiler.replaceCaretAndDollar(ngram);
				logger.trace("After replacing caret and dollar, ngram="+ngram);
				ngramsStr += ngram+" ";
			}
		}
		return ngramsStr;
	}

	public static NgramCompiler ngramsCompiler() {
		return new NgramCompiler(3, true);
	}

	/**
	 * Returns a string that lists all the morpheme ngrams for the word. It makes
	 * it possible for SQL to rapidly find words that contain a particular ngram.
	 */
	private String morphemeNgrams(String[] decomp) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.Row2WordInfo.morphemeNgrams");
		String ngrams = computeMorphNgrams(decomp);
		return ngrams;
	}

	protected static String computeMorphNgrams(Boolean writtenFormsOnly,
		String... morphemesArr) throws SQLException {
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

	protected static String computeMorphNgrams(String... morphemesArr)
		throws SQLException {
		return computeMorphNgrams((Boolean)null, morphemesArr);
	}

	private static String[] writtenFormsForMorphemes(String[] morphemes)
		throws SQLException {
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
						throw new SQLException(e);
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

	public String morphemeNgramsWrittenForms(String[] decomp) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.Row2WordInfo.morphemeNgramsWrittenForms");
		String morphemeNgramsWrittenForms = "";
		if (decomp != null) {
			morphemeNgramsWrittenForms = computeMorphNgrams(true, decomp);
		}
		return morphemeNgramsWrittenForms;
	}

	public String[][] deserializeDecompositionsSample(String json) throws SQLException {
		String[][] decompsSample = new String[0][];
		if (json == null) {
			decompsSample = null;
		} else {
			try {
				decompsSample = mapper.readValue(json, decompsSample.getClass());
			} catch (JsonProcessingException e) {
				throw new SQLException("Error deserializing the 'decompositionSample' column of an SQL row", e);
			}
		}
		return decompsSample;
	}
}
