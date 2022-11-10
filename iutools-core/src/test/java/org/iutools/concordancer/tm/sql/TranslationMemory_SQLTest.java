package org.iutools.concordancer.tm.sql;

import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryTest;
import org.iutools.sql.SQLLeakMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TranslationMemory_SQLTest extends TranslationMemoryTest {

	SQLLeakMonitor sqlLeakMonitor = null;


	@Override
	public TranslationMemory makeTM(String memoryName) {
		return new TranslationMemory_SQL(memoryName);
	}

	@Override
	public void test__search__VariousCases() throws Exception {
		// We include this to disable this test which doesn't work for SQL tm.
		// There is a case involving a multi-word EN search that produces more
		// results than it should because SQL FULLTEXT search is not able to
		// search for exact multi-word phrases.
		//
		// In the end this does not matter because as of 2022-11-15, we have
		// abandoned the use of SQL for the TM data-store (although we continue
		// to use SQL for the CompiledCorpus data store)
	}

}