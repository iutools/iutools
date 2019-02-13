package ca.inuktitutcomputing.core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.gson.Gson;

public class QueryExpanderEvaluator {
	
	static String csvGoldStandardFilePath = "/Users/benoitfarley/Inuktitut/Pirurvik/IU100Words.csv";
	static String compiledCorpusTrieFilePath = "/Users/benoitfarley/temp/trie_compilation.json";
	static CompiledCorpus compiledCorpus = null;
	static HashMap<String,String> results = null;
	
	/*
	 * 0. Mot original (fréquence Google du mot en syllabique),
	 * 1. Mot original en syllabique,
	 * 2. Traduction,
	 * 3. "Reformulations trouvées",
	 * 4. "Meilleures Reformulations",
	 * 5. "Syllabique des refromulations avec #hits Google",
	 * 6. "Traduction des meilleures Reformulations",
	 * 7. Notes
	 */
	
	public static void main(String[] args) {
		
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(csvGoldStandardFilePath));
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
    		FileReader fr = new FileReader(compiledCorpusTrieFilePath);
    		compiledCorpus = new Gson().fromJson(fr, CompiledCorpus.class);    		
    		fr.close();
    		QueryExpander queryExpander = new QueryExpander(compiledCorpus);
    		
    		System.out.println("Size of segments cache: "+compiledCorpus.segmentsCache.size());
            
            Pattern patMotFreq = Pattern.compile("^(.+) \\((\\d+)\\).*$");
            
            int nbTotalCases = 0;
            int nbTotalGoldStandardAlternatives = 0;
            int nbTotalExpansionsFromCorpus = 0;
            int nbTotalExpansionsNotInGSAlternatives = 0;
            int nbTotalGSAlternativesNotInExpansions = 0;
            int nbTotalCasesWithNoExpansion = 0;
            int nbTotalCasesCouldNotBeDecomposed = 0;
            
            boolean stop = false;
           
            int i = 0;
            for (CSVRecord csvRecord : csvParser) {
            		if (i++ == 0) continue;
            		if (stop) break;
            		
                    // Accessing Values by Column Index
                    String motLatinEtFreq = csvRecord.get(0);
                    Matcher m = patMotFreq.matcher(motLatinEtFreq);
                    String mot = null;
                    long freqMotGoogle = -1;
                    if ( m.matches() ) {
                    	mot = m.group(1);
                    	freqMotGoogle = Long.parseUnsignedLong(m.group(2));
                    }
                    System.out.println("\n"+mot+" "+"("+freqMotGoogle+")");
                    if (mot==null) continue;
                    
//                  if (mot.equals("akigiliutinajaqtuq"))
//                    	stop = true;
                    nbTotalCases++;
                    System.out.println("    Gold Standard reformulations (frequencies in compiled corpus):");
                    String[] gsalternatives = (mot+"; "+csvRecord.get(4)).split(";\\s+");
                    List<String> listgsalternatives = Arrays.asList(gsalternatives);
                    nbTotalGoldStandardAlternatives += gsalternatives.length;
                    for (String gsalternative : gsalternatives) {	
                    	long freqGSAlternativeInCorpus = freqDansCorpus(gsalternative);
                    	System.out.println("        "+gsalternative+" : "+freqGSAlternativeInCorpus);
                    }
                    
                    System.out.println("    Query Expander expansions (frequencies in compiled corpus):");
                    try {
                    	QueryExpansion[] expansions = queryExpander.getExpansions(mot);
                    	if (expansions != null) {
                    		ArrayList<String> listexpansions = new ArrayList<String>();
                    		nbTotalExpansionsFromCorpus += expansions.length;
                    		if (expansions.length==0) {
                        		nbTotalCasesWithNoExpansion++;
                        		System.out.println("        0 expansion");
                    		}
                    		for (QueryExpansion expansion : expansions) {
                    			long freqExpansion =expansion.frequency;
                    			System.out.println("        "+expansion.word+" : "+freqExpansion);
                    			if ( !listgsalternatives.contains(expansion.word) )
                    				nbTotalExpansionsNotInGSAlternatives++;
                    			listexpansions.add(expansion.word);
                    		}
                    		for (String gsalternative : gsalternatives)
                    			if ( !listexpansions.contains(gsalternative) )
                    				nbTotalGSAlternativesNotInExpansions++;
                    	} else {
                    		nbTotalGSAlternativesNotInExpansions += gsalternatives.length;
                    		nbTotalCasesWithNoExpansion++;
                    		nbTotalCasesCouldNotBeDecomposed++;
                    		System.out.println("        the word could not be decomposed.");

                    	}
                    } catch(Exception e) {
                    	System.err.println("Error during getting the expansions: "+e.getMessage());
                    }
                    
            }
            
            System.out.println("\nTotal number of evaluated words: "+nbTotalCases);
            System.out.println("Total number of alternatives in Gold Standard: "+nbTotalGoldStandardAlternatives);
            System.out.println("Total number of expansions found in corpus: "+nbTotalExpansionsFromCorpus);
            
            System.out.println("\nTotal number of cases with no expansion found in corpus: "+nbTotalCasesWithNoExpansion+", of which");
            System.out.println("Total number of cases that could not be decomposed: "+nbTotalCasesCouldNotBeDecomposed);
            
            System.out.println("\n\tNo expansion: either the word could not be decomposed"+
            					"\n\tor the decomposition process timed out,"+
            					"\n\tor the corpus contains nothing with the word's root.");
            
            System.out.println("\nTotal number of corpus expansions not in GS alternatives: "+nbTotalExpansionsNotInGSAlternatives);            
            
            int nbTotalGoodExpansions = nbTotalExpansionsFromCorpus - nbTotalExpansionsNotInGSAlternatives;
            System.out.println("\tTotal number of GOOD corpus expansion: "+nbTotalGoodExpansions);
            
            float precision = (float)nbTotalGoodExpansions / (float)nbTotalExpansionsFromCorpus;
            float recall = (float)nbTotalGoodExpansions / (float)nbTotalGoldStandardAlternatives;
            float fmeasure = 2 * precision * recall / (precision + recall);
            
            System.out.println("Precision = "+precision);
            System.out.println("Recall = "+recall);
            System.out.println("F-measure = "+fmeasure);

        } catch(Exception e) {
        	System.err.println(e.getMessage());
        }
	}

	private static long freqDansCorpus(String reformulation) {
		String[] keys = compiledCorpus.segmentsCache.get(reformulation);
		if (keys==null)
			return 0;
		long freqDansCorpus = compiledCorpus.trie.getFrequency(keys);
		return freqDansCorpus;
	}

}
