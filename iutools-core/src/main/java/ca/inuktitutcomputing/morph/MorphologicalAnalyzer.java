// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File: MorphInuk.java
//
// Type/File type: code Java / Java code
// 
// Auteur/Author: Benoit Farley
//
// Organisation/Organization: Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de création/Date of creation:
//
// Description: Fonctions pour la décomposition d'un terme inuktitut
//              en ses diverses parties: base de mot et suffixes.
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.morph;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.*;

import org.apache.log4j.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import ca.inuktitutcomputing.data.constraints.Condition;
import ca.inuktitutcomputing.data.constraints.Conditions;
import ca.inuktitutcomputing.data.constraints.Imacond;
import ca.inuktitutcomputing.data.constraints.ParseException;
import ca.inuktitutcomputing.script.Orthography;
import ca.inuktitutcomputing.script.Roman;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.data.*;
import ca.inuktitutcomputing.morph.Graph.State;
import ca.inuktitutcomputing.phonology.Dialect;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities1.Util;

//-------------------------------------------------
// Cette version fonctionne avec le graphe d'états.
//-------------------------------------------------


public class MorphologicalAnalyzer extends MorphologicalAnalyzerAbstract {

	Vector<Decomposition> decompsSoFar = new Vector<Decomposition>();
	
    private Hashtable<String,Graph.Arc[]> arcsByMorpheme = new Hashtable<String,Graph.Arc[]>();
    
    private static Cache<String, Decomposition[]> 
    	decompsCache = 
    		Caffeine.newBuilder().maximumSize(10000)
    		  .build();
    
    public MorphologicalAnalyzer() throws LinguisticDataException {
    	super();
		LinguisticData.getInstance();
    }
    
	@Override
	protected Decomposition[] doDecompose(String word, Boolean extendedAnalysis) 
			throws MorphologicalAnalyzerException, TimeoutException {
		
		if (extendedAnalysis == null) {
			extendedAnalysis = true;
		}
		
		decompsSoFar = new Vector<Decomposition>();
		
		Decomposition[] cachedDecomps = uncache(word, extendedAnalysis);
		if (cachedDecomps != null) {
			return cachedDecomps;
		}
		
		boolean decomposeCompositeRoot = false; // do not decompose composite root

		String formOfWordToBeAnalyzed = word;
		Decomposition[] decs = null;
		try {
			if (Syllabics.containsInuktitut(formOfWordToBeAnalyzed))
				formOfWordToBeAnalyzed = Syllabics.transcodeToRoman(formOfWordToBeAnalyzed);
			formOfWordToBeAnalyzed = Util.enMinuscule(formOfWordToBeAnalyzed);
			formOfWordToBeAnalyzed = formOfWordToBeAnalyzed.replaceAll("([iua])qk([iua])", "$1qq$2"); // to cope with error of transliteration
	
			Vector<Decomposition> decomps = null;
			if (formOfWordToBeAnalyzed.charAt(formOfWordToBeAnalyzed.length() - 1) != 'n') {
				decomps = _decompose(formOfWordToBeAnalyzed, decomposeCompositeRoot);
				if ( extendedAnalysis && Roman.typeOfLetterLat(formOfWordToBeAnalyzed.charAt(formOfWordToBeAnalyzed.length() - 1)) == Roman.V) {
					Vector<Decomposition> otherDecomps = _decomposeForFinalConsonantPossiblyMissing(formOfWordToBeAnalyzed, decomposeCompositeRoot);
					decomps.addAll(otherDecomps);
					decompsSoFar.addAll(otherDecomps);
				}
			}
			else {
				decomps = _decomposeForFinalN(formOfWordToBeAnalyzed, decomposeCompositeRoot);
			}
						
			// A.
			// Éliminer les décompositions qui contiennent une suite de suffixes
			// pour laquelle il existe un suffixe composé, pour ne garder que
			// la décomposition dans laquelle se trouve le suffixe composé.
			Decomposition decsC[] = Decomposition.removeCombinedSuffixes(decomps.toArray(new Decomposition[] {}));
			
			// B. Éliminer les doublons 
			decs = Decomposition.removeMultiples(decsC);
			
			// C.
			// Ordonner les décompositions selon les règles suivantes:
			// 1. racines les plus longues
			// 2. nombre minimum de suffixes/terminaisons
			// (Ces règles sont incluses dans la classe Decomposition)
			Arrays.sort(decs);
			
			cache(decs, word, extendedAnalysis);
			
		} catch (LinguisticDataException | MorphInukException e) {
			throw new MorphologicalAnalyzerException(e);
		}

		return decs;
	}

	private synchronized void cache(Decomposition[] decs, String word, boolean extendedAnalysis) {
		String key = cacheKeyFor(word, extendedAnalysis);
		decompsCache.put(key, decs);
	}


	private synchronized Decomposition[]  uncache(String word, boolean extendedAnalysis) {
		String key = cacheKeyFor(word, extendedAnalysis);
		Decomposition[] decomps = decompsCache.getIfPresent(key);
		return decomps;
	}


	/*
	 *  Si le mot se termine par la consonne 'n', il est possible qu'il s'agisse 
	 *  d'un 't' nasalisé, phénomène couramment rencontré.
	 */
	private Vector<Decomposition> _decomposeForFinalN(String aWord, boolean decomposeCompositeRoot)
			throws TimeoutException, MorphInukException, LinguisticDataException {
		
		decompsSoFar = new Vector<Decomposition>();
		String wordWithNReplaced = aWord.substring(0, aWord.length() - 1) + "t";
		Vector<Decomposition> newDecomps = _decompose(wordWithNReplaced, decomposeCompositeRoot);
		if (newDecomps != null)
			for (int j = 0; j < newDecomps.size(); j++) {
				Decomposition dec = (Decomposition) newDecomps.elementAt(j);
				dec.stem.term = aWord;
				Object max[] = dec.morphParts;
				AffixPartOfComposition affixPart = null;
				if (max.length != 0) {
					affixPart = (AffixPartOfComposition) max[max.length - 1];
					affixPart.setTerme(affixPart.getTerm().substring(0, affixPart.getTerm().length() - 1) + "n");
				}
			}
		return newDecomps;
	}

    /* Si le mot se termine par une voyelle, il est possible
     * qu'il manque la consonne finale. On ajoute '*' à la fin
     * du mot, qui tient lieu de n'importe quelle consonne.
     */
    private Vector<Decomposition> _decomposeForFinalConsonantPossiblyMissing(
    		String aWord, boolean decomposeCompositeRoot) throws TimeoutException, MorphInukException, LinguisticDataException {
    	stpw.check(
    		"_decomposeForFinalConsonantPossiblyMissing -- upon entry, word="+
    		aWord+", decomposeCompositeRoot="+decomposeCompositeRoot);
    	Vector<Decomposition> newDecomps = _decompose(aWord + "*", false);
        return newDecomps;
	}

	// __decompose/3 retourne un vecteur de Decomposition, ou null.
    //  
    // Les décompositions sont retournées dans l'ordre où elles ont été
    // trouvées. Elles ne sont pas organisées à ce stade-ci.
    // Une Decomposition est un objet composé d'un MorceauRacine
    // et d'une table (array) de MorceauAffixe.
    // 
    // Note: l'argument isSyllabic est à toute fin pratique obsolète,
    // puisque tout mot en syllabique est d'abord translittéré en caractères
    // latins avant d'être décomposé. Tout ce qui a rapport au syllabique
    // devra éventuellement être supprimé d'une grande partie du code. On
    // ne doit pas en tenir compte ici.
    //
    // L'argument decomposeBase, lorsqu'il a la valeur 'true', indique
    // qu'on veut faire la décomposition d'un mot incomplet.

    // Note: cette méthode est appelée par plusieurs autres méthodes actuellement,
    // c'est la raison pour laquelle elle est publique. Mais éventuellement, ces
    // méthodes devraient plutôt appeler decomposeWord, et alors decompose sera
    // faite privée.

	private Vector<Decomposition> _decompose(String term, boolean decomposeCompositeRoot)
			throws TimeoutException, MorphInukException, LinguisticDataException {

		decompsSoFar = new Vector<Decomposition>();
		
		Vector<AffixPartOfComposition> morphPartsInit = new Vector<AffixPartOfComposition>();
		Graph.State state;
		Vector<Decomposition> decomposition = null;
		String simplifiedTerm = null;
		Conditions preCond = null;

		stpw = new StopWatch(millisTimeout);
		Dialect.setStopWatch(stpw);
        if (!timeoutActive) stpw.disactivate(); // for debugging
		stpw.start();

		arcsByMorpheme.clear();

		// Etat de départ dans le graphe d'états.
		if (decomposeCompositeRoot)
			state = null;
		else
			state = Graph.initialState;

		if (term != null) {
			// Simplification de l'orthographe du mot, pour faciliter
			// l'analyse. En caractères latins, ceci signifie que
			// nng devient NN et ng devient N.
			boolean isSyllabic = false;
			simplifiedTerm = Orthography.simplifiedOrthography(term, isSyllabic);
			String transitivity = null;
			// DÉCOMPOSITION du terme simplifié.
			decomposition = __decompose_simplified_term__(simplifiedTerm, simplifiedTerm, simplifiedTerm,
					morphPartsInit, new Graph.State[] { state }, preCond, transitivity);
		}
		return decomposition;
	}

    //==========================DECOMPOSER====================================
    // Lieu véritable de la décomposition des mots.
    //========================================================================

    // Décomposition d'un terme inuktitut.
    // Retourne un Vector avec 0 ou + éléments.
    // TERM  peut être le terme original à décomposer ou un radical normalisé
    //       provenant de la décomposition en cours de TERM.
    //       'normalisé' signifie que lorsqu'un affixe a été trouvé et validé,
    //       la décomposition se poursuit sur le radical restant dont la finale
    //       a été ramenée à sa forme normale (par exemple, juar > juaq).
    //       NOTE: l'orthographe du terme est simplifiée: ng > N ; nng > NN
    // TERMORIG est le terme à décomposer ou le radical tel quel, non normalisé.
    //          Orthographe simplifiée.
    // WORD est le mot original à décomposer, dans sa forme simplifiée.
    // MORPHPARTS est un vecteur contenant les morphèmes trouvés jusqu'à présent.
    // ETAT indique l'état du graphe d'état où l'analyse est rendue.
    // PRECOND conditions sur le morphème précédent.
    // TRANSITIVITY indique la valeur de transitivité du prochain morphème à trouver.

    // Note: le traitement des conditions spécifiques est très embryonnaire, et
    // à toutes fins pratiques, il faut le repenser totalement.

    private Vector<Decomposition> __decompose_simplified_term__(
    		String term, String termOrig, String word, 
            Vector<AffixPartOfComposition> morphParts, 
            Graph.State states[],
            Conditions preConds,
            String transitivity
            ) throws TimeoutException, MorphInukException, LinguisticDataException {

    	stpw.check("__decompose_simplified_term__ -- Upon entry");

        Vector<Decomposition> completeAnalysis = new Vector<Decomposition>();
        
        /*
         * -------------- RACINE -----------------
		 * Le terme à analyser peut être une racine, simple ou complexe, connue
		 * dans la base de données comme une racine nom, verbe, adverbe, etc. On
		 * vérifie cette possibilité, et le cas échéant, on ajoute les
		 * décompositions résultantes à l'analyse complète.
		 */
        Vector<Decomposition> analysesAsRoot = analyzeAsRoot(term,termOrig,
                word,morphParts,states, preConds, transitivity
                );
        completeAnalysis.addAll(analysesAsRoot);
        decompsSoFar.addAll(analysesAsRoot);
        
        /*
         * -------------- MORPHÈMES -----------------
         *  Le terme à analyser peut aussi se décomposer en morphèmes.
         */
        Vector<Decomposition> analysesAsSequenceOfMorphemes = analyzeAsSequenceOfMorphemes(term,
                word,morphParts,states, preConds, transitivity
                );
        completeAnalysis.addAll(analysesAsSequenceOfMorphemes);
        decompsSoFar.addAll(analysesAsSequenceOfMorphemes);

        return completeAnalysis;
    }
    
    private Vector<Decomposition> analyzeAsSequenceOfMorphemes(
			String simplifiedTerm,
			String word,
			Vector<AffixPartOfComposition> morphParts, State[] states,
			Conditions preCond, String transitivity) throws TimeoutException, MorphInukException, LinguisticDataException {

    	stpw.check("analyzeAsSequenceOfMorphemes -- Upon entry");
    	Logger logger = Logger.getLogger("MorphologicalAnalyzer.analyzeAsSequenceOfMorphemes");
    	logger.debug("++++++simplifiedTerm= "+simplifiedTerm);
        Vector<Decomposition> completeAnalysis = new Vector<Decomposition>();
        Vector<SurfaceFormOfAffix> formsOfAffixFound;
        Vector<SurfaceFormOfAffix> otherFormsOfAffixFound;
        /*
         * =================================================================
         * À partir du dernier caractère du terme, reculer 1 caractère à la
         * fois jusqu'à ce qu'un affixe soit trouvé.
         * 
         * Lorsqu'un affixe est trouvé, on crée un point de branchement: sur
         * cette nouvelle branche, on poursuit la décomposition avec le
         * radical qui précède cet affixe.
         * 
         * Lorsque ce processus est terminé, on poursuit sur la branche
         * courante la décomposition courante comme si un affixe n'avait pas
         * été trouvé. Cela permet de trouver toutes les possibilités de
         * combinaisons des lettres en morphèmes de longueurs diverses. 
         * Par exemple, dans un mot qui contient 'lauqsima', 'sima' sera trouvé
         * d'abord ; si on veut que 'lauqsima' soit analysé, il faut poursuivre
         * la branche courante comme si 'sima' n'avait pas été trouvé.
         * 
         * On arrête le compteur 'positionAffix' à 2 puisqu'il n'y a pas de
         * racine qui, amputée de sa consonne finale, n'aurait plus qu'un
         * seul caractère. (Il n'y a pas de racine de 2 caractères dont le
         * dernier caractère est une consonne [susceptible d'être supprimée
         * par un suffixe].)
         */

        int positionAffix = 0; // position dans le mot
        int positionAffixStart = simplifiedTerm.length() - 1;
        //            if (term.charAt(term.length() - 1) == '*')
        //                positionAffixStart--;

        for (positionAffix = positionAffixStart; positionAffix > 1; positionAffix--) {
            /*
             * À la position d'analyse courante dans le terme, on vérifie si
             * la séquence de caractères de cette position à la fin du terme
             * est un affixe.
             */
            String seqOfCharsPossibleAffix = simplifiedTerm.substring(positionAffix);
            /*
             * 'affixCandidate' est donc un candidat correspondant à la
             * seconde partie de 'term', de la position d'analyse courante
             * 'positionAffix' à la fin; le radical est la première partie
             * de 'term', de 0 à positionAffix-1 incl.
             */
            String remainingStem = simplifiedTerm.substring(0, positionAffix);
            stpw.check("analyzeAsSequenceOfMorphemes -- position: "+positionAffix+
        			"; possibleAffix: "+seqOfCharsPossibleAffix+"; remainingStem: "+remainingStem);
            
            /*
             * RECHERCHE D'AFFIXES---------------------------------------
             * Chercher toutes les formes d'affixes correspondant au(x) caractère(s)
             * final(aux) du terme à partir de la position d'analyse
             * courante. Cette recherche est effectuée dans la table de hachage
             * 'surfaceFormsOfAffixes'. Le résultat est un ensemble d'objets
             * qui décrivent des formes de surface d'affixes dans des contextes
             * donnés avec leurs actions. (L'orthographe du mot à décomposer a été
             * simplifié; il faut donc la renormaliser pour faire la
             * recherche lexicale, puisque les données linguistiques sont
             * stockées avec l'orthographe standard.)
             */
            formsOfAffixFound = null;
            otherFormsOfAffixFound = null;                
            /*
             * Certaines combinaisons de caractères à la frontière de deux
             * morphèmes ne sont pas possibles (par exemple, un suffixe
             * commençant par une voyelle ne peut suivre un radical se
             * terminant par un 'm'). Dans ces cas-là, il n'est même pas
             * nécessaire de chercher des suffixes. On évitera ainsi du
             * temps de traitement inutile, puisque dans ces cas-là, ces
             * candidats suffixes seront éventuellement rejetés.
             */
            /*
             * Après avoir été essayé, il s'est avéré que cela ne change pas
             * grand-chose. On enlève donc ce test.
             */
            // String finalRadInitAff = new String(new char[]{
            //                        stem.charAt(stem.length()-1),
            //                        affixCandidate.charAt(0)});
            // boolean test =
            // 		Donnees.finalRadInitAffHashSet.contains(finalRadInitAff);
            // if (test) {
            	boolean isSyllabic = false;
                formsOfAffixFound = lookForForms(seqOfCharsPossibleAffix, isSyllabic);
                /*
                 * Il est possible qu'une différence de prononciation
                 * dialectale se produise dans un groupe de consonnes à la
                 * frontière de deux suffixes. Cela peut se produire à la
                 * fin du candidat et aussi du début du candidat. Pour la
                 * fin du candidat, sa consonne finale peut être le résultat
                 * d'une action de 'validateContextActions' pour retourner
                 * la consonne contextuelle lors de l'analyse du morphème
                 * précédent, action qui tient compte des dialectes; on ne
                 * fait donc pas cette vérification. Pour le début du
                 * candidat, on fait la même chose avec la fin du radical
                 * qui précéde le candidat et le début du candidat. Il est
                 * aussi possible qu'une différence dialectale se soit
                 * produite à l'intérieur du candidat. On y cherche aussi
                 * des équivalences. Toutes les possibilités sont retenues.
                 * La loi de Schneider est aussi prise en considération.
                 */
                Vector<String> newCandidates = Dialect.newCandidates(remainingStem, seqOfCharsPossibleAffix, null);
                if (newCandidates != null)
                    for (int k = 0; k < newCandidates.size(); k++) {
                        Vector<SurfaceFormOfAffix> tr = lookForForms(newCandidates.elementAt(k), isSyllabic);
                        if (otherFormsOfAffixFound == null)
                            otherFormsOfAffixFound = new Vector<SurfaceFormOfAffix>();
                        if (tr != null) {
                            otherFormsOfAffixFound.addAll(tr);
                        }
                    }
            // }
            
            /*
             * POINT DE BRANCHEMENT
             * 
             * On est au point de branchement. On commence une branche en
             * poursuivant la décomposition de 'radical' avec les
             * candidats-suffixes possibles.
             */
            
            /*
             * 1. Les candidats-suffixes à partir de la chaîne originale.
             */
            // Enlever les formes qui ne sont pas acceptables à ce moment-ci
            // (cf. arcsSuivis)
            Vector<Decomposition> anas = null;
            anas = analyzeWithCandidateAffixes(
                		formsOfAffixFound, 
                		remainingStem,
                        seqOfCharsPossibleAffix, states, preCond, transitivity,
                        positionAffix, morphParts, word, true);
            completeAnalysis.addAll(anas);
            decompsSoFar.addAll(anas);
                
            /*
             * 2. Les candidats-suffixes à partir des chaînes transformées
             * contenant des groupes de consonnes équivalents dans d'autres
             * dialectes.
             */
            anas = analyzeWithCandidateAffixes(
                		otherFormsOfAffixFound,
                        remainingStem, seqOfCharsPossibleAffix, states, preCond,
                        transitivity, positionAffix, morphParts, word, false);
            completeAnalysis.addAll(anas);
            decompsSoFar.addAll(anas);
            
            /*
             * Retour de la boucle. On poursuit la décomposition de 'simplifiedTerm',
             * qu'on ait trouvé ou pas un affixe à la position actuelle.
             */
        } // for
        //=================================================================================
        
        return completeAnalysis;
	}
    
    
    public Vector<SurfaceFormOfAffix> lookForForms(String term, boolean syllabic) throws LinguisticDataException {
    	String[] cons = syllabic ? Lexicon.consonantsSyl : Lexicon.consonants;
    	Vector<SurfaceFormOfAffix> formsFound;
        if (term.endsWith("*")) {
            Vector<SurfaceFormOfAffix> formsFoundForTermWithAddedConsonant;
            String termWithoutStar, termWithConsonant;
            formsFound = new Vector<SurfaceFormOfAffix>();
            termWithoutStar = term.substring(0, term.length() - 1);
            for (int i = 0; i < cons.length; i++) {
                termWithConsonant = termWithoutStar + cons[i];
                formsFoundForTermWithAddedConsonant = Lexicon.lookForForms(termWithConsonant, syllabic);
                if (formsFoundForTermWithAddedConsonant != null) {
                	formsFound.addAll(formsFoundForTermWithAddedConsonant);
                }
            }
            if (formsFound.size() == 0)
            	formsFound = null;
        } else {
            // On cherche un affixe, de n'importe quel type:
            // terminaison verbale ou nominale, ou suffixe.
        	formsFound = Lexicon.lookForForms(term,syllabic);
        }
        
        return formsFound;
    }
	

 
	//=========================================================================
    // DES SUFFIXES ONT ÉTÉ TROUVÉS.
    //=========================================================================

    /*
     * Validation des affixes possibles trouvés.
     * 
     * Pour chaque affixe trouvé valide, une nouvelle branche de décomposition
     * est créée, par un appel récursif à décomposer/8.
     */
    @SuppressWarnings("unchecked")
	private Vector<Decomposition> analyzeWithCandidateAffixes(
			Vector<SurfaceFormOfAffix> formsOfAffixFound,
            String stem, 
            String affixCandidateOrig, 
            Graph.State states[],
            Conditions preConds,
            String transitivity, 
            int positionAffix, 
            Vector<AffixPartOfComposition> morphParts,
            String word,
            boolean notResultingFromDialectalPhonologicalTransformation
            ) throws TimeoutException, MorphInukException, LinguisticDataException {

    	Logger logger = Logger.getLogger("MorphologicalAnalyzer.analyzeWithCandidateAffixes");
    	logger.debug("***stem= "+stem);
        Vector<Decomposition> completeAnalysis = new Vector<Decomposition>();
        
        String keyStateIDs = computeStateIDs(states);
        
        Enumeration<SurfaceFormOfAffix> enumForms = null;
        if (formsOfAffixFound != null)
        	enumForms = formsOfAffixFound.elements();
        	
        
        //---------------------------------------
        // Pour chaque (forme de) suffixe trouvé:
        //---------------------------------------
        if (enumForms != null)
        while (enumForms.hasMoreElements()) {
        	
            SurfaceFormOfAffix form = (SurfaceFormOfAffix) enumForms.nextElement();

            stpw.check("decomposeByAffixes -- form: "+form.form);

            Affix affix = null;
            try {
                affix = (Affix) form.getAffix().copyOf();
            } catch (CloneNotSupportedException e1) {
            	throw new MorphInukException(e1);
            }
            boolean accepted = true;


            // Il faut vérifier 6 choses pour accepter le candidat
            // affixe trouvé:
            //   1. il ne peut s'agir du même suffixe que le précédent;
            //   2. l'affixe ne peut démarrer au mème endroit que l'affixe
            //      suivant (ceci arrive à cause de la suppression par
            //      l'affixe suivant)
            //   3. l'affixe doit être d'un type permis à ce moment-ci (état);
            //   4. les conditions spécifiques imposées par le morphème
            //      trouvé précédemment sur le morphème qui le précède
            //      dans le mot (le candidat actuel) doivent être respectées;
            //   5. la transitivité du radical doit être respectée;
            //   6. la forme de l'affixe doit être possible dans le
            //      contexte actuel.
            Graph.Arc[] arcsFollowed = null;
            Graph.State nextStates[] = null;
            Object stemAffs[][] = null;
            boolean conditionsMet, transitivityMet, sameAffixAsNext, samePosition;
            if (
                    (arcsFollowed=arcsSuivis(affix, states, keyStateIDs)) != null &&
                    (conditionsMet = affix.meetsConditions(preConds, morphParts)) &&
                    (transitivityMet = affix.meetsTransitivityCondition(transitivity)) &&
                    (sameAffixAsNext = !sameAsNext(affix, morphParts)) &&
                    (samePosition = !samePosition(positionAffix, morphParts)) &&
                    (stemAffs=agreeWithContextAndActions(affixCandidateOrig, affix, stem, 
                            positionAffix, form,
                            notResultingFromDialectalPhonologicalTransformation)) != null
                    ) {
                accepted = true;
                nextStates = new Graph.State[arcsFollowed.length];
                for (int i=0; i<arcsFollowed.length; i++) {
                    Graph.State dest =  ((Graph.Arc)arcsFollowed[i]).getDestinationState();
                    nextStates[i] = (Graph.State)dest.clone();
                }
            } else
                accepted = false;
                    
                    

           /*
            * Si toutes les étapes ont réussi, on accepte l'affixe comme élément
            * de la décomposition du mot.
            */
            if (accepted) {
                /*
                 * Certains suffixes sont contraints de suivre des morphèmes
                 * spécifiques ou des morphèmes avec des propriétés spécifiques.
                 * Elles deviennent donc les nouvelles conditions spécifiques
                 * pour la suite de la décomposition.
                 */
                Conditions newCond = affix.getPrecCond();
                /* Contrainte sur la transitivité du morphème précédent. */
                String newTransitivity = affix.getTransitivityConstraint();
                if (newTransitivity==null)
                    newTransitivity = transitivity;
                
                //---------------------
                // Pour chaque résultat possible, continuer la
                // décomposition du radical retourné, et ajouter le
                // MorceauAffixe au vecteur des morphParts déjà trouvés.
                //---------------------
                for (int iro = 0; iro < stemAffs.length; iro++) {
                    stpw.check("decomposeByAffixes -- affixes respecting context and actions: "+stemAffs[iro][0]);
					Vector<AffixPartOfComposition> newMorphparts = (Vector<AffixPartOfComposition>) morphParts.clone();
                    AffixPartOfComposition partIro = (AffixPartOfComposition) stemAffs[iro][2];
                    partIro.arcs = arcsFollowed;
                    newMorphparts.add(0, partIro); // morceau ajouté
                    Vector<Decomposition> analyses = __decompose_simplified_term__((String) stemAffs[iro][0],
                            (String) stemAffs[iro][1], word,
                            newMorphparts,
                            nextStates, 
                            newCond,
                            newTransitivity
                            );
                    if (analyses != null && analyses.size() != 0) {
                        completeAnalysis.addAll(analyses);
                    	decompsSoFar.addAll(analyses);
                    }
                }
            } // if <condition et contexte>
            
        } // for <chaque suffixe trouvé>
        //=========================================================================
        return completeAnalysis;
    }

    
    private String computeStateIDs(State[] states) {
        String keyStateIDs = "0";
        for (int i=0; i<states.length; i++)
        	keyStateIDs += "+"+states[i].id;
        
        return keyStateIDs;
	}

	//----------------------------------------------------------------------

    private Object[][] validateContextActions(String context,
            Action action1, Action action2, String stem, int posAffix,
            Affix affix, SurfaceFormOfAffix form, boolean isSyllabic,
            boolean checkPossibleDialectalChanges,
            String affixCandidate) throws TimeoutException, MorphInukException, LinguisticDataException {

        int action1Type = action1.getType();
        int action2Type = action2.getType();
        
        // Initialiser le résultat de la function.
        Vector<Object[]> res = new Vector<Object[]>();

        // Morceau qui sera enregistré dans la décomposition.
        AffixPartOfComposition partOfComp = new AffixPartOfComposition(posAffix, form);

        /*
         * Si cet affixe est non-mobile et qu'il est accepté, il faudra lui ajouter une
         * contrainte sur le morphème précédent, pour passer cette contrainte à
         * l'extérieur de cette méthode. (Seuls les suffixes ont cette propriété).
         */    
        if (affix.isNonMobileSuffix()) {
            Condition avc = new Condition.NonMobilityOfInfix(affix.id);
            affix.addPrecConstraint(avc);
        }
        
            
        if (action1Type == Action.NEUTRAL && action2Type == Action.NULLACTION ) {
        	res = validate_neutral_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp);
        }
        else if (action1Type == Action.NEUTRAL && action2Type == Action.DELETION) {
        	res = validate_neutral_deletion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp);
        }
        else if (action1Type == Action.NEUTRAL && action2Type == Action.INSERTION) {
        	res = validate_neutral_insertion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp);
        }
        else if (action1Type == Action.DELETION && action2Type == Action.NULLACTION) {
        	res = validate_deletion_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp);
        }
        else if (action1Type == Action.DELETIONINSERTION && action2Type == Action.NULLACTION) {
        	res = validate_deletion_insertion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp);
        }
        else if (action1Type == Action.CONDITIONALDELETION && action2Type == Action.NULLACTION) {
        	res = validate_conditionaldeletion_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.VOICING && action2Type == Action.NULLACTION) {
        	res = validate_voicing_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.NASALIZATION) {
        	res = validate_nasalization_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.CONDITIONALNASALIZATION) {
        	res = validate_conditionalnasalization_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.INSERTION && action2Type == Action.NULLACTION) {
        	res = validate_insertion_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.FUSION && action2Type == Action.NULLACTION) {
        	res = validate_fusion_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.ASSIMILATION && action2Type == Action.NULLACTION) {
        	res = validate_assimilation_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.SPECIFICASSIMILATION && action2Type == Action.NULLACTION) {
        	res = validate_specificassimilation_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.DELETION && action2Type == Action.SPECIFICDELETION) {
        	res = validate_deletion_specificdeletion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.DELETION && action2Type == Action.INSERTION) {
        	res = validate_deletion_insertion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.VOWELLENGTHENING && action2Type == Action.CANCELLATION) {
        	res = validate_vowellengthening_cancellation(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.DELETIONVOWELLENGTHENING && action2Type == Action.CANCELLATION) {
        	res = validate_deletionvowellengthening_cancellation(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.INSERTIONVOWELLENGTHENING && action2Type == Action.NULLACTION) {
        	res = validate_insertionvowellengthening_null(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.NEUTRAL && action2Type == Action.SELFDECAPITATION) {
        	res = validate_neutral_selfdecapitation(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.DELETION && action2Type == Action.SELFDECAPITATION) {
        	res = validate_deletion_selfdecapitation(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        }
        else if (action1Type == Action.DELETION && action2Type == Action.DELETION) {
        	res = validate_deletion_deletion(context, action1, action2, stem, affixCandidate, 
            		form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
        } else
            ;

        // Avant de retourner 'res', on vérifie certaines choses, entre autres:
        // a. le radical ne peut pas se terminer par 2 consonnes
        //    (Sauf pour les racines démonstratives!!! exemple: tavv-ani)

        if (!affix.type.equals("tad"))
            for (int i = 0; i < res.size(); i++) {
            	stpw.check("validateContextActions -- checking stem with 2 consonants");
                String stemres = (String) ((Object[]) res.get(i))[0];
                if (stemres.length() > 2
                        && Roman.typeOfLetterLat(stemres
                                .charAt(stemres.length() - 1)) == Roman.C
                        && Roman.typeOfLetterLat(stemres
                                .charAt(stemres.length() - 2)) == Roman.C)
                    res.remove(i--);
            }
        if (res.size() == 0)
            return null;
        else
            return (Object[][]) res.toArray(new Object[][] {});
    }

    
    /*
     * -------------------------RACINE----------------------------------------
     * 
     * Analyse d'un terme comme racine.
     */
    @SuppressWarnings("unchecked")
	private Vector<Decomposition> analyzeAsRoot(String term, String termOrig, 
            String word, Vector<AffixPartOfComposition> morphParts, Graph.State states[],
            Conditions preConds,
            String transitivity) throws TimeoutException, LinguisticDataException {

        Vector<Decomposition> allAnalyses = new Vector<Decomposition>();

        boolean isSyllabic = false;
        String termICI = Orthography.orthographyICI(term, isSyllabic);
        String termOrigICI = Orthography.orthographyICI(termOrig, isSyllabic);

        /*
         * Enlever le '*' à la fin du terme, s'il s'y trouve à la suite d'une
         * tentative pour trouver des analyses au cas où la consonne finale du
         * mot à analyser aurait été omise.
         */
        if (termOrigICI.endsWith("*"))
            termOrigICI = termOrigICI.substring(0,termOrigICI.length()-1);
        
        /*
         * À ce point-ci, nous sommes au début du terme. À cause de la
         * récursivité au point de branchement, le terme en question sera ce qui
         * précède tout affixe trouvé. Cela ira donc du mot entier à la racine
         * réelle, en passant par plusieurs termes intermédiaires.
         * 
         * On vérifie si cette partie initiale du mot est une racine connue.
         * 
         * Chercher le TERME dans les racines.
         */
        Vector<Morpheme> lexs = null;
        Vector<String> newRootCandidates = null;
//        lexs = Lexicon.lookForBase(termICI, isSyllabic);                
        lexs = lookForBase(termICI, isSyllabic);                
        /*
         * Il est possible qu'une différence de prononciation dialectale se
         * produise dans un groupe de consonnes à la frontière de deux suffixes.
         * Il faut vérifier si le suffixe trouvé précédemment commence par une
         * consonne et si le candidat racine finit par une consonne et si ce
         * groupe de deux consonnes correspond à un autre groupe de consonnes.
         * 
         * On cherche aussi des groupes de consonnes équivalents à l'intérieur
         * de la racine candidate. Toutes les possibilités sont retenues.
         */
        newRootCandidates = Dialect.newRootCandidates(termICI); 
        if (newRootCandidates != null)
            for (int k = 0; k < newRootCandidates.size(); k++) {
//                stpw.check("analyzeAsRoot -- newRootCandidate: "+((Base)newRootCandidates.elementAt(k)).morpheme);
                Vector<Morpheme> tr = lookForBase((String) newRootCandidates.elementAt(k), isSyllabic);
                if (tr != null)
                    if (lexs == null)
                        lexs = (Vector<Morpheme>) tr.clone();
                    else {
                        lexs.addAll(tr);
                    }
            }
        Vector<Decomposition> rootAnalyses = checkRoots(lexs,word,termOrigICI,morphParts,states,
                preConds,transitivity);
        
        allAnalyses.addAll(rootAnalyses);
        decompsSoFar.addAll(rootAnalyses);
        
        return allAnalyses;
    }
    
    public Vector<Morpheme> lookForBase(String termICI, boolean isSyllabic) throws LinguisticDataException {
    	Vector<Morpheme> basesFound = null;
    	if (termICI.endsWith("*")) {
    		String[] cons = isSyllabic ? Lexicon.consonantsSyl : Lexicon.consonants;

    		basesFound = new Vector<Morpheme>();
            String termICIWithoutStar = termICI.substring(0, termICI.length() - 1);
            for (int i = 0; i < cons.length; i++) {
                String termICIWithConsonant = termICIWithoutStar + cons[i];
                Vector<Morpheme> morphemesFoundForWordWithAddedConsonant = Lexicon.lookForBase(termICIWithConsonant, isSyllabic);
                if (morphemesFoundForWordWithAddedConsonant != null) {
                    basesFound.addAll(morphemesFoundForWordWithAddedConsonant);
                }
            }
            if (basesFound.size() == 0)
                basesFound = null;
    	} else {
            basesFound = Lexicon.lookForBase(termICI, isSyllabic);
    	}
    	
    	return basesFound;
    }
    
    /**
     * Compute the root morphemes corresponding to the given lexemes.
     * @param lexs Vector<Object>
     * @param word
     * @param termOrigICI
     * @param morphParts
     * @param states
     * @param preCond
     * @param transitivity
     * @return
     * @throws TimeoutException
     * @throws LinguisticDataException 
     */
	private Vector<Decomposition> checkRoots(Vector<Morpheme> lexs, String word, String termOrigICI,
            Vector<AffixPartOfComposition> morphParts, Graph.State states[], Conditions preConds,
            String transitivity) throws TimeoutException, LinguisticDataException {

        Vector<Decomposition> rootAnalyses = new Vector<Decomposition>();
        
        char typeBase = 0;

        if (lexs == null) {
            // RACINE UNKNOWN !!!
            // Pour le moment, on ne fait rien. On ne fait que
            // créer un vecteur vide.
            lexs = new Vector<Morpheme>();
        } 

        //-------------------------------------------
        // Pour chaque base possible du vecteur lexs:----------------
        //-------------------------------------------
        
        for (int ib = 0; ib < lexs.size(); ib++) {
            // Chaque élément de lexs est un ensemble Object []
            // {Integer,Base}.
            Base root = (Base) lexs.elementAt(ib);

            stpw.check("checkRoots -- morpheme: "+root.morpheme);

            typeBase = root.type.charAt(0);
            
            if (typeBase == '?') {
                /*
                 * Si la racine est inconnue, on ajoute simplement une nouvelle
                 * décomposition à la liste des décompositions. (Note: ceci
                 * n'est pas effectué puisqu'on a mis en commentaire plus haut
                 * le traitement des racines inconnues.)
                 */
                Decomposition res = new Decomposition(word, new RootPartOfComposition(
                        termOrigICI, root, transitivity, null), morphParts
                        .toArray(new AffixPartOfComposition[] {}));
                rootAnalyses.add(res);
            } else {
                /*
                 * Si la racine est connue : vérifier la validité du candidat.
                 */
            	Graph.Arc arcFollowed = checkValidityOfRoot(root,states,morphParts,preConds,transitivity);
                
                 if (arcFollowed != null) {
                    /*
                     * Toutes les conditions ont été respectées. Créer une
                     * nouvelle décomposition avec cette racine et les morphParts
                     * trouvés jusqu'ici.
                     */
                	Graph.Arc arc = arcFollowed.copy();
                    RootPartOfComposition mr = new RootPartOfComposition(
                            termOrigICI, root, transitivity, arc);
                    Decomposition res = new Decomposition(word, mr, morphParts
                            .toArray(new AffixPartOfComposition[] {}));
                    rootAnalyses.add(res);
                }
            }
        } //-------------------------- for (ib ...) -------------------------
        
        return rootAnalyses;
    }
	
	private Graph.Arc checkValidityOfRoot(Morpheme root, Graph.State states[],
			Vector<AffixPartOfComposition> morphParts, Conditions preConds,
            String transitivity) throws TimeoutException, LinguisticDataException {
       	/* il faut vérifier si le type de la
         * racine correspond à un arc à partir de l'état actuel, et cet
         * arc doit conduire à l'état final (aucun arc partant de cet
         * état final). En principe, il ne devrait y avoir qu'un seul
         * arc accepté puisqu'on est rendu à la racine et qu'une racine
         * ne peut prendre qu'un seul arc.
         */
        
        /*
         * Il faut aussi vérifier que la finale de la base est une
         * lettre valide: une voyelle, un k, un q, un t.
         */
        //                          // *** Mis en quarantaine pour le moment ***
        //                                String motbase = racine.morpheme;
        //                                char dernierChar = motbase.charAt(motbase
        //                                        .length() - 1);
        //                                						if (dernierChar == 'a'
        //                                							|| dernierChar == 'i'
        //                                							|| dernierChar == 'u'
        //                                							|| dernierChar == 't'
        //                                							|| dernierChar == 'k'
        //                               							|| dernierChar == 'q') {
        
        /*
         * Il faut aussi que les conditions spécifiques soient
         * rencontrées. Il y a les conditions sur ce qui précède,
         * et les conditions sur ce qui peut suivre. Par exemple, si le suffixe 
         * trouvé précédemment
         * exige de suivre immédiatement un nom au cas datif, le suffixe
         * ou la terminaison actuelle doit rencontrer cette contrainte.
         */
        /*
         * Vérifier si la transitivité imposée par le morphème trouvé
         * précédemment sur le radical, i.e. sur le morphème qui le
         * précède dans le mot (le candidat actuel) est respectée. Cette
         * valeur de transitivité imposée par le morphème trouvé est
         * indiquée dans le champ 'condTrans'.  Elle ne s'applique qu'aux
         * racines.
         */
        boolean accepted = false;

		String keyStateIDs = computeStateIDs(states);

        Graph.Arc arcFollowed = null;
        Graph.Arc[] arcsFollowed = arcsSuivis(root,states,keyStateIDs);
		if (arcsFollowed != null) {
			arcFollowed = arcToZero(arcsFollowed);
			if (arcFollowed != null) {
				boolean preConditionsMet = root.meetsConditions(preConds, morphParts);
				if (preConditionsMet) {
					Conditions postConds = root.getNextCond();
					boolean postConditionsMet = true;
					if (morphParts.size()!=0) {
						postConditionsMet = morphParts.firstElement().getAffix().meetsConditions(postConds);
					}
					if (postConditionsMet) {
						if (root.type.equals("v")) {
							boolean transitivityMet = root.meetsTransitivityCondition(transitivity);
							if (transitivityMet)
								accepted = true;
						} else {
							accepted = true;
						}
					} else {
						accepted = false;
					}
				}
			}
        }
         
        return accepted?arcFollowed:null;
	}

	//-----------------------------------------------
	
	private Graph.Arc arcToZero(Graph.Arc[] arcsFollowed) throws TimeoutException {
        for (int i=0; i<arcsFollowed.length; i++) {
        	stpw.check("arcToZero -- arc: "+arcsFollowed[i].toString());
            if (arcsFollowed[i].getDestinationState() == Graph.finalState) {
                return arcsFollowed[i];
            }
        }
        return null;
    }
    
    
    /*
     * 1. Vérifier si ce suffixe est le même que le dernier suffixe trouvé
     * précédemment. Cela permet d'éliminer certaines analyses, entre autres,
     * celles qui retournent le suffixe "a" d'action de groupe deux fois
     * lorsqu'on a un double "a" dans le mot.
     */
    private boolean sameAsNext(Morpheme morpheme, Vector<AffixPartOfComposition> partsAlreadyAnalyzed) throws LinguisticDataException {
        boolean isSameAsNext = false;
        if (partsAlreadyAnalyzed.size() != 0) {
            Affix affPrec = ((AffixPartOfComposition) partsAlreadyAnalyzed.elementAt(0)).getAffix();
            if (morpheme.id.equals(affPrec.id))
                isSameAsNext = true;
        }
        return isSameAsNext;
    }
    
    /*
     * 2. Vérifier si ce suffixe est à la même position dans le mot que le
     * suffixe trouvé précédemment (celui qui le suit dans le mot dans l'analyse
     * courante). Cela permet d'éliminer certaines analyses, entre autres,
     * celles où le suffixe suivant, à cause de son action de suppression,
     * ajoute les caractères supprimés au radical, lesquels caractères sont
     * interprétés comme suffixe. Or un suffixe ne peut logiquement supprimer un
     * autre suffixe.
     */
    private boolean samePosition(int positionAffixInWord, 
            Vector<AffixPartOfComposition> partsAlreadyAnalyzed) {
        boolean isAtSamePosition = false;
        if (partsAlreadyAnalyzed.size() != 0) {
            AffixPartOfComposition nextMorphpart = (AffixPartOfComposition) partsAlreadyAnalyzed
                .elementAt(0);
            if (nextMorphpart.getPosition() == positionAffixInWord) 
                isAtSamePosition = true;       
        }
        return isAtSamePosition;
    }

    
    /*
     * 3. Vérifier si ce suffixe est permis en ce moment. Il doit
     * correspondre à un des arcs partant de l'état actuel.
     */
        
    private Graph.Arc[] arcsSuivis(Morpheme morpheme, Graph.State states[],
			String keyStateIDs) throws TimeoutException, LinguisticDataException {
		Graph.Arc arcsFollowed[] = null;
		String keyMorphemeStateIDs = morpheme.id + ":" + keyStateIDs;
		Graph.Arc[] arcsFollowedByHash = (Graph.Arc[]) arcsByMorpheme
				.get(keyMorphemeStateIDs);
		if (arcsFollowedByHash == null) {
			Vector<Graph.Arc> arcs = null;
			Vector<Graph.Arc> arcsFollowedV = new Vector<Graph.Arc>();
			for (int j = 0; j < states.length; j++) {
				stpw.check("arcsSuivis --- morpheme: "+morpheme.morpheme);
				arcs = states[j].verify(morpheme);
				arcsFollowedV.addAll(arcs);
			}
			if (arcsFollowedV.size() != 0) {
				arcsFollowed = (Graph.Arc[]) arcsFollowedV
						.toArray(new Graph.Arc[] {});
				arcsByMorpheme.put(keyMorphemeStateIDs, arcsFollowed);
			} 
		} else {
			arcsFollowed = arcsFollowedByHash;
		}
		return arcsFollowed;
	}
    
    

    /*
     * 6. Vérifier si le contexte est respecté. La forme candidate
     * trouvée est associée à un contexte de radical et à des actions.
     * On vérifie si le radical et la forme de l'affixe correspondent à
     * ce contexte et à ces actions. Si c'est le cas, on retourne les
     * résultats possibles:
     * 
     * a. radical sans les changements morphologiques causés par
     * l'affixe; b. un objet de classe MorceauAffixe contenant: 1. la
     * position de l'affixe dans le mot (la valeur de i); 2. un objet de
     * classe SurfaceFormOfAffix décrivant totalement l'affixe.
     */
    private Object[][] agreeWithContextAndActions(
    		String affixCandidateOrig,
            Affix affix, 
            String stem, 
            int positionAffixInWord, 
            SurfaceFormOfAffix form,
            boolean notResultingFromDialectalPhonologicalTransformation) throws TimeoutException, MorphInukException, LinguisticDataException {
        Object[][] stemAffs = null;
        boolean checkStartOfConsonantsGroup = true;
        /*
         * Si la forme du candidat affixe est le résultat de changements
         * phonologiques, et si ces changements impliquent la consonne initiale,
         * on ne vérifiera pas la possibilité de changements phonologiques parce
         * qu'on ne veut pas que le groupe de consonne soit vérifié à nouveau.
         */
        if (!notResultingFromDialectalPhonologicalTransformation) {
            if (Roman.isConsonant(form.form.charAt(0)) &&
                    Roman.isConsonant(affixCandidateOrig.charAt(0)) &&
                    form.form.charAt(0) != affixCandidateOrig.charAt(0) )
                checkStartOfConsonantsGroup = false;
        }
        String context = (String) form.context;
        Action action1 = form.action1;
        Action action2 = form.action2;
        stemAffs = validateContextActions(context, action1, action2,
                stem, positionAffixInWord, affix, form, false,
                checkStartOfConsonantsGroup,affixCandidateOrig);
        return stemAffs;
    }
    
    //--------------------- CONTEXT VALIDATION -------------------------------
    
    /*
     * ------- NEUTRAL + NULL
     *
     * Avec une première action neutre et une deuxième action nulle, le
     * contexte sera respecté si les caractères finaux du radical
     * correspondent au contexte spécifié, i.e. une voyelle pour le contexte
     * V, ou les lettres t, k, q pour les contextes t, k, q. Comme l'action
     * est neutre, le résultat est unique: le radical tel quel et le suffixe
     * tel quel.
     * 
     * Note: Il y a un cas où le contexte est nul: les terminaisons
     * démonstratives. On accepte tout simplement.
     */
    protected Vector<Object[]> validate_neutral_null(
    		String context, Action action1, Action action2, String stem, String affixCandidate, 
    		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    		) throws TimeoutException, MorphInukException {
    	
    	Vector<Object[]> res = new Vector<Object[]>();
        // Caractère final du radical.
        char stemEndChar = stem.charAt(stem.length() - 1);
        // Caractère pénultien du radical, s'il existe.

        char formFirstChar = affixCandidate.charAt(0);

        // Type de ces caractères: entier correspondant à C, V.
        int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
        int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

        if (context == null) {
            res.add(new Object[] { stem, stem, partOfComp });
        } else if (context.equals("V")) {
            if (typeOfStemEndChar == Roman.V)
                res.add(new Object[] { stem, stem, partOfComp });
        } else {
            // Treat 'ita' case first.
            String stemOrig = new String(stem);
            if (stemEndChar == 'i' && form.form.length() > 1
                    && form.form.substring(0, 2).equals("ta")) {
                stem = stem + 't';
                stemEndChar = 't';
                typeOfStemEndChar = Roman.C;
            }

            // 
            if (stemEndChar==context.charAt(0))
                res.add(new Object[] { stem, stemOrig, partOfComp });
            
            /*
             * What precedes makes for the final consonants q, t and k of
             * the stem as unmodified consonants. It is also possible that
             * the final consonant of the stem has been changed due to a
             * dialectal phonological phenomenon. The resulting consonant
             * could happen to be one of the context consonants (t,k,q), or
             * another one (s,n,m,...). What follows is to take care of
             * those possibilities.
             */

            // DIALECTALLY EQUIVALENT CONSONANT CLUSTERS
            /*
             * Are there consonant clusters equivalent to that formed by the
             * final consonant of the stem and the initial consonant of the
             * suffix?
             */
            if (true
                    && typeOfStemEndChar == Roman.C 
                    && typeOfFormFirstChar == Roman.C) {
                // Both are consonants
                // Find equivalent clusters
                Vector<String> grs = Dialect.equivalentGroups(stemEndChar,
                        formFirstChar);
                /*
                 * For each equivalent cluster the first consonant of which
                 * is the context consonant and the second is the initial consonant of
                 * the form, add to the result vector a stem
                 * resulting from the current stem with its final consonant
                 * replaced with the context consonant.
                 *  
                 * Ex.: stem='inus' suffix='siut' returns, for context 'k':
                 * new stem='inuk' because of ks <> ss.
                 */
                if (grs != null)
                    for (int i = 0; i < grs.size(); i++) {
                    	stpw.check("validateContextActions -- NEUTRAL, checking equivalent groups");
                        if (((String) grs.elementAt(i)).charAt(0) == context
                                .charAt(0) &&
                                ((String)grs.elementAt(i)).charAt(1) == formFirstChar) {
                            res.add(new Object[] {
                                    stem.substring(0, stem.length() - 1)
                                            + context, stemOrig, partOfComp });
                            break;
                        }
                    }
            }

            /*
             * If the context is a consonant and if the stem ends with a
             * vowel, it might be that the contextual consonant is missing
             * because of Schneider's law. In that case, let's return the
             * suffixe with the modified stems.
             */
            if (typeOfStemEndChar==Roman.V && typeOfFormFirstChar==Roman.C) {
                Object x[] = Dialect.schneiderStateAtEnd(stemOrig);
                boolean doubleConsonants = ((Boolean) x[0]).booleanValue();
                if (doubleConsonants) {
                    /*
                     * The stem ends with a vowel and there are 2 consonants
                     * before it. And the candidate suffix starts with a
                     * consonant. This is a place where Schneider's law
                     * might have worked. Let's add the possibly suppressed
                     * consonant to the stem.
                     */
                    res.add(new Object[] { stemOrig + context,
                                    stemOrig, partOfComp });
                }
            }
        }
        /*
         * S'il y a une condition (cas de ji/1vv)
         */
        String cond = action1.getCondition();
        if (res.size() != 0 && cond != null && cond.startsWith("id:")) {
            // Il faut ajouter à 'affix' une condition spécifique
            // précédente, pour la passer à l'extérieur de cette
            // méthode.
            try {
                Condition avc = new Imacond(
                        new ByteArrayInputStream(cond.getBytes())).ParseCondition();
                affix.addPrecConstraint(avc);
            } catch (ParseException e) {
            	throw new MorphInukException(e);
            }
        } else {
            // Aucun suffixe ne mêne ici.
        }
        
        return res;
    }
    
    /*
     * ------- NEUTRAL + DELETION
     * 
     * L'action 2 est spécifiée pour les suffixes qui se trouvent à être
     * ajoutés à un radical finissant par deux voyelles (souvent après
     * suppression de la consonne finale) et qui commencent (souvent)eux-
     * mêmes par une voyelle, provoquant ainsi une situation par laquelle on
     * se retrouve avec une succession de trois voyelles, ce qui est
     * toujours évité en inuktitut. Dans le contexte V(oyelle): Puisque
     * l'action 1 est 'neutre', il n'y a donc pas de suppression de consonne
     * et conséquemment, cela signifie que la seconde des deux voyelles
     * finales est supprimée. Donc, le contexte sera respecté si le radical
     * se termine par une voyelle (précédée d'une consonne). Dans le
     * contexte consonnantique: L'action1 neutre suggère que normalement, il
     * n'y a pas de suppression de la consonne finale; mais l'action2
     * suggère que si la syllabe finale contient 2 voyelles, la consonne
     * finale sera supprimée.
     */
    protected Vector<Object[]> validate_neutral_deletion(
    		String context, Action action1, Action action2, String stem, String affixCandidate, 
    		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);
		// Caractère pénultien du radical, s'il existe.
		char stemPenultEndChar = (char) -1;
		if (stem.length() > 1)
			stemPenultEndChar = stem.charAt(stem.length() - 2);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfStemPenultEndChar = Roman.typeOfLetterLat(stemPenultEndChar);

		if (context.equals("V")) {
			if (typeOfStemEndChar == Roman.V && (typeOfStemPenultEndChar == -1 || typeOfStemPenultEndChar == Roman.C)) {
				// L'action2 suggère qu'il est possible qu'une voyelle ait
				// été supprimée. Mais pas nécessairement. Le résultat
				// doit donc contenir le radical tel quel, mais aussi le
				// radical auquel on ajoute cette voyelle supprimée.
				res.add(new Object[] { stem, stem, partOfComp }); // tel quel
				res.add(new Object[] { stem + "a", stem, partOfComp });
				res.add(new Object[] { stem + "i", stem, partOfComp });
				res.add(new Object[] { stem + "u", stem, partOfComp });
			}
		} else {
			/*
			 * Dans le contexte consonnantique, avec une première action neutre: l'action 2
			 * suggère que dans le cas où la dernière syllabe contient 2 voyelles suivies
			 * d'une consonne, cette consonne contextuelle sera supprimée. Dans ce cas, on a
			 * un radical qui se termine par deux voyelles.
			 */
			if (stem.length() > 1) {
				char charBeforeStemEndChar = stem.charAt(stem.length() - 2);
				int typeOfCharBeforeStemEndChar = Roman.typeOfLetterLat(charBeforeStemEndChar);
				if (typeOfStemEndChar == Roman.V && typeOfCharBeforeStemEndChar == Roman.V)
					res.add(new Object[] { stem + context, stem, partOfComp });
			}
		}

		return res;
	}
    
    
    
    /*
     * ------- NEUTRAL + INSERTION
     * 
     * L'action 2 est spécifiée pour les suffixes qui commencent par une
     * voyelle. Ces suffixes suppriment normalement la consonne finale du
     * radical auquel ils s'attachent. Si le radical se termine par deux
     * voyelles, avec possiblement une consonne qui sera supprimée, on se
     * retrouve avec 3 voyelles consécutives, ce qui n'est pas permis. Ici,
     * la stratégie est d'insérer quelque chose entre les deux voyelles du
     * radical et la voyelle du suffixe.
     * 
     * Ici, l'action 1 est neutre, donc pas de suppression de consonne.
     * (Ceci n'arrive en fait que dans le contexte de voyelle.)
     * 
     * Dans ce cas, le contexte sera respecté uniquement si le radical se
     * termine par deux voyelles suivies du caractère à insérer, ou s'il se
     * termine par une voyelle. Dans le premier cas, le résultat est unique:
     * le radical sans le caractère à insérer et le suffixe avec le
     * caractère à insérer. Dans le second cas, le résultat est unique
     * aussi: le radical tel quel et le suffixe tel quel.
     */
    protected Vector<Object[]> validate_neutral_insertion(
    		String context, Action action1, Action action2, String stem, String affixCandidate, 
    		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

		boolean isSyllabic = false;
		String insert = Orthography.simplifiedOrthography(action2.getInsert(), isSyllabic);
		int linsert = insert.length();
		int lstem = stem.length();

		if (context.equals("V")) {
			// Dans le contexte d'un radical se terminant par une voyelle,
			// avec une première action neutre:

			// Si le radical se termine avec le caractère à insérer et
			// qu'il est précédé par deux voyelles, le contexte est
			// possible:
			// le radical possible pour la suite de l'analyse est donc le
			// radical actuel sans le caractère à insérer.

			// La seule autre possibilité est que le radical se termine par
			// une voyelle, car aucune autre consonne que la consonne à
			// insérer
			// ne peut s'y trouver.
			if (stem.endsWith(insert) && lstem > linsert + 2
					&& Roman.typeOfLetterLat(stem.charAt(lstem - linsert - 1)) == Roman.V
					&& Roman.typeOfLetterLat(stem.charAt(lstem - linsert - 2)) == Roman.V) {
				AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - linsert, form);
				res.add(new Object[] { stem.substring(0, lstem - linsert), stem.substring(0, lstem - linsert),
						npartOfComp });
			} else if (typeOfStemEndChar == Roman.V)
				res.add(new Object[] { stem, stem, partOfComp });
		}

		return res;
	}
    
    /*
     * ------- DELETION
     * 
     * La suppression comme action première, sans seconde action, est
     * toujours celle d'une consonne. On n'a donc pas à tenir compte du
     * contexte de voyelle.
     * 
     * Le contexte sera respecté selon les conditions suivantes:
     * 
     * Dans le contexte d'une consonne, le contexte sera respecté si le
     * radical se termine par une voyelle (étant donné qu'un radical ne peut
     * terminer par deux consonnes), et le résultat sera unique: le radical
     * augmenté de la consonne contextuelle, et le suffixe tel quel.
     */
   protected Vector<Object[]> validate_deletion_null(
    		String context, Action action1, Action action2, String stem, String affixCandidate, 
    		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        // on suppose un contexte de consonne spécifique, i.e. t, k, q
        if (context.equals("t") || context.equals("k")
                || context.equals("q"))
            if (typeOfStemEndChar == Roman.V) {
                res.add(new Object[] { stem + context, stem, partOfComp });
                // Le dialecte d'Aivilik utilise "r" plutôt que "q":
                // on ajoute cette possibilité.
                // 		    if (context.equals("q"))
                // 			res.add(new Object [] {stem+"r",stem,partOfComp});
            }
        
        return res;
    }

   /*
    * //----- DELETION ET INSERTION
    * 
    * La suppression de la consonne finale du radical est suivie de
    * l'insertion de caractères. Le contexte sera donc respecté si le
    * radical se termine par les caractères d'insertion précédé d'une
    * voyelle. Puisque l'action2 est nulle, on ne s'occupe pas du cas où il
    * y a deux voyelles. Le résultat est unique: le radical sans les
    * caractères insérés et augmenté de la consonne contextuelle, et le
    * suffixe tel quel.
    */
   protected Vector<Object[]> validate_deletion_insertion(
   		String context, Action action1, Action action2, String stem, String affixCandidate, 
   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();

		boolean isSyllabic = false;
        String carsInsere = Orthography.simplifiedOrthography(action1
                .getInsert(), isSyllabic);
        int linsert = carsInsere.length();
        int lstem = stem.length();

        if ((stem.endsWith(carsInsere))) {

            AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - linsert,
                    form);
            res.add(new Object[] {
                    stem.substring(0, lstem - linsert) + context,
                    stem.substring(0, lstem - linsert), npartOfComp });
        }
        
        return res;
   }
   
   /*
    * ----- DELETION CONDITIONNELLE
    * 
    * La suppression de la consonne finale du radical est conditionnelle à
    * la présence dans le radical de la séquence définie par l'expression
    * régulière de la condition. Le contexte sera respecté si le radical se
    * termine par une voyelle (étant donné qu'un radical ne peut terminer
    * par deux consonnes), et si la condition sur le radical est respectée,
    * et le résultat sera unique: le radical augmenté de la consonne
    * contextuelle, et le suffixe tel quel.
    * 
    * On a aussi un cas où la suppression conditionnelle s'exerce dans un
    * contexte de voyelle: le suffixe antipassif -ji/1vv- après -uti/1vv- où le
    * 'i' de 'uti' est supprimé par contraction.
    */
   protected Vector<Object[]> validate_conditionaldeletion_null(
	   		String context, Action action1, Action action2, String stem, String affixCandidate, 
	   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
	   		boolean checkPossibleDialectalChanges
	   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);
		char formFirstChar = affixCandidate.charAt(0);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

		// on suppose un contexte de consonne spécifique, i.e. t, k, q
		if (((context.equals("t") || context.equals("k") || context.equals("q")) && typeOfStemEndChar == Roman.V)
				|| (context.equals("V") && typeOfStemEndChar == Roman.C)) {
			String cond = action1.getCondition();
			if (cond.startsWith("id:")) {
				// Il faut ajouter à 'affix' une condition spécifique
				// précédente, pour la passer à l'extérieur de cette
				// méthode.
				try {
					Condition avc = new Imacond(new ByteArrayInputStream(cond.getBytes())).ParseCondition();
					affix.addPrecConstraint(avc);
				} catch (ParseException e) {
					throw new MorphInukException(e);
				}
				if (!context.equals("V"))
					// Suppression de consonne
					res.add(new Object[] { stem + context, stem, partOfComp });
				else {
					// Suppression de voyelle
					String formInCond = cond.substring(3, cond.indexOf("/"));
					// 'form' devrait finir avec une voyelle (ex.: uti)
					res.add(new Object[] { stem + formInCond.substring(formInCond.length() - 1), stem, partOfComp });
					/*
					 * Comme il y a eu suppression de voyelle, la dernière lettre du radical peut
					 * être une voyelle ou une consonne. Si c'est une consonne, et que le suffixe
					 * commence par une consonne, il est possible qu'il y ait eu un changement
					 * phonologique dialectal dans le groupe de consonnes.
					 */
					if (typeOfStemEndChar == Roman.C && typeOfFormFirstChar == Roman.C
							&& checkPossibleDialectalChanges) {
						Vector<String> grs = Dialect.equivalentGroups(stemEndChar, formFirstChar);
						if (grs != null)
							for (int i = 0; i < grs.size(); i++) {
								stpw.check(
										"validateContextActions -- CONDITIONALDELETION+NULLACTION, checking equivalent groups");
								if (((String) grs.elementAt(i)).charAt(1) == formFirstChar) {
									res.add(new Object[] {
											stem.substring(0, stem.length() - 1) + ((String) grs.elementAt(i)).charAt(0)
													+ formInCond.substring(formInCond.length() - 1),
											stem, partOfComp }); // or 'form' ?
								}
							}
					}
				}
			} else {
				Pattern pattern = Pattern.compile(action1.getCondition());
				Matcher matcher = pattern.matcher(stem);
				if (matcher.find()) {
					res.add(new Object[] { stem + context, stem, partOfComp });
				}
			}
		}

		return res;
	}
   

   /*
    * ----- VOICING
    * 
    * La sonorisation affecte toujours une consonne. Elle n'a de sens que
    * dans un contexte de consonne. Si le contexte spécifié est une
    * consonne définie, le radical doit se terminer par la version sonore
    * de cette consonne. Par exemple, 'g' remplace 'k'. Mais il est aussi
    * possible que ce 'g' soit assimilé à la consonne suivante par
    * gémination dialectale; par exemple, 'g' devant 'v' devient 'v'. C'est
    * possible par exemple dans le mot 'ikupivvilirijikkunnut' = ikupik vik
    * liri ji kkut nut Le résultat sera unique: le radical dont la consonne
    * finale est remplacée par sa version sourde, et le suffixe tel quel.
    * 
    */
   protected Vector<Object[]> validate_voicing_null(
	   		String context, Action action1, Action action2, String stem, String affixCandidate, 
	   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
	   		boolean checkPossibleDialectalChanges
	   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		char formFirstChar = affixCandidate.charAt(0);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

        if (!context.equals("V")) {
            char voicedCorrespondingChar = Roman.voicedOfOcclusiveUnvoicedLat(context.charAt(0));
            if (stemEndChar == voicedCorrespondingChar) {
                res.add(new Object[] {
                        stem.substring(0, stem.length() - 1) + context, stem,
                        partOfComp });
            } else if (typeOfStemEndChar == Roman.V) {
                /*
                 * If the context is a consonant and if the stem ends with a
                 * vowel, it might be that the contextual consonant is
                 * missing because of Schneider's law. In that case, let's
                 * return the suffixe with the modified stems.
                 */
                if (typeOfStemEndChar==Roman.V && typeOfFormFirstChar==Roman.C) {
                    Object x[] = Dialect.schneiderStateAtEnd(stem);
                    boolean doubleConsonants = ((Boolean) x[0]).booleanValue();
                    if (doubleConsonants) {
                        /*
                         * The stem ends with a vowel and there are 2
                         * consonants before it. And the candidate suffix
                         * starts with a consonant. This is a place where
                         * Schneider's law might have worked. Let's add the
                         * possibly suppressed consonant to the stem.
                         */
                        res.add(new Object[] { stem + context,
                                        stem, partOfComp });
                    }
                }
            } else if (checkPossibleDialectalChanges){
                Vector<String> grs = Dialect.equivalentGroups(stemEndChar,
                        formFirstChar);
                if (grs != null)
                    for (int i = 0; i < grs.size(); i++) {
                    	stpw.check("validateContextActions -- VOICING+NULLACTION, checking equivalent groups");
                        if (((String) grs.elementAt(i)).charAt(0) == voicedCorrespondingChar &&
                                ((String)grs.elementAt(i)).charAt(1) == formFirstChar) {
                            res.add(new Object[] {
                                    stem.substring(0, stem.length() - 1)
                                            + context, stem, partOfComp });
                            break;
                        }
                    }
            }
        }
        
        return res;
   }
   

   /*
    * ----- NASALIZATION
    * 
    * La nasalisation affecte toujours une consonne. Elle n'a de sens que
    * dans un contexte de consonne.
    * 
    * Si le contexte spécifié est une consonne définie, le radical doit se
    * terminer par la version nasale de cette consonne. Par exemple, 'N'
    * (ng) remplace 'k'. Mais il est possible que ce 'N' soit assimilé à la
    * consonne suivante par gémination dialectale; par exemple, 'N' devant
    * 'n' devient 'n'. C'est possible par exemple dans le mot
    * 'atangirasunniaqqauviit' ('Were you planning to finish all at once?')
    * = atangiq nasuk niaqqau viit, où le 'k' de 'nasuk', qui devrait être
    * nasalisé en 'N (ng)' est plutôt assimilé à 'n'. Le résultat sera
    * unique: le radical dont la consonne finale est remplacée par sa
    * version sourde, et le suffixe tel quel.
    */
   protected Vector<Object[]> validate_nasalization_null(
	   		String context, Action action1, Action action2, String stem, String affixCandidate, 
	   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
	   		boolean checkPossibleDialectalChanges
	   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		char formFirstChar = affixCandidate.charAt(0);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

        if (!context.equals("V")) {
            // Consonant context
            char nasalCorrespondingChar = Roman.nasalOfOcclusiveUnvoicedLat(context.charAt(0));
            if (stemEndChar == nasalCorrespondingChar) {
                res.add(new Object[] {
                        stem.substring(0, stem.length() - 1) + context, stem,
                        partOfComp });
            } else if (typeOfStemEndChar == Roman.V) {
                /*
                 * If the context is a consonant and if the stem ends with a
                 * vowel, it might be that the contextual consonant is
                 * missing because of Schneider's law. In that case, let's
                 * return the suffixe with the modified stems.
                 */
                if (typeOfStemEndChar==Roman.V && typeOfFormFirstChar==Roman.C) {
                    Object x[] = Dialect.schneiderStateAtEnd(stem);
                    boolean doubleConsonants = ((Boolean) x[0]).booleanValue();
                    if (doubleConsonants) {
                        /*
                         * The stem ends with a vowel and there are 2
                         * consonants before it. And the candidate suffix
                         * starts with a consonant. This is a place where
                         * Schneider's law might have worked. Let's add the
                         * possibly suppressed consonant to the stem.
                         */
                        res.add(new Object[] { stem + context,
                                        stem, partOfComp });
                    }
                }
            } else if (checkPossibleDialectalChanges) {
                Vector<String> grs = Dialect.equivalentGroups(stemEndChar,
                        formFirstChar);
                if (grs != null)
                    for (int i = 0; i < grs.size(); i++) {
                    	stpw.check("validateContextActions -- NASALISATION, checking equivalent groups");
                        if (((String) grs.elementAt(i)).charAt(0) == nasalCorrespondingChar &&
                                ((String)grs.elementAt(i)).charAt(1) == formFirstChar) {
                            res.add(new Object[] {
                                    stem.substring(0, stem.length() - 1)
                                            + context, stem, partOfComp });
                            break;
                        }
                    }
            }
        }
        
        return res;
   }
   
   /*
    * ------- NASALIZATION CONDITIONNELLE
    */
   protected Vector<Object[]> validate_conditionalnasalization_null(
	   		String context, Action action1, Action action2, String stem, String affixCandidate, 
	   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
	   		boolean checkPossibleDialectalChanges
	   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		char formFirstChar = affixCandidate.charAt(0);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

        String cond = action1.getCondition();
        // Il faut ajouter à 'affix' une condition spécifique
        // précédente, pour la passer à l'extérieur de cette
        // méthode.
        char nasalCorrespondingChar = Roman.nasalOfOcclusiveUnvoicedLat(context.charAt(0));
        if (stemEndChar == nasalCorrespondingChar) {
            try {
                Condition avc = new Imacond(
                        new ByteArrayInputStream(cond.getBytes())).ParseCondition();
                affix.addPrecConstraint(avc);
            } catch (ParseException e) {
            	throw new MorphInukException(e);
            }
            res.add(new Object[] {
                    stem.substring(0, stem.length() - 1) + context, stem,
                    partOfComp });
        } else if (typeOfStemEndChar == Roman.V) {
            /*
             * If the context is a consonant and if the stem ends with a
             * vowel, it might be that the contextual consonant is
             * missing because of Schneider's law. In that case, let's
             * return the suffixe with the modified stems.
             */
            if (typeOfStemEndChar==Roman.V && typeOfFormFirstChar==Roman.C) {
                Object x[] = Dialect.schneiderStateAtEnd(stem);
                boolean doubleConsonants = ((Boolean) x[0]).booleanValue();
                if (doubleConsonants) {
                    try {
                        Condition avc = new Imacond(
                                new ByteArrayInputStream(cond.getBytes())).ParseCondition();
                        affix.addPrecConstraint(avc);
                    } catch (ParseException e) {
                    	throw new MorphInukException(e);
                    }
                    /*
                     * The stem ends with a vowel and there are 2
                     * consonants before it. And the candidate suffix
                     * starts with a consonant. This is a place where
                     * Schneider's law might have worked. Let's add the
                     * possibly suppressed consonant to the stem.
                     */
                    res.add(new Object[] { stem + context,
                                    stem, partOfComp });
                }
            }
        } else if (checkPossibleDialectalChanges) {
            Vector<String> grs = Dialect.equivalentGroups(stemEndChar,
                    formFirstChar);
            if (grs != null)
                for (int i = 0; i < grs.size(); i++) {
                	stpw.check("validateContextActions -- CONDITIONAL NASALIZATION, checking equivalent groups");
                    if (((String) grs.elementAt(i)).charAt(0) == nasalCorrespondingChar &&
                            ((String)grs.elementAt(i)).charAt(1) == formFirstChar) {
                        try {
                            Condition avc = new Imacond(
                                    new ByteArrayInputStream(cond.getBytes())).ParseCondition();
                            affix.addPrecConstraint(avc);
                        } catch (ParseException e) {
                        	throw new MorphInukException(e);
                        }
                        res.add(new Object[] {
                                stem.substring(0, stem.length() - 1)
                                        + context, stem, partOfComp });
                        break;
                    }
                }
        }
        
        return res;
   }
      
   /*
    * ------- INSERTION
    * 
    * Pour que le contexte soit respecté, il faut que le radical se termine
    * par le caractère correspondant au contexte spécifié. Le résultat est
    * unique: le radical sans les caractères insérés, et le suffixe tel
    * quel.  La forme du suffixe passée ici contient le caractère inséré, par
    * exemple vvik pour vik dans le contexte de voyelle.
    */
   protected Vector<Object[]> validate_insertion_null(
	   		String context, Action action1, Action action2, String stem, String affixCandidate, 
	   		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
	   		boolean checkPossibleDialectalChanges
	   		) throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();

        if ((context.equals("V") && 
                (stem.endsWith("a")
                        || stem.endsWith("i") 
                        || stem.endsWith("u")))
                        || (stem.endsWith(context))) {
            res.add(new Object[] { stem,
                    stem, partOfComp });
        }
        
        return res;
   }


   /*
    * ------- FUSION
    * 
    * La fusion est toujours spécifiée dans un contexte défini de consonne.
    * Elle implique que la consonne finale du radical, spécifiée par le
    * contexte, a été en fait supprimée pour être fondue dans l'initiale du
    * suffixe. Cela signifie que le contexte ne peut être respecté que si
    * le radical se termine par une voyelle. Le résultat sera unique: le
    * radical auquel on a ajouté la consonne spécifiée par le contexte, et
    * le suffixe tel quel.
    */
	protected Vector<Object[]> validate_fusion_null(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

		if ((context.equals("t") || context.equals("k") || context.equals("q")) && typeOfStemEndChar == Roman.V)
			res.add(new Object[] { stem + context, stem, partOfComp });

		return res;
	}
	
    /*
     * ------- ASSIMILATION
     * 
     * L'assimilation est toujours spécifiée dans un contexte défini de
     * consonne. Pour que le contexte soit respecté, il faut que la dernière
     * lettre du radical soit une consonne, identique à la consonne initiale
     * du suffixe. Le résultat sera unique: le radical dont la consonne
     * finale assimilée est remplacée par la consonne spécifiée par le
     * contexte, et le suffixe tel quel.
     */
	protected Vector<Object[]> validate_assimilation_null(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		char formFirstChar = affixCandidate.charAt(0);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);
		int typeOfFormFirstChar = Roman.typeOfLetterLat(formFirstChar);

        if (typeOfStemEndChar == Roman.C && stemEndChar == form.form.charAt(0))
            res
                    .add(new Object[] {
                            stem.substring(0, stem.length() - 1) + context,
                            stem, partOfComp });
        /*
         * If the context is a consonant and if the stem ends with a vowel,
         * it might be that the contextual consonant is missing because of
         * Schneider's law. In that case, let's return the suffixe with the
         * modified stems.
         */
        if (typeOfStemEndChar==Roman.V && typeOfFormFirstChar==Roman.C) {
            Object x[] = Dialect.schneiderStateAtEnd(stem);
            boolean doubleConsonants = ((Boolean) x[0]).booleanValue();
            if (doubleConsonants) {
                /*
                 * The stem ends with a vowel and there are 2 consonants
                 * before it. And the candidate suffix starts with a
                 * consonant. This is a place where Schneider's law might
                 * have worked. Let's add the possibly suppressed consonant
                 * to the stem.
                 */
                res.add(new Object[] { stem + context,
                                stem, partOfComp });
            }
        }
        
        return res;
	}
	
    /*
     * ------- SPECIFICASSIMILATION
     * 
     * L'assimilation spécifique d'une consonne dans un contexte donné
     * signifie qu'un radical verra sa consonne finale (contexte) assimilée
     * (changée) à la lettre spécifiée. Pour que le contexte soit respecté,
     * il faut que le radical se termine par la consonne désignée (ex.
     * assim(k) -> k). Le résultat est unique: le radical avec la consonne
     * finale remplacée par le contexte, et le suffixe tel quel.
     */
	protected Vector<Object[]> validate_specificassimilation_null(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

        if (stemEndChar == action1.getAssimA().charAt(0))
            res
                    .add(new Object[] {
                            stem.substring(0, stem.length() - 1) + context,
                            stem, partOfComp });
        
        return res;
	}
	
	

    /*
     * ------- DELETION + SPECIFICDELETION
     * 
     * Ces actions combinées ne se rencontrent que pour les suffixes -it- et
     * -ut-, dans le contexte d'un radical en -q. Le contexte sera
     * donc respecté si la dernière lettre du radical est une voyelle, parce
     * que l'action 2 suppose la présence de deux voyelles à la fin du
     * radical, une fois la consonne finale supprimée. Il y a 2
     * possibilités: 1) le radical n'a pas deux voyelles devant la consonne
     * contextuelle (q); il y a eu suppression de cette consonne: le
     * résultat est le radical auquel on ajoute la consonne. 2) le radical
     * se termine par deux voyelles, dont la seconde est définie par
     * l'action 2; il y a eu suppression de la consonne et de cette voyelle:
     * le résultat est le radical auquel on ajoute la voyelle puis la
     * consonne.  Dans ce dernier cas, s'il y a condition à la suppression,
     * elle doit être respectée par le radical.
     */
	protected Vector<Object[]> validate_deletion_specificdeletion(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        if (typeOfStemEndChar == Roman.V) {
            res.add(new Object[] { stem + context, stem, partOfComp });
            if (action2.getCondition() != null) {
                String cond = action2.getCondition();
                Pattern p = Pattern.compile(cond);
                Matcher m = p.matcher(stem);
                if (m.find())
                    res.add(new Object[] { stem + action2.getSuppr() + context, stem,
                            partOfComp });
            } else
                res.add(new Object[] { stem + action2.getSuppr() + context, stem,
                        partOfComp });
        }
        
        return res;
	}
	
	
    

    /*
     * ------- DELETION + INSERTION
     * 
     * Cette combinaison d'action se rencontre dans les contexts de
     * consonne.
     *
     * Pour que le contexte soit respecté, il faut ce qui suit:
     * 
     * a) si le radical se termine par deux voyelles et les caractères à
     * insérer:
     *    1. si le radical se termine par une voyelle:
     *       (les caractères à insérer se terminent par une voyelle)
     *       il y a 2 résultats:
     *           i. le radical sans les caractères insérés + consonne de contexte,
     *              et le suffixe avec les caractères insérés;
     *          ii. le radical + consonne de contexte,
     *              et le suffixe tel quel sans les caractères insérés.
     *    2. si le radical ne se termine pas par une voyelle:
     *       (les caractères à insérer ne se terminent pas par une voyelle)
     *       il n'y a qu'un seul résultat:
     *           i. le radical sans les caractères insérés + consonne de contexte,
     *              et le suffixe avec les caractères insérés.
     * b) si le radical se termine par une seule voyelle:
     *    1. il y a un résultat unique:
     *       i. le radical + consonne de contexte,
     *          et le suffixe tel quel sans les caractères insérés.
     */
	protected Vector<Object[]> validate_deletion_insertion(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();

		boolean isSyllabic = false;
        String insert = Orthography.simplifiedOrthography(action2
                .getInsert(), isSyllabic);
        int lstem = stem.length();
        int linsert = insert.length();
        String cntx = null;

        // Note: Le contexte de la forme vérifiée est V, t, k ou q
        if (context.equals("V"))
            cntx = "";
        else
            cntx = context;

        if (stem.endsWith(insert)
                && lstem - linsert > 2
                && Roman.typeOfLetterLat(stem.charAt(lstem - linsert - 1)) == Roman.V
                && Roman.typeOfLetterLat(stem.charAt(lstem - linsert - 2)) == Roman.V)
            if (Roman.typeOfLetterLat(stem.charAt(lstem - 1)) == Roman.V
                    && Roman.typeOfLetterLat(stem.charAt(lstem - 2)) == Roman.C) {

                AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - linsert,
                        form);
                res.add(new Object[] {
                        stem.substring(0, lstem - linsert) + cntx,
                        stem.substring(0, lstem - linsert), npartOfComp });
                res.add(new Object[] { stem + cntx, stem, partOfComp });
            } else {
                AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - linsert,
                        form);
                res.add(new Object[] {
                        stem.substring(0, lstem - linsert) + cntx,
                        stem.substring(0, lstem - linsert), npartOfComp });
            }

        else if (lstem > 2
                && Roman.typeOfLetterLat(stem.charAt(lstem - 1)) == Roman.V
                && Roman.typeOfLetterLat(stem.charAt(lstem - 2)) == Roman.C)
            res.add(new Object[] { stem + cntx, stem, partOfComp });

        else
            ;
        
        return res;
	}
	
	

    /*
     * ------- VOWELLENGTHENING + ANNULATION
     * 
     * L'allongement de la voyelle finale d'un nom par une terminaison
     * nominale est annulée si cette voyelle finale est elle-même précédée
     * d'une autre voyelle, i.e. si le radical se termine par 2 voyelles. Le
     * contexte sera respecté si:
     * 
     * a) le radical se termine par deux voyelles identiques.
     *    - Le résultat est le radical sans la dernière voyelle (résultat de
     *      l'allongement) et le suffixe tel quel.
     *    - Il y a aussi possibilité que le radical lui-même se termine par
     *      ces deux voyelles identiques, auquel cas il n'y a pas d'
     *      allongement; il y a donc un second résultat possible: le radical
     *      tel quel et le suffixe tel quel.
     * b) le radical se termine par deux voyelles différentes.
     *    - Le résultat est le radical tel quel (parce qu'alors,
     *      l'allongement de la voyelle finale est annulé) et le suffixe tel
     *      quel.
     * 
     */
	protected Vector<Object[]> validate_vowellengthening_cancellation(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        if (typeOfStemEndChar == Roman.V) {
            if (stem.length() > 3
                    && stem.charAt(stem.length() - 2) == stemEndChar) {

                AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - 1, form);
                res.add(new Object[] { stem.substring(0, stem.length() - 1),
                        stem.substring(0, stem.length() - 1), npartOfComp });
                res.add(new Object[] { stem, stem, partOfComp });
            } else if (stem.length() > 3
                    && Roman.typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.V) {

                res.add(new Object[] { stem, stem, partOfComp });
            }
        }
        
        return res;
	}
	
	
    /*
     * ------- DELETIONVOWELLENGTHENING + ANNULATION
     * 
     * La suppression de la consonne finale du radical est suivie de l'
     * allongement de la voyelle précédant cette consonne. Si cette voyelle
     * est elle-même précédée d'une autre voyelle, l'allongement est annulé.
     * Le contexte est respecté dans les mêmes conditions que ci-dessus pour
     * l'allongement de voyelle + annulation. Il y a aussi les mêmes
     * résultats, sauf qu'à cause de la suppression, le radical se voit
     * ajouter la consonne du contexte. (voir suppression ci-dessus)
     */
	protected Vector<Object[]> validate_deletionvowellengthening_cancellation(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        if (typeOfStemEndChar == Roman.V) {
            if (stem.length() > 3
                    && stem.charAt(stem.length() - 2) == stemEndChar) {

                AffixPartOfComposition npartOfComp = new AffixPartOfComposition(posAffix - 1, form);
                res.add(new Object[] {
                        stem.substring(0, stem.length() - 1) + context,
                        stem.substring(0, stem.length() - 1), npartOfComp });
                res.add(new Object[] { stem + context, stem, partOfComp });
            } else if (stem.length() > 3
                    && Roman.typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.V) {

                res.add(new Object[] { stem + context, stem, partOfComp });
            }
        }
        
        return res;
	}
	
	
    /*
     * ------- INSERTIONVOWELLENGTHENING
     * 
     * Ceci n'arrive que dans le contexte d'une finale nominale en t. Un i
     * est inséré après le t final du radical, et allongé avant l'addition
     * du suffixe. (L'allongement est une marque du duel). Le contexte est
     * respecté si le radical se termine par la consonne de contexte et deux
     * occurrences de la voyelle insérée.
     */
	protected Vector<Object[]> validate_insertionvowellengthening_null(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

        if (stemEndChar == action1.getInsert().charAt(0) && stem.length() > 3
                && stem.charAt(stem.length() - 2) == stemEndChar
                && stem.charAt(stem.length() - 3) == context.charAt(0)) {
            res.add(new Object[] { stem.substring(0, stem.length() - 2),
                    stem.substring(0, stem.length() - 2), partOfComp });
        }
        
        return res;
	}
	
	
    /*
     * ------- NEUTRAL + DECAPITATION
     * 
     * Si le radical termine par 1 voyelle, l'autodécapitation n'a pas lieu
     * et la forme du suffixe devrait être la forme intégrale. Dans ce cas,
     * le contexte est respecté. Si le radical termine par 2 voyelles, la
     * voyelle initiale du suffixe/ terminaison (puisque ceci n'arrive que
     * pour les suffixes/terminaisons commençant par une voyelle) est
     * supprimée, et la forme devrait être la forme autodécapitée. Le
     * contexte est respecté si le radical se termine par deux voyelles.
     */
	protected Vector<Object[]> validate_neutral_selfdecapitation(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException, LinguisticDataException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        if (typeOfStemEndChar == Roman.V) {
            if (stem.length() > 1)
                if (Roman.typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.V
                        && form.form.length() == form.getAffix().morpheme
                                .length() - 1) {
                    res.add(new Object[] { stem, stem, partOfComp });
                } else if (Roman
                        .typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.C
                        && form.form.length() == form.getAffix().morpheme
                                .length()) {
                    res.add(new Object[] { stem, stem, partOfComp });
                }
        }
        
        return res;
	}
	
	
    /*
     * ------- DELETION + DECAPITATION
     * 
     * La consonne finale du radical est supprimée. Si cette consonne finale
     * supprimée est précédée de deux voyelles, la voyelle initiale du
     * suffixe/ terminaison est supprimée. Mêmes conditions que ci-dessus
     * pour que le contexte soit respecté. Même résultat, sauf que la
     * consonne de contexte est ajoutée au radical.
     */
  	protected Vector<Object[]> validate_deletion_selfdecapitation(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException, LinguisticDataException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        if (typeOfStemEndChar == Roman.V) {
            if (stem.length() > 2)
                if (Roman.typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.V
                        && form.form.length() == form.getAffix().morpheme
                                .length() - 1) {
                    res.add(new Object[] { stem + context, stem, partOfComp });
                } else if (Roman
                        .typeOfLetterLat(stem.charAt(stem.length() - 2)) == Roman.C
                        && form.form.length() == form.getAffix().morpheme
                                .length()) {
                    res.add(new Object[] { stem + context, stem, partOfComp });
                }
        }
        
        return res;
	}
  	
  	
    /*
     * ------- DELETION + DELETION
     * 
     * La consonne finale du radical est supprimée. Si cette consonne finale
     * supprimée est précédée de deux voyelles, la dernière voyelle du
     * radical est supprimée.
     */
	protected Vector<Object[]> validate_deletion_deletion(String context, Action action1, Action action2, String stem,
			String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
			AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
			throws TimeoutException, MorphInukException {

		Vector<Object[]> res = new Vector<Object[]>();
		// Caractère final du radical.
		char stemEndChar = stem.charAt(stem.length() - 1);

		// Type de ces caractères: entier correspondant à C, V.
		int typeOfStemEndChar = Roman.typeOfLetterLat(stemEndChar);

        // Puisque ce suffixe supprime la consonne précédente, le radical
        // actuel
        // doit se terminer par une voyelle.
        if (typeOfStemEndChar == Roman.V) {
            // La deuxième action indique qu'un radical terminant avec deux
            // voyelles
            // voit la dernière supprimée. Le radical actuel ne peut donc
            // avoir
            // qu'une seule voyelle.
            res.add(new Object[] { stem + context, stem, partOfComp });
            res.add(new Object[] { stem + "a" + context, stem, partOfComp });
            res.add(new Object[] { stem + "i" + context, stem, partOfComp });
            res.add(new Object[] { stem + "u" + context, stem, partOfComp });
        }
        
        return res;
	}

	public static void removeFromCache(String word) {
		removeFromCache(word, null);
	}

	public static synchronized void removeFromCache(String word, Boolean extendedAnalyses) {
		if (extendedAnalyses == null) {
			extendedAnalyses = false;
		}
		
		String key = cacheKeyFor(word, extendedAnalyses);
		
		decompsCache.invalidate(key);
	}


	private static String cacheKeyFor(String word, boolean extendedAnalyses) {
		String key = word; 
		if (extendedAnalyses) {
			key += "/extended";
		}
		return key;
	}
}