package org.iutools.corpus;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.iutools.config.IUConfig;
import ca.nrc.config.ConfigException;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.sql.CompiledCorpus_SQL;

public class CompiledCorpusRegistry {
	
	private static Map<String,File> registry = null;
	public static final String defaultCorpusName = "hansard-1999-2002";
	public static final String emptyCorpusName = "emptycorpus";

	public CompiledCorpusRegistry() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpusRegistry.constructor");
		init_CompiledCorpusRegistry();
		return;
	}

	private void init_CompiledCorpusRegistry() throws CompiledCorpusException {
		Logger tLogger = LogManager.getLogger("org.iutools.corpus.CompiledCorpusRegistry.init_CompiledCorpusRegistry");
		// Initialize the static registry map
		if (registry == null) {
			try {
				registry = new HashMap<String,File>();
				registerCorpus(
					defaultCorpusName,
					new File(
						IUConfig.getIUDataPath(
							"data/compiled-corpora/HANSARD-1999-2002.json")));
			} catch (ConfigException | CompiledCorpusRegistryException e) {
				// Reset registry to null if we weren't able to initialize it.
				// That way, the error will not be "swept under the carpet" for
				// future calls to the class (ex: in the context of a Tomcat app)
				//
				registry = null;
				throw new CompiledCorpusException("Could not initialize the static registry map", e);
			}
		}
	}

	public static void registerCorpus(
		String corpusName, File jsonFile)
		throws CompiledCorpusRegistryException {
		if (registry.containsKey(corpusName)
			&&  !registry.get(corpusName).equals(jsonFile) ) {
			throw new CompiledCorpusRegistryException("The name '"+corpusName+"' is already associated with a different compilation file.");
		} else if ( !jsonFile.exists() ) {
			throw new CompiledCorpusRegistryException("The file "+jsonFile.getAbsolutePath()+" does not exist.");
		} else {
			registry.put(corpusName, jsonFile);
		}

		return;
	}

	public static Set<String> availableCorpora() throws CompiledCorpusException {
		try {
			new CompiledCorpusRegistry();
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusException(e);
		}
		Set<String> corpNames = new HashSet<String>();
		if (registry != null) {
			corpNames = registry.keySet();
		}
		return corpNames;
	}

	public static Path jsonFile4corpus(CompiledCorpus corpus) {
		String corpusName = corpus.canonicalName();
		return jsonFile4corpus(corpusName);
	}

	public static Path jsonFile4corpus(String corpusName) {
		corpusName = CompiledCorpus.canonizeCorpusName(corpusName);
		File jsonFile = registry.get(corpusName);
		Path jsonFilePath = null;
		if (jsonFile != null) {
			jsonFilePath = Paths.get(jsonFile.toString());
		}
		return jsonFilePath;
	}

	@JsonIgnore
	public CompiledCorpus getCorpus()
		throws CompiledCorpusRegistryException {
		try {
			return getCorpus(defaultCorpusName);
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}
	}

	@JsonIgnore
	public CompiledCorpus getCorpus(String corpusName)
		throws CompiledCorpusRegistryException, CompiledCorpusException {
		return getCorpus(corpusName, (Boolean)null, (Boolean)null);
	}

	@JsonIgnore
	public CompiledCorpus getCorpus(String corpusName, Boolean reloadFromJson)
		throws CompiledCorpusRegistryException, CompiledCorpusException {
		return getCorpus(corpusName, reloadFromJson, (Boolean)null);
	}
											  @JsonIgnore
	public CompiledCorpus getCorpus(String corpusName, Boolean reloadFromJson,
		Boolean allowNonRegistered)
		throws CompiledCorpusRegistryException, CompiledCorpusException {
		Logger logger = LogManager.getLogger("CompiledCorpusRegistry.getCorpusWithName");
		logger.debug("corpusName= '"+corpusName+"'");
		if (corpusName == null) {
			corpusName = defaultCorpusName;
		} else {
			corpusName = CompiledCorpus.canonizeCorpusName(corpusName);
		}
		if (reloadFromJson == null) {
			reloadFromJson = false;
		}
		if (allowNonRegistered == null) {
			allowNonRegistered = new Boolean(false);
		}
		if (!allowNonRegistered && !registry.containsKey(corpusName)) {
			throw new CompiledCorpusRegistryException(
				"There is no corpus by the name of "+corpusName);
		}
		File corpusFile = registry.get(corpusName);


		CompiledCorpus corpus = makeCorpus(corpusName);

		try {
			if (corpusFile != null &&
				(reloadFromJson || !corpus.isUpToDateWithFile(corpusFile))) {
				// Should load the corpus
				File jsonFile = registry.get(corpusName);
				corpus.loadFromFile(jsonFile, true, reloadFromJson);
			}
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}

		return corpus;
	}

	public static CompiledCorpus makeCorpus(String corpusName) throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = null;
		try {
			String dataStore = new IUConfig().corpusDataStore();
			if (dataStore.equals("elasticsearch")) {
				corpus =new CompiledCorpus_ES(corpusName);
			} else if (dataStore.equals("sql")) {
				corpus =new CompiledCorpus_SQL(corpusName);
			} else {
				throw new CompiledCorpusRegistryException("Unknown data store type: "+dataStore);
			}
		} catch (ConfigException | CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}

		return corpus;
	}

	private static String scanDataDirForCorpusFile(String corpusName) 
		throws CompiledCorpusRegistryException {
		Logger logger = LogManager.getLogger("CompiledCorpusRegistry.scanDataDirForCorpusFile");
		logger.debug("corpusName= '"+corpusName+"'");
		
		String corpusesPath;
		try {
			corpusesPath = IUConfig.getIUDataPath("data/compiled-corpuses");
		} catch (ConfigException e) {
			throw new CompiledCorpusRegistryException(e);
		}
		File directory = new File(corpusesPath);
		File[] files = directory.listFiles();
		String corpusFile = null;
		for (int ifile = 0; ifile < files.length; ifile++) {
			String fileName = files[ifile].getName();
			String patternStr = "compiled[_\\-]corpus-" + corpusName;
			logger.debug("pattern: "+patternStr);
			logger.debug("fileName: "+fileName);
			Pattern pattern = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(fileName);
			if (matcher.find()) {
				logger.debug("MATCH");
				corpusFile = fileName;
				break;
			}
		}
		
		if (corpusFile != null) {
			corpusFile = new File(corpusesPath, corpusFile).toString();
		}
		
		return corpusFile;
	}
}
