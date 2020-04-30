package ca.inuktitutcomputing.applications;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Syllabics;
import ca.pirurvik.iutools.corpus.CorpusDocument_File;
import ca.pirurvik.iutools.corpus.CorpusReader_Directory;

public class IUTextExtractor {
	
	public ArrayList<String> wordsList;
	public ArrayList<String> selectedWords;
	
	public IUTextExtractor() {
		this.wordsList = new ArrayList<String>();
		this.selectedWords = new ArrayList<String>();
	}

	public static void main(String[] args) throws Exception {
		IUTextExtractor extractor = new IUTextExtractor();
		//String directory = "/Users/benoitfarley/Dropbox/NRC Project - Files Shared/iu_web_sample-500";
		String directory = "/Users/benoitfarley/Dropbox/NRC Project - Files Shared/iu_web_sample_excl_eng";
		extractor.extractIUWords(directory);
		System.out.println("Nb. Inuktitut words: "+extractor.wordsList.size());
		int nbWordsToSelect = 100;
		extractor.selectWords(nbWordsToSelect);
		for (int i=0; i<extractor.selectedWords.size(); i++)
			System.out.println(
					"[WORD] "
					//		+extractor.selectedWords.get(i)
					//		+" ("
							+Syllabics.transcodeToRoman(extractor.selectedWords.get(i))
					//		")"
							);
	}

	private void selectWords(int nbWordsToSelect) {
		int rand;
		int randUpperLimit = this.wordsList.size();
		Random numGen = new Random();;
		while (this.selectedWords.size()<nbWordsToSelect) {
			rand = numGen.nextInt(randUpperLimit);
			String selectedWord = this.wordsList.get(rand-1);
			if ( !this.selectedWords.contains(selectedWord) )
				this.selectedWords.add(selectedWord);
		}
	}

	private void extractIUWords(String directory) throws Exception {
		CorpusReader_Directory corpusReader = new CorpusReader_Directory();
		Iterator<CorpusDocument_File> files = corpusReader.getFiles(directory);
		while ( files.hasNext() ) {
			CorpusDocument_File doc = files.next();
			System.out.print("[INFO] file: "+doc.id+" --- ");
			String content = doc.getContents();
			int nWords = extractIUWordsFromFile(content);
			System.out.println(nWords);
		}
	}

	private int extractIUWordsFromFile(String content) {
		int nWords = 0;
		String[] words = content.split("\\s+");
		for (String word : words) {
			if (Orthography.isUnicodeInuktitutWord(word) && word.length()>6 ) {
				this.wordsList.add(word);
				nWords++;
			}
		}
		return nWords;
	}

}
