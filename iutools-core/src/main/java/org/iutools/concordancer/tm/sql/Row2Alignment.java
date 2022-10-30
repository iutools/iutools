package org.iutools.concordancer.tm.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.WordAlignment;
import org.iutools.sql.Row2Pojo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Row2Alignment extends Row2Pojo<Alignment> {
	public Row2Alignment() {
		super(new AlignmentSchema(), new Alignment());
	}

	@Override
	public void convertPojoAttributes(Alignment alignment, JSONObject row) throws SQLException {
		// Remove Alignment fields that should not be columns
		removeColumn(row, "sentences");
		removeColumn(row, "walign4langpair");

		// Create columns that are derived from some Alignment fields
		try {
			String walignsJson = mapper.writeValueAsString(alignment.walign4langpair);
			row.put("word_aligns_json", walignsJson);
		} catch (JsonProcessingException e) {
			throw new SQLException("Could not serialize word alignments", e);
		}

		JSONArray languages = new JSONArray();
		String enText = alignment.sentence4lang("en");
		if (enText != null) {
			row.put("en_text", enText);
			languages.put("en");
		}

		String iuText = alignment.sentence4lang("iu");
		if (iuText != null) {
			row.put("iu_text", alignment.sentence4lang("iu"));
			languages.put("iu");
		}

		row.put("languages", languages.toString());
	}

	@Override
	public Alignment toPOJO(JSONObject row) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.Row2Alignment.convertRowColumns");

		Alignment alignment = null;

		try {
			// Remove SQL columns that:
			// - are not attributes of WordInfo
			//   OR
			// - have different types in the row vs WordInfo (ex: JSONArray vs Array)
			String languagesJSON = (String) removeColumn(row, "langs_json");
			String enText = (String) removeColumn(row, "en_text");
			String iuText = (String) removeColumn(row, "iu_text");
			String jsonWordAligns = (String) removeColumn(row, "word_aligns_json");
			String languagesJson = (String) removeColumn(row, "languages");
			String topicsJson = (String) removeColumn(row, "topics");

			// Create the WordInfo from the remaining fields
			alignment = mapper.readValue(jsonWordAligns, Alignment.class);

			// Set some fields whose values are derived from the removed SQL columns
			alignment.setTopics(mapper.readValue(topicsJson, List.class));
			if (enText != null) {
				alignment.setSentence("en", enText);
			}
			if (iuText != null) {
				alignment.setSentence("iu", iuText);
			}

			Map<String,WordAlignment> walign4langpair = deserializeWordAlignmentMap(jsonWordAligns);
			for (String langPair: walign4langpair.keySet()) {
				alignment.setWordAlignment(walign4langpair.get(langPair));
			};
		} catch (JsonProcessingException e) {
			throw new SQLException("Error converting SQL row to WordInfo instance", e);
		}

		return alignment;

	}

	private Map<String,WordAlignment> deserializeWordAlignmentMap(
		String jsonWordAligns) throws SQLException {
		Map<String,WordAlignment> walignMap = new HashMap<String, WordAlignment>();
		if (jsonWordAligns != null) {
			try {
				walignMap = mapper.readValue(jsonWordAligns, walignMap.getClass());
			} catch (JsonProcessingException e) {
				throw new SQLException("Could not deserialize json for WordAlignment map. Json string was:\n" + jsonWordAligns, e);
			}
		}

		return walignMap;
	}
}
