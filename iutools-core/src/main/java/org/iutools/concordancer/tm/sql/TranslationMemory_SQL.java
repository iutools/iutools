package org.iutools.concordancer.tm.sql;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.dtrc.elasticsearch.ESFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.sql.*;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * EXPERIIMENTAL: SQL implementation of the TranslationMemory
 */
public class TranslationMemory_SQL extends TranslationMemory {

	AlignmentSchema alignmentSchema = new AlignmentSchema();
	Row2Alignment row2Alignment = new Row2Alignment();
	QueryProcessor queryProcessor = new QueryProcessor();

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
		CloseableIterator<Alignment> iter = null;
		String sql =
			"SELECT * FROM "+alignmentSchema.tableName+"\n"+
			"WHERE\n";
		String sourceTextCol = sourceLang+"_text";
		sql += "  MATCH(`"+sourceTextCol+"`) AGAINST(?)";
		ResultSetWrapper rsw = null;
		try {
			rsw = new QueryProcessor().query(sql, sourceExpr);
			return rsw.iterator(new Row2Alignment());
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
	}

	@Override
	public void delete() throws TranslationMemoryException {
		return;
	}

	@Override
	public List<Alignment> search(String sourceLang, String sourceExpr, String... targetLangs) throws TranslationMemoryException {
		List<Alignment> hits = new ArrayList<Alignment>();
		ensureLangIsSupported(sourceLang);
		String[] sourceExpressions = new String[] {sourceExpr};
		String textColName = sourceLang+"_text";
		if (sourceLang.equals("iu")) {
			try {
				sourceExpressions = new String[] {
					sourceExpr, TransCoder.inOtherScript(sourceExpr)
				};
			} catch (TransCoderException e) {
				// If there is a problem converting to the other script, don't worrry
				// about it.
			}
		}
		String sql =
			"SELECT * FROM "+alignmentSchema.tableName+"\n"+
			"WHERE\n"+
			"  (";
		boolean isFirst = true;
		for (String aSourceExpr: sourceExpressions) {
			if (!isFirst) {
				sql += " OR\n";
			}
			sql += "    MATCH(`"+textColName+"`) AGAINST(?)";
			isFirst = false;
		}
		sql += "\n  )\n";
		ResultSetWrapper rsw = null;
		try {
			rsw = queryProcessor.query(sql, sourceExpressions);
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		try (CloseableIterator<Alignment> iter = rsw.iterator(row2Alignment)) {
			while (iter.hasNext()) {
				hits.add(iter.next());
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
		return hits;
	}

	private void ensureLangIsSupported(String lang) throws TranslationMemoryException {
		if (!lang.matches("^(en|iu)$")) {
			throw new TranslationMemoryException("Unsupported language: "+lang);
		}
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
