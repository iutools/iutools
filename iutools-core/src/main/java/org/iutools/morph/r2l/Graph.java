/*
 * Conseil national de recherche Canada 2003
 * 
 * Cr�� le 9-Dec-2003
 * par Benoit Farley
 * 
 */

package org.iutools.morph.r2l;

import java.util.Vector;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.constraints.Conditions;
import org.iutools.linguisticdata.constraints.Imacond;
import org.iutools.linguisticdata.constraints.ParseException;


public class Graph {
	static State[] states;
	static public State initialState;
	static public State finalState;
	static public State verbState;

    

	static {
		State word, wordWithLiLuTail, wordWithoutTail, noun, verb, adverb, expression, conjunction, nominalStem, nominalCompositeStem, verbalStem, demonstrativeAdverb, demonstrativePronounSingular, demonstrativePronounPlural, demonstrative, personalPronoun = null;
//		State pr = null;
		State personalPronounUvaIli, pp2, personalPronounStemUvaIli, personalPronounStemOther, personalPronounRootUva, personalPronounRootIli, zero = null;
		
		word = new State("w");	// word with a tail element
		wordWithLiLuTail = new State("wt");	// word with an intermediate 'li' or 'lu' tail element
		wordWithoutTail = new State("wnt");		// word without a tail element
		noun = new State("n");		// nominal word
		verb = new State("v");		// verbal word
		verbState = verb;
		adverb = new State("a");		// adverb
		expression = new State("e");		// expression
		conjunction = new State("c");		// conjunction
		nominalStem = new State("ns");	// nominal stem
		nominalCompositeStem = new State("nsc");	// composite nominal stem
		verbalStem = new State("rv");	// verbal stem
		demonstrativeAdverb = new State("ad");	// demonstrative adverb
		demonstrativePronounSingular = new State("pds");	// singular demonstrative pronoun
		demonstrativePronounPlural = new State("pdp");	// plural demonstrative pronoun
		demonstrative = new State("d");		// demonstrative
		personalPronoun = new State("pp");	// personal pronoun
		personalPronounUvaIli = new State("pp12");  // personal pronoun based on uva (1st person) or ili (2nd person)
//		pp2 = new State("pp2");
		personalPronounStemUvaIli = new State("pps12"); // personal pronoun stem based on uva or ili
		personalPronounStemOther = new State("pps"); // personal pronoun other than uva and ili
		personalPronounRootUva = new State("ppr1"); // personal pronoun root 'uva'
		personalPronounRootIli = new State("ppr2"); // personal pronoun root 'ili'
		zero = new State("0");	// beginning of the word
		
		word.setArcs(	new Arc[] {
		        new Arc(makeCond("id:guuq/1q"), wordWithLiLuTail),
		        new Arc(makeCond("id:kia/1q"), wordWithLiLuTail),
		        new Arc(makeCond("id:ttauq/1q"), wordWithLiLuTail),
		        new Arc(makeCond("id:qai/1q"), wordWithLiLuTail),
		        new Arc(makeCond("type:q"), wordWithoutTail),
		        new Arc(null, wordWithoutTail)
		});
	
		wordWithLiLuTail.setArcs(new Arc[] {
		        new Arc(makeCond("id:li/1q"), wordWithoutTail),
		        new Arc(makeCond("id:lu/1q"), wordWithoutTail)
		});
		
		wordWithoutTail.setArcs(new Arc[] {
		        new Arc(null, noun),
		        new Arc(null, verb),
		        new Arc(null, expression),
		        new Arc(null, adverb),
		        new Arc(null, conjunction),
//		        new Arc(null, pr),
		        new Arc(null, personalPronoun),
		        new Arc(null, demonstrative),
		        // addition of the next arc: see comment below
		        new Arc(null, nominalStem),
		});
	
		noun.setArcs(new Arc[] {
		        new Arc(makeCond("type:tn"), nominalStem),
		        new Arc(makeCond("type:tn"), nominalCompositeStem),
		        new Arc(makeCond("type:n,number:d"), zero),
		        new Arc(makeCond("type:n,number:p"), zero),
		        /*
                 * The following arc is fine on paper, but in practice, it makes
                 * that there is a duplication: Rv (by NV) to N (by null) to Rn
                 * (by nominal root) to 0; Rv (by NV) to Rn (by nominal root) to
                 * 0.  So, it is commented out and replaced by a null arc from M
                 * to Rn
                 */
//		        new Arc(null, rn)
		        new Arc(makeCond("type:tn,number:s,possPers:null"), adverb),
		});
		
		verb.setArcs(new Arc[] {
		        new Arc(makeCond("type:tv"), verbalStem)
		});
		
		adverb.setArcs(new Arc[] {
		        new Arc(makeCond("type:a"), zero)
		});
		
		expression.setArcs(new Arc[] {
		        new Arc(makeCond("type:e"), zero)
		});
		
		conjunction.setArcs(new Arc[] {
		        new Arc(makeCond("type:c"), zero)
		});
		
		nominalStem.setArcs(new Arc[] {
		        new Arc(makeCond("function:nn"), nominalStem),
		        new Arc(makeCond("function:nn"), nominalCompositeStem),
		        new Arc(makeCond("function:nn"), adverb),
		        new Arc(makeCond("function:vn"), adverb),
		        new Arc(makeCond("function:vn"), verbalStem),
		        new Arc(makeCond("type:n,number:s"), zero),
		        new Arc(makeCond("type:p,!nature:per"), zero),
		});
		
		nominalCompositeStem.setArcs(new Arc[] {
		        new Arc(makeCond("type:n,subtype:nc"), zero),
		});
		
		verbalStem.setArcs(new Arc[] {
		        new Arc(makeCond("function:vv"), verbalStem),
		        new Arc(makeCond("function:nv"), noun),
		        new Arc(makeCond("function:nv"), nominalStem),
		        new Arc(makeCond("function:nv"), nominalCompositeStem),
		        new Arc(makeCond("function:nv"), adverb),
		        new Arc(makeCond("function:nv"), demonstrative),
		        new Arc(makeCond("type:v"), zero)
		});
		
		// Demonstratives
		demonstrative.setArcs(new Arc[] {
		        new Arc(makeCond("type:ad"), zero),
		        new Arc(makeCond("type:pd"), zero),
		        new Arc(makeCond("type:tad"), demonstrativeAdverb),
		        new Arc(makeCond("type:tpd,number:s"), demonstrativePronounSingular),
		        new Arc(makeCond("type:tpd,number:p"), demonstrativePronounPlural)
		});
		
		demonstrativeAdverb.setArcs(new Arc[] {
		        new Arc(makeCond("type:rad"), zero)
		});
		
		demonstrativePronounSingular.setArcs(new Arc[] {
		        new Arc(makeCond("type:rpd,number:s"), zero)
		});
		
		demonstrativePronounPlural.setArcs(new Arc[] {
		        new Arc(makeCond("type:rpd,number:p"), zero)
		});
		
		// Personal pronouns
		personalPronoun.setArcs(new Arc[] {
		        new Arc(null, personalPronounUvaIli),
		        new Arc(null, personalPronounStemUvaIli),
		        new Arc(null, personalPronounStemOther),
		        new Arc(makeCond("type:tn,possPers:null"), personalPronounStemOther),
		});
		
		personalPronounUvaIli.setArcs(new Arc[] {
		        new Arc(makeCond("function:nn"), personalPronounStemUvaIli),
		        new Arc(makeCond("function:nn"), personalPronounUvaIli)
		});
		
//		pp2.setArcs(new Arc[] {
//		        new Arc(makeCond("function:nn"), radpp2),
//		        new Arc(makeCond("function:nn"), pp2)
//		});
		
		personalPronounStemUvaIli.setArcs(new Arc[]{
		        new Arc(makeCond("type:tn,possPers:1,possNumber:Xnumber"), personalPronounRootUva),
		        new Arc(makeCond("type:tn,possPers:2,possNumber:Xnumber"), personalPronounRootIli)
		});
		
		personalPronounStemOther.setArcs(new Arc[]{
		        new Arc(makeCond("type:p"), zero),   // pr to p
		        new Arc(makeCond("function:nn"), personalPronounStemOther)
		});

		personalPronounRootUva.setArcs(new Arc[]{
		        new Arc(makeCond("id:uva/1rpr"), zero)
		});
		
		personalPronounRootIli.setArcs(new Arc[]{
		        new Arc(makeCond("id:ili/1rp"), zero)
		});
		
		
		// Arcs of state 'zero' are null since this is the final state

		initialState = word;
		finalState = zero;
		
		states =
			new State[] {word, wordWithLiLuTail, wordWithoutTail, noun, verb, adverb, expression, conjunction, nominalStem, verbalStem, demonstrativeAdverb, demonstrativePronounSingular, demonstrativePronounPlural, demonstrative, personalPronoun,
		        personalPronounUvaIli, personalPronounStemUvaIli, personalPronounStemOther, personalPronounRootUva, personalPronounRootIli, zero,
					// pp2
			};

	}

//	public static State getState(String str) { // *** not used
//		for (int i = 0; i < states.length; i++)
//			if (states[i].getId().equals(str))
//				return states[i];
//		return null;
//	}
    
    static private Conditions makeCond(String str) {
        try {
            return (Conditions)new Imacond(str).ParseCondition();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------------------ Internal Classes ------------------------------
	public static class State implements Cloneable {
		public String id;
		Arc[] arcs;

		public State(String id) {
			this.id = id;
		}

		private State() {
		}

//		public String getId() { *** not used
//			return id;
//		}

		public Arc[] getArcs() {
			return arcs;
		}

		private void setArcs(Arc [] arcs) {
			this.arcs = arcs;
			for (int i=0; i<arcs.length; i++)
				arcs[i].setStartState(this);
		}

		public Vector<Graph.Arc> verify(Morpheme affixe) throws LinguisticDataException {
			Vector<Graph.Arc> possibleArcs = new Vector<Graph.Arc>();
			for (int i = 0; i < arcs.length; i++) {
				Graph.Arc arc = arcs[i];
				Conditions conds = arc.getCondition();
				if (conds!=null) {
					if (conds.isMetByFullMorphem(affixe))
						possibleArcs.add(arc);
				} else {
					Vector<Graph.Arc> possibles1 = arc.destState.verify(affixe);
					possibleArcs.addAll(possibles1);
				}
			}
			return possibleArcs;
		}


		public Object clone() {
			State cl = new State();
			cl.id = new String(this.id);
			cl.arcs = (Graph.Arc [])arcs.clone();
			return cl;
		}
	}

	public static class Arc implements Cloneable {
		Conditions cond;
		State startState;
		State destState;

		public Arc(Conditions cond, State destState) {
			this.cond = cond;
			this.destState = destState;
		}

		public State getDestinationState() {
			return destState;
		}

		public Conditions getCondition() {
			return cond;
		}

		public String getDestinationStateStr() {
			return destState.id;
		}

		public void setStartState(State ss) {
			startState = ss;
		}

		public Arc copy()  {
			Arc arc = null;
			try {
				arc = (Arc)this.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return arc;
		}


	}

}
