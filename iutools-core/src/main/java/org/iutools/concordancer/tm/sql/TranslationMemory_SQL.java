package org.iutools.concordancer.tm.sql;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.dtrc.elasticsearch.ESFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.*;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIIMENTAL: SQL implementation of the TranslationMemory
 */
public class TranslationMemory_SQL extends TranslationMemory {

	SentenceInLangSchema sentenceSchema = new SentenceInLangSchema();
	Row2Alignment sql2alignment = new Row2Alignment();

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
	public CloseableIterator<Alignment> searchIter(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.searchIter");
		AlignmentsIterator iter = null;
//		// Note: We DON'T use try-with because the returned iterator will need
//		// to have the SQL resources still opened.
//		CloseableIterator<SentenceInLang> sourceSentsIter = null;
//		try {
//			sourceSentsIter =
//				searchSourceLang(sourceLang, sourceExpr);
//			iter = new AlignmentsIterator(sourceSentsIter, targetLangs);
//		} catch (Exception e) {
//			throw new TranslationMemoryException(e);
//		}
//
//		if (logger.isTraceEnabled()) {
//			logger.trace("For sourceExpr='"+sourceExpr+"', returning iter with hasNext()="+iter.hasNext());
//		}

		return iter;
	}

	@Override
	public void delete() throws TranslationMemoryException {
		return;
	}

	@Override
	public List<Alignment> search(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		List<Alignment> hits = new ArrayList<Alignment>();
//		try (CloseableIterator<SentenceInLang> sourceSentsIter =
//			  searchSourceLang(sourceLang, sourceExpr)) {
//			AlignmentsIterator alignIter =
//				new AlignmentsIterator(sourceSentsIter, targetLangs);
//			while (alignIter.hasNext()) {
//				Alignment nextAlign = alignIter.next();
//				hits.add(nextAlign);
//			}
//		} catch (Exception e) {
//			throw new TranslationMemoryException(e);
//		}
		return hits;
	}

	public void putAligments(List<Alignment> alignents, Boolean replace) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.putAlignments");
		if (replace == null) {
			replace = false;
		}
		if (alignents != null && alignents.size() > 0) {
			try {
				new QueryProcessor().insertObjects(alignents, new Row2Alignment(), true);
			} catch (SQLException e) {
				throw new TranslationMemoryException(e);
			}
		}
		return;
	}
}
