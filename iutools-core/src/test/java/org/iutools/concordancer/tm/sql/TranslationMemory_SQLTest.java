package org.iutools.concordancer.tm.sql;

import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryTest;

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

	// To disable this test
	@Override
	public void test__TranslationMemory__Synopsis() throws Exception {
		return;
	}

}