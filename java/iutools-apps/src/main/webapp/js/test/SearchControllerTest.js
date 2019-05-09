var srchControllerConfig = {
		btnSearch: "btn-search",
		txtQuery: "txt-query",
		divError: "div-error-msg",
		divResults:  "div-search-results",
		divTotalHits: "div-total-hits",
		divPageNumbers: "page-numbers"
	};

var srchController = null;
var mockResp = null;



QUnit.module("SearchController Tests", {
	beforeEach: function(assert) {
		
		// Add HTML elements that are used by this srchController
        var formHTML =
                  "Query: <input id=\""+srchControllerConfig.txtQuery+"\" type=\"text\"><br/>\n"
                + "<button id=\""+srchControllerConfig.btnSearch+"\">Search</button><br/>\n"
                + "<p/>\n<br/>\n<p/>\n"
                + "Error message: <div id=\""+srchControllerConfig.divError+"\"></div><br/>\n"
                + "Total Hits: <div id=\""+srchControllerConfig.divTotalHits+"\"></div><br/>\n"
                + "Results:<p/><div id=\""+srchControllerConfig.divResults+"\"></div><br/>\n"
                + "<div id=\""+srchControllerConfig.divPageNumbers+"\"></div><br/>\n"
                ;
		$("#testMainDiv").html(formHTML);
		
		mockResp = {
				"errorMessage": null,
				"expandedQuery": "ᓄᓇᕗᑦ",
				"totalHits": 18,
				"hits": [
					
					// First page of hits
					{title: "Title of hit #1", url: "http://www.domainHit1.com/hit1.html",
						snippet: "... snippet of hit #1 ..."},
					{title: "Title of hit #2", url: "http://www.domainHit2.com/hit2.html",
						snippet: "... snippet of hit #2 ..."},
					{title: "Title of hit #3", url: "http://www.domainHit3.com/hit3.html",
						snippet: "... snippet of hit #3 ..."},
					{title: "Title of hit #4", url: "http://www.domainHit4.com/hit4.html",
						snippet: "... snippet of hit #4 ..."},
					{title: "Title of hit #5", url: "http://www.domainHit5.com/hit5.html",
						snippet: "... snippet of hit #5 ..."},
					{title: "Title of hit #6", url: "http://www.domainHit6.com/hit6.html",
						snippet: "... snippet of hit #6 ..."},
					{title: "Title of hit #7", url: "http://www.domainHit7.com/hit7.html",
						snippet: "... snippet of hit #7 ..."},
					{title: "Title of hit #8", url: "http://www.domainHit8.com/hit8.html",
						snippet: "... snippet of hit #8 ..."},
					{title: "Title of hit #9", url: "http://www.domainHit9.com/hit9.html",
						snippet: "... snippet of hit #9 ..."},
					{title: "Title of hit #10", url: "http://www.domainHit10.com/hit10.html",
						snippet: "... snippet of hit #10 ..."},
						
					// Second page of hits
					{title: "Title of hit #11", url: "http://www.domainHit11.com/hit11.html",
						snippet: "... snippet of hit #11 ..."},
					{title: "Title of hit #12", url: "http://www.domainHit12.com/hit12.html",
						snippet: "... snippet of hit #12 ..."}
						
				]
			};		
		
	    srchController = new SearchController(srchControllerConfig);
	    attachMockAjaxResponse(srchController, mockResp);		
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

QUnit.test("SearchController.Acceptance -- HappyPath", function( assert ) 
{
	var caseDescr = "SearchController.Acceptance -- HappyPath";
	
    var helpers = new TestHelpers();
    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
	console.log("-- SearchController.Acceptance -- HappyPath: AFTER typeText, $('#'+srchControllerConfig.txtQuery).length="+$('#'+srchControllerConfig.txtQuery).length+", $('#'+srchControllerConfig.txtQuery).val()="+$('#'+srchControllerConfig.txtQuery).val());
    helpers.clickOn(srchControllerConfig.btnSearch);
    
    assertNoErrorDisplayed(assert, caseDescr);
	assertQueryEquals(assert, "ᓄᓇᕗᑦ");
	assertSearchButtonEnabled(assert, caseDescr);
	assertDisplayedTotalHitsIs(assert, "Found 18 hits", caseDescr);
	var expHits = mockResp.hits;
	assertHitsEqual(assert, expHits, caseDescr)
	assertPageButtonsAreOK(assert, 2, caseDescr)				
});

QUnit.test("SearchController.Acceptance -- Query field is empty -- Displays error", function( assert ) 
{
	var caseDescr = "SearchController.Acceptance -- Query field is empty -- Displays error";
	
    var helpers = new TestHelpers();
    helpers.typeText(srchControllerConfig.txtQuery, "");
    helpers.clickOn(srchControllerConfig.btnSearch);
    
    assertErrorDisplayed(assert, "You need to enter something in the query field", caseDescr);
	assertSearchButtonEnabled(assert, caseDescr);
	assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
	var expHits = [];
	assertHitsEqual(assert, expHits, caseDescr)
	assertPageButtonsAreOK(assert, 0, caseDescr)			
});

QUnit.test("SearchController.Acceptance -- Press Return in Query field -- Runs the search", function( assert ) 
		{
			var caseDescr = "SearchController.Acceptance -- Press Return in Query field -- Runs the search";
			
		    var helpers = new TestHelpers();
		    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
		    helpers.pressEnter(srchControllerConfig.txtQuery);
		    
		    assertNoErrorDisplayed(assert, caseDescr);
			assertQueryEquals(assert, "ᓄᓇᕗᑦ");
			assertSearchButtonEnabled(assert, caseDescr);
			assertDisplayedTotalHitsIs(assert, "Found 18 hits", caseDescr);
			var expHits = mockResp.hits;
			assertHitsEqual(assert, expHits, caseDescr)
			assertPageButtonsAreOK(assert, 2, caseDescr)			
		});

QUnit.test("SearchController.Acceptance -- Web service returns errMessage -- Displays message", function( assert ) 
		{
			var caseDescr = "SearchController.Acceptance -- Web service returns errorMessage -- Displays message";
			
			mockResp = {"errorMessage": "There was an error in the web service"};
			attachMockAjaxResponse(srchController, mockResp);
			
		    var helpers = new TestHelpers();
		    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
		    helpers.pressEnter(srchControllerConfig.txtQuery);
		    
		    assertErrorDisplayed(assert, "There was an error in the web service", caseDescr);
			assertSearchButtonEnabled(assert, caseDescr);
			assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
			var expHits = [];
			assertHitsEqual(assert, expHits, caseDescr)
			assert.ok(true);
		});

QUnit.test("SearchController.generatePagesButtons -- HappyPath", function( assert ) 
		{
			var caseDescr = "SearchController.generatePagesButtons -- HappyPath";
			
			srchController.generatePagesButtons(143);
			assertPageButtonsAreOK(assert, 15, caseDescr)
		});

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

function attachMockAjaxResponse(controller, _mockResp) {
	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successCallback", "failureCallback");		
}

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

function assertErrorDisplayed(assert, expErr, caseDescr) {
	var message = "Checking the displayed error message";
	if (caseDescr != null) message = caseDescr+"\n"+message;

	assert.deepEqual(getErrorMessage(), expErr, message);	
	
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
	var totalHits = srchController.elementForProp("divTotalHits").text();
	return totalHits;
}

function assertHitsEqual(assert, expHits, caseDescr) {
	
	var gotHits = [];
	$("#"+srchControllerConfig.divResults+" .hitDiv").each(function( index ) {
			var hit = 
					{
						title: $( this ).find("#hitTitle").text().trim(),
						url: $( this ).find("#hitURL").text().trim(),
						snippet: $( this ).find("#hitSnippet").text().trim()
					};
			gotHits.push(hit);
		});
	assert.deepEqual(gotHits, expHits, "Displayed hits were not as expected");
}

function assertPageButtonsAreOK(assert, expNum, caseDescr) {
	var divPageButtons = srchController.elementForProp('divPageNumbers');
	
	var gotPageNumbers = [];
	$('#'+srchController.config['divPageNumbers']+" input").each(
			function (index, value) {
				console.log('-- assertPageButtonsAreOK: button text=' + $(this).text() + ', value=' + $(this).attr('value'));
				gotPageNumbers.push($(this).attr('value'));
			}
		);
	
	var expPageNumbers = [];
	for (var ii=0; ii < expNum; ii++) expPageNumbers.push((ii+1).toString());
	assert.deepEqual(gotPageNumbers, expPageNumbers, "Page number buttons were not as expected");
}




