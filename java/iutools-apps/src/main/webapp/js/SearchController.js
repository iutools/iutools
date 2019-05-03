/*
 * Controller for the search.html page.
 */

class SearchController {

	constructor(config) {
		this.btnSearch = config.btnSearch;    
		 
		this.txtQuery = config.txtQuery;
		this.divError = config.divError;
		this.divResults = config.divResults;
		this.divTotalHits = config.divTotalHits;
	} 

	onSearch() {
			var isValid = this.validateQueryInput();
			if (isValid) {
				this.isBusy(true);
				this.invokeSearchService(this.getSearchRequestData(), 
						SearchController.prototype.successCallback, SearchController.prototype.failureCallback)
			}
	}
	
	invokeSearchService(jsonRequestData, _successCbk, _failureCbk) {
			$.ajax({
				type: 'POST',
				url: 'srv/search',
				data: {'jsonRequest': jsonRequestData},
				dataType: 'json',
				async: true,
		        success: _successCbk,
		        error: _failureCbk
			});
	}

	validateQueryInput() {
		var isValid = true;
		var query = $("#"+this.txtQuery).val();
		if (query == null || query === "") {
			isValid = false;
			this.error("You need to enter something in the query field");
			this.setTotalHits(0);
		}
		return isValid;
	}

	successCallback(resp) {
		if (resp.errorMessage != null) {
			this.onSearchFailure(resp);
		} else {
			this.setQuery(resp.expandedQuery);
			this.setTotalHits(resp.totalHits);
			this.setResults(resp.hits);		
		}
	}

	failureCallback(resp) {
		this.enableSearchButton();
		this.error(resp.errorMessage);
	}
	
	
	isBusy(flag) {
		if (flag) {
			this.disableSearchButton();		
			this.error("");
		} else {
			this.enableSearchButton();		
		}
	}
	
	
	getSearchRequestData() {
		
		var request = {
				txtQuery: $("#"+this.txtQuery).val()
		};
		
		var jsonInputs = JSON.stringify(request);
		
		return jsonInputs;
	}
	
	disableSearchButton() {
		$("#"+this.btnTrain).attr("disabled", true);
	}
	
	enableSearchButton() {
		$("#"+this.btnTrain).attr("disabled", false);
	}
	
	error(err) {
		$("#"+this.divError).html(err);
		$("#"+this.divError).show();	 
	}
	
	setQuery(query) {
		$("#"+this.txtQuery).val(query);
	}
	
	setTotalHits(totalHits) {
		$("#"+this.divTotalHits).text(totalHits);
	}
	
	
	setResults(results) {
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
	
	trimStr(str) {
		if (null != str) {
			str = str.replace(/(^\s+|\s+$)/g,"");
			if (str.length == 0) str = null;
		}
		return str;
	}
}
