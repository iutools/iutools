package ca.pirurvik.iutools;

public abstract class CorpusDocument {

	abstract public String getContents() throws Exception;
	abstract public String getId();
}