package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * This class converts a JSONObject to a Plain Old Java Object (POJO)
 */
public abstract class Row2Pojo<T> {

	/**
	 * Given a POJO and its JSONObject serialization, convert some of the original
	 * attributes into into their SQL row representation */
	public abstract void convertPojoAttributes(T pojo, JSONObject rawRow) throws SQLException;

	/**
	 * Given the SQL row representation of a POJO, convert it to a POJO
	 * */
	public abstract T toPOJO(JSONObject row) throws SQLException;

	protected TableSchema schema = null;
	protected ObjectMapper mapper = new ObjectMapper();
	protected T pojoPrototype = null;

	public Row2Pojo(TableSchema _schema, T _pojoPrototype) {
		init__Row2Pojo(_schema, _pojoPrototype);
	}

	private void init__Row2Pojo(TableSchema _schema, T _pojoPrototype) {
		this.schema = _schema;
		this.pojoPrototype = _pojoPrototype;
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
			convertPojoAttributes(object, rowJson);
			validateRow(rowJson);
		} catch (JsonProcessingException e) {
			throw new SQLException(e);
		}
		return rowJson;
	}

	private void validateRow(JSONObject row) throws SQLException {
		Set<String> columns = row.keySet();
		for (String colName: columns) {
			Object val = row.get(colName);
			if (val instanceof JSONObject) {
				throw new SQLException("Column "+colName+" was of non-SQL type JSONObject. Value was:\n   "+((JSONObject) val).toString());
			} else if (val instanceof JSONArray) {
				throw new SQLException("Column "+colName+" was of non-SQL type JSONArray. Value was:\n   "+((JSONArray) val).toString());
			}
		}
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
