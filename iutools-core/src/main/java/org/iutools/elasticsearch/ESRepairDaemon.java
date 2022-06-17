package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Use this class to run a process that monitor and repair
 * the various ElasticSearch indices used by iutools.
 *
 * There are two ways to launch the process.
 *
 * As a Main()
 * -----------
 *
 * Just invoke the class as a terminal app
 *
 * As a Tomcat app listener
 * ------------------------
 *
 * Add the following lines in the <web-app> section of your web.xml file:
 *
 *		<listener>
 * 		<listener-class>org.iutools.elasticsearch.ESRepairDaemon</listener-class>
 * 	</listener>
 *
 * When Tomcat starts, it will then automatically start the daemon in a separate
 * thread.
 */
public class ESRepairDaemon implements ServletContextListener, Runnable {

	Logger _daemonLogger = null;
	private static Thread thr = null;
	public static final String thrName = "ESRepairDaemon thread";
	ExecutorService executor = null;

	public static void main(String[] args) {
		boolean inSeparateThread = false;
		if (args.length > 1) {
			usage();
		}
		if (args.length > 0 ) {
			inSeparateThread = Boolean.parseBoolean(args[0]);
		}
		start(inSeparateThread);
	}

	private static void usage() {
		System.err.println(
			"Usage: ESRepairDaemon blcking?\n\n"+
			"ARGUMENTS\n"+
			"   blocking(OPTIONAL): If \"false\", starts the daemon in a non-blocking thread."
		);
		System.exit(1);
	}

	public static void start() {
		start(null);
	}

	public static void start(Boolean blocking) {
		if (blocking == null) {
			blocking = true;
		}
		ESRepairDaemon daemon = new ESRepairDaemon();
		if (!blocking) {
			ensureDaemonThreadIsRunning(daemon);
		} else {
			daemon.run();
		}
	}

	private static synchronized void ensureDaemonThreadIsRunning(ESRepairDaemon daemon) {
		boolean started = false;
		if (thr == null) {
				thr = new Thread(daemon, thrName);
				System.out.println("Starting the thread");
				thr.start();
				System.out.println("DONE Starting the thread");
		}
	}

	public void run() {
//		String daemonName = "iutools ElasticSearch index repair daemon";
//		daemonLogger().info(daemonName + " STARTED");
//
//		try {
//			Set<String> corpora = CompiledCorpusRegistry.availableCorpora();
//			int counter = 0;
//			while (true) {
//				counter++;
//				daemonLogger().trace(daemonName + " checking indices for the "+counter+"th time");
//
//				try {
//					for (String corpusName : corpora) {
//						checkIndices4Corpus(corpusName);
//					}
//					Thread.sleep(1000);
//				} catch (InterruptedException e) {
//					daemonLogger().info(daemonName + " STOPPED NORMALLY");
//					break;
//				}
//			}
//		} catch (Exception e) {
//			daemonLogger().error(
//				daemonName + " CRASHED!!!\n"+
//				"Exception details:\n"+ Debug.printCallStack(e));
//		}
	}

	private Logger daemonLogger() {
		_daemonLogger = Logger.getLogger("org.iutools.elasticsearch.ESRepairDaemon");
		if (_daemonLogger.getLevel() == null) {
			_daemonLogger.setLevel(Level.INFO);
		}
		return _daemonLogger;
	}

	private void checkIndices4Corpus(String index) throws ElasticSearchException {
		repairCorpusIndex(index);
		repairSpellCheckerIndexForCorpus(index);
	}

	private void repairCorpusIndex(String index) throws ElasticSearchException {
		ESIndexRepair repairMan = new ESIndexRepair(index, _daemonLogger);
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

		ESIndexRepair repairMan = new ESIndexRepair(spellIndex);

		Iterator<String> corruptedIDs = null;
		try {
			corruptedIDs = repairMan.corruptedDocIDs(
				CompiledCorpus.WORD_INFO_TYPE, CompiledCorpus.winfoPrototype);
		} catch (ElasticSearchException e) {
		}

		repairMan.deleteCorruptedDocs(corruptedIDs, CompiledCorpus.WORD_INFO_TYPE);
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {

	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}
}
