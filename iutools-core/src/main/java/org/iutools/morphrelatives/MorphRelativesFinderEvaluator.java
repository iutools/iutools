package org.iutools.morphrelatives;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.morph.Decomposition;
import org.iutools.utilities.StopWatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.junit.Assert;

import ca.nrc.debug.Debug;

public class MorphRelativesFinderEvaluator {
	
	public boolean verbose = false;

	public Integer stopAfterNWords = null;
	
	public CSVParser csvParser = null;
	public boolean computeStatsOverSurfaceForms = true;
	public float precision = -1;
	public float recall = -1;
	public float fmeasure = -1;

	int nbTotalCases = 0;
	protected long elapsedTime = -1;

	MorphRelativesFinder relsFinder = null;
	private String focusOnWord;

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
	
	public MorphRelativesFinderEvaluator() throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(null, null);
	}
	
	public MorphRelativesFinderEvaluator(String csvGoldStandardFilePath)
		throws MorphRelativesFinderException {
		File goldStandardFile = new File(csvGoldStandardFilePath);
		init__MorphRelativesFinderEvaluator(null, goldStandardFile);
	}

	public MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile) throws MorphRelativesFinderException {
		init__MorphRelativesFinderEvaluator(finder, csvGoldStandardFile);
	}

	public void init__MorphRelativesFinderEvaluator(MorphRelativesFinder finder,
		File csvGoldStandardFile) throws MorphRelativesFinderException {
		if (finder == null) {
			finder = new MorphRelativesFinder();
		}
		setRelsFinder(finder);
		try {
			setGoldStandard(csvGoldStandardFile);
		} catch (IOException e) {
			throw new MorphRelativesFinderException(e);
		}
	}

	public void setStopAfterNWords(Integer _stopAfterNWords) {
		this.stopAfterNWords = _stopAfterNWords;
	}

	public void setFocusOnWord(String _word) {
		this.focusOnWord = _word;
	}

	public void setVerbose(boolean value) {
		verbose = value;
	}
	
	public void setGoldStandard(File csvGoldStandardFile) throws IOException {
		if (csvGoldStandardFile != null) {
			BufferedReader reader = Files.newBufferedReader(Paths.get(csvGoldStandardFile.getAbsolutePath()));
			csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
		}
	}
	
	public void setGoldStandard(String[] csvGoldStandardLines) throws IOException {
		BufferedReader reader = new BufferedReader(new StringReader(String.join("\n", csvGoldStandardLines)));
		CSVParser blah;
		CSVFormat blobl;
		csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
	}
	
	public void setOptionComputeStatsOverSurfaceForms(boolean value) {
		computeStatsOverSurfaceForms = value;
	}
	
	public void run() {

		long startTime = StopWatch.nowMSecs();

		Logger logger = Logger.getLogger("QueryExpanderEvaluator");
		
        try {
            Pattern patMotFreq = Pattern.compile("^(.+) \\((\\d+)\\).*$");
            
            nbTotalCases = 0;
            int nbTotalGoldStandardAlternatives = 0;
            int nbTotalExpansionsFromCorpus = 0;
            int nbTotalExpansionsNotInGSAlternatives = 0;
				int nbTotalCasesWithNoExpansion = 0;
            int nbTotalCasesCouldNotBeDecomposed = 0;
            
            ArrayList<String> listAllGsAlternatives = new ArrayList<String>();
            
            boolean stop = false;

            int wordCount = 0;
            for (CSVRecord csvRecord : csvParser) {
            		if (stop) break;
            		
                    // Accessing Values by Column Index
                    String motLatinEtFreq = csvRecord.get(0);
                    Matcher m = patMotFreq.matcher(motLatinEtFreq);
                    String mot = null;
                    long freqMotGoogle = -1;
                    if ( m.matches() ) {
                    	mot = m.group(1);
                    	freqMotGoogle = Long.parseUnsignedLong(m.group(2));
                    } else {
                    	continue;
					}

					wordCount++;
					if (stopAfterNWords != null &&
							wordCount > stopAfterNWords) {
						break;
					}

                    if (focusOnWord != null && !mot.equals(focusOnWord)) {
                    	continue;
                    }
                    echo("\nProcessing word #"+wordCount+": "+mot+" "+"("+freqMotGoogle+")");
                    if (mot==null) continue;
                    
                    //String decMot = String.join(" ",compiledCorpus.getSegmenter().segment(mot))+" \\";
                    nbTotalCases++;
                    echo("    Gold Standard reformulations (frequencies in compiled corpus):");
                    String[] gsalternatives = (mot+"; "+csvRecord.get(4)).split(";\\s*");
                    String[] gsalternativesMorphemes = new String[gsalternatives.length];
                    List<String> listgsalternatives = Arrays.asList(gsalternatives);
                    nbTotalGoldStandardAlternatives += gsalternatives.length;
                    listAllGsAlternatives.addAll(listgsalternatives);
                    
                    for (int igs=0; igs<gsalternatives.length; igs++) {	
                    	String gsalternative = gsalternatives[igs];
                    	long freqGSAlternativeInCorpus = freqDansCorpus(gsalternative);
                    	echo("        "+gsalternative+" : "+freqGSAlternativeInCorpus);
                    	String altDecomp = null;
                    	try {
                    		altDecomp = String.join(" ", relsFinder.compiledCorpus.decomposeWord(gsalternative));
							altDecomp =
								Decomposition.formatDecompStr(altDecomp);
                        	gsalternativesMorphemes[igs] = altDecomp;
                    	} catch (Exception e) {
                    		altDecomp = "";
                    	}
                    }
                    List<String> listgsalternativesmorphemes = Arrays.asList(gsalternativesMorphemes);
                    
                    echo("    Morphological Relatives found (frequencies in compiled corpus):");
                    try {
                    	MorphologicalRelative[] expansions = relsFinder.findRelatives(mot);
                    	if ( expansions != null ) {
                        	logger.debug(mot+" - expansions: "+expansions.length);
                        	removeTailingBackslashFromDecomps(expansions);
                    		ArrayList<String> listexpansionsmorphemes = new ArrayList<String>();
                    		ArrayList<String> listexpansions = new ArrayList<String>();
                    		nbTotalExpansionsFromCorpus += expansions.length;
                    		if (expansions.length==0) {
                        		nbTotalCasesWithNoExpansion++;
                        		echo("        0 expansion");
                    		}
                    		Arrays.sort(expansions, (MorphologicalRelative a, MorphologicalRelative b) ->
                    			{
                    				if (a.getFrequency() == b.getFrequency())
                    					return 0;
                    				else
                    					return a.getFrequency() < b.getFrequency()? 1 : -1;
                    			});
                    		for (MorphologicalRelative expansion : expansions) {
                    			long freqExpansion =expansion.getFrequency();
                    			boolean expansionInGSalternatives = true;
                    			String expansionMorphemes = String.join(" ", expansion.getMorphemes());
                    			expansionMorphemes = Decomposition.formatDecompStr(expansionMorphemes);
                    			if (computeStatsOverSurfaceForms) {
                    				String relative = expansion.getWord();
                    				if ( !listgsalternatives.contains(expansion.getWord()) ) {
                    					nbTotalExpansionsNotInGSAlternatives++;
                    					expansionInGSalternatives = false;
                    					echo("  '"+relative+"' WAS NOT in GS alternatives");
                    				} else {
										echo("  '"+relative+"' was in GS alternatives");
									}
                    			} else {
                    				if ( !listgsalternativesmorphemes.contains(expansionMorphemes) ) {
                    					nbTotalExpansionsNotInGSAlternatives++;
                    					expansionInGSalternatives = false;
                    				}
                    			}
                    			echo("        "+expansion.getWord()+" : "+freqExpansion+(expansionInGSalternatives? " ***":""));
                    			listexpansionsmorphemes.add(expansionMorphemes);
                    			listexpansions.add(expansion.getWord());
                    		}
                    		if (computeStatsOverSurfaceForms) {
                    			for (String gsalternative : gsalternatives)
                    				if ( !listexpansions.contains(gsalternative) )
										;
                    		} else {
                    			for (String gsalternativeMorphemes : gsalternativesMorphemes)
                    				if ( !listexpansionsmorphemes.contains(gsalternativeMorphemes) )
										;
                    		}
                    	} else {
                    		logger.debug(mot+" - expansions null");
							nbTotalCasesWithNoExpansion++;
                    		nbTotalCasesCouldNotBeDecomposed++;
                    		echo("        the word could not be decomposed.");
                    	}
                    } catch(Exception e) {
                    	System.out.println(
                    		"Error getting the expansions for word: "+mot+"\n"+
                    		Debug.printCallStack(e));
                    }
            }
            csvParser.close();
			elapsedTime = StopWatch.elapsedMsecsSince(startTime);

            for (int igsa=0; igsa<listAllGsAlternatives.size(); igsa++) {
            	echo((igsa+1)+". "+listAllGsAlternatives.get(igsa));
            }
            
            if (verbose) {
	            System.out.println("\nTotal number of evaluated words: "+nbTotalCases);
	            System.out.println("Total number of alternatives in Gold Standard: "+nbTotalGoldStandardAlternatives);
	            System.out.println("Total number of expansions found in corpus: "+nbTotalExpansionsFromCorpus);
				System.out.format("Average time per evaluated word: %.2f secs", secsPerCase());
	            System.out.println("\nTotal number of cases with no expansion found in corpus: "+nbTotalCasesWithNoExpansion+", of which");
	            System.out.println("Total number of cases that could not be decomposed: "+nbTotalCasesCouldNotBeDecomposed);
	            
	            System.out.println("\n\tNo expansion: either the word could not be decomposed"+
	            					"\n\tor the decomposition process timed out,"+
	            					"\n\tor the corpus contains nothing with the word's root.");
	            
	            System.out.println("\nTotal number of corpus expansions not in GS alternatives: "+nbTotalExpansionsNotInGSAlternatives);            
            }            
            int nbTotalGoodExpansions = nbTotalExpansionsFromCorpus - nbTotalExpansionsNotInGSAlternatives;
            echo("\tTotal number of GOOD corpus expansion: "+nbTotalGoodExpansions);
            
            precision = (float)nbTotalGoodExpansions / (float)nbTotalExpansionsFromCorpus;
            recall = (float)nbTotalGoodExpansions / (float)nbTotalGoldStandardAlternatives;
            fmeasure = 2 * precision * recall / (precision + recall);
            
            echo("Precision = "+precision);
            echo("Recall = "+recall);
            echo("F-measure = "+fmeasure);
            
        } catch(Exception e) {
        	if (verbose) System.err.println("Exception raised: "+e.getClass()+"\n"+e.getMessage());
        	if (csvParser != null)
				try {
					csvParser.close();
				} catch (IOException e1) {
					if (verbose) System.err.println(e1.getMessage());
				}
        }


        if (focusOnWord != null) {
        	Assert.fail("TEST WAS RUN ON A SINGLE WORD!\nRemember to set focusOnWord = null to run the test on all words");
        }
	}

	private long freqDansCorpus(String reformulation) 
			throws MorphRelativesFinderException {

		long freqDansCorpus = 0;
		try {
			WordInfo wInfo = relsFinder.compiledCorpus.info4word(reformulation);
			if (wInfo != null) {
				freqDansCorpus = wInfo.frequency;
			}
		} catch (CompiledCorpusException e) {
			throw new MorphRelativesFinderException(e);
		}
	
		return freqDansCorpus;
	}
	
	private void removeTailingBackslashFromDecomps(
		MorphologicalRelative[] morphologicalRelatives) {
		MorphologicalRelative[] morphRelativesTrimmed =
			new MorphologicalRelative[morphologicalRelatives.length];
		for (int ii=0; ii < morphologicalRelatives.length; ii++) {
			MorphologicalRelative aRelative = morphologicalRelatives[ii];
			String[] aDecomp = aRelative.getMorphemes();
			if (aDecomp != null && aDecomp.length > 0 &&
				aDecomp[aDecomp.length-1].equals("\\")) {
				aDecomp = Arrays.copyOfRange(aDecomp, 0, aDecomp.length-1);
				aRelative.setMorphemes(aDecomp);
			}
		}
	}

	public double secsPerCase() {
		double mSecs = 1.0 * elapsedTime / nbTotalCases;
		double secs = mSecs / 1000;
		return secs;
	}

	public void setRelsFinder(MorphRelativesFinder finder) {
		this.relsFinder = finder;
	}

	protected void echo(String mess) {
		if (verbose) {
			System.out.println(mess);
		}
	}
}