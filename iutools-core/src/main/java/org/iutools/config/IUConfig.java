package org.iutools.config;

import java.util.Set;

import ca.nrc.config.Config;
import ca.nrc.config.ConfigException;

public class IUConfig extends Config {

	public static final String propName_BingSearchKey =
		"org.iutools.search.bingKey";

	public static final String propName_wordSpotting =
		"org.iutools.concordancer.wordSpotting";

	private static Set<String> nodesToTrace = null;
	
	public static int esVersion() throws ConfigException {
		return getConfigProperty("org.iutools.elasticsearch.version", 5);
	}

	public static String getIUDataPath() throws ConfigException {
		return getIUDataPath(null);
	}

	public static String getIUDataPath(String file) throws ConfigException {
		String iuDataPath = getConfigProperty("org.iutools.datapath", true);
		
		if (file != null) {
			iuDataPath.replaceAll("[\\/]$", "");
			file.replaceAll("^[\\/]", "");
			iuDataPath += "/" + file;
		}
		return iuDataPath;
	}

	public static String getBingSearchKey() throws ConfigException {
		String bingKey = getConfigProperty(propName_BingSearchKey, false);
		return bingKey;
	}

	public String nodesToTraceRegex() throws ConfigException {
		String regex =
			getConfigProperty(
				"ca.nrc.datastructure.trie.nodesToTraceRegex",
				false);

		return regex;
	}
}
