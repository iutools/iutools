package org.iutools.morph.exp;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.iutools.linguisticdata.Affix;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.SurfaceFormInContext;
import org.iutools.phonology.research.PhonologicalChange;
import ca.nrc.json.PrettyPrinter;

public class FormGenerator {
	
	private String baseForm, morphId;
	
	public List<SurfaceFormInContext> run(String morphemeId) throws FormGeneratorException, LinguisticDataException {
		Logger logger = Logger.getLogger("FormGenerator.run");
		HashSet<SurfaceFormInContext> surfaceForms = new HashSet<SurfaceFormInContext>();
		String[] morphemeParts = morphemeId.split("/");
		baseForm = morphemeParts[0];
		morphId = morphemeParts[1];

		String typeOfMorpheme = determineTypeOfMorpheme(morphId);
		if ( typeOfMorpheme==null )
			throw new FormGeneratorException("The morpheme "+morphemeId+" could not be recognized as a root or affix.");
	
		/*
		 * The surface form of a root can change only at its end through actions of
		 * the affix that follows it (voicing; deletion; etc.). There may also
		 * be variants due to dialectal assimilation of consonants.
		 */
		else if (typeOfMorpheme=="root") {
			// see if possible geminate consonant clusters possible and add them (ex. iglu > illu)
			HashSet<String> formsWithGeminate = formsWithGeminate(baseForm);
			// generate SurfaceFormInContext forms with alternative endings
			Iterator<String> itfwg = formsWithGeminate.iterator();
			while (itfwg.hasNext()) {
				String form = itfwg.next();
				logger.debug("form= "+form);
				HashSet<SurfaceFormInContext> surfaceFormsWithAlternateEndings = objectFormsWithEnds(form,morphemeId,null,null);
				surfaceForms.addAll(surfaceFormsWithAlternateEndings);
			}
		} 
		/*
		 * The surface form of an affix can change at its beginning and at its end
		 * through actions of the following affix (voicing; deletion; etc.) and 
		 * through its own actions (insertion; self-decapitation; etc.) 
		 */
		else {
			Set<SurfaceFormInContext> formsWithBeginnings = formsWithBeginnings(morphemeId);
			logger.debug("formsWithBeginnings= "+formsWithBeginnings.size());
			Iterator<SurfaceFormInContext> iter = formsWithBeginnings.iterator();
			while (iter.hasNext()) {
				SurfaceFormInContext formWithBeginning = iter.next();
				logger.debug("formWithBeginning= "+formWithBeginning.surfaceForm);
				String formStrWithBeginning = formWithBeginning.surfaceForm;
				HashSet<String> formsWithEnds = formsWithEnds(formStrWithBeginning);
				Iterator<String> itFormsWithEnds = formsWithEnds.iterator();
				while (itFormsWithEnds.hasNext()) {
					String form = itFormsWithEnds.next();
					SurfaceFormInContext surfaceForInContextForFormWithBeginning = 
							new SurfaceFormInContext(
									form,
									formWithBeginning.endOfStem,
									formWithBeginning.context,
									morphemeId);
					logger.debug(surfaceForInContextForFormWithBeginning);
					surfaceForms.add(surfaceForInContextForFormWithBeginning);
					logger.debug("surfaceForms: "+surfaceForms.size());
				}
			}
		}
		List<SurfaceFormInContext> listOfForms = new ArrayList<SurfaceFormInContext>();
		listOfForms.addAll(surfaceForms);	
		logger.debug("listOfForms: "+listOfForms.size());
		
		return listOfForms;
	}
	
	private String determineTypeOfMorpheme(String morphId) {
		String typeOfMorpheme = null;
		Pattern pType = Pattern.compile("^([0-9])?(.+)$");
		Matcher mType = pType.matcher(morphId);
		if (mType.matches()) {
			if (mType.group(2).equals("n") || mType.group(2).equals("v") ||
					mType.group(2).equals("a") || mType.group(2).equals("c") || 
					mType.group(2).equals("p") || mType.group(2).equals("pr") ||
					mType.group(2).startsWith("rp") || mType.group(2).startsWith("ra") || 
					mType.group(2).equals("e") ||
					mType.group(2).startsWith("pd") || mType.group(2).startsWith("ad")
					)
				typeOfMorpheme = "root";	
			else
				typeOfMorpheme = "affix";
		}
		
		return typeOfMorpheme;
	}
	
	
	protected HashSet<String> formsWithGeminate(String morpheme) {
//		return DialectalGeminate.formsWithGeminate(morpheme);
		return (HashSet<String>) PhonologicalChange.formsInAllDialects(morpheme);
	}
	

	public Set<SurfaceFormInContext> formsWithBeginnings(String morphemeId) {
		Logger logger = Logger.getLogger("FormGenerator.formsWithBeginnings");
		logger.debug("morphemeId: "+morphemeId);
		Set<SurfaceFormInContext> allSurfaceFormsInContext = new HashSet<SurfaceFormInContext>();
		Affix affix = LinguisticData.getInstance().getAffixWithId(morphemeId);
		char[] contexts = new char[] {'V','t','k','q','C'};
		for (int iCtxt=0; iCtxt<contexts.length; iCtxt++) {
			logger.debug("context: "+contexts[iCtxt]);
			Set<SurfaceFormInContext> surfaceFormsInContext = affix.getFormsInContext(contexts[iCtxt]);
			logger.debug("surfaceFormsInContext: "+surfaceFormsInContext);
			allSurfaceFormsInContext.addAll(surfaceFormsInContext);
			logger.debug("allSurfaceFormsInContext: "+allSurfaceFormsInContext.size());
		}

		return allSurfaceFormsInContext;
	}


	protected HashSet<String> formsWithEnds(String morphemeCanonicalForm) throws FormGeneratorException {
		HashSet<String> forms = new HashSet<String>();
		if (morphemeCanonicalForm.length()==0)
			return forms;
		forms.add(morphemeCanonicalForm);
		if ( !morphemeCanonicalForm.endsWith("i") && !morphemeCanonicalForm.endsWith("u") && !morphemeCanonicalForm.endsWith("a")) {
			String finalConsonant = morphemeCanonicalForm.substring(morphemeCanonicalForm.length()-1);
			String baseFormWithoutFinalConsonant = morphemeCanonicalForm.substring(0,morphemeCanonicalForm.length()-1);
			forms.add(baseFormWithoutFinalConsonant);
			if (finalConsonant.equals("t")) {
				// tp>pp: t can be assimilated by p: tikit+puq -> tikippuq
				// tk>kk: t can be assimilated by k: taatkua -> taakkua
				// ts>ss: t can be assimilated by s: taatsuma -> taassuma
				// ts>tt: t can be assimilated by t: natsilik -> nattilik
				// t is voiced to l: uvannut+li -> uvannulli
				// l can be assimilated by v,j and g: tikit+vik+u+lauq+gama -> tikilviulaurama -> tikivviulaurama
				// t is nasalized to n: tikit+niaq-tuq -> tikinniaqtuq
				// n can be assimilated by m: tikit+mat -> tikinmat -> tikimmat
				// n can be assimilated by ng:
				forms.add(baseFormWithoutFinalConsonant+"p");
				forms.add(baseFormWithoutFinalConsonant+"k");
				forms.add(baseFormWithoutFinalConsonant+"s");
//				forms.add(baseFormWithoutFinalConsonant+"t");
				// voicing
				forms.add(baseFormWithoutFinalConsonant+"l");
				forms.add(baseFormWithoutFinalConsonant+"v");
				// nasalization
				forms.add(baseFormWithoutFinalConsonant+"n");
				forms.add(baseFormWithoutFinalConsonant+"m");
				forms.add(baseFormWithoutFinalConsonant+"ng");
			} else if (finalConsonant.equals("k")) {
				// k can be assimilated by p: pisuk+pak+lauq+mata -> pisuppalaurmata
				// k can be assimilated by t: pisuk+ti+it -> pisuttiit
				// k can be assimilated by s: iksivautaq -> issivautaq
				// k can be assimilated to t by s: iksivautaq -> itsivautaq
				// k is voiced to g: pisuk+vik+ksaq+u+juq+nik -> pisugviksaujunik
				// k is nalasized to ng: iqaluk+ni -> iqalungni
				// g can be assimilated by j: ilinniaq+vik+juaq -> ilinniarvigjuaq -> ilinniarvijjuaq
				// g can be assimilated by l: malik+lugit -> maliglugit -> malillugit
				// g can be assimilated by v: allak+vik+mi -> allagvimmi -> allavvimmi
				// ng can be assimilated by m:
				// ng can be assimilated by n:
				forms.add(baseFormWithoutFinalConsonant+"p");
				forms.add(baseFormWithoutFinalConsonant+"t");
				forms.add(baseFormWithoutFinalConsonant+"s");
				// voicing
				forms.add(baseFormWithoutFinalConsonant+"g");
				forms.add(baseFormWithoutFinalConsonant+"v");
				forms.add(baseFormWithoutFinalConsonant+"l");
				forms.add(baseFormWithoutFinalConsonant+"j");
				// nasalization
				forms.add(baseFormWithoutFinalConsonant+"ng");
				forms.add(baseFormWithoutFinalConsonant+"m");
				forms.add(baseFormWithoutFinalConsonant+"n");
			} else if (finalConsonant.equals("q")) {
				// q is voiced and nasalized to r
				// no assimilation of q by any consonant
				forms.add(baseFormWithoutFinalConsonant+"r");
			} else {
				// Some entries may have a final other than V, t, k, q.
				// For example, demonstrative radicals (uv; tauv)
//				throw new FormGeneratorException("The base form of the morpheme ends with a consonant different than t, k, q.");
			}
		}
		
		return forms;	
	}
	
	protected HashSet<SurfaceFormInContext> objectFormsWithEnds(String morphemeCanonicalForm, String morphemeID, String endOfStem, String context) throws FormGeneratorException {
		Logger logger = Logger.getLogger("FormGenerator.objectFormsWithEnds");
		logger.debug("morphemeID= "+morphemeID);
		Character contextC = context==null? null : context.charAt(0);
		HashSet<SurfaceFormInContext> surfaceForms = new HashSet<SurfaceFormInContext>();
		HashSet<String> forms = formsWithEnds(morphemeCanonicalForm);
		Iterator<String> itForms = forms.iterator();
		while (itForms.hasNext()) {
			String formWithAlternateEnding = itForms.next();
			logger.debug("formWithAlternateEnding= '"+formWithAlternateEnding+"'");
			SurfaceFormInContext sfic = new SurfaceFormInContext(formWithAlternateEnding,endOfStem,contextC,morphemeID);
			logger.debug("sfic= "+sfic);
			surfaceForms.add(sfic);
		}
		
		return surfaceForms;
	}

}
