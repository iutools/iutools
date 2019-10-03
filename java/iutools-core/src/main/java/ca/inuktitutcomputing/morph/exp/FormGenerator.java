package ca.inuktitutcomputing.morph.exp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.Affix;
import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.SurfaceFormInContext;
import ca.inuktitutcomputing.phonology.research.PhonologicalChange;
import ca.nrc.json.PrettyPrinter;

public class FormGenerator {
	
	private String baseForm, morphId;
	
	public List<SurfaceFormInContext> run(String morpheme) throws FormGeneratorException, LinguisticDataException {
		Logger logger = Logger.getLogger("FormGenerator.run");
		HashSet<SurfaceFormInContext> surfaceForms = new HashSet<SurfaceFormInContext>();
		String[] morphemeParts = morpheme.split("/");
		baseForm = morphemeParts[0];
		morphId = morphemeParts[1];
		Pattern pType = Pattern.compile("^([0-9])?(.+)$");
		Matcher mType = pType.matcher(morphId);
		boolean morphemeIsRoot = false;
		boolean morphemeIsAffix = false;
		if (mType.matches()) {
			if (mType.group(2).equals("n") || mType.group(2).equals("v") ||
					mType.group(2).equals("a") || mType.group(2).equals("c") || 
					mType.group(2).equals("p") || mType.group(2).equals("pr") ||
					mType.group(2).startsWith("rp") || mType.group(2).startsWith("ra") || 
					mType.group(2).equals("e") ||
					mType.group(2).startsWith("pd") || mType.group(2).startsWith("ad")
					)
				morphemeIsRoot = true;	
			else
				morphemeIsAffix = true;
		}
		
		if ( !morphemeIsRoot && !morphemeIsAffix)
			throw new FormGeneratorException("The morpheme "+morpheme+" could not be recognized as a root or affix.");
	
		/*
		 * The surface form of a root can change only at its end through actions of
		 * the affix that follows it (voicing; deletion; etc.). There may also
		 * be variants due to dialectal assimilation of consonants.
		 */
		else if (morphemeIsRoot) {
			// see if possible geminate consonant clusters possible and add them (ex. iglu > illu)
			HashSet<String> formsWithGeminate = formsWithGeminate(morpheme);
			HashSet<String> forms = formsWithEnds(morpheme);
			Iterator<String> itForms = forms.iterator();
			while (itForms.hasNext()) {
				String form = itForms.next();
				surfaceForms.add(new SurfaceFormInContext(form,"","",morpheme));
			}
		} 
		/*
		 * The surface form of an affix can change at its beginning and at its end
		 * through actions of the following affix (voicing; deletion; etc.) and 
		 * through its own actions (insertion; self-decapitation; etc.) 
		 */
		else {
			HashSet<SurfaceFormInContext> formsWithBeginnings = formsWithBeginnings(morpheme);
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
									formWithBeginning.contextualConstraintOnStem,
									formWithBeginning.contextualContraintOnReceivingMorpheme,morpheme);
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
	
	
	private HashSet<String> formsWithGeminate(String morpheme) {
//		return DialectalGeminate.formsWithGeminate(morpheme);
		return (HashSet<String>) PhonologicalChange.formsInAllDialects(morpheme);
	}


	public HashSet<SurfaceFormInContext> formsWithBeginnings(String morphemeId) {
		Logger logger = Logger.getLogger("FormGenerator.formsWithBeginnings");
		logger.debug("morphemeId: "+morphemeId);
		HashSet<SurfaceFormInContext> allSurfaceFormsInContext = new HashSet<SurfaceFormInContext>();
		Affix affix = LinguisticData.getInstance().getAffixWithId(morphemeId);
		char[] contexts = new char[] {'V','t','k','q'};
		for (int iCtxt=0; iCtxt<contexts.length; iCtxt++) {
			logger.debug("context: "+contexts[iCtxt]);
			HashSet<SurfaceFormInContext> surfaceFormsInContext = affix.getSurfaceFormsInContext(contexts[iCtxt],morphemeId);
			logger.debug("surfaceFormsInContext: "+PrettyPrinter.print(surfaceFormsInContext));
			allSurfaceFormsInContext.addAll(surfaceFormsInContext);
			logger.debug("allSurfaceFormsInContext: "+allSurfaceFormsInContext.size());
		}
		
		return allSurfaceFormsInContext;
	}


	protected HashSet<String> formsWithEnds(String morpheme) throws FormGeneratorException {
		HashSet<String> forms = new HashSet<String>();			
		String[] morphemeParts = morpheme.split("/");
		String baseForm = morphemeParts[0];
		forms.add(baseForm);
		if ( !baseForm.endsWith("i") && !baseForm.endsWith("u") && !baseForm.endsWith("a")) {
			String finalConsonant = baseForm.substring(baseForm.length()-1);
			String baseFormWithoutFinalConsonant = baseForm.substring(0,baseForm.length()-1);
			forms.add(baseFormWithoutFinalConsonant);
			if (finalConsonant.equals("t")) {
				// t can be assimilated by p: tikit+puq -> tikippuq
				// t is voiced to l: uvannut+li -> uvannulli
				// t is nasalized to n: tikit+niaq-tuq -> tikinniaqtuq
				// l can be assimilated by v: tikit+vik+u+lauq+gama -> tikilviulaurama -> tikivviulaurama
				// n can me assimilated by m: tikit+mat -> tikinmat -> tikimmat
				forms.add(baseFormWithoutFinalConsonant+"p");
				forms.add(baseFormWithoutFinalConsonant+"l");
				forms.add(baseFormWithoutFinalConsonant+"n");
				forms.add(baseFormWithoutFinalConsonant+"l");
				forms.add(baseFormWithoutFinalConsonant+"v");
				forms.add(baseFormWithoutFinalConsonant+"m");
			} else if (finalConsonant.equals("k")) {
				// k can be assimilated by p: pisuk+pak+lauq+mata -> pisuppalaurmata
				// k can be assimilated by t: pisuk+ji+it -> pisuttiit
				// k is voiced to g: pisuk+vik+ksaq+u+juq+nik -> pisugviksaujunik
				// k is nalasized to ng: iqaluk+ni -> iqalungni
				// g can be assimilated by j: ilinniaq+vik+juaq -> ilinniarvigjuaq -> ilinniarvijjuaq
				// g can be assimilated by l: malik+lugit -> maliglugit -> malillugit
				// g can be assimilated by v: allak+vik+mi -> allagvimmi -> allavvimmi
				forms.add(baseFormWithoutFinalConsonant+"p");
				forms.add(baseFormWithoutFinalConsonant+"t");
				forms.add(baseFormWithoutFinalConsonant+"g");
				forms.add(baseFormWithoutFinalConsonant+"ng");
				forms.add(baseFormWithoutFinalConsonant+"j");
				forms.add(baseFormWithoutFinalConsonant+"l");
				forms.add(baseFormWithoutFinalConsonant+"v");
				// k is voiced to g and nasalized to ng
			} else if (finalConsonant.equals("q")) {
				// q is voiced and nasalized to r
				// r can be assimilated by ng: pi+taqaq+ngat -> pitaqarngat -> pitaqanngat
				// r can be assimilated by n: atuq+niaq+guma -> aturniaruma -> atunniaruma
				// r can be assimilated by m: kimmiruq+miut -> kimmirurmiut -> kimmirummiut
				// r can be assimilated by j: no example found
				// r can be assimilated by l: katimaji+u+juq+lu -> katimajiujurlu -> katimajiujullu
				// r can be assimilated by v: no example found
				forms.add(baseFormWithoutFinalConsonant+"r");
				forms.add(baseFormWithoutFinalConsonant+"n"); // for ng and n
				forms.add(baseFormWithoutFinalConsonant+"m");
				forms.add(baseFormWithoutFinalConsonant+"l");
			} else {
//				throw new FormGeneratorException("The base form of the morpheme ends with a consonant different than t, k, q.");
			}
		}
		
		return forms;
			
	}

}
