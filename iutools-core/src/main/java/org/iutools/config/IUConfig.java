package org.iutools.config;

import java.util.Set;

import ca.nrc.config.Config;
import ca.nrc.config.ConfigException;

public class IUConfig extends Config {

	public static final String propName_wordSpotting =
		"org.iutools.concordancer.wordSpotting";

	private static Set<String> nodesToTrace = null;
	
	public static int esVersion() throws ConfigException {
		return getConfigProperty("org.iutools.elasticsearch.version", 5);
	}

	public static boolean esMultiIndex() throws ConfigException {
		boolean multiIndex = false;
		String multiIndex_str = getConfigProperty("org.iutools.elasticsearch.multiindex", false);
		if (multiIndex_str != null) {
			multiIndex = Boolean.parseBoolean(multiIndex_str);
		}
		return multiIndex;
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

	public String nodesToTraceRegex() throws ConfigException {
		String regex =
			getConfigProperty(
				"ca.nrc.datastructure.trie.nodesToTraceRegex",
				false);

		return regex;
	}

	public String sqlHostName() throws ConfigException {
		String name =
			getConfigProperty(
				"org.iutools.sql.hostname",
				false);
		if (name == null) {
			name = "localhost";
		}
 		return name;
	}


	public String sqlDbName() throws ConfigException {
		String name =
			getConfigProperty(
				"org.iutools.sql.dbname",
				false);
		if (name == null) {
			name = "iutools_db";
		}
 		return name;
	}

	public String sqlUserName() throws ConfigException {
		String name =
			getConfigProperty(
				"org.iutools.sql.username",
				true);
		return name;
	}

	public String sqlPasswd() throws ConfigException {
		String name =
			getConfigProperty(
				"org.iutools.sql.passwd",
				true);
		return name;
	}

	public String sqlPortNumber() throws ConfigException {
		String portNum =
			getConfigProperty(
				"org.iutools.sql.portnum",
				"3306");
		return portNum;
	}

	public String corpusDataStore() throws ConfigException {
		String dataStore =
			getConfigProperty(
				"org.iutools.corpus.datastore",
				"sql");
		dataStore.toLowerCase();
		if (!dataStore.matches("^(sql|elasticsearch)$")) {
			throw new ConfigException(
				"Bad value for org.iutools.corpus.datastore: "+dataStore+
				"\nShould be either sql or elasticsearch");
		}
		return dataStore;
	}

	public String tmDataStore() throws ConfigException {
		String dataStore =
			getConfigProperty(
				"org.iutools.concordancer.tm.datastore",
					"elasticsearch");
		dataStore.toLowerCase();
		if (!dataStore.matches("^(sql|elasticsearch)$")) {
			throw new ConfigException(
				"Bad value for org.iutools.concordancer.tm.datastore: "+dataStore+
				"\nShould be either sql or elasticsearch");
		}
		return dataStore;
	}

	/**
	 * If true, then the SQL LeakTracker will remember what methods (i.e. what
	 * call stack) generated each resource.
	 * @return
	 */
	public boolean monitorSQLResourceProvenance() throws ConfigException {
		Boolean monitor =
			getConfigProperty(
				"org.iutools.sql.monitorResourceProvenance",
					new Boolean(false));
		return monitor;
	}

	/**
	 * Get the list of email addresses to which user feedback should
	 * be sent.
	 */
	public String[] userFeedbackEmails() throws ConfigException {
		String[] emails = null;
		String emailsStr  =
			getConfigProperty(
				"org.iutools.apps.feedkback_emails",
					false);
		if (emailsStr != null) {
			emails = emailsStr.split("\\s*[;,]\\s*");
		}
		return emails;
	}
}
