package org.iutools.bin;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Syllabics;
import org.iutools.corpus.CorpusDocument_File;
import org.iutools.corpus.CorpusReader_Directory;
import org.iutools.text.segmentation.IUTokenizer;

public class TextExtractor {
	
	public ArrayList<String> wordsList;
	private String defaultScript = "roman";
	public String script;
	public HashMap<String,List<String>> wordsInFiles = new HashMap<String,List<String>>();
	
	public TextExtractor() {
		setScript(this.defaultScript);
		this.wordsList = new ArrayList<String>();
	}
	public TextExtractor(String _script) {
		setScript(_script);
		this.wordsList = new ArrayList<String>();
	}
	
	public void setScript(String _script) {
		if (_script.equals("roman") || _script.equals("syllabic"))
			this.script = _script;
		else
			this.script = this.defaultScript;
	}

	private void extractIUWords(String pathname) throws Exception {
		File file = new File(pathname);
		if (file.isDirectory())
			extractIUWordsFromDirectory(pathname);
		else
			extractIUWordsFromFile(new CorpusDocument_File(pathname),null);
	}
	
	private void extractIUWordsFromDirectory(String directoryPathname) throws Exception {
		Logger logger = Logger.getLogger("TextExtractor.extractIUWordsFromDirectory");
    	CorpusReader_Directory corpusReader = new CorpusReader_Directory();
    	Iterator<CorpusDocument_File> files = (Iterator<CorpusDocument_File>) corpusReader.getFiles(directoryPathname);
    	while (files.hasNext()) {
    		CorpusDocument_File corpusDocumentFile = files.next();
    		File file = new File(corpusDocumentFile.id);
    		logger.debug("file: "+file.getAbsolutePath());
    		if (file.isDirectory()) {
    			extractIUWordsFromDirectory(corpusDocumentFile.id);
    		} else {
    			extractIUWordsFromFile(corpusDocumentFile,directoryPathname);
    		}
    	}
	}


	public void extractIUWordsFromFile(CorpusDocument_File corpusDocumentFile, String directoryPathname) throws Exception {
		String content = corpusDocumentFile.getContents();
		List<String> wordList = extractIUWordsFromString(content);
		wordsInFiles.put(corpusDocumentFile.id, wordList);
	}
	
	public List<String> extractIUWordsFromString(String content) {
		List<String> wordsList = new ArrayList<String>();
		IUTokenizer iuTokenizer = new IUTokenizer();
		List<String> words = iuTokenizer.tokenize(content);
		for (int iw=0; iw<words.size(); iw++) {
			String word = words.get(iw);
			if (Orthography.isUnicodeInuktitutWord(word)) {
				if (toRomanScript())
					word = Syllabics.transcodeToRoman(word);
				wordsList.add(word);
			}
		}
		
		return wordsList;
	}
	
	public boolean toRomanScript() {
		return this.script.equals("roman");
	}
	
	
	public static void main(String args[]) throws Exception {
		String directory = null;
		boolean digestOnly = false;
		if (args.length==0) {
			printHelp();
			System.exit(0);
		}
		else if (args[0].startsWith("-")) {
			if (args[0].equals("-h")) {
				printHelp();
				System.exit(0);
			} else if (args[0].equals("-d")) {
				digestOnly = true;
				directory = args[1];
			} else {
				printHelp();
				System.exit(0);
			}		
		} else {
			directory = args[0];
		}
		TextExtractor textExtractor = new TextExtractor();
		textExtractor.extractIUWords(directory);
		
		int totalNumberOfWords = 0;
		int numberOfWordsInDirectory = 0;
		Iterator<String> iterFileNames = textExtractor.wordsInFiles.keySet().iterator();
		while (iterFileNames.hasNext()) {
			String fileName = iterFileNames.next();
			List<String> wordsInThisFile = textExtractor.wordsInFiles.get(fileName);
			totalNumberOfWords += wordsInThisFile.size();
			System.out.println("File: "+fileName+" ["+wordsInThisFile.size()+"]");
		}
		System.out.println("*** Total number of words: "+totalNumberOfWords);
		
		if (digestOnly)
			System.exit(0);
		
		
		iterFileNames = textExtractor.wordsInFiles.keySet().iterator();
		while (iterFileNames.hasNext()) {
			String fileName = iterFileNames.next();
			List<String> wordsInThisFile = textExtractor.wordsInFiles.get(fileName);
			System.out.println("\n\nFile: "+fileName+" ["+wordsInThisFile.size()+"]");
			System.out.print("    ");
			int maxWordsPerLine = 10;
			for (int i=0; i<wordsInThisFile.size(); i++) {
				System.out.print(wordsInThisFile.get(i)+" ");
				if ( (i+1)%maxWordsPerLine == 0)
					System.out.println("    ");
			}
		}
	}
	
	private static void printHelp() {
		System.out.println("Print the number of words and the list of words in a file or in each file of a directory.");
		System.out.println(MethodHandles.lookup().lookupClass().getCanonicalName()+" options* pathname");
		System.out.println("Options:");
		System.out.println("-h : print this message");
		System.out.println("-d : (digest) print only the number of words in each file");
	}


}
