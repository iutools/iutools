package org.iutools.cli;

import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.WordAlignment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Cmd_tmjson2iu_en_portage extends ConsoleCommand {
	private Path tmjsonFile;
	File portageDir = null;
	Path portageRoot = null;
	List<String> topics = null;
	String[] langs = null;
	String walignDir = null;

	FileWriter sentIDsWriter = null;
	FileWriter tokenPairsWriter = null;
	Map<String, FileWriter> sentsWriter4lang =
		new HashMap<String, FileWriter>();
	Map<String, FileWriter> tokensWriter4lang =
		new HashMap<String, FileWriter>();

	String currDoc = null;
	int currSentPair = 0;

	public Cmd_tmjson2iu_en_portage(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Convert an iutools tm.json file to the portage format";
	}


	@Override
	public void execute() throws Exception {
		this.tmjsonFile = getInputFile();
		langs = getLangs(true);
		openAllFiles();
		try {
			convertFile();
		} finally {
			closeAllFiles();
		}
	}

	private void closeAllFiles() {
		for (String lang: sentsWriter4lang.keySet()) {
			try {
				sentsWriter4lang.get(lang).close();
			} catch (IOException e) {}
		}
		for (String lang: tokensWriter4lang.keySet()) {
			try {
				tokensWriter4lang.get(lang).close();
			} catch (IOException e) {}
		}
	}

	private void openAllFiles() {
		try {
			portageDir = getOutputDir();
			portageRoot = portageRoot(tmjsonFile, portageDir);
			FileWriter sentIDsWriter =
				new FileWriter(new File(portageRoot+".id"));
			openTokenPairsWriter();
			for (String lang: langs) {
				openSentenceWriter(lang);
				openTokensWriter(lang);
			}
		} catch (IOException | ConsoleException e) {
			e.printStackTrace();
		}
	}

	private Path portageRoot(Path tmjsonFile, File portageDir) {
		String fileStem = tmjsonFile.toFile().getName().replaceAll("\\.tm\\.json", "");
		Path root = Paths.get(portageDir.toString()+"/"+fileStem);
		return root;
	}

	private void convertFile() throws Exception {
		try (ObjectStreamReader reader =
			  	new ObjectStreamReader(tmjsonFile.toFile())) {
			Alignment_ES alignment = (Alignment_ES) reader.readObject();
			while (alignment != null) {
				writeAlignment(alignment);
				alignment = (Alignment_ES) reader.readObject();
			}

		}
	}

	private void writeAlignment(Alignment_ES alignment) throws ConsoleException {
		writeID(alignment);
		writeSentences(alignment);
		writeTokens(alignment);
		writeTokenPairs(alignment);
	}

	private void writeTokenPairs(Alignment_ES alignment) {
		if (walignDir == null) {


		}
//		alignment.walign4langpair.get()
	}

	private void writeTokens(Alignment_ES alignment) throws ConsoleException {
		for (String lang: langs) {
			WordAlignment wordAlignment = alignment.walign4langpair.get(lang);
			String tokens = String.join(" ", wordAlignment.tokens4lang.get(lang));
			try {
				tokensWriter4lang.get(lang).write(tokens);
			} catch (IOException e) {
				throw new ConsoleException("Could not write to tokens file (lang="+lang+")");
			}
		}
	}

	private void writeSentences(Alignment_ES alignment) throws ConsoleException {
		for (String lang: langs) {
			String sent = alignment.sentence4lang(lang);
			try {
				sentsWriter4lang.get(lang).write(sent);
			} catch (IOException e) {
				throw new ConsoleException("Could not write to sentences file (lang="+lang+")");
			}
		}
	}

	private void writeID(Alignment_ES alignment) throws ConsoleException {
		if (currDoc == null || ! alignment.from_doc.equals(currDoc)) {
			currDoc = alignment.from_doc;
			currSentPair = 0;
		}
		try {
			sentIDsWriter.write(alignment.from_doc+"\t"+alignment.pair_num);
		} catch (IOException e) {
			throw new ConsoleException("Could not write to pair IDs file");
		}
		currSentPair++;
	}


	private void openTokenPairsWriter() throws ConsoleException {
		File tokPairFile = new File(portageDir+"/word.align");
		try {
			tokenPairsWriter =
				new FileWriter(tokPairFile);
		} catch (IOException e) {
			throw new ConsoleException("Could not open word alignment file: "+tokPairFile);
		}
	}

	private void openTokensWriter(String lang) throws ConsoleException {
		File tokensFile = new File(portageRoot.toString() + "." + lang + ".tok");
		try {
			sentsWriter4lang.put(lang, new FileWriter(tokensFile));
		} catch (IOException e) {
			throw new ConsoleException(e);
		}
	}

	private void openSentenceWriter(String lang) throws ConsoleException {
		File sentencesFile = new File(portageRoot.toString() + "." + lang);
		try {
			sentsWriter4lang.put(lang, new FileWriter(sentencesFile));
		} catch (IOException e) {
			throw new ConsoleException(e);
		}
	}
}
