package org.iutools.sql;

import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Row2Map extends Row2Pojo<Map> {

	public Row2Map() {
		super((TableSchema)null, new HashMap<String,Object>());
	}

	@Override
	public void convertPojoAttributes(Map pojo, JSONObject rawRow) throws SQLException {
		// No attributes to be converted
	}

	@Override
	public Map toPOJO(JSONObject row) throws SQLException {
		Map map = new HashMap();
		for (String key: row.keySet()) {
			map.put(key, row.get(key));
		}
		return map;
	}
}
