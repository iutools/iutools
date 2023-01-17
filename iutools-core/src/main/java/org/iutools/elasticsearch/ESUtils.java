package org.iutools.elasticsearch;

import co.elastic.clients.json.JsonpSerializable;
import co.elastic.clients.json.jackson.JacksonJsonProvider;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import jakarta.json.stream.JsonGenerator;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.StringWriter;

public class ESUtils {
	public static String toJson(JsonpSerializable esObject) {
		StringWriter writer = new StringWriter();
		JsonGenerator generator = JacksonJsonProvider.provider().createGenerator(writer);
		esObject.serialize(generator, new JacksonJsonpMapper());
		generator.flush();
		String json = writer.toString();
		return json;
	}

	public static JSONArray mustMatchFields(Pair<String,String>... fields) {
		JSONArray mustArr = new JSONArray();
		for (Pair<String,String> aField: fields) {
			String fldName = aField.getLeft();
			String fldVal = aField.getRight();
			// If fldVal == null, it means we don't care about that particular field's value
			if (fldVal != null) {
				mustArr.put(new JSONObject()
					.put("match", new JSONObject()
						.put(fldName, new JSONObject()
							.put("query", fldVal)
						)
					)
				);
			}
		}
		return mustArr;
	}

}
