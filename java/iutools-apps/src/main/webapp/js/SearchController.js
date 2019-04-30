/*
 * Controller for the search.html page.
 */

function SearchController(config) {
	this.btnSearch = config.btnSearch;
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
		url: 'search',
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
		this.enableTrainButton();
		this.setResults(resp.results.scrapedRelations);		
	}
}

SearchController.prototype.onSearchFailure = function(resp) {
	this.enableTrainButton();
	this.error(resp.errorMessage);
}


SearchController.prototype.isBusy = function(flag) {
	if (flag) {
		this.disableTrainButton();		
		this.error("");
	} else {
		this.enableTrainButton();		
	}
}


SearchController.prototype.getSearchRequestData = function() {
	
	var request = {
			'action': "train",
			'trainingURL': this.getTrainURL(),
			'trainingFields': this.getTrainingFields(),
	};
	
	var jsonInputs = JSON.stringify(request);
	
	return jsonInputs;
}
	

SearchController.prototype.getTrainURL = function() {
	var url = this.trimStr($("#"+this.txtTrainURL).val());
	return url;
}

SearchController.prototype.getTrainingFields = function() {
	
	var fiedNames = this.getTrainingFieldNames();
	
	var fields = [];
	var ii = 1;
	while (true) {
		var iithSampleRelationID = this.txtSampleRelations + ii; 
		
		var length = $("#"+iithSampleRelationID).length;
		
		
		// If this element does not exist, then we have reached
		// the end of the list of elements.
		var iithRelationField = $("#"+iithSampleRelationID);
		if (!iithRelationField.val() || /^\s*$/.test(iithRelationField.val()) || ii > 5) {
			break;			
		}	
		
		var sampleRelationStr = $("#"+iithSampleRelationID).val();
		var valuesThisRelation = sampleRelationStr.split(';');
		for (var jj=0; jj < valuesThisRelation.length; jj++) {
			var jjValue = this.trimStr(valuesThisRelation[jj]);
			fields.push({'name': fiedNames[jj], 'value': jjValue});
		}
		
		ii++;
	}
	
	return fields;
}

SearchController.prototype.getTrainingFieldNames = function() {
	var fieldNamesStr = $("#"+this.txtFieldNames).val();
	var fieldNamesUntrimmed = fieldNamesStr.split(";");
	var fieldNames = [];
	for (var ii=0; ii < fieldNamesUntrimmed.length; ii++) {
		fieldNames.push(this.trimStr(fieldNamesUntrimmed[ii]));
	}
	
	return fieldNames;
}


SearchController.prototype.getTestingURLs = function() {
	var urls = [];
	return urls;
}

SearchController.prototype.disableTrainButton = function() {
	$("#"+this.btnTrain).attr("disabled", true);
}

SearchController.prototype.enableTrainButton = function() {
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
