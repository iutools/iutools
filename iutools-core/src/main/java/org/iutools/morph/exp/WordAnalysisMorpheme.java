package org.iutools.morph.exp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.data.constraints.Conditions;
import ca.inuktitutcomputing.data.Action;
import ca.inuktitutcomputing.data.Affix;
import ca.inuktitutcomputing.data.LinguisticData;
import org.iutools.script.Roman;

public class WordAnalysisMorpheme {
	
	public String surfaceString;
	public String nominalForm;
	public String id;
	public Type type;
	public Morpheme dbmorpheme;
	
	public WordAnalysisMorpheme(String str) {
		String[] rootParts = str.split(":");
		this.surfaceString = rootParts[0].substring(1);
		this.id = rootParts[1].substring(0, rootParts[1].length()-1);
		String[] idParts = this.id.split("/");
		this.nominalForm = idParts[0];
		this.type = new Type(this.id);
		this.dbmorpheme = LinguisticData.getInstance().getMorpheme(this.id);
	}
	
	public boolean agreesInTypeWith(WordAnalysisMorpheme receptorMorpheme) {
		if (this.type.valForMatching.equals("*"))
			return true;
		else if (this.type.valForMatching.equals(receptorMorpheme.type.valToMatch))
			return true;
		else
			return false;
	}
	
	public boolean agreesInContextWith(WordAnalysisMorpheme wamorpheme) {
		boolean result = false;
		char stemFinalNominalChar = wamorpheme.nominalForm.charAt(wamorpheme.nominalForm.length()-1);
		System.out.println("stemFinalNominalChar= "+stemFinalNominalChar);
		String[] formsInContext = null;
		Action[] actions1InContext = null;
		Action[] actions2InContext = null;
		formsInContext = ((Affix)this.dbmorpheme).getForm(stemFinalNominalChar);
		actions1InContext = ((Affix)this.dbmorpheme).getAction1(stemFinalNominalChar);
		actions2InContext = ((Affix)this.dbmorpheme).getAction2(stemFinalNominalChar);
		for (int ifa=0; ifa<formsInContext.length; ifa++) {
			String contextForm = formsInContext[ifa];
			System.out.println("contextForm= "+contextForm);
			System.out.println("surfaceString= "+this.surfaceString);
			Action action1 = actions1InContext[ifa];
			Action action2 = actions2InContext[ifa];
			boolean resultOnFormAndActions =_agreesWithFormAndActions(contextForm,action1,action2,wamorpheme);
			if (resultOnFormAndActions) {
				result = true;
				break;
			}
		}

		return result;
	}

	private boolean _agreesWithFormAndActions(String contextForm, Action action1, Action action2, 
			WordAnalysisMorpheme wamorpheme ) {
		boolean result = false;
		char stemFinalNominalChar = wamorpheme.nominalForm.charAt(wamorpheme.nominalForm.length()-1);
		char stemFinalSurfaceChar = wamorpheme.surfaceString.charAt(wamorpheme.surfaceString.length()-1);
		char stemPreFinalSurfaceChar = wamorpheme.surfaceString.charAt(wamorpheme.surfaceString.length()-2);
		Pattern p = Pattern.compile("^"+this.surfaceString+".*$");
		Matcher m = p.matcher(contextForm);
		boolean surfacestringMatchesContextForm = m.matches();
		
		System.out.println("action1= "+action1.type);
		System.out.println("action2= "+action2.type);
		
		if (action1.type==Action.NEUTRAL)
				result = surfacestringMatchesContextForm && stemFinalSurfaceChar==stemFinalNominalChar;
		else if (action1.type==Action.DELETION || action1.type==Action.FUSION)
			result = surfacestringMatchesContextForm && Roman.isVowel(stemFinalSurfaceChar);
		else if (action1.type==Action.NASALIZATION)
			result = surfacestringMatchesContextForm && 
					 (stemFinalSurfaceChar==Action.Nasalization.changementPhonologique1(stemFinalNominalChar) ||
					  stemFinalSurfaceChar==contextForm.charAt(0));
		else if (action1.type==Action.VOICING)
			result = surfacestringMatchesContextForm && 
			         (stemFinalSurfaceChar==Action.Voicing.changementPhonologique1(stemFinalNominalChar) ||
					  stemFinalSurfaceChar==contextForm.charAt(0));
		else if (action1.type==Action.INSERTION)
			result = this.surfaceString.equals( action1.getInsert()+contextForm );

		if (action2.type!=Action.NULLACTION) {
			if (Roman.isVowel(stemFinalSurfaceChar) && Roman.isVowel(stemPreFinalSurfaceChar)) {
				if (action2.type==Action.INSERTION) {
					Pattern p2i = Pattern.compile("^"+this.surfaceString+".*$");
					Matcher m2i = p2i.matcher(action2.getInsert()+contextForm);
					result = m2i.matches();
				}
				else if (action2.type==Action.SELFDECAPITATION) {
					Pattern p2i = Pattern.compile("^"+this.surfaceString+".*$");
					Matcher m2i = p2i.matcher(contextForm.substring(1));
					result = m2i.matches();
				}
				else if (action2.type==Action.DELETION) {
					Pattern p2i = Pattern.compile("^"+this.surfaceString+".*$");
					Matcher m2i = p2i.matcher(contextForm.substring(1));
					result = m2i.matches();
				}
			}
		}
		
		return result;
	}
	
	
	public boolean agreesWithOtherConstraints (WordAnalysisMorpheme stemwamorpheme) throws LinguisticDataException {		
		boolean result = true;
		Morpheme stem = stemwamorpheme.dbmorpheme;
		Affix affix = (Affix) this.dbmorpheme;
		Conditions condition = affix.getPrecCond();
		if (condition != null)
			result = condition.isMetBy(stem);
		
		return result;
	}
		
		
		
	public class Type {
		
		String cl = null;
		String val = null;
		String valForMatching = "";
		String valToMatch = null;
		
		public Type(String id) {
			Pattern pQueue = Pattern.compile("^(.+\\/\\d?(q))$");
			Matcher mQueue = pQueue.matcher(id);
			Pattern pRoot = Pattern.compile("^(.+\\/\\d?(.))$");
			Matcher mRoot = pRoot.matcher(id);
			
			if (mQueue.matches()) {
				cl = "queue";
				val = mQueue.group(2);
				valForMatching = "*";
				valToMatch = "";
			}
			else if (mRoot.matches()) {
				cl = "root";
				val = mRoot.group(2);
				valToMatch = val;
			}
			else {
				Pattern pSuffix = Pattern.compile("^(.+\\/\\d(.)(.))$");
				Matcher mSuffix = pSuffix.matcher(id);
				if (mSuffix.matches()) {
					cl = "suffix";
					val = mSuffix.group(2)+mSuffix.group(3);
					valForMatching = mSuffix.group(2);
					valToMatch = mSuffix.group(3);
				}
				else {
					Pattern pEnding = Pattern.compile("^(.+\\/(t)(.).+)$");
					Matcher mEnding = pEnding.matcher(id);
					if (mEnding.matches()) {
						cl = "ending";
						val = mEnding.group(2)+mEnding.group(3);
						valForMatching = mEnding.group(3);
						valToMatch = mEnding.group(3);
					}
				}
			}
		}
	}

}
