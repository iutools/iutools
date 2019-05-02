var srchControllerConfig = {
		btnSearch: "btn-search",
		txtQuery: "txt-query-words",
		divError: "div-error-msg",
		divResults:  "div-search-results",
		divTotalHits: "div-total-hits"
	};

var srchController = null;


QUnit.module("SearchController Tests", {
	beforeEach: function(assert) {
		
		var resp = {
				"errorMessage": null,
				"expandedQuery": "ᓄᓇᕗᑦ",
				"totalHits": 18,
				"hits": [
					{title: "Title of hit #1", url: "http://www.domainHit1.com/hit1.html"},
					{title: "Title of hit #2", url: "http://www.domainHit2.com/hit2.html"}
				]
		}
	    window.srchController = new SearchControllerMock(srchControllerConfig, resp);
		
		
		// Add HTML elements that are used by this srchController
        var formHTML =
                  "Query: <input id=\""+srchControllerConfig.txtQuery+"\"><br/>\n"
                + "<button id=\"btn-search\" \"onClick\"=\"srchController.onSearch()\">Search</button><br/>\n"
                + "<p/>\n<br/>\n<p/>\n"
                + "Error message: <div id=\""+srchControllerConfig.divError+"\"></div><br/>\n"
                + "Total Hits: <div id=\""+srchControllerConfig.divTotalHits+"\"></div><br/>\n"
                + "Results:<p/><div id=\""+srchControllerConfig.divResults+"\"></div><br/>\n"
                ;
        console.log("-- SearchControllerTest.setup: formHTML=\n"+formHTML);
		$("#testMainDiv").html(formHTML);
		
		$("#"+srchControllerConfig.btnSearch).off('click').on("click", function() {srchController.onSearch();});
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

QUnit.test("SearchController.onSearchFailure", function( assert ) 
{
	var caseDescr = "SearchController.onSearchFailure -- HappyPath";
	
	var expErrMessage = "There was some kind of error, etc..."
	var resp = {
			"errorMessage": expErrMessage		
	}
	srchController.onSearchFailure(resp);
	
	assertErrorMessageWasDisplayed(assert, expErrMessage, caseDescr);
	assertTrainButtonIsEnabled(assert, caseDescr);
});

QUnit.test("SearchController.Acceptance -- HappyPath", function( assert ) 
{
	var caseDescr = "SearchController.Acceptance -- HappyPath";
	
    var helpers = new TestHelpers();
    helpers.clickOn("btn-search");
    
    assertNoErrorDisplayed(assert, caseDescr);
	assertQueryEquals(assert, "ᓄᓇᕗᑦ");
	assertSearchButtonEnabled(assert, caseDescr);
	assertDisplayedTotalHitsIs(assert, "18", caseDescr);
	var expHits = [
		{url: "BLAH"}
	];
	assertHitsEqual(assert, expHits, caseDescr)
});

//QUnit.test("SearchController.Acceptance -- HappyPath", function( assert ) 		
//		{
//			var caseDescr = "SearchController.Acceptance -- HappyPath";
//
//			var helpers = new TestHelpers();
//			
//			// Attach the mock service response to the controller
//			var resp = {
//					'expandedQuery': 'ᓄᓇᕗᑦ',
//					'totalHist': 114,
//					'hits': []
//				};
////			helpers.attachMockResponse(srchController, resp, "Search");
//			
//			// Fill the query field and hit Search button
//			helpers.typeText(srchController.txtQuery, 'ᓄᓇᕗᑦ');
////			helpers.clickOn(srchController.btnSearch)
//			srchController.onSearchSuccess(resp);
//
//			
//			
//			
//			srchController.onSearchSuccess(resp);
//			
//			
//			assertDisplayedResultsAre(assert, resp.results.scrapedRelations, caseDescr);
//		});


//
//QUnit.test("SearchController.getTrainingRequestData -- HappyPath", function( assert ) 
//		{
//			var caseDescr = "SearchController.getTrainingRequestData -- HappyPath";
//			
//			enterTrainingURL("http://weknowmovies.com/sci-fi/");
//			enterFieldNames("title; director; year");
//			enterSampleRelationValues(1, "Blade Runner 2049; Denis Villeneuve; 2017")
//			enterSampleRelationValues(2, "Blade Runner; Ridley Scott; 1982")
//			
//
//			var gotRequestData = JSON.parse(srchController.getTrainingRequestData());
//			var expRequestData = 
//					{
//						action: "train", trainingURL: "http://weknowmovies.com/sci-fi/",
//						trainingFields:
//							[
//							 {name:"title",value:"Blade Runner 2049"},{name:"director",value:"Denis Villeneuve"},{name: "year", value:  "2017"},
//							 {name:"title",value:"Blade Runner"}, {name:"director",value:"Ridley Scott"},{name:"year",value:"1982"}
//							]
//					};
//			
//			assert.deepEqual(gotRequestData, expRequestData, caseDescr);
//		});
//
//QUnit.test("SearchController.getTrainingRequestData -- One of Two Sample Relations is Empty", function( assert ) 
//		{
//			var caseDescr = "SearchController.getTrainingRequestData -- One of Two Sample Relations is Empty";
//			
//			enterTrainingURL("http://weknowmovies.com/sci-fi/");
//			enterFieldNames("title; director; year");
//			enterSampleRelationValues(1, "Blade Runner 2049; Denis Villeneuve; 2017")
//			
//
//			var gotRequestData = JSON.parse(srchController.getTrainingRequestData());
//			var expRequestData = 
//					{
//						action: "train", trainingURL: "http://weknowmovies.com/sci-fi/",
//						trainingFields:
//							[
//							 {name:"title",value:"Blade Runner 2049"},{name:"director",value:"Denis Villeneuve"},{name: "year", value:  "2017"},
//							]
//					};
//			
//			assert.deepEqual(gotRequestData, expRequestData, caseDescr);
//		});

/**********************************
 * HELPER METHODS
 **********************************/

function assertQueryEquals(assert, expQuery) {
	var gotQuery = $("#"+srchControllerConfig.txtQuery).val();
	assert.deepEqual(gotQuery, expQuery, "Query field did not contain the expected string.");
}


function enterTrainingURL(trainingURL) {
	$("#"+srchControllerConfig.txtTrainURL).val(trainingURL);
}

function enterFieldNames(namesString) {
	$("#"+srchControllerConfig.txtFieldNames).val(namesString);
}

function enterSampleRelationValues(ii, fieldValuesString) {
	$("#"+srchControllerConfig.txtSampleRelations+ii).val(fieldValuesString);
}


function assertDisplayedResultsAre(assert, expResults, caseDescr) {
	var message = "Checking that results were properly displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
	
	assertNoErrorDisplayed(assert, message);
	assertTrainButtonIsEnabled(assert, message);
	
	var displayedResults = $("#"+srchControllerConfig.divResults).html();	
	var expResultsJson = JSON.stringify(expResults);
	assert.deepEqual(displayedResults, expResultsJson, message);
}

function assertErrorMessageWasDisplayed(assert, expErrMessage, caseDescr) {
	var message = "Checking that errror message was displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
		
	assert.deepEqual(getErrorMessage(), expErrMessage, message);
}

function assertNoErrorDisplayed(assert, caseDescr) {
	var message = "Checking that no errror messages were displayed.";
	if (caseDescr != null) message = caseDescr+"\n"+message;

	assert.deepEqual(getErrorMessage(), "", message);	
}

function getErrorMessage() {
	var errMessage = $("#"+srchControllerConfig.divError).html();
	return errMessage;
}

function assertSearchButtonEnabled(assert, caseDescr) {
	var message = "Checking that 'Search' button is enabled.";
	if (caseDescr != null) message = caseDescr+"\n"+message;
	
	var isDisabled = $("#"+srchControllerConfig.btnSearch).prop('disabled')
	
	assert.ok(!isDisabled);
}

function assertDisplayedTotalHitsIs(assert, expTotalHits, caseDescr) {
	var message = "Checking that total number of hits displayed is: "+expTotalHits;
	if (caseDescr != null) message = caseDescr+"\n"+message;
	
	assert.deepEqual(getTotalHits(), expTotalHits, message);
}

function getTotalHits() {
	var totalHits = $("#"+srchControllerConfig.divTotalHits).text();
	return totalHits;
}

function assertHitsEqual(assert, expHits, caseDescr) {
	
	$("#"+srchControllerConfig.divResults+" #hit").each(function( index ) {
		  console.log( "-- assertHitsEqual: Looking at hit with text: " + $( this ).text() );
		});
	assert.ok(false);
}




