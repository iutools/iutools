package org.iutools.concordancer.tm.elasticsearch;

import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryTest;

public class TranslationMemory_ESTest extends TranslationMemoryTest {

	@Override
	public TranslationMemory makeTM(String memoryName) {
		return new TranslationMemory_ES(memoryName);
	}
}
