package org.iutools.corpus;

import org.iutools.config.IUConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

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

	public static void main(String[] args) throws Exception {
		tmFile = Paths.get(IUConfig.getIUDataPath("data/translation-memories/nrc-nunavut-hansard.tm.json"));
//		try {
//			int totalBatches = new TMLoader().countBatches();
//			ProgressMonitor_Terminal progress =
//				new ProgressMonitor_Terminal(totalBatches,
//					"Loading alignments from TM file: "+tmFile);
//			progress.refreshEveryNSecs = 1;
//			int alignNum = 0;
//			ObjectStreamReader reader = new ObjectStreamReader(tmFile.toFile());
//			List<Alignment> alignsBatch = new ArrayList<Alignment>();
//			Alignment algnmt;
//			while ((algnmt = (Alignment)reader.readObject()) != null) {
//				alignNum++;
//			}
//
	}
}
