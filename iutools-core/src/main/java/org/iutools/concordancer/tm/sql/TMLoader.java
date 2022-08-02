package org.iutools.concordancer.tm.sql;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.sql.QueryProcessor;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TMLoader {
	protected static int BATCH_SIZE = 500;
	Path tmFile = null;
	String tmName = null;
	boolean verbose = true;

	TranslationMemory_SQL tm = null;


	private QueryProcessor queryProcessor = new QueryProcessor();


	public TMLoader(Path _tmFile) {
		init_TMLoader(_tmFile, (String)null, (Boolean)null);
	}

	public TMLoader(Path _tmFile, String _tmName) {
		init_TMLoader(_tmFile, _tmName, (Boolean)null);
	}

	private void init_TMLoader(Path _tmFile, String _tmName, Boolean _verbose) {
		tmFile = _tmFile;
		tmName = _tmName;
		if (_verbose != null) {
			verbose = _verbose;
		}
		tm = new TranslationMemory_SQL(tmName);
	}

	public void load() throws TranslationMemoryException {
		ensureAlignmentTableIsDefined();
		loadAlignmentTable();
		return;
	}

	private void ensureAlignmentTableIsDefined() throws TranslationMemoryException {
		try {
			new QueryProcessor().ensureTableIsDefined(new AlignmentSchema());
		} catch (SQLException e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}

	private void loadAlignmentTable() throws TranslationMemoryException {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.tm.sql.TMLoader.loadAlignmentTable");
		try {
			int totalBatches = countBatches();
			ProgressMonitor_Terminal progress =
				new ProgressMonitor_Terminal(totalBatches,
					"Loading alignments from TM file: "+tmFile);
			progress.refreshEveryNSecs = 1;
			int alignNum = 0;
			ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile());
			List<Alignment_ES> alignsBatch = new ArrayList<Alignment_ES>();
			Alignment_ES algnmt;
			while ((algnmt = (Alignment_ES)reader.readObject()) != null) {
				alignNum++;
				if (verbose) {
					System.out.println("Looking at alignment #"+alignNum+": "+algnmt.getIdWithoutType());
				}
				alignsBatch.add(algnmt);
				if (alignsBatch.size() == BATCH_SIZE) {
					System.out.println("** ADDING BATCH OF ALIGNMENTS TO DB");
					progress.stepCompleted();
					alignsBatch = addBatch(alignsBatch);
				}
			}
			if (!alignsBatch.isEmpty()) {
				System.out.println("** ADDING LAST BATCH OF WORDS TO DB");
				alignsBatch = addBatch(alignsBatch);
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}

	private List<Alignment_ES> addBatch(List<Alignment_ES> alignsBatch) {
//		// We don't need to replace the rows because we assume all alignments will have
//		// been deleted before loading the corpus.
//		// This will speed up the loading
//		boolean replace = false;
//		tm.putAligments(alignsBatch, replace);
//		try {
//			// For some reason, if we don's sleep after each batch, we eventually
//			// end up with error:
//			//
//			//   java.lang.OutOfMemoryError: GC overhead limit exceeded
//			//
//			Thread.sleep(2*1000);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//		wordsBatch = new ArrayList<WordInfo>();
//		return wordsBatch;
//
		return null;
	}


//	int totalBatches = countBatches();
//		ProgressMonitor_Terminal progress = new ProgressMonitor_Terminal(totalBatches,
//			"Loading alignments from TM file: "+tmFile);
//		try (ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile())) {
//			while ()
//		} catch (IOException e) {
//			throw new TranslationMemoryException(e);
//		}
//	}

	private int countBatches() throws TranslationMemoryException {
		int totalWords = 0;
		try {
			System.out.println("Counting batches in TM file: "+tmFile);
			ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile());
			Alignment_ES algn = null;
			while ((algn = (Alignment_ES)reader.readObject()) != null) {
				totalWords++;
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new TranslationMemoryException(e);
		}
		int totalBatches = totalWords / BATCH_SIZE;
		System.out.println("Total batches: "+totalBatches);
		return totalBatches;
	}

}
