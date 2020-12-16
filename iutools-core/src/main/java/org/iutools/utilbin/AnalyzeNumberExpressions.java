package org.iutools.utilbin;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;

import org.iutools.config.IUConfig;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.Decomposition;
import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzerException;
import ca.nrc.config.ConfigException;
import org.iutools.text.ngrams.NgramCompiler;

public class AnalyzeNumberExpressions {
	
	HashMap<String,Integer> expressions = null;
	String[] makeUpWords = new String[] {"sivu","sia"};
	String replacementTermForNumericPart = "0000";
	public ArrayList<String> acceptedDeterminants = new ArrayList<String>();
	ArrayList<String> rejectedDeterminants = new ArrayList<String>();
	HashMap<String,ArrayList<String>> acceptedDeterminantsWithRoot = new HashMap<String,ArrayList<String>>();
	String decomposedNormalizedNumericTermsSuite = ",,";
	Map<String,Long> ngramStats = null;
	@JsonIgnore
	transient NgramCompiler ngramCompiler;

	public static void main(String[] args) throws IOException, ConfigException, LinguisticDataException {
		// TODO: compile ngramStats and allWords (",,"...) just like in corpus compiler
		AnalyzeNumberExpressions numericTermAnalyzer = new AnalyzeNumberExpressions();
		Result result = numericTermAnalyzer.findAllExpressions();
		numericTermAnalyzer.expressions = result.exprs;
		String[] endings = numericTermAnalyzer.expressions.keySet().toArray(new String[] {});
		HighestFreqFirstComparator comparator = numericTermAnalyzer.new HighestFreqFirstComparator();
		Arrays.sort(endings,comparator);
		for (int i=0; i<endings.length; i++) {
			String ending = endings[i];
			Integer exprFreq = numericTermAnalyzer.expressions.get(ending);
			System.out.println("["+ending+"] => "+exprFreq.intValue());
            // analyze with IMA
			numericTermAnalyzer.assessEndingWithIMA(ending);
		}
		System.out.println("\nNumber of numeric terms: "+result.nbTerms);
		System.out.println("\nSuccessful endings:");
		String[] succs = numericTermAnalyzer.acceptedDeterminants.toArray(new String[] {});
		Arrays.sort(succs);
		for (int i=0; i<succs.length; i++) {
			System.out.println(succs[i]);
		}
		System.out.println("\nUnsuccessful endings:");
		String[] rejs = numericTermAnalyzer.rejectedDeterminants.toArray(new String[] {});
		Arrays.sort(rejs);
		for (int i=0; i<rejs.length; i++) {
			System.out.println(rejs[i]);
		}
		
		numericTermAnalyzer.setNgramStats();
		
		String dataPath = IUConfig.getIUDataPath();
		System.out.println("dataPath= "+dataPath);
		FileWriter saveFileWriter = new FileWriter(dataPath+"/data/"+"numericTermsCorpus.json");
		new Gson().toJson(numericTermAnalyzer, saveFileWriter);
		saveFileWriter.flush();
		saveFileWriter.close();
	}


	public Result findAllExpressions() throws IOException {
		//String filename = "/Users/benoitfarley/Documents/git_repositories/iutools/java/iutools-data/data/NunHanSearch/1999-2007/SingleLineAligned.txt";
		String filename = "/Users/benoitfarley/Downloads/Nunavut-Hansard-Inuktitut-English-Parallel-Corpus-3.0/NunavutHansard.iu_lat.txt";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		HashMap<String,Integer> exprs = new HashMap<String,Integer>();
		String line = null;
		int nNumericTerms = 0;
		Pattern pInuk = Pattern.compile("^(.+)@----@(.+)$");
		Pattern pNumber = Pattern.compile("\\b(\\d+(?:[\\.\\,\\:]\\d+)*)(\\-?)([agijklmnpqrstuv&]+)");
		while ( (line=br.readLine()) != null ) {
			Matcher mpInuk = pInuk.matcher(line);
		    //if (mpInuk.find()) {
		    	Matcher mpNumber = pNumber.matcher(line); //pNumber.matcher(mpInuk.group(1));
		    	while (mpNumber.find()) {
		    		nNumericTerms++;
		            String number = mpNumber.group(1);
		            String hyphen = mpNumber.group(2);
		            String determinant = mpNumber.group(3);
//		            if (determinant.equals("galammi")) {
//		            	System.out.println(mpInuk.group(1));
//		            	System.out.println(mpInuk.group(2));
//		            	System.out.print(number+hyphen+determinant);
//		            	System.exit(1);
//		            }
		            if (exprs.containsKey(determinant))
		                exprs.put(determinant, new Integer(exprs.get(determinant).intValue()+1));
		            else 
		            	exprs.put(determinant, new Integer(1));
		    	}
		    //}
		}
		br.close();
		
		Result result = new Result();
		result.nbTerms = nNumericTerms;
		result.exprs = exprs;
		
		return result;
	}

	protected void assessEndingWithIMA(String ending) throws LinguisticDataException {
		Logger logger = Logger.getLogger("AnalyseNumberExpressions.assessEndignWithIMA");
		logger.debug("ending: "+ending);
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();
		boolean accepted = false;
		for (int i=0; i<makeUpWords.length; i++) {
			String makeUpWord = makeUpWords[i];
			String term = makeUpWord+ending;
			Decomposition[] decs = null;
			try {
				decs = morphAnalyzer.decomposeWord(term);
			} catch (TimeoutException | MorphologicalAnalyzerException e) {
			}
			logger.debug("decs: "+(decs==null?"null":decs.length));
			if (decs!=null && decs.length!=0) {
				String regexp = "^\\{"+makeUpWord+":"+makeUpWord+"/1n\\}";
				logger.debug("regexp= "+regexp);
				Pattern p = Pattern.compile(regexp);
				for (int j=0; j<decs.length; j++) {
					String dec = decs[j].toStr2();
					logger.debug("dec= "+dec);
					Matcher mp = p.matcher(dec);
					if (mp.find()) {
						logger.debug("accepted");
						accepted = true;
						if (acceptedDeterminantsWithRoot.containsKey(ending)) {
							acceptedDeterminantsWithRoot.get(ending).add(makeUpWord);
						} else {
							acceptedDeterminantsWithRoot.put(ending,new ArrayList<String>(Arrays.asList(makeUpWord)));
						}
						break;
					}
				}
			if (i==0)
				addToDecomposedNumericTerms(replacementTermForNumericPart+ending); // only 1 per ending
			}
		}
		if (accepted) {
			acceptedDeterminants.add(ending);
		} else {
			rejectedDeterminants.add(ending);
		}
	}
	
	private void addToDecomposedNumericTerms(String term) {
		decomposedNormalizedNumericTermsSuite += term+",,";
	}
	
	public void setNgramStats() {
		ngramStats = new HashMap<String,Long>();
		String[] terms = decomposedNormalizedNumericTermsSuite.split(",,");
		ngramCompiler = new NgramCompiler(3,0,false);
		for (int it=1; it<terms.length; it++) {
			updateSequenceNgramsForWord(terms[it]);
		}
	}
	private void updateSequenceNgramsForWord(String word) {
		Set<String> seqsSeenInWord = new HashSet<String>();
		seqsSeenInWord = ngramCompiler.compile(word);
		Iterator<String> itngram = seqsSeenInWord.iterator();
		while (itngram.hasNext()) {
			String ngram = itngram.next();
			Long freq = ngramStats.get(ngram);
			if (freq==null)
				freq = (long)0;
			ngramStats.put(ngram, ++freq);
		}
	}
	
	public Map<String,Long> getNgramStats() {
		return ngramStats;
	}
	
	public String getDecomposedNormalizedNumericTermsSuite() {
		return decomposedNormalizedNumericTermsSuite;
	}



	class HighestFreqFirstComparator implements Comparator {

		@Override
		public int compare(Object a, Object b) {
			if (expressions.get(a).intValue() > expressions.get(b).intValue())
				return -1;
			else if (expressions.get(a).intValue() < expressions.get(b).intValue())
				return 1;
			else
				return 0;
		}
		
	}
	
	class Result {
		int nbTerms;
		HashMap<String,Integer> exprs;
	}
}

