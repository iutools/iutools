package org.iutools.concordancer.tm.sql;

import ca.nrc.dtrc.elasticsearch.ESFactory;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

/**
 * EXPERIIMENTAL: SQL implementation of the TranslationMemory
 */
public class TranslationMemory_SQL extends TranslationMemory {

	public TranslationMemory_SQL() {
		super();
	}

	public TranslationMemory_SQL(String tmName) {
		super(tmName);
	}

	@Override
	public void loadFile(Path tmFile, ESFactory.ESOptions... options) throws TranslationMemoryException {
		TMLoader loader = new TMLoader(tmFile);
		loader.load();
		return;
	}

	@Override
	public void addAlignment(Alignment alignment) throws TranslationMemoryException {
		return;
	}

	@Override
	public Iterator<Alignment_ES> searchIter(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		return null;
	}

	@Override
	public void delete() throws TranslationMemoryException {
		return;
	}

	@Override
	public List<Alignment_ES> search(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		return null;
	}

	public void putAligments(List<Alignment> alignents, Boolean replace) {
//		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.putAlignments");
//		if (replace == null) {
//			replace = false;
//		}
//		if (alignents != null && alignents.size() > 0) {
//			List<Row> rows = new ArrayList<Row>();
//			for (Alignment anAlignment: alignents) {
//				Alignment_SQL alignSQL = new Alignment_SQL(anAlignment);
//				Row row = winfoSQL.toSQLRow();
//				row.setColumn("corpusName", corpusName);
//				rows.add(row);
//			}
//			try {
//				new QueryProcessor().insertRows(rows, true);
//			} catch (SQLException e) {
//				throw new CompiledCorpusException(e);
//			}
//		}
		return;
	}
}
