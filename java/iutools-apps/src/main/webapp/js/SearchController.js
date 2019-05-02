/*
 * Controller for the search.html page.
 */

function SearchController(config) {
	this.btnSearch = config.btnSearch;    
 
	this.txtQuery = config.txtQuery;
	this.divError = config.divError;
	this.divResults = config.divResults;
	this.divTotalHits = config.divTotalHits;
}


SearchController.prototype.onSearch = function() {
	console.log("-- SearchController.onSearch: REAL version invoked");

	this.isBusy(true);
	var jsonRequestData = this.getSearchRequestData();
	
	$.ajax({
		type: 'POST',
		url: 'srv/search',
		data: {'jsonRequest': jsonRequestData},
		dataType: 'json',
		async: true,
//        success: successCallback,
        success: SearchController.prototype.onSearchSuccess,
//        error: failureCallback
        error: SearchController.prototype.onSearchFailure
	});
}

SearchController.prototype.onSearchSuccess = function(resp) {
	if (resp.errorMessage != null) {
		this.onSearchFailure(resp);
	} else {
		this.enableSearchButton();
		this.setQuery(resp.expandedQuery);
		this.setTotalHits(resp.totalHits);
		this.setResults(resp.hits);		
	}
    console.log("-- SearchController.onSearchSuccess: exting");
}

SearchController.prototype.onSearchFailure = function(resp) {
	this.enableSearchButton();
	this.error(resp.errorMessage);
}


SearchController.prototype.isBusy = function(flag) {
	if (flag) {
		this.disableSearchButton();		
		this.error("");
	} else {
		this.enableSearchButton();		
	}
}


SearchController.prototype.getSearchRequestData = function() {
	
	var request = {
			txtQuery: $("#"+this.txtQuery).val()
	};
	
	var jsonInputs = JSON.stringify(request);
	
	return jsonInputs;
}

SearchController.prototype.disableSearchButton = function() {
	$("#"+this.btnTrain).attr("disabled", true);
}

SearchController.prototype.enableSearchButton = function() {
	$("#"+this.btnTrain).attr("disabled", false);
}

SearchController.prototype.error = function(err) {
	$("#"+this.divError).html(err);
	$("#"+this.divError).show();	 
}

SearchController.prototype.setQuery = function(query) {
	$("#"+this.txtQuery).val(query);
}

SearchController.prototype.setTotalHits = function(totalHits) {
	$("#"+this.divTotalHits).text(totalHits);
}


SearchController.prototype.setResults = function(results) {
	var jsonResults = JSON.stringify(results);
	var divResults = $("#"+this.divResults);
	
	divResults.empty();
	
	for (var ii = 0; ii < results.length; ii++) {
		var aHit = results[ii];
		var hitHtml = 
				"<div id=\"hit"+ii+"\" class=\"hitDiv\">\n" +
				"  <div id=\"hitTitle\">"+aHit.title+"</div><br/>\n" +
				"  <div id=\"hitSnippet\">"+aHit.snippet+"</div><br/>\n" +
				"  <div id=\"hitURL\">"+aHit.url+"</div><br/>\n" +
				"<div>"
			;
		var aHitDiv = $.parseHTML(hitHtml);
		divResults.append(aHitDiv);
    }
	divResults.show();
}

SearchController.prototype.trimStr = function(str) {
	if (null != str) {
		str = str.replace(/(^\s+|\s+$)/g,"");
		if (str.length == 0) str = null;
	}
	return str;
}
