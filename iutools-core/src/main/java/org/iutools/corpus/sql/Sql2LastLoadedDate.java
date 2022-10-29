package org.iutools.corpus.sql;

import static org.iutools.corpus.CompiledCorpus.LastLoadedDate;
import org.iutools.sql.Sql2Pojo;
import org.json.JSONObject;

import java.sql.SQLException;

public class Sql2LastLoadedDate extends Sql2Pojo<LastLoadedDate> {
	public Sql2LastLoadedDate() {
		super(new LastLoadedDateSchema());
	}

	@Override
	public LastLoadedDate toPOJO(JSONObject jObj) throws SQLException {
		return null;
	}
}
