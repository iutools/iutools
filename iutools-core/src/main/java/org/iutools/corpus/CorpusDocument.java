package org.iutools.corpus;

import java.io.BufferedReader;

public abstract class CorpusDocument {

	abstract public String getContents() throws Exception;
	abstract public BufferedReader contentsReader() throws CorpusDocumentException;
	abstract public String getId();
}
