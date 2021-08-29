package org.iutools.morph;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.morph.r2l.StateGraphForward;

import java.util.ArrayList;
import java.util.List;

public class DecompositionSimple {
	
	private String[] _components;
	public String decompSpecs;

	public DecompositionSimple(String _expression) {
		decompSpecs = _expression;

		return;
	}

	public static String[][] decomps2morphemes(DecompositionSimple[] decompObjs) throws DecompositionException {
		String[][] morphemes = new String[decompObjs.length][];
		int ii=0;
		for (DecompositionSimple aDecomp: decompObjs) {
			morphemes[ii] = aDecomp.morphemeIDs();
			ii++;
		}
		return morphemes;
	}

	private String[] morphemeIDs() throws DecompositionException {
		String[] morphemes = new String[components().length];
		int ii=0;
		for (String aComponent: components()) {
			Pair<String,String> parsedcomp = DecompositionSimple.parseComponent(aComponent);
			morphemes[ii] = parsedcomp.getRight();
			ii++;
		}
		return morphemes;
	}

	public List<String> surfaceForms() throws DecompositionException {
		List<String> surfaceForms = new ArrayList<String>();
		for (String aComponent: components()) {
			Pair<String,String> parsedcomp = DecompositionSimple.parseComponent(aComponent);
			surfaceForms.add(parsedcomp.getLeft());
		}
		return surfaceForms;
	}


	public static Pair<String,String> parseComponent(String comp) throws DecompositionException {
		String[] parsed = comp.split(":");
		if (parsed.length != 2) {
			throw new DecompositionException("Could not parse component '"+comp+"'");
		}
		return Pair.of(parsed[0], parsed[1]);
	}

	public String[] components() {
		if (_components == null) {
			_components = decompSpecs.split("\\s+");
		}
		return _components;
	}
	
	public boolean validateForFinalComponent() {
		Logger logger = Logger.getLogger("DecompositionSimple.validateForFinalComponent");
		boolean res;
		String lastComponent = components()[components().length-1];
		lastComponent = lastComponent.substring(1,lastComponent.length()-1);
		logger.debug("last component: "+lastComponent);
		String[] parts = lastComponent.split(":");
		String surfaceForm = parts[0];
		String morphemeId = parts[1];
		logger.debug("surfaceForm: "+surfaceForm);
		String[] morphemeIdParts = morphemeId.split("/");
		String basicForm = morphemeIdParts[0];
		logger.debug("basicForm: "+basicForm);
		String id = morphemeIdParts[1];
		logger.debug("id: "+id);
		res = StateGraphForward.morphemeCanBeAtEndOfWord(morphemeId);
		logger.debug("res= "+res);
		return res;
	}
	
	public String toStr() {
		return decompSpecs;
	}
	
	public String[] getMorphemes() {
		String[] morphemes = new String[components().length];
		for (int im=0; im<components().length; im++) {
			String component = components()[im].substring(1,components()[im].length()-1);
			String[] parts = component.split(":");
			morphemes[im] = parts[1];
		}
		
		return morphemes;
	}
	
	public String getSurfaceForm(String component) {
		String componentStr = component.substring(1,component.length()-1);
		String[] parts = componentStr.split(":");
		return parts[0];
	}

	public String getMorphemeId(String component) {
		String componentStr = component.substring(1,component.length()-1);
		String[] parts = componentStr.split(":");
		return parts[1];
	}

	public String toString() {
		String toS = "{";
		for (int ii=0; ii < components().length; ii++) {
			if (ii > 0) {
				toS += "}{";
			}
			toS += components()[ii];
		}
		toS += "}";
		return toS;
	}
}
