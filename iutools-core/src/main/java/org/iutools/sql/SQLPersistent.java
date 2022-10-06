package org.iutools.sql;

import ca.nrc.dtrc.elasticsearch.Document;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.corpus.sql.WordInfoSchema;
import org.json.JSONObject;

import java.sql.SQLException;

public class SQLPersistent extends Document {

	private TableSchema schema = null;
	ObjectMapper mapper = new ObjectMapper();

	public SQLPersistent(TableSchema _schema) {
		this.schema = _schema;
	}

	public JSONObject toJsonObject() throws SQLException {
		JSONObject jsonObj = null;
		try {
			String jsonStr = mapper.writeValueAsString(this);
			jsonObj = new JSONObject(jsonStr);
		} catch (JsonProcessingException e) {
			throw new SQLException(e);
		}
		return jsonObj;
	}

	public String tableName() {
		return schema.tableName;
	}

	public String idFieldName() {
		return schema.idColumnName;
	}

	public Row toRow() throws SQLException {
		Row row = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writeValueAsString(this);
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

//			String decompsSampleStr = mapper.writeValueAsString(jsonObj.get("decompositionsSample"));
//			jsonObj.remove("decompositionsSample");
//			jsonObj.put("decompositionsSample", decompsSampleStr);

			row = new Row(jsonObj, schema.tableName, schema.idColumnName);
		} catch (JsonProcessingException e) {
			throw new SQLException(e);
		}

		return row;
	}
}
