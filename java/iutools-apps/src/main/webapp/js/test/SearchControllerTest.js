var controllerConfig = {
		btnSearch = "btn-search",
		txtQuery = "txt-query-words",
		divResults =  "div-search-results"
	};

var controller = null;


QUnit.module("SearchController Tests", {
	beforeEach: function(assert) {
		
		
		controller = new SearchController(controllerConfig);
		
		// Add HTML elements that are used by this controller
		$("#testMainDiv").html(
				  "Train URL: <input id=\""+controllerConfig.txtTrainURL+"\"><br/>\n"
				+ "Field Names: <input id=\""+controllerConfig.txtFieldNames+"\"><br/>\n"
				+ "Sample Relation 1: <input id=\""+controllerConfig.txtSampleRelations+"1\"><br/>\n"
				+ "Sample Relation 2: <input id=\""+controllerConfig.txtSampleRelations+"2\"><br/>\n"
				+ "<button id=\""+controllerConfig.btnTrain+"\">Train then Test</button><br/>\n"
				+ "<p/>\n<br/>\n<p/>\n"
				+ "Error message: <div id=\""+controllerConfig.divError+"\"></div><br/>\n"
				+ "Results:<p/><div id=\""+controllerConfig.divResults+"\"></div><br/>\n"
			);
	},
	
	afterEach: function(assert) {
		
	}
});


/**********************************
 * DOCUMENTATION TESTS
 **********************************/



/**********************************
 * VERIFICATION TESTS
 **********************************/

QUnit.test("SearchController.onTrainFailure", function( assert ) 
{
	var caseDescr = "SearchController.onTrainFailure -- HappyPath";
	
	var expErrMessage = "There was some kind of error, etc..."
	var resp = {
			"errorMessage": expErrMessage		
	}
	controller.onTrainFailure(resp);
	
	assertErrorMessageWasDisplayed(assert, expErrMessage, caseDescr);
	assertTrainButtonIsEnabled(assert, caseDescr);
});

QUnit.test("SearchController.onTrainSuccress", function( assert ) 
		{
			var caseDescr = "SearchController.onSuccess -- HappyPath";
			
			var resp = {
					'results': {
						'scrapedRelations' : [
							"blah"
						]
					}
			}
			controller.onTrainSuccess(resp);
			
			
			assertDisplayedResultsAre(assert, resp.results.scrapedRelations, caseDescr);
		});

QUnit.test("SearchController.getTrainingRequestData -- HappyPath", function( assert ) 
		{
			var caseDescr = "SearchController.getTrainingRequestData -- HappyPath";
			
			enterTrainingURL("http://weknowmovies.com/sci-fi/");
			enterFieldNames("title; director; year");
			enterSampleRelationValues(1, "Blade Runner 2049; Denis Villeneuve; 2017")
			enterSampleRelationValues(2, "Blade Runner; Ridley Scott; 1982")
			

			var gotRequestData = JSON.parse(controller.getTrainingRequestData());
			var expRequestData = 
					{
						action: "train", trainingURL: "http://weknowmovies.com/sci-fi/",
						trainingFields:
							[
							 {name:"title",value:"Blade Runner 2049"},{name:"director",value:"Denis Villeneuve"},{name: "year", value:  "2017"},
							 {name:"title",value:"Blade Runner"}, {name:"director",value:"Ridley Scott"},{name:"year",value:"1982"}
							]
					};
			
			assert.deepEqual(gotRequestData, expRequestData, caseDescr);
		});

QUnit.test("SearchController.getTrainingRequestData -- One of Two Sample Relations is Empty", function( assert ) 
		{
			var caseDescr = "SearchController.getTrainingRequestData -- One of Two Sample Relations is Empty";
			
			enterTrainingURL("http://weknowmovies.com/sci-fi/");
			enterFieldNames("title; director; year");
			enterSampleRelationValues(1, "Blade Runner 2049; Denis Villeneuve; 2017")
			

			var gotRequestData = JSON.parse(controller.getTrainingRequestData());
			var expRequestData = 
					{
						action: "train", trainingURL: "http://weknowmovies.com/sci-fi/",
						trainingFields:
							[
							 {name:"title",value:"Blade Runner 2049"},{name:"director",value:"Denis Villeneuve"},{name: "year", value:  "2017"},
							]
					};
			
			assert.deepEqual(gotRequestData, expRequestData, caseDescr);
		});

/**********************************
 * HELPER METHODS
 **********************************/

function enterTrainingURL(trainingURL) {
	$("#"+controllerConfig.txtTrainURL).val(trainingURL);
}

function enterFieldNames(namesString) {
	$("#"+controllerConfig.txtFieldNames).val(namesString);
}

function enterSampleRelationValues(ii, fieldValuesString) {
	$("#"+controllerConfig.txtSampleRelations+ii).val(fieldValuesString);
}


function assertDisplayedResultsAre(assert, expResults, caseDescr) {
	var message = "Checking that results were properly displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
	
	assertNoErrorDisplayed(assert, message);
	assertTrainButtonIsEnabled(assert, message);
	
	var displayedResults = $("#"+controllerConfig.divResults).html();	
	var expResultsJson = JSON.stringify(expResults);
	assert.deepEqual(displayedResults, expResultsJson, message);
}

function assertErrorMessageWasDisplayed(assert, expErrMessage, caseDescr) {
	var message = "Checking that errror message was displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
		
	assert.deepEqual(getErrorMessage(), expErrMessage, message);
}

function assertNoErrorDisplayed(assert, expErrMessage, caseDescr) {
	var message = "Checking that no errror messages were displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;

	assert.deepEqual(getErrorMessage(), "", message);	
}

function getErrorMessage() {
	var errMessage = $("#"+controllerConfig.divError).html();
	return errMessage;
}

function assertTrainButtonIsEnabled(assert, caseDescr) {
	var message = "Checking that 'Train' button is enabled.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
	
	var isDisabled = $("#"+controllerConfig.btnTrain).prop('disabled')
	
	assert.ok(!isDisabled);
}
