package ca.pirurvik.iutools.corpus;

import java.io.FileNotFoundException;

import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistryException extends Exception {
	
	
	public CompiledCorpusRegistryException(String mess) {
		super(mess);
	}

	public CompiledCorpusRegistryException(Exception e) {
		super(e);
	}

	public CompiledCorpusRegistryException(String mess, Exception e) {
		super(mess, e);
	}

}
