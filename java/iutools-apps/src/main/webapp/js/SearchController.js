/*
 * Controller for the search.html page.
 */

function SearchController(config) {
	this.btnSearch = config.btnSearch;
	this.txtQuery = config.txtQuery;
	this.divResults = config.divResults;
}

SearchController.prototype.onSearch = function() {
	this.isBusy(true);
	var jsonRequestData = this.getSearchRequestData();
	
	var thisController = this;
	function successCallback(resp) {
		thisController.onSearchSucess(resp)
	}
	
	function failureCallback(resp) {
		thisController.onSearchFailure(resp)
	}
	
	$.ajax({
		type: 'POST',
		url: 'srv/search',
		data: {'jsonRequest': jsonRequestData},
		dataType: 'json',
		async: true,
		success: successCallback,
		error: failureCallback			
	});			
}

SearchController.prototype.onSearchSucess = function(resp) {
	if (resp.errorMessage != null) {
		this.onSearchFailure(resp);
	} else {
		this.enableSearchButton();
		this.setResults(resp.results.scrapedRelations);		
	}
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
			txtQuery = $("#"+this.txtQuery).val()
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

SearchController.prototype.setResults = function(results) {
	var jsonResults = JSON.stringify(results);
	$("#"+this.divResults).html(jsonResults);
	$("#"+this.divResults).show();
}

SearchController.prototype.trimStr = function(str) {
	if (null != str) {
		str = str.replace(/(^\s+|\s+$)/g,"");
		if (str.length == 0) str = null;
	}
	return str;
}
