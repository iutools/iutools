package org.iutools.concordancer.tm;

import ca.nrc.config.ConfigException;
import org.iutools.concordancer.tm.elasticsearch.TranslationMemory_ES;
import org.iutools.concordancer.tm.sql.TranslationMemory_SQL;
import org.iutools.config.IUConfig;

/** Class for creating TranslationMemories */
public class TMFactory {

	public TranslationMemory makeTM() throws TranslationMemoryException {
		return makeTM((String)null);
	}

	public TranslationMemory makeTM(String tmName) throws TranslationMemoryException {
		TranslationMemory tm = null;
		try {
			String dataStore = new IUConfig().tmDataStore();
			if (dataStore.equals("sql")) {
				tm = new TranslationMemory_SQL(tmName);
			} else {
				tm = new TranslationMemory_ES(tmName);
			}
		} catch (ConfigException e) {
			throw new TranslationMemoryException(e);
		}

		return tm;
	}
}
