package org.iutools.config;

import java.util.Set;

import ca.nrc.config.Config;
import ca.nrc.config.ConfigException;

public class IUConfig extends Config {

	public static final String propName_BingSearchKey =
		"org.iutools.search.bingKey";
	
	private static Set<String> nodesToTrace = null;
	
	
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
	
//	public static String getTrieFilePath() throws ConfigException {
//		String trieFPath = getIUDataPath("src/test/resources/org/utoolstrie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
//		if (! new File(trieFPath).exists()) {
//			throw new ConfigException("Did not find the large corpus compilation file. Please download it and place it in "+
//					trieFPath+". You can download the file from "+
//					"https://www.dropbox.com/s/ka3cn778wgs1mk4/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json?dl=0");
//		}
//		
//		return trieFPath;
//	}
}
