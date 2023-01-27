package org.iutools.cli;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import org.iutools.concordancer.Alignment;
import org.iutools.config.IUConfig;
import org.iutools.script.TransCoder;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.utilities.StopWatch;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Temporary main for compiling all the words contained in the TM and adding
 * them to the WordDict.
 * This will NOT include the word's decomp as that will be done later with
 * Eric's thing.
 */
public class DELETEME_CompileWordsFromTM {

	File tmFile = null;
	FrequencyHistogram<String> wordFreqs = new FrequencyHistogram<String>();
	IUTokenizer tokenizer = new IUTokenizer();

	public static void main(String[] args) throws Exception {
		new DELETEME_CompileWordsFromTM().run(args);
	}

	private void run(String[] args) throws Exception {
		compileTMWords();
		addTMWordsToCorpus();
	}

	private void compileTMWords() throws Exception {
		tmFile = new File(
			IUConfig.dataFilePath("data/translation-memories/nrc-nunavut-hansard.tm.json"));

		int totalAlignments = countAlignments();
		ProgressMonitor_Terminal progress =
			new ProgressMonitor_Terminal(totalAlignments,
				"Compiling alignments from TM file: "+tmFile);
		progress.refreshEveryNSecs = 1;
		int alignNum = 0;
		ObjectStreamReader reader = new ObjectStreamReader(tmFile);
		Alignment algnmt = null;
		String currAlignDescr = null;
		StopWatch sw = new StopWatch().start();
		while ((algnmt = (Alignment)reader.readObject()) != null) {
			alignNum++;
			progress.stepCompleted();
			String iuText = algnmt.sentence4lang("iu");
			iuText = TransCoder.ensureRoman(iuText);
			List<String> words = tokenizer.tokenize(iuText);
			for (String word: words) {
				wordFreqs.updateFreq(word);
			}
			if (sw.totalTime(TimeUnit.SECONDS) > 30) {
				printWordFreqs();
				sw.reset();
			}
		}
	}

	private void printWordFreqs() {
		System.out.println("== Stats");
		System.out.println("  Total words      : "+wordFreqs.allValues().size());
		System.out.println("  Total occurences : "+wordFreqs.totalOccurences());
	}

	public int countAlignments() throws Exception {
		int totalAlignments = 0;
		System.out.println("Counting alignments in TM file: "+tmFile+"\n  This may take a few minutes...");
		ObjectStreamReader reader = new ObjectStreamReader(tmFile);
		Alignment algn = null;
		while ((algn = (Alignment)reader.readObject()) != null) {
			totalAlignments++;
		}
		System.out.println("  Total # alignemts: "+totalAlignments);
		return totalAlignments;
	}


	private void addTMWordsToCorpus() {

	}
}
