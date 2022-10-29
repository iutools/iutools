package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;

/**
 * This class converts a JSONObject to a Plain Old Java Object (POJO)
 */
public abstract class Sql2Pojo<T> {

	public abstract T toPOJO(JSONObject jObj) throws SQLException;

	TableSchema schema = null;
	ObjectMapper mapper = new ObjectMapper();

	public Sql2Pojo(TableSchema _schema) {
		this.schema = _schema;
	}

	public String tableName() {
		return schema.tableName;
	}

	public List<String> schemaColNames() {
		return schema.columnNames();
	}

	public void ensureRowIsCompatibleWithSchema(JSONObject row) throws SQLException {
		schema.rowIsCompatible(row);
	}

	public JSONObject toRowJson(T object) throws SQLException {
		JSONObject rowJson = null;
		try {
			String objJson = mapper.writeValueAsString(object);
			rowJson = new JSONObject(objJson);
			rowJson.remove("longDescription");
			rowJson.remove("additionalFields");
			rowJson.remove("_detect_language");
			rowJson.remove("idWithoutType");
			rowJson.remove("shortDescription");
			rowJson.remove("id");
			rowJson.remove("lang");
			rowJson.remove("type");
			rowJson.remove("creationDate");
			rowJson.remove("content");
		} catch (JsonProcessingException e) {
			throw new SQLException(e);
		}
		return rowJson;
	}

	public Object[] colValues(JSONObject row) {
		List<String> colOrder = schemaColNames();
		Object[] values = new Object[colOrder.size()];
		for (int ii=0; ii < colOrder.size(); ii++) {
			String colName = colOrder.get(ii);
			Object colValue = null;
			if (row.has(colName)) {
				colValue = row.get(colName);
			}
			if (colValue == JSONObject.NULL) {
				colValue = null;
			}
			values[ii] = colValue;
		}
		return values;
	}

	/**
	 * Remove a column from a row's JSON data.
	 * The value of the removed column is returned.
	 */
	protected Object removeColumn(JSONObject jObj, String colName) {
		Object removed = null;
		if (jObj.has(colName)) {
			removed = jObj.get(colName);
			jObj.remove(colName);
		}
		if (removed == JSONObject.NULL) {
			removed = null;
		}
		return removed;
	}

}
