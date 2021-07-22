package org.iutools.cli;

import ca.nrc.data.file.FileGlob;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.CommandLineException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.Alignment_ES;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Cmd_portage2tmjson extends ConsoleCommand {
	File inputDir = null;
	Path outputFile = null;
	Path inputRoot = null;
	List<String> topics = null;
	Set<String> langs = new HashSet<String>();
	Map<String, BufferedReader> sentsreader4lang =
		new HashMap<String, BufferedReader>();
	Map<String, BufferedReader> tokensreader4lang =
		new HashMap<String, BufferedReader>();
	BufferedReader sentIDsReader = null;
	BufferedReader walignReader = null;
	FileWriter tmjsonFileWriter = null;

	static Set<String> ignoreFields = new HashSet<String>();
	static {
		Collections.addAll(ignoreFields,
			new String[]{
				"_detect_language", "additionalFields", "content",
				"creationDate", "id", "lang", "shortDescription"});
	}


	@Override
	public String getUsageOverview() {
		return "Convert alignments from Portage format to iutools .tm.json format";
	}

	public Cmd_portage2tmjson(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		inputDir = getInputDir();
		outputFile = getOutputFile();
		topics = getTopics(true);
		openAllFiles();
		processAllSentences();
		closeAllFiles();
	}

	private void processAllSentences() throws ConsoleException {
		Pair<String,Integer> sentInfo = readSentenceInfo();
		while (sentInfo != null) {
			Alignment_ES alignment =
				initAlignment(sentInfo.getLeft(), sentInfo.getRight());
			addSentences(alignment);
			addWordAlignments(alignment);
			printAlignment(alignment);
			sentInfo = readSentenceInfo();
		}

		return;
	}

	private void addSentences(Alignment_ES alignment) throws ConsoleException {
		for (String lang: langs) {
			try {
				String sent = sentsreader4lang.get(lang).readLine();
				if (sent == null) {
					throw new ConsoleException(
						"Prematurely reached end of sentences file for langauge "+lang);
				}
				alignment.setSentence(lang, sent);
			} catch (IOException e) {
				throw new ConsoleException(
					"Exception raised while reading sentences file for lang "+lang,
					e);
			}
		}
	}

	private void addWordAlignments(Alignment_ES alignment) throws ConsoleException {
		String walign = null;
		try {
			walign = walignReader.readLine();
		} catch (IOException e) {
			throw new ConsoleException(
				"Exception raised while reading word align file", e);
		}
		String enTokens = null;
		try {
			enTokens = tokensreader4lang.get("en").readLine();
		} catch (Exception e) {
			throw new ConsoleException(
				"Exception raised while reading en token file", e);
		}
		String iuTokens = null;
		try {
			iuTokens = tokensreader4lang.get("iu").readLine();
		} catch (Exception e) {
			throw new ConsoleException(
				"Exception raised while reading iu token file", e);
		}
		alignment.setWordAlignment("en", enTokens, "iu", iuTokens, walign);
		return;
	}

	private Alignment_ES initAlignment(String docID, Integer sentNum) {
		Alignment_ES alignment =
			new Alignment_ES(docID, sentNum)
				.setTopics(topics)
				.setWebDomain(webDomain(docID));
		return alignment;
	}

	private String webDomain(String docID) {
		String domain = null;
		try {
			URL url = new URL(docID);
		} catch (Exception e) {
			// Nothing to do. It just means the docID was not a URL
			// So there is no web domain for it.
		}
		return domain;
	}

	private void printAlignment(Alignment_ES alignment) throws ConsoleException {
		String json = PrettyPrinter.print(alignment, ignoreFields);
		try {
			tmjsonFileWriter.write(json+"\n\n");
		} catch (IOException e) {
			throw new ConsoleException("Could not write to tmjson file: "+outputFile,
				e);
		}
	}

	private Pair<String, Integer> readSentenceInfo() throws ConsoleException {
		Pair<String,Integer> info = null;
		try {
			String idLine = sentIDsReader.readLine();
			if (idLine != null) {
				String docID = null;
				Integer sentNum = null;
				String[] lineFields = idLine.split("\\s+");
				if (lineFields.length == 2) {
					docID = lineFields[0];
					try {
						sentNum = Integer.parseInt(lineFields[1]);
					} catch (Exception e) {
						throw new ConsoleException(
						"2nd field of sentence ID line was not an integer:\n" + idLine);
					}
					info = Pair.of(docID, sentNum);
				} else {
					throw new ConsoleException(
					"Sentence ID line did not have 2 fields:\n" + idLine);
				}
			}
		} catch (IOException e) {
			throw new ConsoleException(
				"Could not read sentence Id from file "+sentsIDFile(), e);
		}

		return info;
	}

	private void openAllFiles() throws ConsoleException {
		for (File aFile : FileGlob.listFiles(inputDir.toString() + "/*.*")) {
			String ext = FilenameUtils.getExtension(aFile.toString());
			if (!ext.matches("(en|iu|fr)")) {
				continue;
			}
			// Strip extenstion from path
			langs.add(ext);
			inputRoot = Paths.get(FilenameUtils.removeExtension(aFile.toString()));
		}

		for (String lang : langs) {
			Path sentsFile = sentsFilePath(lang);
			try {
				sentsreader4lang.put(lang,
					new BufferedReader(new FileReader(sentsFile.toFile())));
			} catch (FileNotFoundException e) {
				throw new ConsoleException(
					"Could not open sentence file for reading: "+sentsFile, e);
			}
		}

		for (String lang : langs) {
			Path tokensFile = tokensFilePath(lang);
			try {
				tokensreader4lang.put(lang,
					new BufferedReader(new FileReader(tokensFile.toFile())));
			} catch (FileNotFoundException e) {
				throw new ConsoleException(
					"Could not open tokens file for reading: "+tokensFile, e);
			}
		}

		try {
			sentIDsReader = new BufferedReader(new FileReader(sentsIDFile()));
		} catch (FileNotFoundException e) {
			throw new ConsoleException(
				"Could not open sentence ids file for reading: "+sentsIDFile(), e);
		}

		Path walign = null;
		try {
			walign = walignFile();
			walignReader = new BufferedReader(new FileReader(walign.toFile()));
		} catch (FileNotFoundException e) {
			throw new ConsoleException(
			"Could not open word alignment file for reading: "+walign, e);
		}

		try {
			walign = walignFile();
			tmjsonFileWriter = new FileWriter(outputFile.toFile());
		} catch (IOException e) {
			throw new ConsoleException(
			"Could not open tmjson file for writing: "+outputFile, e);
		}


		return;
	}

	private void closeAllFiles() {
		for (String lang: langs) {
			try {
				sentsreader4lang.get(lang).close();
			} catch (IOException e) {}
			try {
				tokensreader4lang.get(lang).close();
			} catch (IOException e) {}
		}
		try {
			sentIDsReader.close();
		} catch (IOException e) {}
		try {
			walignReader.close();
		} catch (IOException e) {}
		try {
			tmjsonFileWriter.close();
		} catch (IOException e) {}

		return;
	}

	private Path walignFile() {
		Path file = Paths.get(inputDir.toString(), "word.align");
		return file;
	}

	private File sentsIDFile() {
		File file = new File(inputRoot+".id");
		return file;
	}

	private Path tokensFilePath(String lang) {
		Path file = Paths.get(inputRoot.toString()+"."+lang+".tok");
		return file;
	}

	private Path sentsFilePath(String lang) {
		Path file = Paths.get(inputRoot.toString()+"."+lang);
		return file;
	}

}