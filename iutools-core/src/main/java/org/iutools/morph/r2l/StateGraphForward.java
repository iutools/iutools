/*
 * Conseil national de recherche Canada 2003
 * 
 * Cr�� le 9-Dec-2003
 * par Benoit Farley
 * 
 */

package org.iutools.morph.r2l;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.constraints.Conditions;
import org.iutools.linguisticdata.constraints.Imacond;
import org.iutools.linguisticdata.constraints.ParseException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


public class StateGraphForward {
	static Map<String,State> statesMap;
	static State[] states;
	static public State initialState;
	static public State finalState;
	static public State verbState;


	static {
		State word, wordWithIntermediateTail, wordWithoutTail, noun, verb, adverb, expression, conjunction;
		State nominalStem, nominalCompositeStem, verbalStem, demonstrativeAdverb, demonstrative;
		State demonstrativePronoun_singular, demonstrativePronoun_plural;
		State personalPronoun, personalPronoun_uva_ili;
		State personalPronounStem_uva_ili, personalPronounStem_other, personalPronounRoot_uva_1, personalPronounRoot_ili_2;
		State start;
		
		word = new State("word");	// word ending with a tail morpheme * mq
		wordWithIntermediateTail = new State("wordwithintermediatetail");	// word ending with an intermediate tail morpheme * mqi
		wordWithoutTail = new State("wordwithouttail");		// word without a tail morpheme * m
		noun = new State("noun");		// nominal word * n
		verb = new State("verb");		// verbal word * v
		verbState = verb;
		adverb = new State("adverb");		// adverbial word * a
		expression = new State("expression");		// expression * e
		conjunction = new State("conjunction");		// conjunction * c
		nominalStem = new State("nounstem");	// nominal stem * rn
		nominalCompositeStem = new State("compositenounstem");	// composite nominal stem * rnc
		verbalStem = new State("verbstem");	// verbal stem * rv
		demonstrativeAdverb = new State("adverbialdemonstrative");	// demonstrative adverb * ad
		demonstrativePronoun_singular = new State("singulardemonstrativepronoun");	// singular demonstrative pronoun * pds
		demonstrativePronoun_plural = new State("pluraldemonstrativepronoun");	// plural demonstrative pronoun * pdp
		demonstrative = new State("demonstrative");		// demonstrative word * d
		personalPronoun = new State("personalpronoun");		// personal pronoun * pp
		personalPronoun_uva_ili = new State("personalpronounuva1ili2"); // 1st and 2nd person singular personal pronouns * pp1
		personalPronounStem_uva_ili = new State("personalpronounstemuva1ili2"); // 1st and 2nd person singular personal pronoun stems * radpp1
		personalPronounStem_other = new State("personalpronounstemother"); // other personal pronoun stem * radpp2
		personalPronounRoot_uva_1 = new State("personalpronounrootuva1");// 1st person singular personal pronoun root * racpp1
		personalPronounRoot_ili_2 = new State("personalpronounrootili2");// 2nd person singular personal pronoun root * racpp2
		start = new State("start");	// beginning of the word * 0

		start.setArcs( new Arc[] {
				new Arc(makeCond("type:n,number:d"), noun),
				new Arc(makeCond("type:n,number:p"), noun),
				new Arc(makeCond("type:a"), adverb),
				new Arc(makeCond("type:e"), expression),
				new Arc(makeCond("type:c"), conjunction),
				new Arc(makeCond("type:n,number:s"), nominalStem),
				new Arc(makeCond("type:p,!nature:per"), nominalStem),
				new Arc(makeCond("type:n,subtype:nc"), nominalCompositeStem),
				new Arc(makeCond("type:v"), verbalStem),
				new Arc(makeCond("type:ad"), demonstrative),
				new Arc(makeCond("type:pd"), demonstrative),
				new Arc(makeCond("type:rad"), demonstrativeAdverb),
				new Arc(makeCond("type:rpd,number:s"), demonstrativePronoun_singular),
				new Arc(makeCond("type:rpd,number:p"), demonstrativePronoun_plural),
				new Arc(makeCond("id:uva/1rpr"), personalPronounRoot_uva_1),
				new Arc(makeCond("id:ili/1rp"), personalPronounRoot_ili_2),
				new Arc(makeCond("type:p"), personalPronounStem_other)
		});
		noun.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail),
				new Arc(makeCond("function:nv"), verbalStem)
		});
		verb.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail)
		});
		adverb.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail),
				new Arc(makeCond("type:tn,number:s,possPers:null"), noun),
				new Arc(makeCond("function:nn"), nominalStem),
				new Arc(makeCond("function:vn"), nominalStem),
				new Arc(makeCond("function:nv"), verbalStem)
		});
		expression.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail)
		});
		conjunction.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail)
		});
		nominalStem.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail),
				new Arc(makeCond("type:tn"), noun),
				new Arc(makeCond("function:nn"), nominalStem),
				new Arc(makeCond("function:nv"), verbalStem)
		});
		nominalCompositeStem.setArcs( new Arc[] {
				new Arc(makeCond("type:tn"), noun),
				new Arc(makeCond("function:nn"), nominalStem),
				new Arc(makeCond("function:nv"), verbalStem)
		});
		verbalStem.setArcs( new Arc[] {
				new Arc(makeCond("type:tv"), verb),
				new Arc(makeCond("function:vn"), nominalStem),
				new Arc(makeCond("function:vv"), verbalStem)
		});
		demonstrative.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail),
				new Arc(makeCond("function:nv"), verbalStem)
		});
		demonstrativeAdverb.setArcs( new Arc[] {
				new Arc(makeCond("type:tad"), demonstrative)
		});
		demonstrativePronoun_singular.setArcs( new Arc[] {
				new Arc(makeCond("type:tpd,number:s"), demonstrative)
		});
		demonstrativePronoun_plural.setArcs( new Arc[] {
				new Arc(makeCond("type:tpd,number:p"), demonstrative)
		});
		personalPronoun.setArcs( new Arc[] {
				new Arc(null, wordWithoutTail)
		});
		personalPronounRoot_uva_1.setArcs( new Arc[] {
				new Arc(makeCond("type:tn,possPers:1,possNumber:Xnumber"), personalPronounStem_uva_ili)
		});
		personalPronounRoot_ili_2.setArcs( new Arc[] {
				new Arc(makeCond("type:tn,possPers:2,possNumber:Xnumber"), personalPronounStem_uva_ili)
		});
		personalPronounStem_uva_ili.setArcs( new Arc[] {
				new Arc(null, personalPronoun),
				new Arc(makeCond("function:nn"), personalPronoun_uva_ili)
		});
		personalPronoun_uva_ili.setArcs( new Arc[] {
				new Arc(null, personalPronoun),
				new Arc(makeCond("function:nn"), personalPronoun_uva_ili)
		});
		personalPronounStem_other.setArcs( new Arc[] {
				new Arc(null, personalPronoun),
				new Arc(makeCond("type:tn,possPers:null"), personalPronoun),
				new Arc(makeCond("function:nn"), personalPronounStem_other)
		});
		wordWithoutTail.setArcs( new Arc[] {
				new Arc(null, word),
				new Arc(makeCond("type:q"), word),
				new Arc(makeCond("id:li/1q"), wordWithIntermediateTail),
				new Arc(makeCond("id:lu/1q"), wordWithIntermediateTail)
		});
		wordWithIntermediateTail.setArcs( new Arc[] {
				new Arc(makeCond("id:guuq/1q"), word),
				new Arc(makeCond("id:kia/1q"), word),
				new Arc(makeCond("id:ttauq/1q"), word),
				new Arc(makeCond("id:qai/1q"), word),
		});
		word.setArcs( new Arc[] { // no arcs since this is the final state
		});

		initialState = start;
		finalState = word;
		
		states = new State[] {
					word, wordWithIntermediateTail, wordWithoutTail, noun, verb, adverb, expression, conjunction,
					nominalStem, nominalCompositeStem, verbalStem, demonstrativeAdverb, demonstrativePronoun_singular,
					demonstrativePronoun_plural,
					demonstrative, personalPronoun, personalPronoun_uva_ili,
					personalPronounStem_uva_ili, personalPronounStem_other, personalPronounRoot_uva_1,
					personalPronounRoot_ili_2, start
		};
		statesMap = new HashMap<String,State>();
		for (int istate=0; istate<states.length; istate++) {
			statesMap.put(states[istate].id, states[istate]);
		}
	}

	public static State getState(String stateId) {
		return statesMap.get(stateId);
	}
    
    static private Conditions makeCond(String str) {
        try {
            return (Conditions)new Imacond(str).ParseCondition();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public boolean morphemeCanBeAtEndOfWord(String morphemeId) {
		String[] partsMorphemeId = morphemeId.split("/");
		String id = partsMorphemeId[1];
		boolean res;
		// TODO: instead of this "hard coding", use data from states (final state)
		if ( id.matches("^\\d+[nv]?n$") || id.matches("^t[nv].+") || id.matches("^\\d+q$") ) {
			res = true;
		} else
			res = false;

		return res;
	}


	// ---------------------------- Internal Classes --------------------------------

	public static class State implements Cloneable {
		public String id;
		Arc[] arcs;

		public State(String id) {
			this.id = id;
		}
		private State() {
		}

		public String getId() {
			return id;
		}

		/**
		 * Determine the next state where the morpheme given in argument gets the analysis.
		 *
		 * Every arc from any state is unique. The give morpheme can correspond to only 1 arc, if any.
		 *
		 * @param morpheme A Morpheme object
		 * @return a State
		 */
		public State nextState(Morpheme morpheme) {
			Logger logger = LogManager.getLogger("StateGraph.State.nextState");
			logger.debug("morpheme: "+morpheme.id);
			State nextState = null;
			for (int iarc=0; iarc<arcs.length; iarc++) {
				Arc arc = arcs[iarc];
				Conditions conds = arc.getCondition();
				logger.debug("conds= "+(conds==null?null:conds.toText("fr")));
				if (conds != null) {
					try {
						if (conds.isMetByFullMorphem(morpheme)) {
							nextState = arc.getDestinationState();
							break;
						}
					} catch (LinguisticDataException e) {
						break;
					}
				} else {
					nextState = arc.getDestinationState().nextState(morpheme);
				}
			}

			return nextState;
		}

		public boolean canBeFinal() {
			if (this==finalState) {
				return true;
			}
			boolean stateHasNullArc = false;
			for (int iarc = 0; iarc < arcs.length; iarc++) {
				Arc arc = arcs[iarc];
				Conditions conds = arc.getCondition();
				if (conds == null) {
					State nextState = arc.getDestinationState();
					return nextState.canBeFinal();
				}
			}
			return false;
		}

		public Arc[] getArcs() {
			return arcs;
		}

		private void setArcs(Arc [] arcs) {
			this.arcs = arcs;
			for (int i=0; i<arcs.length; i++)
				arcs[i].setStartState(this);
		}

		public Vector<StateGraphForward.Arc> verify(Morpheme affixe) throws LinguisticDataException {
			Vector<StateGraphForward.Arc> possibleArcs = new Vector<StateGraphForward.Arc>();
			for (int i = 0; i < arcs.length; i++) {
				StateGraphForward.Arc arc = arcs[i];
				Conditions conds = arc.getCondition();
				if (conds!=null) {
					if (conds.isMetByFullMorphem(affixe))
						possibleArcs.add(arc);
				} else {
					Vector<StateGraphForward.Arc> possibles1 = arc.destState.verify(affixe);
					possibleArcs.addAll(possibles1);
				}
			}
			return possibleArcs;
		}

		public Object clone() {
			State cl = new State();
			cl.id = new String(this.id);
			cl.arcs = (StateGraphForward.Arc [])arcs.clone();
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
