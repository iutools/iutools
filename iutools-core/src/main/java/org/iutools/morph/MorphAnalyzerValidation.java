package org.iutools.morph;

import org.iutools.linguisticdata.Action;
import org.iutools.linguisticdata.Affix;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.SurfaceFormOfAffix;
import org.iutools.linguisticdata.constraints.Condition;
import org.iutools.linguisticdata.constraints.Imacond;
import org.iutools.linguisticdata.constraints.ParseException;
import org.iutools.phonology.Dialect;
import org.iutools.script.Orthography;
import org.iutools.script.Roman;
import org.iutools.utilities.StopWatch;

import java.io.ByteArrayInputStream;
import java.util.Vector;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MorphAnalyzerValidation {

    private static StopWatch stpw;

    public static void setStopWatch(StopWatch _stpw) {
        stpw = _stpw;
    }

    public static ContextualResult[] validateContextActions(String context,
		Action action1, Action action2, String stem, int posAffix,
		Affix affix, SurfaceFormOfAffix form, boolean isSyllabic,
		boolean checkPossibleDialectalChanges,
		String affixCandidate) throws TimeoutException, MorphologicalAnalyzerException {

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

        try {
			  if (action1Type == Action.NEUTRAL && action2Type == Action.NULLACTION) {
				  res = validate_neutral_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp);
			  } else if (action1Type == Action.NEUTRAL && action2Type == Action.DELETION) {
				  res = validate_neutral_deletion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp);
			  } else if (action1Type == Action.NEUTRAL && action2Type == Action.INSERTION) {
				  res = validate_neutral_insertion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp);
			  } else if (action1Type == Action.DELETION && action2Type == Action.NULLACTION) {
				  res = validate_deletion_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp);
			  } else if (action1Type == Action.DELETIONINSERTION && action2Type == Action.NULLACTION) {
				  res = validate_deletion_insertion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp);
			  } else if (action1Type == Action.CONDITIONALDELETION && action2Type == Action.NULLACTION) {
				  res = validate_conditionaldeletion_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.VOICING && action2Type == Action.NULLACTION) {
				  res = validate_voicing_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.NASALIZATION) {
				  res = validate_nasalization_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.CONDITIONALNASALIZATION) {
				  res = validate_conditionalnasalization_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.INSERTION && action2Type == Action.NULLACTION) {
				  res = validate_insertion_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.FUSION && action2Type == Action.NULLACTION) {
				  res = validate_fusion_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.ASSIMILATION && action2Type == Action.NULLACTION) {
				  res = validate_assimilation_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.SPECIFICASSIMILATION && action2Type == Action.NULLACTION) {
				  res = validate_specificassimilation_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.DELETION && action2Type == Action.SPECIFICDELETION) {
				  res = validate_deletion_specificdeletion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.DELETION && action2Type == Action.INSERTION) {
				  res = validate_deletion_insertion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.VOWELLENGTHENING && action2Type == Action.CANCELLATION) {
				  res = validate_vowellengthening_cancellation(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.DELETIONVOWELLENGTHENING && action2Type == Action.CANCELLATION) {
				  res = validate_deletionvowellengthening_cancellation(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.INSERTIONVOWELLENGTHENING && action2Type == Action.NULLACTION) {
				  res = validate_insertionvowellengthening_null(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.NEUTRAL && action2Type == Action.SELFDECAPITATION) {
				  res = validate_neutral_selfdecapitation(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.DELETION && action2Type == Action.SELFDECAPITATION) {
				  res = validate_deletion_selfdecapitation(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  } else if (action1Type == Action.DELETION && action2Type == Action.DELETION) {
				  res = validate_deletion_deletion(context, action1, action2, stem, affixCandidate,
				  form, affix, posAffix, partOfComp, checkPossibleDialectalChanges);
			  }
		  } catch (LinguisticDataException e) {
        	throw new MorphologicalAnalyzerException(e);
		  }


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
        else {
            Vector<ContextualResult> contextualResults = new Vector<>();
            for (Object[] x : res) {
                contextualResults.add(new ContextualResult((String)x[0],(String)x[1],(AffixPartOfComposition)x[2]));
            }
//			return (Object[][]) res.toArray(new Object[][]{});
            return (ContextualResult[]) contextualResults.toArray(new ContextualResult[]{});
        }
    }

    //----------------------------------------

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
    protected static Vector<Object[]> validate_neutral_null(
		String context, Action action1, Action action2, String stem, String affixCandidate,
		SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
            if (typeOfStemEndChar == Roman.C
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
                Object[] x = Dialect.schneiderStateAtEnd(stemOrig);
                boolean doubleConsonants = (Boolean) x[0];
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
                throw new MorphologicalAnalyzerException(e);
            }
        }
        // else: Aucun suffixe ne mêne ici.


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
    protected static Vector<Object[]> validate_neutral_deletion(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    ) throws TimeoutException, MorphologicalAnalyzerException {

        Vector<Object[]> res = new Vector<Object[]>();
        // Last character of the stem
        char stemEndChar = stem.charAt(stem.length() - 1);
        // Second last character of the stem, if there is one
        char stemPenultEndChar;
        if (stem.length() > 1)
            stemPenultEndChar = stem.charAt(stem.length() - 2);
        else
            stemPenultEndChar = (char) -1;

        // Type of those 2 characters: integer corresponding to C (consonant) and V (vowel)
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
                int typeOfCharBeforeStemEndChar = Roman.typeOfLetterLat(stemPenultEndChar);
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
    protected static Vector<Object[]> validate_neutral_insertion(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_deletion_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_deletion_insertion(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_conditionaldeletion_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
            boolean checkPossibleDialectalChanges
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
                    throw new MorphologicalAnalyzerException(e);
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
    protected static Vector<Object[]> validate_voicing_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
            boolean checkPossibleDialectalChanges
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
                    boolean doubleConsonants = (Boolean) x[0];
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
    protected static Vector<Object[]> validate_nasalization_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
            boolean checkPossibleDialectalChanges
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
                    boolean doubleConsonants = (Boolean) x[0];
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
    protected static Vector<Object[]> validate_conditionalnasalization_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
            boolean checkPossibleDialectalChanges
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
                throw new MorphologicalAnalyzerException(e);
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
                boolean doubleConsonants = (Boolean) x[0];
                if (doubleConsonants) {
                    try {
                        Condition avc = new Imacond(
                                new ByteArrayInputStream(cond.getBytes())).ParseCondition();
                        affix.addPrecConstraint(avc);
                    } catch (ParseException e) {
                        throw new MorphologicalAnalyzerException(e);
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
                            throw new MorphologicalAnalyzerException(e);
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
    protected static Vector<Object[]> validate_insertion_null(
            String context, Action action1, Action action2, String stem, String affixCandidate,
            SurfaceFormOfAffix form, Affix affix, int posAffix, AffixPartOfComposition partOfComp,
            boolean checkPossibleDialectalChanges
    ) throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_fusion_null(String context, Action action1, Action action2, String stem,
                                                           String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                           AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException {

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
    protected static Vector<Object[]> validate_assimilation_null(String context, Action action1, Action action2, String stem,
                                                                 String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                 AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
            Object[] x = Dialect.schneiderStateAtEnd(stem);
            boolean doubleConsonants = (Boolean) x[0];
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
    protected static Vector<Object[]> validate_specificassimilation_null(String context, Action action1, Action action2, String stem,
                                                                         String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                         AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_deletion_specificdeletion(String context, Action action1, Action action2, String stem,
                                                                         String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                         AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_deletion_insertion(String context, Action action1, Action action2, String stem,
                                                                  String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                  AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_vowellengthening_cancellation(String context, Action action1, Action action2, String stem,
                                                                             String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                             AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_deletionvowellengthening_cancellation(String context, Action action1, Action action2, String stem,
                                                                                     String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                                     AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_insertionvowellengthening_null(String context, Action action1, Action action2, String stem,
                                                                              String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                              AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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
    protected static Vector<Object[]> validate_neutral_selfdecapitation(String context, Action action1, Action action2, String stem,
                                                                        String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                        AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException, LinguisticDataException {

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
    protected static Vector<Object[]> validate_deletion_selfdecapitation(String context, Action action1, Action action2, String stem,
                                                                         String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                         AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException, LinguisticDataException {

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
    protected static Vector<Object[]> validate_deletion_deletion(String context, Action action1, Action action2, String stem,
                                                                 String affixCandidate, SurfaceFormOfAffix form, Affix affix, int posAffix,
                                                                 AffixPartOfComposition partOfComp, boolean checkPossibleDialectalChanges)
            throws TimeoutException, MorphologicalAnalyzerException {

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


    //----------------------------------------

    static class ContextualResult {

        protected String stemBeforeAffixAction;
        protected String stemAfterAffixAction;
        protected AffixPartOfComposition affixPartOfComposition;

        public ContextualResult(String stemBeforeAffixAction, String stemAfterAffixAction, AffixPartOfComposition affixPartOfComposition) {
            this.stemBeforeAffixAction = stemBeforeAffixAction;
            this.stemAfterAffixAction = stemAfterAffixAction;
            this.affixPartOfComposition = affixPartOfComposition;
        }
    }
}


