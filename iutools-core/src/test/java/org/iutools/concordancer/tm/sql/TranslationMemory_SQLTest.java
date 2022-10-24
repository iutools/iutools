package org.iutools.concordancer.tm.sql;

import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryTest;
import org.iutools.sql.SQLLeakMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TranslationMemory_SQLTest extends TranslationMemoryTest {

	SQLLeakMonitor sqlLeakMonitor = null;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		sqlLeakMonitor = new SQLLeakMonitor();
	}

	@AfterEach
	public void tearDown() {
		sqlLeakMonitor.assertNoLeaks();
	}


	@Override
	public TranslationMemory makeTM(String memoryName) {
		return new TranslationMemory_SQL(memoryName);
	}

//	// To disable this test
//	@Override
//	public void test__search__HappyPath() throws Exception {
//		return;
//	}
//
//	// To disable this test
//	@Override
//	public void test__TranslationMemory__Synopsis() throws Exception {
//		return;
//	}
}