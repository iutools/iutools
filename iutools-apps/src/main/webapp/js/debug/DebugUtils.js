function DebugUtils() {
}

DebugUtils.filterAttributes = function(val, namesFilter, filterType) {
	var results = val;
	if (filterType == null) {
		// By default, namesFilter is the list of attributes to keep.
		filterType = "keep";
	}
	
	if ($.type(val) === "string") {
		// Value is just a string. 
		// No further filtering needed.
		results = val;
	} else if (val instanceof Array) {
		// Value is an array.
		// Do further filtering on each of its elements.
		results = [];
		for (var ii=0; ii < val.length; ii++) {
			var elt = val[ii];
			var eltFiltered = DebugUtils.filterAttributes(elt, namesFilter, filterType);
			results.push(eltFiltered);
		}
	} else if (val instanceof Object){
		// Value is an object. 
		// Do further filtering on each of its values.
		results = {};
		$.each(
			val, 
			function(eltKey, eltVal) {
//				if ($.inArray(eltKey, keepNames) > -1) {
				if (DebugUtils.keepAttribute(eltKey, namesFilter, filterType)) {
					// This attribute should be kept.
					// Do further filtering on its value
					var eltValFiltered = DebugUtils.filterAttributes(eltVal, namesFilter, filterType);
					results[eltKey] = eltValFiltered;
				}
			});
	}
	
	return results;
}

DebugUtils.keepAttribute = function(attrName, namesFilter, filterType) {
	var keep = null;
	if (filterType == 'keep') {
		// Positive filter. Keep the attribute only if its name appears in 
		// the namesFilter
		if ($.inArray(attrName, namesFilter) > -1) {
			keep = true;
		} else {
			keep = false;
		}
	} else {
		// Negative filter. Keep the attribute only if its name DOES NOT appear in 
		// the namesFilter
		if ($.inArray(attrName, namesFilter) > -1) {
			keep = false;
		} else {
			keep = true;
		}
	}
	
	return keep;
}

		
