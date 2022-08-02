package org.iutools.sql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.SQLException;

/** Base class for converting between SQL Row and Plain Old Java Object (POJO) */
public class Row2Pojo<T> {
	ObjectMapper mapper = new ObjectMapper();
	public T toPojo(Row row, T proto) throws SQLException {
		String jsonStr = row.asJsonString();
		T pojo = null;
		try {
			pojo = (T) mapper.readValue(jsonStr, proto.getClass());
		} catch (JsonProcessingException e) {
			throw new SQLException(e);
		}
		return pojo;
	}

	public Row toRow(T pojo, TableSchema schema) throws SQLException {
		Row row = null;
//		try {
//			String jsonStr = mapper.writeValueAsString(pojo);
//			row = new Row(new JSONObject(jsonStr), schema);
//		} catch (JsonProcessingException e) {
//			throw new SQLException(e);
//		}
		return row;
	}
}
