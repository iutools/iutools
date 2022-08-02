package org.iutools.sql;

import org.json.JSONObject;

import java.sql.SQLException;

/**
 * This class converts a JSONObject to a Plain Old Java Object (POJO)
 */
public interface Sql2Pojo<T> {
	public T toPOJO(JSONObject jObj) throws SQLException;
}
