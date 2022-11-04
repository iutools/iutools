package org.iutools.concordancer.tm.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
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

	PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
		.builder()
		.allowIfBaseType(Map.class)
		.allowIfBaseType(Alignment.class)
		.allowIfBaseType(Object.class)
		.build();

	// Use this mapper to serialize/deserialize the walig4langpair attribute of
	// the Alignment object.
	// This attribute is a Map<String,WordAlignment> and if we use a barebone
	// ObjectMapper to serialize it, we will loose the fact that the values are
	// of type WordAlignment. This in turn will cause problems when we try to
	// deserialize the json string back to Map<String,WordAlignment>.
	//
	// Note that for some reason, this mapper does not work when we try to
	// serialize/deserialize an Alignment. For the latter, we use the barebone
	// mapper.
	ObjectMapper mapper_walign4langpair = JsonMapper.builder()
			.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
			.build();


	// For some reason, the above mapper_walign4langpair doesn't work when we
	// try to serialize/deserialize an Alignment object. So for that case, we
	// use a barebones ObjectMapper.
	//
	ObjectMapper mapper_barebone = new ObjectMapper();

	public Row2Alignment() {
		super(new AlignmentSchema(), new Alignment());
		init__Row2Alignment();
	}

	private void init__Row2Alignment() {
	}

	@Override
	public void convertPojoAttributes(Alignment alignment, JSONObject row) throws SQLException {
		// Remove Alignment fields that should not be columns
		removeColumn(row, "sentences");
		removeColumn(row, "walign4langpair");
		JSONArray topics = (JSONArray) removeColumn(row, "topics");
		removeColumn(row, "languages");
		removeColumn(row, "sentences");

		// Create columns that are derived from some Alignment fields
		row.put("topics_json", (topics == null?"[]":topics.toString()));

		try {
			String walignsJson = mapper_walign4langpair.writeValueAsString(alignment.walign4langpair);
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

		row.put("langs_json", languages.toString());
		return;
	}

	@Override
	public Alignment toPOJO(JSONObject row) throws SQLException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.Row2Alignment.toPOJO");

		logger.trace("invoked");
		Alignment alignment = null;

		try {
			// Remove SQL columns that:
			// - are not attributes of Alignment
			//   OR
			// - have different types in the row vs Alignment (ex: JSONArray vs Array)
			removeColumn(row, "langs_json");
			String enText = (String) removeColumn(row, "en_text");
			String iuText = (String) removeColumn(row, "iu_text");
			String jsonWordAligns = (String) removeColumn(row, "word_aligns_json");
			String topicsJson = (String) removeColumn(row, "topics_json");

			// Create the Alignment from the remaining fields
			if (logger.isTraceEnabled()) {
				logger.trace("Creating Alignment object from json string: "+row.toString());
			}
			alignment = mapper_barebone.readValue(row.toString(), Alignment.class);
			logger.trace("Alignment object created from compatible column.");
			Map<String,WordAlignment> waligns4langPairs =
				new HashMap<String,WordAlignment>();
			logger.trace("Recreating the word alignment index from the retrieved JSON string");
			waligns4langPairs = mapper_walign4langpair.readValue(jsonWordAligns, waligns4langPairs.getClass());
			logger.trace("Word alignment index successfully deserialized");
			for (String langPair: waligns4langPairs.keySet()) {
				WordAlignment walignOnePair = waligns4langPairs.get(langPair);
				logger.trace("Got word alignment for langauge pair "+langPair);
				alignment.setWordAlignment(walignOnePair);
				logger.trace("Sucessfully set word alignment for langauge pair "+langPair);
			}

			// Set some fields whose values are derived from the removed SQL columns
			if (topicsJson != null) {
				alignment.setTopics(mapper_barebone.readValue(topicsJson, List.class));
			}
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
			throw new SQLException("Error converting SQL row to Alignment instance", e);
		}

		logger.trace("exiting");
		return alignment;

	}

	private Map<String,WordAlignment> deserializeWordAlignmentMap(
		String jsonWordAligns) throws SQLException {
		Map<String,WordAlignment> walignMap = new HashMap<String, WordAlignment>();
		if (jsonWordAligns != null) {
			try {
				walignMap = mapper_walign4langpair.readValue(jsonWordAligns, walignMap.getClass());
			} catch (JsonProcessingException e) {
				throw new SQLException("Could not deserialize json for WordAlignment map. Json string was:\n" + jsonWordAligns, e);
			}
		}

		return walignMap;
	}
}
