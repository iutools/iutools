package ca.inuktitutcomputing.morph;

import java.util.concurrent.TimeoutException;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.utilities.StopWatch;

public abstract class MorphologicalAnalyzerAbstract {
	
    protected long millisTimeout = 10000;
    protected boolean stpwActive = true;
    protected StopWatch stpw;
    
    /** 
     * DÉCOMPOSITION DU MOT.
     * Les décompositions résultantes sont ordonnées selon certaines règles.
     * L'analyse tient compte de certaines habitudes : l'omission de la consonne finale,
     * et l'usage de 'n' au lieu de 't' en finale.
     * @param word String word to be decomposed, in syllabics or roman alphabet
     * @return Decomposition[] array of decompositions
     * @throws LinguisticDataException 
     * @throws MorphologicalAnalyzerException 
     */
    abstract public Decomposition[] decomposeWord(String word) 
    		throws TimeoutException, MorphInukException, LinguisticDataException;
    /** 
     * DÉCOMPOSITION DU MOT.
     * Les décompositions résultantes sont ordonnées selon certaines règles.
     * @param word String word to be decomposed, in syllabics or roman alphabet
     * @param extendedAnalysis boolean if true, check also for possible missing consonant at the end of the word
     * @return Decomposition[] array of decompositions
     * @throws LinguisticDataException 
     * @throws MorphologicalAnalyzerException 
     */
    abstract public Decomposition[] decomposeWord(String word, boolean extendedAnalysis) throws TimeoutException, MorphInukException, LinguisticDataException, MorphologicalAnalyzerException;


    public MorphologicalAnalyzerAbstract()  throws LinguisticDataException {
    	
    }
    
    public MorphologicalAnalyzerAbstract setTimeout(long val) {
    	millisTimeout = val;
    	return this;
    }
    public MorphologicalAnalyzerAbstract disactivateTimeout() {
    	stpwActive = false;
    	return this;
    }
    public MorphologicalAnalyzerAbstract activateTimeout() {
    	stpwActive = true;
    	return this;
    }
    

}
