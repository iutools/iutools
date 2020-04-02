/**
 * Utilities for processing Iunktut text
 */

class IUUtils {
	constructor() {
		
	}
	
	static isInuktut(text) {
		return this.isSyllabic(text)|| this.isRoman(text);
	}
	
	static isRoman(text) {
		var res = true;
		var iuchars = ["a","i","u","g","j","k","l","m","n","p","q","r","s","t","v","&"];
		for (var ich=0; ich<text.length; ich++)
			if ( !iuchars.includes(text.charAt(ich)) ) {
				res = false;
				break;
			}
		return res;
	}
	
	static isSyllabic(text) {
		// TODO: Implement this method
		return false;
	}
} 