package org.iutools.concordancer.tm;

import org.iutools.concordancer.tm.elasticsearch.TranslationMemory_ES;

public class TranslationMemory_ESTest extends TranslationMemoryTest{
	@Override
	public TranslationMemory makeTM(String tmName) {
		return new TranslationMemory_ES(esIndexName);
	}
}
