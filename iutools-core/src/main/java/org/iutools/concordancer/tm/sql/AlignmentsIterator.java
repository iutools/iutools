package org.iutools.concordancer.tm.sql;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.QueryProcessor;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class AlignmentsIterator implements Iterator<Alignment_ES>, Closeable {
	private Connection conn = null;
	private Iterator<SentenceInLang> sourceSentsIter = null;
	private String[] targetLangs = null;
	private SentenceInLangSchema sentsSchema = new SentenceInLangSchema();;
	private QueryProcessor queryProcessor = new QueryProcessor();

	public AlignmentsIterator(Connection conn,
		Iterator<SentenceInLang> sourceSentsIter, String... targetLangs) {
		init__AlignmentsIterator(conn, sourceSentsIter, targetLangs);
	}

	private void init__AlignmentsIterator(Connection conn,
		Iterator<SentenceInLang> sourceSentsIter, String[] targetLangs) {
		this.conn = conn;
		this.sourceSentsIter = sourceSentsIter;
		this.targetLangs = targetLangs;
	}

	@Override
	public void close() throws IOException {
		try {
			conn.close();
		} catch (SQLException e) {
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
			"  pair_num=? AND\n"+
			"  ("
			;
		for (int ii=0; ii < targetLangs.length; ii++) {
			sql += "    lang=?";
			if (ii != targetLangs.length-1) {
				sql += " OR";
			}
			sql += "\n";
		}
		sql += " );";
		Object[] args = new Object[targetLangs.length+2];
		args[0] = sourceSent.from_doc;
		args[1] = sourceSent.pair_num;
		for (int ii=0; ii < targetLangs.length; ii++) {
			args[ii+2] = targetLangs[ii];
		}

		try (ResultSet rs = queryProcessor.query2(sql, args)) {
			List<SentenceInLang> targetSents =
				QueryProcessor.rs2pojoLst(rs, new Sql2SentenceInLang());
			for (SentenceInLang aTargSent: targetSents) {
				align.setSentence(aTargSent.lang, aTargSent.text);
			}
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}
}
