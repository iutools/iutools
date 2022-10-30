package org.iutools.corpus.sql;

import static org.iutools.corpus.CompiledCorpus.LastLoadedDate;
import org.iutools.sql.Row2Pojo;
import org.iutools.sql.Row2PojoTest;

public class Row2LastLoadedDateTest extends Row2PojoTest<LastLoadedDate> {
	@Override
	protected Row2Pojo<LastLoadedDate> makeRow2Pojo() {
		return new Row2LastLoadedDate();
	}

	@Override
	protected LastLoadedDate makeTestPojo() {
		LastLoadedDate lastLoaded = new LastLoadedDate();
		lastLoaded.corpusName = "some-corpus";
		lastLoaded.timestamp = new Long(99999);

		return lastLoaded;
	}
}
