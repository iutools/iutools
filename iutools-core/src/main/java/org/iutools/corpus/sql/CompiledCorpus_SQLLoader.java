package org.iutools.corpus.sql;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.sql.QueryProcessor;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompiledCorpus_SQLLoader {
	private final int BATCH_SIZE = 500;
	private String corpusName;
	private File jsonFile;
	private Boolean overwrite;
	private Boolean verbose;
	private CompiledCorpus_SQL corpus;
	private QueryProcessor queryProcessor = new QueryProcessor();

	public CompiledCorpus_SQLLoader(File jsonFile, String corpusName,
		Boolean overwrite, Boolean verbose) throws CompiledCorpusException {
		this.corpusName = corpusName;
		this.jsonFile = jsonFile;
		this.overwrite = overwrite;
		this.verbose = verbose;
		this.corpus = new CompiledCorpus_SQL(corpusName);
	}

	public void load() throws CompiledCorpusException {
		loadDbDefinition();
		loadWordInfoTable();
		return;
	}

	private void loadDbDefinition() throws CompiledCorpusException {
		String sql =
			"SET SQL_MODE = \"NO_AUTO_VALUE_ON_ZERO\";\n" +
//			"SET time_zone = \"+00:00\";\n" +
			"\n"
			;
		try {
			queryProcessor.query(sql);
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
	}

	private void loadWordInfoTable() throws CompiledCorpusException {
		try {
			queryProcessor.execStatements(new WordInfoSchema().schemaStatements());
			loadWordInfoRows();
		} catch (SQLException e) {
			throw new CompiledCorpusException(e);
		}
		return;
	}

	private void loadWordInfoRows() throws CompiledCorpusException {
		Logger logger = LogManager.getLogger("org.iutools.corpus.sql.CompiledCorpusSQLLoader.loadWordInfoRows");
		try {
			int totalBatches = countJsonFileBatches(jsonFile);
			ProgressMonitor_Terminal progress =
				new ProgressMonitor_Terminal(totalBatches,
					"Loading corpus file: "+jsonFile);
			progress.refreshEveryNSecs = 1;
			int wordNum = 0;
			ObjectStreamReader reader = new ObjectStreamReader(jsonFile);
			List<WordInfo> wordsBatch = new ArrayList<WordInfo>();
			WordInfo winfo;
			while ((winfo = (WordInfo)reader.readObject()) != null) {
				wordNum++;
				if (verbose) {
					System.out.println("Looking at word #"+wordNum+": "+winfo.word);
				}
				wordsBatch.add(winfo);
				if (wordsBatch.size() == BATCH_SIZE) {
					System.out.println("** ADDING BATCH OF WORDS TO DB");
					progress.stepCompleted();
					wordsBatch = addBatch(wordsBatch);
				}
			}
			if (!wordsBatch.isEmpty()) {
				System.out.println("** ADDING LAST BATCH OF WORDS TO DB");
				wordsBatch = addBatch(wordsBatch);
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new CompiledCorpusException(e);
		}
		return;
	}

	private int countJsonFileBatches(File jsonFile) throws CompiledCorpusException {
		int totalWords = 0;
		ObjectStreamReader reader = null;
		try {
			System.out.println("Counting number of batches in file: "+jsonFile);
			reader = new ObjectStreamReader(jsonFile);
			WordInfo winfo;
			while ((winfo = (WordInfo)reader.readObject()) != null) {
  				totalWords++;
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new CompiledCorpusException(e);
		}

		int totalBatches = totalWords / BATCH_SIZE;
		System.out.println("File contains "+totalBatches+" batches");

		return totalBatches;
	}

	private List<WordInfo> addBatch(List<WordInfo> wordsBatch) throws CompiledCorpusException {
		// We don't need to replace the rows because we assume all words will have
		// been deleted before loading the corpus.
		// This will speed up the loading
		boolean replace = false;
		corpus.putInfo4words(wordsBatch, replace);
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
		wordsBatch = new ArrayList<WordInfo>();
		return wordsBatch;
	}
}
