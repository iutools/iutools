package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Row {
	public String tableName = null;
	public String idFieldName = null;
	public Object idValue = null;
	private JSONObject rowData = new JSONObject();
	private ObjectMapper mapper = new ObjectMapper();
	private List<String> colNamesSorted = null;

	public Row(SQLPersistent object) throws SQLException {
		JSONObject json = object.toJsonObject();
		String tableName = object.tableName();
		String idColName = object.idFieldName();
		init__Row(json, tableName, idColName);
	}

	public Row(String _tableName, String _idFieldName) {
		init__Row((JSONObject)null, _tableName, _idFieldName);
	}

	public Row(String _tableName, String _idFieldName, Object idFieldValue) {
		rowData.put(_idFieldName, idFieldValue);
		init__Row(rowData, _tableName, _idFieldName);
	}

	public Row(JSONObject rowData, String _tableName, String _idFieldName) {
		init__Row(rowData, _tableName, _idFieldName);
	}

	public Row addColumn(String colName, Object colValue) {
		rowData.put(colName, colValue);
		return this;
	}

	private void init__Row(
		JSONObject _rowData, String _tableName, String _idFieldName) {
		this.tableName = _tableName;
		this.idFieldName = _idFieldName;
		if (_rowData != null) {
			this.rowData = _rowData;
		}
		this.idValue = this.rowData.get(idFieldName);
	}

	public Object getColumn(String colName) {
		Object val = rowData.get(colName);
		return val;
	}

	public List<String> colNames() {
		if (colNamesSorted == null) {
			colNamesSorted = new ArrayList<String>();
			colNamesSorted.addAll(rowData.keySet());
			if (colNamesSorted.contains("type")) {
				colNamesSorted.remove("type");
			}
			Collections.sort(colNamesSorted);
		}
		return colNamesSorted;
	}

	public String sqlColNames() {
		String sql = "";
		for (String attrName: colNames()) {
			if (!sql.isEmpty()) {
				sql += ", ";
			}
			sql += attrName;
		}
		return sql;
	}

	public String sqlColNameValuePairs() throws SQLException {
		return sqlColNameValuePairs(" ");
	}

	public Object[] colValues() {
		List<String> colOrder = colNames();
		Object[] values = new Object[colOrder.size()];
		for (int ii=0; ii < colOrder.size(); ii++) {
			String colName = colOrder.get(ii);
			Object colValue = getColumn(colName);
			values[ii] = colValue;
		}
		return values;
	}

	/**
	 * Returns the values of the row in the form of an SQL string.
	 */
	public String sqlColValues() throws SQLException {
		String sql = "";
		for (String attrName: colNames()) {
			if (!sql.isEmpty()) {
				sql += ", ";
			}
			sql += sqlColValue(attrName);
		}
		return sql;
	}

	public String sqlColNameValuePairs(String padding) throws SQLException {
		if (!padding.matches("^ *$")) {
			throw new SQLException("Padding was not blank: '"+padding+"'");
		}
		String descr = "";
		ObjectMapper mapper = new ObjectMapper();
		for (String attrName: colNames()) {
			if (!descr.isEmpty()) {
				descr += ",\n"+padding;
			}
			descr += attrName+" = "+sqlColValue(attrName);
		}
		if (!descr.isEmpty()) {
			descr = padding + descr;
		}
		return descr;
	}

	private String sqlColValue(String attrName) throws SQLException {
		Object attrVal = rowData.get(attrName);
		String attrValStr = "NULL";
		if (attrVal != JSONObject.NULL) {
			try {
				attrValStr = mapper.writeValueAsString(attrVal);
			} catch (JsonProcessingException e) {
				throw new SQLException("Cannot write column value as string: "+attrName);
			}
		}
		attrValStr = attrValStr.replaceAll("(^\"|\"$)", "'");
		return attrValStr;
	}

	public String sqlOnDuplicateUpdate() {
		String sql = "";
		List<String> colNames = colNames();
		int colCounter = 0;
		for (String colName: colNames) {
			colCounter++;
			if (colCounter == 1) {
				sql = "ON DUPLICATE KEY UPDATE\n";
			}
			sql += "  " + colName + " = VALUES("+colName+")";
			if (colCounter != colNames.size()) {
				sql += ",";
			}
			sql += "\n";
		}
		return sql;
	}


	@Override
	public String toString() {
		String toS =
			"Row(Table="+tableName+"; Id="+ idValue.toString()+")";
		return toS;
	}

	public void setColumn(String colName, Object colValue) {
		rowData.put(colName, colValue);
		colNamesSorted = null;
	}

	public JSONObject asJSONObject() {
		return rowData;
	}

	public String asJsonString() {
		return rowData.toString();
	}

	public void ensureHasSameColumnNamesAs(Row otherRow) throws SQLException {
		boolean differentSizes = false;
		Integer faultyIndex = null;
		List<String> otherRowNames = otherRow.colNames();
		List<String> thisRowNames = colNames();
		if (otherRowNames.size() != thisRowNames.size()) {
			differentSizes = true;
		} else {
			for (int ii=0; ii < thisRowNames.size(); ii++) {
				if (!otherRowNames.get(ii).equals(thisRowNames.get(ii))) {
					faultyIndex = ii;
					break;
				}
			}
		}
		if (differentSizes || faultyIndex != null) {
			try {
				String errMess =
					"The two rows did not have the same column names.\n"+
					"This row:\n "+mapper.writeValueAsString(thisRowNames)+"\n"+
					"Other row:\n "+mapper.writeValueAsString(otherRowNames)
					;
				if (differentSizes) {
					errMess += "The two lists had different lengths";
				} else {
					errMess += "The two lists differed at index "+faultyIndex+"\n";
				}
				throw new SQLException(errMess);
			} catch (JsonProcessingException e) {
				throw new SQLException(e);
			}
		}
	}
}
