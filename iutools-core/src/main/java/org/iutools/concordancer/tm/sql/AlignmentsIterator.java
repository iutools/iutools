package org.iutools.concordancer.tm.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TranslationMemoryException;
import ca.nrc.datastructure.CloseableIterator;
import org.iutools.sql.QueryProcessor;
import org.iutools.sql.ResultSetWrapper;

import java.io.IOException;
import java.util.List;

public class AlignmentsIterator implements CloseableIterator<Alignment> {
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
	public Alignment next() {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.AlignmentsIterator.next");
		Alignment nextAlign = null;
//		SentenceInLang nextSourceSent = sourceSentsIter.next();
//		logger.trace("next source sent ID: "+nextSourceSent.sentence_id);
//		Alignment_SQL nextAlign =
//			new Alignment_SQL(nextSourceSent.from_doc, (String)null,
//				(List)null, nextSourceSent.pair_num);
//		nextAlign.from_doc = nextSourceSent.from_doc;
//		nextAlign.setSentence(nextSourceSent.lang, nextSourceSent.text);
//		try {
//			fillTargetLangSentences(nextAlign, nextSourceSent);
//		} catch (TranslationMemoryException e) {
//			throw new RuntimeException(e);
//		}

		return nextAlign;
	}
}
