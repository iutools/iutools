$.safeSelect = function(selector) {
	var elts = $(selector);
	if (elts.length == 0) {
		throw "No elements found for jQuery selector '"+selector+"''";
	}	
	return elts;
}