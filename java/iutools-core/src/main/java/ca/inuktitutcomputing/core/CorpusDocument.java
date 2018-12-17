package ca.inuktitutcomputing.core;

import java.io.IOException;
import java.net.MalformedURLException;

public abstract class CorpusDocument {

	abstract public String getContents() throws Exception;
	abstract public String getId();
}
