package org.iutools.concordancer.tm.sql;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
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
//	protected static int BATCH_SIZE = 1;
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
			new QueryProcessor().ensureTableIsDefined(new SentenceInLangSchema());
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
			List<Alignment> alignsBatch = new ArrayList<Alignment>();
			Alignment algnmt;
			while ((algnmt = (Alignment)reader.readObject()) != null) {
				alignNum++;
				if (verbose) {
					System.out.println("Looking at alignment #"+alignNum+": "+algnmt.getIdWithoutType());
				}
				alignsBatch.add(algnmt);
				if (alignsBatch.size() == BATCH_SIZE) {
					System.out.println("** ADDING BATCH OF ALIGNMENTS TO DB");
					progress.stepCompleted();
					alignsBatch = new ArrayList<Alignment>();
				}
			}
			if (!alignsBatch.isEmpty()) {
				System.out.println("** ADDING LAST BATCH OF WORDS TO DB");
				addBatch(alignsBatch);
				alignsBatch.clear();
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new TranslationMemoryException(e);
		}
		return;
	}

	private List<Alignment>  addBatch(List<Alignment> alignsBatch) throws TranslationMemoryException {
		// We don't need to replace the rows because we assume all alignments will have
		// been deleted before loading the corpus.
		// This will speed up the loading
		boolean replace = false;
		tm.putAligments(alignsBatch, replace);
		try {
			// For some reason, if we don's sleep after each batch, we eventually
			// end up with error:
			//
			//   java.lang.OutOfMemoryError: GC overhead limit exceeded
			//
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return alignsBatch;
	}

	public int countBatches() throws TranslationMemoryException {
		int totalAlignments = countAlignments();
		int totalBatches = totalAlignments / BATCH_SIZE;
		System.out.println("Total batches: "+totalBatches);
		return totalBatches;
	}

	public int countAlignments() throws TranslationMemoryException {
		int totalAlignments = 0;
		try {
			System.out.println("Counting alignments in TM file: "+tmFile);
			ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile());
			Alignment_ES algn = null;
			while ((algn = (Alignment_ES)reader.readObject()) != null) {
				totalAlignments++;
			}
			System.out.println("  Total # alignemts: "+totalAlignments);
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new TranslationMemoryException(e);
		}
		return totalAlignments;
	}

}
