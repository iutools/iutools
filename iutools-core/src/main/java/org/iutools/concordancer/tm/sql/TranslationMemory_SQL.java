package org.iutools.concordancer.tm.sql;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.dtrc.elasticsearch.ESFactory;
import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.sql.IUWordLengthener;
import org.iutools.corpus.sql.QueryComposer;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.sql.*;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

/**
 * EXPERIIMENTAL: SQL implementation of the TranslationMemory
 */
public class TranslationMemory_SQL extends TranslationMemory {

	AlignmentSchema alignmentSchema = new AlignmentSchema();
	Row2Alignment row2Alignment = new Row2Alignment();
	QueryProcessor queryProcessor = new QueryProcessor();
	PrettyPrinter prettyPrinter = new PrettyPrinter();

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
	public void removeAligmentsFromDoc(String docID) throws CompiledCorpusException {
		String sql =
			"DELETE FROM "+alignmentSchema.tableName+"\n"+
			"WHERE\n"+
			"  `from_doc` = ?;"
			;
		try (ResultSetWrapper rsw = new QueryProcessor().query(sql, docID)) {

		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}

	@Override
	public CloseableIterator<Alignment> search(
		String sourceLang, String[] sourceExprVariants, String targetLang) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TranslationMemory_SQL.searchIter");
		CloseableIterator<Alignment> iter = null;
		String sql =
			"SELECT * FROM "+alignmentSchema.tableName+"\n"+
			"WHERE\n"+
			"  has_word_alignments = true AND\n";
		String sourceTextCol = sourceLang+"_text";
		String[] sourceExprsNormalized = new String[sourceExprVariants.length];
		sql += "  (";
		for (int ii=0; ii < sourceExprVariants.length; ii++) {
			if (ii > 0) {
				sql += "OR";
			}
			sql += "\n";
			sql += "    MATCH(`"+sourceTextCol+"`) AGAINST(?)";
			sourceExprsNormalized[ii] =
				normalizeSearchExpression(sourceExprVariants[ii], sourceLang);
		}
		sql += "\n  )";

		String[] sortCriteria = new String[]{
			// We favor short sentences because the word alignment is more likely to be correct (presumably).
			//
			sourceLang + "_length:asc",
			// We also sort alignments alphabetically to provide a predictable result.
//			sourceTextCol
		};
		try {
			sql += "\n"+QueryComposer.sqlOrderBy(sortCriteria);
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("sourceLang="+sourceLang+
				", sourceExprVariants="+prettyPrinter.pprint(sourceExprVariants)+
				", sourceExprsNormalized="+prettyPrinter.pprint(sourceExprsNormalized));
			logger.trace("sql="+sql);
		}

		sql += "\nLIMIT 1000";

		ResultSetWrapper rsw = null;
		try {
			rsw = new QueryProcessor().query(sql, sourceExprsNormalized);
			if (logger.isTraceEnabled()) {
				logger.trace("returned rsw "+(rsw.isEmpty()?"IS":"is NOT")+" empty");
				logger.trace("sourceExprVariant="+prettyPrinter.pprint(sourceExprVariants));
			}

			return rsw.iterator(new Row2Alignment());
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
	}

	private String normalizeSearchExpression(String searchExpression, String inLang) {
		String normalized = searchExpression;
		if (inLang.equals("iu")) {
			// We need to artificially lengthen short words so they won't be ignored
			// by the SQL MyISAM FULLTEXT search engine.
			//
			normalized = IUWordLengthener.lengthen(searchExpression);
		}
		return normalized;
	}

	@Override
	public void delete() throws TranslationMemoryException {
		return;
	}

	/**
	 * Given a source expression to be searched in a language, "normalize" it
	 * and possibly expand it.
	 */
	private String[] normalizeAndExpandSearchExpression(String expression, String lang) throws TranslationMemoryException {
		String[] modifiedExpressions = null;
		if (lang.equals("iu")) {
			try {
				// For an iu query, we need to expand it to include the word in the
				// both scripts.
				//
				// Also, we need to lengthen short words so they won't be ignored
				// by the MyISAM FULLTEXT search
				modifiedExpressions = new String[2];
				String expOtherScript = TransCoder.inOtherScript(expression);
				modifiedExpressions[0] = IUWordLengthener.lengthen(expression);
				modifiedExpressions[1] = IUWordLengthener.lengthen(expOtherScript);
			} catch (TransCoderException e) {
				throw new TranslationMemoryException(e);
			}
		} else {
			// Nothing special to do for expressions that are in languages other than
			// iu
			modifiedExpressions = new String[] {expression};
		}
		return modifiedExpressions;
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
