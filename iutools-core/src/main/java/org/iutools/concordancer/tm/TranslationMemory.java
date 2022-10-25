package org.iutools.concordancer.tm;

import static ca.nrc.dtrc.elasticsearch.ESFactory.*;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.sql.CloseableIterator;

import java.nio.file.Path;
import java.util.List;


/**
 * A basic Translation Memory that uses ElasticSearch
 */
public abstract class TranslationMemory {

	public abstract void loadFile(Path tmFile, ESOptions... options)
		throws TranslationMemoryException;
	public abstract void addAlignment(Alignment alignment) throws TranslationMemoryException;
	public abstract CloseableIterator<Alignment_ES> searchIter(
		String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException;
	public abstract void delete() throws TranslationMemoryException;
	public abstract List<Alignment_ES> search(String sourceLang, String sourceExpr,
		String... targetLangs) throws TranslationMemoryException;


	public static final String DEFAULT_TM_NAME = "iutools_tm";

	protected UserIO userIO = new UserIO().setVerbosity(UserIO.Verbosity.Level0);

	protected String tmName = DEFAULT_TM_NAME;

	public TranslationMemory() {
		init_TranslationMemory(DEFAULT_TM_NAME);
	}

	public TranslationMemory(String _tmName) {
		init_TranslationMemory(_tmName);
	}

	private void init_TranslationMemory(String _tmName) {
		if (_tmName != null) {
			this.tmName = _tmName;
		}
		return;
	}

	public void loadFile(Path tmFile) throws TranslationMemoryException {
		loadFile(tmFile, new ESOptions[0]);
	}

	public TranslationMemory setUserIO(UserIO _userIO) {
		this.userIO = _userIO;
		return this;
	}
}
