package org.iutools.corpus.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.sql.Sql2Pojo;
import org.json.JSONObject;

import java.sql.SQLException;

/** Convert JSONObject to a WordInfo object */
public class Sql2WordIinfo implements Sql2Pojo<WordInfo> {
	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public WordInfo toPOJO(JSONObject jObj) throws SQLException {
		jObj.remove("noid");
		jObj.remove("corpusName");
		WordInfo_SQL winfo = null;
		String jsonStr = jObj.toString();
		try {
			winfo = mapper.readValue(jsonStr, WordInfo_SQL.class);
			String[][] decompsSample = deserializeDecompositionsSample(winfo.decompositionsSampleJSON);
			winfo.setDecompositions(decompsSample, winfo.totalDecompositions);
		} catch (JsonProcessingException| CompiledCorpusException e) {
			throw new SQLException("Error converting SQL row to WordInfo instance", e);
		}

		return winfo;
	}

	public String[][] deserializeDecompositionsSample(String json) throws SQLException {
		String[][] decompsSample = new String[0][];
		try {
			decompsSample = mapper.readValue(json, decompsSample.getClass());
		} catch (JsonProcessingException e) {
			throw new SQLException("Error deserializing the 'decompositionSample' column of an SQL row", e);
		}
		return decompsSample;
	}
}
