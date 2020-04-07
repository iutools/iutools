/**
 * Utilities for processing Iunktut text
 */


class HtmlUtils {
		
	constructor() {
	}
	
	static escapeHtmlEntities(text) {
		text = text
			.replace(/&/g,'&amp;')
			.replace(/</g,'&lt;')
			.replace(/>/g,'&gt;') 
		;
		return text;
	}
}