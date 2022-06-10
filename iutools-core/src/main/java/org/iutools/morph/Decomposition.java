package org.iutools.morph;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.morph.r2l.StateGraphForward;

import java.util.*;

public class Decomposition {
	
	private String[] _components;
	public String decompSpecs;

	public Decomposition(String _expression) {
		decompSpecs = _expression;

		return;
	}

	public static String[][] decomps2morphemes(Decomposition[] decompObjs) throws DecompositionException {
		String[][] morphemes = new String[decompObjs.length][];
		int ii=0;
		for (Decomposition aDecomp: decompObjs) {
			morphemes[ii] = aDecomp.morphemeIDs();
			ii++;
		}
		return morphemes;
	}

	static public Decomposition[] removeMultiples(Decomposition[] decs)  {
		Decomposition[] removed = decs;
		if (decs != null && decs.length > 0) {
			List<Decomposition> v = new ArrayList<Decomposition>();
			List<String> vc = new ArrayList<String>();
			v.add(decs[0]);
			vc.add(decs[0].toString());
			for (int i = 1; i < decs.length; i++) {
				String c = decs[i].toString();
				if (!vc.contains(c)) {
					v.add(decs[i]);
					vc.add(c);
				}
			}
			removed = v.toArray(new Decomposition[0]);
		}

		return removed;
	}

	private String[] morphemeIDs() throws DecompositionException {
		String[] morphemes = new String[components().length];
		int ii=0;
		for (String aComponent: components()) {
			Pair<String,String> parsedcomp = Decomposition.parseComponent(aComponent);
			morphemes[ii] = parsedcomp.getRight();
			ii++;
		}
		return morphemes;
	}

	public List<String> surfaceForms() throws DecompositionException {
		List<String> surfaceForms = new ArrayList<String>();
		for (String aComponent: components()) {
			Pair<String,String> parsedcomp = Decomposition.parseComponent(aComponent);
			surfaceForms.add(parsedcomp.getLeft());
		}
		return surfaceForms;
	}

	public static Pair<String,String> parseComponent(String comp) throws DecompositionException {
		return parseComponent(comp, (Boolean)null);
	}


	public static Pair<String,String> parseComponent(
		String comp, Boolean mayMissFirstComponent) throws DecompositionException {
		if (mayMissFirstComponent == null) {
			mayMissFirstComponent = false;
		}
		comp = comp.replaceAll("[{}]", "");
		String[] parsed = comp.split(":");
		boolean correctlyParsed = true;
		if (parsed.length == 0 || parsed.length > 2 ||
			(parsed.length == 1 &&  !mayMissFirstComponent)) {
			 correctlyParsed = false;
		}
		if (!correctlyParsed) {
			throw new DecompositionException("Could not parse component '"+comp+"'");
		}

		String matchedString = null;
		String morphID = null;
		if (parsed.length == 2) {
			matchedString = parsed[0];
			morphID = parsed[1];
		} else {
			morphID = parsed[0];
		}

		return Pair.of(matchedString, morphID);
	}

	public String[] components() {
		if (_components == null) {
			_components = decompSpecs.split("\\s+");
		}
		return _components;
	}
	
	public boolean validateForFinalComponent() {
		Logger logger = Logger.getLogger("Decomposition.validateForFinalComponent");
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
	
	public String[] getMorphemes() throws DecompositionException {
		String[] morphemes = new String[components().length];
		int ii=0;
		for (String component: components()) {
			String morphID = Decomposition.parseComponent(component).getRight();
			morphemes[ii] = morphID;
			ii++;
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
