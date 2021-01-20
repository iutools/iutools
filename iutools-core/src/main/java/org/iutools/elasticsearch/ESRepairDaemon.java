package org.iutools.elasticsearch;

import ca.nrc.debug.Debug;
import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;

import java.nio.file.Path;
import java.util.*;

/**
 * Run this main() as a background process to monitor and repair
 * the various ElasticSearch indices used by iutools
 */
public class ESRepairDaemon {

	Logger daemonLogger = Logger.getLogger("org.iutools");

	public static void main(String[] args) {
		ESRepairDaemon daemon = new ESRepairDaemon();
		daemon.run();
	}

	private void run() {
		String daemonName = "iutools ElasticSearch index repair daemon";
		daemonLogger.info(daemonName + " STARTED");

		try {
			Set<String> corpora = CompiledCorpusRegistry.availableCorpora();
			while (true) {
				try {
					for (String corpusName : corpora) {
						checkIndices4Corpus(corpusName);
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					daemonLogger.info(daemonName + " STOPPED NORMALLY");
					break;
				}
			}
		} catch (Exception e) {
			daemonLogger.error(
				daemonName + " CRASHED!!!\n"+
				"Exception details:\n"+ Debug.printCallStack(e));
		}
	}

	private void checkIndices4Corpus(String index) throws ElasticSearchException {
		repairCorpusIndex(index);
		repairSpellCheckerIndexForCorpus(index);
	}

	private void repairCorpusIndex(String index) throws ElasticSearchException {
		ESIndexRepair repairMan = new ESIndexRepair(index, daemonLogger);
		Path jsonFile = CompiledCorpusRegistry.jsonFile4corpus(index);

		Iterator<String> corruptedIDs =
		repairMan.corruptedDocIDs(
			CompiledCorpus.WORD_INFO_TYPE, CompiledCorpus.winfoPrototype);
		repairMan.repairCorruptedDocs(corruptedIDs,
			CompiledCorpus.WORD_INFO_TYPE, CompiledCorpus.winfoPrototype,
			jsonFile);
	}

	private void repairSpellCheckerIndexForCorpus(String index) throws ElasticSearchException {
		String spellIndex = index+"_expliclty_correct";

		ESIndexRepair repairMan = new ESIndexRepair(index);

		Iterator<String> corruptedIDs =
		null;
		try {
			corruptedIDs = repairMan.corruptedDocIDs(
				CompiledCorpus.WORD_INFO_TYPE, CompiledCorpus.winfoPrototype);
		} catch (ElasticSearchException e) {
		}

		repairMan.deleteCorruptedDocs(corruptedIDs, CompiledCorpus.WORD_INFO_TYPE);
	}
}
