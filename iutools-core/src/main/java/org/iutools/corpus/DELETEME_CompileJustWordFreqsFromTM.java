package org.iutools.corpus;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.dtrc.stats.FrequencyHistogram;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.concordancer.tm.sql.TMLoader;
import org.iutools.config.IUConfig;
import org.iutools.script.TransCoder;
import org.iutools.text.segmentation.IUTokenizer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a temporary main() class developed to add all words from the NRC
 * aligned hansard, to the CompiledCorpus.
 *
 * Reason for this:
 * - The current corpus only contains the first two years worth of the Hansard
 * - It would be good to have the full Hansard in there.
 * - For now however, we don't want to compute the Decomps for all those words
 *   because it is very time consuming.
 *   - We just want to add the word with its frequency to the corpus, and leave its
 *     decomp at null.
 */
public class DELETEME_CompileJustWordFreqsFromTM {

	static Path tmFile = null;
	private static IUTokenizer tokenizer = null;
	private static FrequencyHistogram<String> histogram =
		new FrequencyHistogram<String>();

	public static void main(String[] args) throws Exception {
		tmFile = Paths.get(IUConfig.getIUDataPath("data/translation-memories/nrc-nunavut-hansard.tm.json"));
		try {
			int totalBatches = new TMLoader(tmFile).countBatches();
			ProgressMonitor_Terminal progress =
				new ProgressMonitor_Terminal(totalBatches,
					"Adding words from alignments from TM file: " + tmFile);
			progress.refreshEveryNSecs = 1;
			int alignNum = 0;
			ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile());
			List<Alignment> alignsBatch = new ArrayList<Alignment>();
			Alignment algnmt;
			while ((algnmt = (Alignment) reader.readObject()) != null) {
				alignNum++;
				addWordsFromAlignment(algnmt);
			}
		} catch (TranslationMemoryException e) {
			e.printStackTrace();
		}

		return;
	}

	private static void addWordsFromAlignment(Alignment algnmt) {
		String sent = algnmt.sentence4lang("iu");
		sent = TransCoder.ensureSyllabic(sent);
		List<String> words = tokenizer.tokenize(sent);
		for (String aWord: words) {
			histogram.updateFreq(aWord);
			System.out.println("Freq of "+aWord+" is now: "+histogram.frequency(aWord));
		}
	}
}
