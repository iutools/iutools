package org.iutools.concordancer.tm.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.CloseableIterator;
import org.iutools.sql.QueryProcessor;
import org.iutools.sql.ResultSetWrapper;

import java.io.IOException;
import java.util.List;

public class AlignmentsIterator implements CloseableIterator<Alignment_ES> {
	private CloseableIterator<SentenceInLang> sourceSentsIter = null;
	private String[] targetLangs = null;
	private SentenceInLangSchema sentsSchema = new SentenceInLangSchema();;
	private QueryProcessor queryProcessor = new QueryProcessor();

	public AlignmentsIterator(
		CloseableIterator<SentenceInLang> sourceSentsIter, String... targetLangs) {
		init__AlignmentsIterator(sourceSentsIter, targetLangs);
	}

	private void init__AlignmentsIterator(
		CloseableIterator<SentenceInLang> sourceSentsIter, String[] targetLangs) {
		this.sourceSentsIter = sourceSentsIter;
		this.targetLangs = targetLangs;
	}

	@Override
	public void close() throws IOException {
		try {
			sourceSentsIter.close();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return sourceSentsIter.hasNext();
	}

	@Override
	public Alignment_ES next() {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.AlignmentsIterator.next");
		SentenceInLang nextSourceSent = sourceSentsIter.next();
		logger.trace("next source sent ID: "+nextSourceSent.sentence_id);
		Alignment_SQL nextAlign =
			new Alignment_SQL(nextSourceSent.from_doc, (String)null,
				(List)null, nextSourceSent.pair_num);
		nextAlign.from_doc = nextSourceSent.from_doc;
		nextAlign.setSentence(nextSourceSent.lang, nextSourceSent.text);
		try {
			fillTargetLangSentences(nextAlign, nextSourceSent);
		} catch (TranslationMemoryException e) {
			throw new RuntimeException(e);
		}

		return nextAlign;
	}

	private void fillTargetLangSentences(Alignment align,
		SentenceInLang sourceSent) throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.AlignmentsIterator.fillTargetLangSentences");
		logger.trace("Filling target lang sentences for sentence_id="+align.getIdWithoutType());
		String sql =
			"SELECT * FROM "+sentsSchema.tableName+"\n"+
			"WHERE\n"+
			"  from_doc=? AND\n"+
			"  pair_num=?"
			;
		if (targetLangs != null && targetLangs.length > 0) {
			sql += " AND\n  (";
			for (int ii = 0; ii < targetLangs.length; ii++) {
				sql += "    lang=?";
				if (ii != targetLangs.length - 1) {
					sql += " OR";
				}
				sql += "\n";
			}
			sql += " )";
		}
		sql += ";";
		Object[] args = new Object[targetLangs.length+2];
		args[0] = sourceSent.from_doc;
		args[1] = sourceSent.pair_num;
		for (int ii=0; ii < targetLangs.length; ii++) {
			args[ii+2] = targetLangs[ii];
		}

		try (ResultSetWrapper rsw = queryProcessor.query3(sql, args)) {
			List<SentenceInLang> targetSents =
				rsw.toPojoLst(new Sql2SentenceInLang());
			for (SentenceInLang aTargSent: targetSents) {
				align.setSentence(aTargSent.lang, aTargSent.text);
			}
		} catch (Exception e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}
}
