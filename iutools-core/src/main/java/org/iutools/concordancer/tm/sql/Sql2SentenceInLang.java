package org.iutools.concordancer.tm.sql;

import org.iutools.sql.Sql2Pojo;
import org.json.JSONObject;

import java.sql.SQLException;

public class Sql2SentenceInLang extends Sql2Pojo<SentenceInLang> {

	public Sql2SentenceInLang() {
		super(new SentenceInLangSchema());
	}

	@Override
	public SentenceInLang toPOJO(JSONObject jObj) throws SQLException {
		SentenceInLang pojo = new SentenceInLang(jObj);
		return pojo;
	}

	@Override
	public JSONObject toRowJson(SentenceInLang sent) throws SQLException {
		String langCopy = sent.lang;
		JSONObject rowJson = super.toRowJson(sent);
		rowJson.put("lang", langCopy);
		return rowJson;
	}
}

