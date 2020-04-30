package ca.pirurvik.iutools.corpus;

public abstract class CorpusDocument {

	abstract public String getContents() throws Exception;
	abstract public String getId();
}
