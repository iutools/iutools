package org.iutools.concordancer.tm.sql;

import ca.nrc.dtrc.elasticsearch.ESFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.*;
import org.json.JSONObject;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIIMENTAL: SQL implementation of the TranslationMemory
 */
public class TranslationMemory_SQL extends TranslationMemory {

	SentenceInLangSchema sentenceSchema = new SentenceInLangSchema();
	Sql2SentenceInLang sql2sent = new Sql2SentenceInLang();

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
	public CloseableIterator<Alignment_ES> searchIter(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.searchIter");
		AlignmentsIterator iter = null;
		// Note: We DON'T use try-with because the returned iterator will need
		// to have the SQL resources still opened.
		CloseableIterator<SentenceInLang> sourceSentsIter = null;
		try {
			sourceSentsIter =
				searchSourceLang(sourceLang, sourceExpr);
			iter = new AlignmentsIterator(sourceSentsIter, targetLangs);
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}

		if (logger.isTraceEnabled()) {
			logger.trace("For sourceExpr='"+sourceExpr+"', returning iter with hasNext()="+iter.hasNext());
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
		try (CloseableIterator<SentenceInLang> sourceSentsIter =
			  searchSourceLang(sourceLang, sourceExpr)) {
			AlignmentsIterator alignIter =
				new AlignmentsIterator(sourceSentsIter, targetLangs);
			while (alignIter.hasNext()) {
				Alignment_ES nextAlign = alignIter.next();
				hits.add(nextAlign);
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
		return hits;
	}

	private CloseableIterator<SentenceInLang> searchSourceLang(
		String sourceLang, String sourceExpr) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.searchSourceLang");
		SentenceInLangSchema schema = new SentenceInLangSchema();
		String sql =
			"SELECT * FROM "+schema.tableName+"\n"+
			"WHERE\n"+
			"  lang = ? AND\n"+
			"  MATCH(text) AGAINST(?);";

		logger.trace(
			"Executing sql="+sql+"\n"+
			"  With arguments: sourceLang="+sourceLang+", sourceExpr="+sourceExpr);
		CloseableIterator<SentenceInLang> iter = null;
		ResultSetWrapper rsw  = null;
		try {
			rsw = new QueryProcessor().query(sql, sourceLang, sourceExpr);
			iter = rsw.iterator(new Sql2SentenceInLang());
		} catch (SQLException e) {
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
			List<JSONObject> rows = new ArrayList<JSONObject>();
			for (Alignment anAlignment: alignents) {
				sentenceLenghtsDoNotExceedSchemaMaximum(anAlignment);
				for (String lang: anAlignment.languages()) {
					addRow4Lang(lang, anAlignment, rows);
				}
			}
			try {
				new QueryProcessor().insertRows(rows, sql2sent, true);
			} catch (SQLException e) {
				throw new TranslationMemoryException(e);
			}
		}
		return;
	}

	private boolean sentenceLenghtsDoNotExceedSchemaMaximum(Alignment anAlignment) {
		boolean answer = true;
		for (String lang: anAlignment.languages()) {
			if (anAlignment.sentence4lang(lang).length() > SentenceInLangSchema.MAX_SENTENCE_LEN) {
				answer = false;
				break;
			}
		}
		return answer;
	}

	private void addRow4Lang(String lang, Alignment anAlignment, List<JSONObject> rows) throws TranslationMemoryException {
		SentenceInLang sent =
			new SentenceInLang(lang, anAlignment.sentence4lang(lang),
				anAlignment.from_doc, anAlignment.pair_num);
		try {
			JSONObject row = sql2sent.toRowJson(sent);
			if (sentenceSchema.rowIsCompatible(row)) {
				rows.add(row);
			}
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}
}
