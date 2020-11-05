package ca.pirurvik.iutools.corpus;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistry {
	
	private static Map<String,CompiledCorpus_InMemory> corpusCache = new HashMap<String,CompiledCorpus_InMemory>();
	private static Map<String,File> registry = new HashMap<String,File>();
	private static Map<String,File> registryES = new HashMap<String,File>();
	public static final String defaultCorpusName = "HANSARD-1999-2002.v2020-11-02";
	public static final String emptyCorpusName = "EMPTYCORPUS";
	
	static {
		try {
			registerCorpus_ES(
					defaultCorpusName,
				new File(
					IUConfig.getIUDataPath(
				"data/compiled-corpuses/HANSARD-1999-2002.v2020-11-02.ES.json")));

		} catch (ConfigException | CompiledCorpusRegistryException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static void registerCorpus(String corpusName, File jsonFile) throws CompiledCorpusRegistryException {
		if (registry.containsKey(corpusName) 
				&&  !registry.get(corpusName).equals(jsonFile) ) {
				throw new CompiledCorpusRegistryException("The name '"+corpusName+"' is already associated with a different compilation file.");
		} else if ( !jsonFile.exists() ) {
			throw new CompiledCorpusRegistryException("The file "+jsonFile.getAbsolutePath()+" does not exist.");
		} else {
			registry.put(corpusName, jsonFile);
		}
	}

	public static void registerCorpus_ES(
		String corpusName, File jsonFile)
		throws CompiledCorpusRegistryException {
		if (registryES.containsKey(corpusName)
			&&  !registryES.get(corpusName).equals(jsonFile) ) {
			throw new CompiledCorpusRegistryException("The name '"+corpusName+"' is already associated with a different compilation file.");
		} else if ( !jsonFile.exists() ) {
			throw new CompiledCorpusRegistryException("The file "+jsonFile.getAbsolutePath()+" does not exist.");
		} else {
			registryES.put(corpusName, jsonFile);
		}

		try {
			CompiledCorpus_ES corpus = new CompiledCorpus_ES(corpusName);
			if (!corpus.isUpToDateWithFile(jsonFile)) {
				corpus.loadFromFile(jsonFile, true);
			}
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}
	}

	@JsonIgnore
	public static CompiledCorpus getCorpusWithName()
		throws CompiledCorpusRegistryException {
		return getCorpusWithName(defaultCorpusName);
	}

	@JsonIgnore
	public static CompiledCorpus getCorpusWithName(String corpusName)
		throws CompiledCorpusRegistryException {
		Logger logger = Logger.getLogger("CompiledCorpusRegistry.getCorpusWithName");
		logger.debug("corpusName= '"+corpusName+"'");
		if (corpusName == null) {
			corpusName = defaultCorpusName;
		}
		if (!registryES.containsKey(corpusName)) {
			throw new CompiledCorpusRegistryException(
				"There is no corpus by the name of "+corpusName);
		}
		File corpusFile = registryES.get(corpusName);
		CompiledCorpus_ES corpus = null;
		try {
			corpus = new CompiledCorpus_ES(corpusName);
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}

		try {
			if (!corpus.isUpToDateWithFile(corpusFile)) {
				// Should load the corpus
				int x = 1;
			}
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}

		return corpus;
	}
	
	private static String scanDataDirForCorpusFile(String corpusName) 
		throws CompiledCorpusRegistryException {
		Logger logger = Logger.getLogger("CompiledCorpusRegistry.scanDataDirForCorpusFile");
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

	private static CompiledCorpus_InMemory makeCorpus(String corpusJsonFPath) throws CompiledCorpusRegistryException  {
		CompiledCorpus_InMemory corpus = null;
		if (! new File(corpusJsonFPath).exists()) {
			throw new CompiledCorpusRegistryException("Did not find the corpus compilation file. Please retrieve it and place it in "+
					corpusJsonFPath+".");
		}
		try {
			corpus = CompiledCorpus_InMemory.createFromJson(corpusJsonFPath);
		} catch (Exception e) {
			throw new CompiledCorpusRegistryException("Could not read compiled corpus from file: "+corpusJsonFPath, e);
		}
		
		return corpus;
	}

	public static Map<String,File> getRegistry() {
		return registry;
	}
	public static Map<String,CompiledCorpus_InMemory> getCorpusCache() {
		return corpusCache;
	}
}
