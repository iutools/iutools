package org.iutools.concordancer.tm;

import org.iutools.concordancer.tm.sql.TranslationMemory_SQL;

public class TranslationMemory_SQLTest extends TranslationMemoryTest {
	@Override
	public TranslationMemory makeTM(String memoryName) {
		return new TranslationMemory_SQL(memoryName);
	}

	// To disable this test
	@Override
	public void test__search__HappyPath() throws Exception {
		return;
	}
}