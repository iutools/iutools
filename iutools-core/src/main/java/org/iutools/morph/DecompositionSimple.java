package org.iutools.morph;

import org.apache.log4j.Logger;
import org.iutools.morph.StateGraphForward;

public class DecompositionSimple {
	
	public String[] components;
	public String expression;

	public DecompositionSimple(String _expression) {
		expression = _expression;
		components = _expression.split("\\s+");
		return;
	}
	
	public boolean validateForFinalComponent() {
		Logger logger = Logger.getLogger("DecompositionSimple.validateForFinalComponent");
		boolean res;
		String lastComponent = components[components.length-1];
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
		return expression;
	}
	
	public String[] getMorphemes() {
		String[] morphemes = new String[components.length];
		for (int im=0; im<components.length; im++) {
			String component = components[im].substring(1,components[im].length()-1);
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
		for (int ii=0; ii < components.length; ii++) {
			if (ii > 0) {
				toS += "}{";
			}
			toS += components[ii];
		}
		toS += "}";
		return toS;
	}
}
