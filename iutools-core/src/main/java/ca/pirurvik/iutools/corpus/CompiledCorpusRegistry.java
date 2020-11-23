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
	
	private static Map<String,File> registry = new HashMap<String,File>();
	public static final String defaultCorpusName = "HANSARD-1999-2002";
	public static final String emptyCorpusName = "EMPTYCORPUS";
	
	static {
		try {
			registerCorpus_ES(
					defaultCorpusName,
				new File(
					IUConfig.getIUDataPath(
				"data/compiled-corpuses/HANSARD-1999-2002.json")));

		} catch (ConfigException | CompiledCorpusRegistryException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	public static void registerCorpus_ES(
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

		try {
			CompiledCorpus corpus = new CompiledCorpus(corpusName);
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
		if (!registry.containsKey(corpusName)) {
			throw new CompiledCorpusRegistryException(
				"There is no corpus by the name of "+corpusName);
		}
		File corpusFile = registry.get(corpusName);
		CompiledCorpus corpus = null;
		corpus = new CompiledCorpus(corpusName);

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
}
