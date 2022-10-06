package org.iutools.concordancer.tm.sql;

import org.iutools.sql.Sql2Pojo;
import org.json.JSONObject;

import java.sql.SQLException;

public class Sql2SentenceInLang implements Sql2Pojo<SentenceInLang> {

	@Override
	public SentenceInLang toPOJO(JSONObject jObj) throws SQLException {
		SentenceInLang pojo = new SentenceInLang(jObj);
		return pojo;
	}
}

