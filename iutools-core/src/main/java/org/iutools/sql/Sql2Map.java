package org.iutools.sql;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Sql2Map extends Sql2Pojo<Map> {

	public Sql2Map() {
		super((TableSchema)null);
	}

	@Override
	public Map toPOJO(JSONObject jObj) throws SQLException {
		Map map = new HashMap();
		for (String key: jObj.keySet()) {
			map.put(key, jObj.get(key));
		}
		return map;
	}
}
