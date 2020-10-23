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
	public static final String defaultCorpusName = "Hansard1999-2002";
	public static final String defaultESCorpusName = "HANSARD-1999-2002.v2020-10-06";
	public static final String emptyCorpusName = "EMPTYCORPUS";
	
	static {
		try {
			String Hansard19992002_compilationFilePath = IUConfig.getIUDataPath("data/compiled-corpuses/compiled-corpus-HANSARD-1999-2002--withWordInfoMap.json");
			registry.put("Hansard1999-2002", new File(Hansard19992002_compilationFilePath));
			registry.put(
				defaultESCorpusName,
				new File(
					IUConfig.getIUDataPath(
					"data/compiled-corpuses/HANSARD-1999-2002.v2020-10-06.ES.json")));
		} catch (ConfigException e) {
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

	@JsonIgnore
	public static CompiledCorpus getCorpusWithName_ES() throws CompiledCorpusRegistryException {
		return getCorpusWithName_ES(defaultESCorpusName);
	}

	@JsonIgnore
	public static CompiledCorpus_InMemory getCorpusWithName() throws CompiledCorpusRegistryException {
		return getCorpusWithName(defaultCorpusName);
	}

	@JsonIgnore
	public static CompiledCorpus getCorpusWithName_ES(String corpusName) throws CompiledCorpusRegistryException {
		Logger logger = Logger.getLogger("CompiledCorpusRegistry.getCorpusWithName");
		logger.debug("corpusName= '"+corpusName+"'");
		if (corpusName == null) {
			corpusName = defaultESCorpusName;
		}
		CompiledCorpus_ES corpus = null;
		try {
			corpus = new CompiledCorpus_ES(corpusName);
		} catch (CompiledCorpusException e) {
			throw new CompiledCorpusRegistryException(e);
		}
		return corpus;
	}

	@JsonIgnore
	public static CompiledCorpus_InMemory getCorpusWithName(String corpusName) throws CompiledCorpusRegistryException {
		Logger logger = Logger.getLogger("CompiledCorpusRegistry.getCorpusWithName");
		logger.debug("corpusName= '"+corpusName+"'");
		if (corpusName == null) {
			corpusName = defaultCorpusName;
		}
		CompiledCorpus_InMemory corpus = null;
		if (corpusName == emptyCorpusName) {
			corpus = new CompiledCorpus_InMemory();
		} else {
			String corpusFile = null;
			if (registry.containsKey(corpusName)) {
				corpusFile = registry.get(corpusName).toString();
			}  else {
				corpusFile = scanDataDirForCorpusFile(corpusName);
			}
			
			if (corpusFile == null) {
				throw new CompiledCorpusRegistryException(
						"Could not find a corpus that matches name "+corpusName);
			}
			logger.debug("building corpus");
			corpus = makeCorpus(corpusFile);
			logger.debug("corpus built");
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

	@JsonIgnore
	public static CompiledCorpus_InMemory getCorpus() throws CompiledCorpusRegistryException {
		return getCorpus(defaultCorpusName);
	}

	@JsonIgnore
	public static CompiledCorpus_InMemory getCorpus(String corpusName) throws CompiledCorpusRegistryException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.corpus.CompiledCorpusRegistry.getCorpus");
		tLogger.trace("corpusName="+corpusName);
		if (corpusName==null) {
			corpusName = defaultCorpusName;
		}
		
		CompiledCorpus_InMemory corpus = null;
		if (corpusName.equals(emptyCorpusName)) {
			corpus = new CompiledCorpus_InMemory();
		} else {
			if (corpusCache.containsKey(corpusName)) {
				// We have already generated a corpus for that name
				corpus = corpusCache.get(corpusName);
			} else {
				// We have NOT generated a corpus for that name.
				// Check if the name corresponds to an actual corpus name
				if (registry.containsKey(corpusName)) {
					// The name is an actual corpus name.
					// Get the file it corresponds to and load it
					String jsonFilePath = registry.get(corpusName).toString();
					tLogger.trace("This is an EXPLICITLY registered name; loading it from associated file: "+jsonFilePath);
					corpus = makeCorpus(jsonFilePath);
					corpusCache.put(corpusName, corpus);
					corpusCache.put(jsonFilePath, corpus);
				} else {
					// The "name" does not correspond to the name of an actual 
					// corpus. See if there is a corpus file or directory that
					// contains that "name"
					//
					String corpusFile = scanDataDirForCorpusFile(corpusName);
					if (corpusFile != null) {
						// Found a file that matches corpusName
						// Load it
						if (corpusCache.containsKey(corpusFile)) {
							corpus = corpusCache.get(corpusFile);
						} else {
							corpus = makeCorpus(corpusFile);
							corpusCache.put(corpusFile, corpus);
						}
					} else {
						throw new CompiledCorpusRegistryException("Unknown corpus name: "+corpusName);
					}
				}
			}
		}
		
		return corpus;
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
