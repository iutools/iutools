package org.iutools.corpus.sql;

import static org.iutools.corpus.CompiledCorpus.LastLoadedDate;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.iutools.sql.Row2Pojo;
import org.json.JSONObject;

import java.sql.SQLException;

public class Row2LastLoadedDate extends Row2Pojo<LastLoadedDate> {
	public Row2LastLoadedDate() {
		super(new LastLoadedDateSchema(), new LastLoadedDate());
	}

	@Override
	public void convertPojoAttributes(LastLoadedDate lastLoaded, JSONObject rawRow) throws SQLException {
		// No POJO attributes to be converted
	}

	@Override
	public LastLoadedDate toPOJO(JSONObject row) throws SQLException {
		LastLoadedDate lastLoaded = null;
		if (row != null) {
			try {
				lastLoaded = mapper.readValue(row.toString(), LastLoadedDate.class);
			} catch (JsonProcessingException e) {
				throw new SQLException(e);
			}
		}
		return lastLoaded;
	}
}
