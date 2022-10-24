package org.iutools.concordancer.tm.sql;

import ca.nrc.dtrc.elasticsearch.ESFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.*;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

		AlignmentsIterator iter = null;
		try(Connection conn = new ConnectionPool().getConnection()) {
			Iterator<SentenceInLang> sourceSentsIter = null;
			sourceSentsIter = searchSourceLang(sourceLang, sourceExpr, conn);
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}

		return iter;
	}

	@Override
	public void delete() throws TranslationMemoryException {
		return;
	}

	@Override
	public List<Alignment_ES> search(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		List<Alignment_ES> hits = new ArrayList<Alignment_ES>();
		try (Connection conn = new ConnectionPool().getConnection()) {
			Iterator<SentenceInLang> sourceSentsIter = searchSourceLang(sourceLang, sourceExpr, conn);
			AlignmentsIterator alignIter = new AlignmentsIterator(conn, sourceSentsIter, targetLangs);
			while (alignIter.hasNext()) {
				Alignment_ES nextAlign = alignIter.next();
				hits.add(nextAlign);
			}
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		return hits;
	}

	private Iterator<SentenceInLang> searchSourceLang(String sourceLang, String sourceExpr, Connection conn) throws TranslationMemoryException {
		SentenceInLangSchema schema = new SentenceInLangSchema();
		String sql =
			"SELECT * FROM "+schema.tableName+"\n"+
			"WHERE\n"+
			"  lang = ? AND\n"+
			"  MATCH(text) AGAINST(?);";
		Iterator<SentenceInLang> iter = null;
		try (ResultSetWrapper rsw  =
				new QueryProcessor().query3(sql, sourceLang, sourceExpr)) {
			iter = rsw.iterator(new Sql2SentenceInLang());
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
		return iter;
	}

	public void putAligments(List<Alignment> alignents, Boolean replace) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.putAlignments");
		if (replace == null) {
			replace = false;
		}
		if (alignents != null && alignents.size() > 0) {
			List<Row> rows = new ArrayList<Row>();
			for (Alignment anAlignment: alignents) {
				for (String lang: anAlignment.languages()) {
					addRow4Lang(lang, anAlignment, rows);
				}
			}
			try {
				new QueryProcessor().insertRows(rows, true);
			} catch (SQLException e) {
				throw new TranslationMemoryException(e);
			}
		}
		return;
	}

	private void addRow4Lang(String lang, Alignment anAlignment, List<Row> rows) throws TranslationMemoryException {
		SentenceInLang sent =
			new SentenceInLang(lang, anAlignment.sentence4lang(lang),
				anAlignment.from_doc, anAlignment.pair_num);
		try {
			Row row = sent.toRow();
			rows.add(row);
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
	}
}
