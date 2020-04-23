package ca.inuktitutcomputing.morph.exp;

import org.apache.log4j.Logger;

public class Decomposition {
	
	String[] components;
	String expression;
	
	public Decomposition(String _expression) {
		expression = _expression;
		components = _expression.split(" ");
	}
	
	public boolean validateForFinalComponent() {
		Logger logger = Logger.getLogger("Decomposition.validateForFinalComponent");
		boolean res;
		String lastComponent = components[components.length-1];
		logger.debug("last component: "+lastComponent);
		lastComponent = lastComponent.substring(1,lastComponent.length()-1);
		logger.debug("last component: "+lastComponent);
		String[] parts = lastComponent.split(":");
		String surfaceForm = parts[0];
		String[] morphemeIdParts = parts[1].split("/");
		String basicForm = morphemeIdParts[0];
		String id = morphemeIdParts[1];
		logger.debug("id: "+id);
		if (!surfaceForm.substring(surfaceForm.length()-1).equals(basicForm.substring(basicForm.length()-1)))
				res = false;
		else if (id.matches("^\\d+q$")) {
			res = true;
		} else if (id.matches("^\\d+.n$") || id.matches("^tn.+") || id.matches("^tv.+") ||
				   id.matches("^\\d+.n$")) {
			res = true;
		} else	
			res = false;
		
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

}
