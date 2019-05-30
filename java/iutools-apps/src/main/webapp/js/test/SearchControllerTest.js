var srchControllerConfig = {
		btnSearch: "btn-search",
		txtQuery: "txt-query",
		divMessage: "div-message",
		divError: "div-error-msg",
		divResults:  "div-search-results",
		divTotalHits: "div-total-hits",
		divPageNumbers: "page-numbers",
		prevPage: "previous-page",
		nextPage: "next-page"
	};

var srchController = null;
var mockRespPage1 = null;
var mockRespPage2 = null;


QUnit.module("SearchController Tests", {
	
	beforeEach: function(assert) {
	    
	    ///////////////////////////////////////
	    // DEFINE HELPER METHODS
	    ///////////////////////////////////////

	    getErrorMessage = function() {
	    	var errMessage = srchController.elementForProp('divError').html();
	    	return errMessage;
	    }
	    
	    attachMockAjaxResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successCallback", "failureCallback");		
	    }

	    assertQueryEquals = function(assert, expQuery) {
	    	var gotQuery = $("#"+srchControllerConfig.txtQuery).val();
	    	assert.deepEqual(gotQuery, expQuery, "Query field did not contain the expected string.");
	    }


	    enterTrainingURL = function(trainingURL) {
	    	$("#"+srchControllerConfig.txtTrainURL).val(trainingURL);
	    }

	    enterFieldNames = function(namesString) {
	    	$("#"+srchControllerConfig.txtFieldNames).val(namesString);
	    }

	    enterSampleRelationValues = function(ii, fieldValuesString) {
	    	$("#"+srchControllerConfig.txtSampleRelations+ii).val(fieldValuesString);
	    }


	    assertDisplayedResultsAre = function(assert, expResults, caseDescr) {
	    	var message = "Checking that results were properly displayed.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	assertNoErrorDisplayed(assert, message);
	    	assertTrainButtonIsEnabled(assert, message);
	    	
	    	var displayedResults = $("#"+srchControllerConfig.divResults).html();	
	    	var expResultsJson = JSON.stringify(expResults);
	    	assert.deepEqual(displayedResults, expResultsJson, message);
	    }

	    assertErrorMessageWasDisplayed = function(assert, expErrMessage, caseDescr) {
	    	var message = "Checking that errror message was displayed.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    		
	    	assert.deepEqual(getErrorMessage(), expErrMessage, message);
	    }

	    assertNoErrorDisplayed = function(assert, caseDescr) {
	    	var message = "Checking that no errror messages were displayed.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;

	    	assert.deepEqual(getErrorMessage(), "", message);	
	    }

	    assertErrorDisplayed = function(assert, expErr, caseDescr) {
	    	var message = "Checking the displayed error message";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;

	    	assert.deepEqual(getErrorMessage(), expErr, message);	
	    	
	    }

	    assertSearchButtonEnabled = function(assert, caseDescr) {
	    	var message = "Checking that 'Search' button is enabled.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	var isDisabled = $("#"+srchControllerConfig.btnSearch).prop('disabled')
	    	
	    	assert.ok(!isDisabled);
	    }

	    assertDisplayedTotalHitsIs = function(assert, expTotalHits, caseDescr) {
	    	var message = "Checking that total number of hits displayed is: "+expTotalHits;
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	assert.deepEqual(getTotalHits(), expTotalHits, message);
	    }

	    getTotalHits = function() {
	    	var totalHits = srchController.elementForProp("divTotalHits").text();
	    	return totalHits;
	    }

	    assertHitsEqual = function(assert, expHits, caseDescr) {
	    	
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

	    assertPageButtonsAreOK = function(assert, expNum, caseDescr) {
	    	var divPageButtons = srchController.elementForProp('divPageNumbers');
	    	
	    	var gotPageNumbers = [];
	    	$('#'+srchController.config['divPageNumbers']+" input").each(
	    			function (index, value) {
//	    				console.log('-- assertPageButtonsAreOK: button text=' + $(this).text() + ', value=' + $(this).attr('value'));
	    				gotPageNumbers.push($(this).attr('value'));
	    			}
	    		);
	    	
	    	var expPageNumbers = [];
	    	for (var ii=0; ii < expNum; ii++) expPageNumbers.push((ii).toString());
	    	assert.deepEqual(gotPageNumbers, expPageNumbers, "Page number buttons were not as expected");
	    }	 
		
		/*********************************************
		 * Setup HTML page and controller for testing
		 *********************************************/ 

		// Add HTML elements that are used by this srchController
        var formHTML =
                  "Query: <input id=\""+srchControllerConfig.txtQuery+"\" type=\"text\"><br/>\n"
                + "<button id=\""+srchControllerConfig.btnSearch+"\">Search</button><br/>\n"
                + "<p/>\n<br/>\n<p/>\n"
                + "Progress message: <div id=\""+srchControllerConfig.divMessage+"\"></div><br/>\n"
                + "Error message: <div id=\""+srchControllerConfig.divError+"\"></div><br/>\n"
                + "Total Hits: <div id=\""+srchControllerConfig.divTotalHits+"\"></div><br/>\n"
                + "Results:<p/><div id=\""+srchControllerConfig.divResults+"\"></div><br/>\n"
                + "<button id=\""+srchControllerConfig.prevPage+"\">\n"
                + "<div id=\""+srchControllerConfig.divPageNumbers+"\"></div><br/>\n"
                + "<button id=\""+srchControllerConfig.nextPage+"\">\n"
                ;
		$("#testMainDiv").html(formHTML);
		
		// First page of hits
		mockRespPage1 = {
				"errorMessage": null,
				"expandedQuery": "ᓄᓇᕗᑦ",
				"totalHits": 12,
				"hits": [
					
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
						snippet: "... snippet of hit #10 ..."}
				]
			};	
		
		// Second page of hits
		mockRespPage2 = {
				"errorMessage": null,
				"expandedQuery": "ᓄᓇᕗᑦ",
				"totalHits": 12,
				"hits": [
					{title: "Title of hit #11", url: "http://www.domainHit11.com/hit11.html",
						snippet: "... snippet of hit #11 ..."},
					{title: "Title of hit #12", url: "http://www.domainHit12.com/hit12.html",
						snippet: "... snippet of hit #12 ..."}	
				]
			};
		
		srchController = new SearchController(srchControllerConfig);
	    attachMockAjaxResponse(srchController, mockRespPage1);	
	    
	    	    
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
	var done = assert.async();
	
	var caseDescr = "SearchController.Acceptance -- HappyPath";
	
    var helpers = new TestHelpers();
    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
    helpers.clickOn(srchControllerConfig.btnSearch);
    
    assertNoErrorDisplayed(assert, caseDescr);
	assertQueryEquals(assert, "ᓄᓇᕗᑦ");
	assertSearchButtonEnabled(assert, caseDescr);
	assertDisplayedTotalHitsIs(assert, "Found 12 hits", caseDescr);
	var expHits = mockRespPage1.hits;
	assertHitsEqual(assert, expHits, caseDescr)
	assertPageButtonsAreOK(assert, 2, caseDescr)
	
	done();
});

QUnit.test("SearchController.Acceptance -- Query field is empty -- Displays error", function( assert ) 
{
	var done = assert.async();
	
	var caseDescr = "SearchController.Acceptance -- Query field is empty -- Displays error";
	
    var helpers = new TestHelpers();
    helpers.typeText(srchControllerConfig.txtQuery, "");
    helpers.clickOn(srchControllerConfig.btnSearch);
    
    assertErrorDisplayed(assert, "You need to enter something in the query field", caseDescr);
	assertSearchButtonEnabled(assert, caseDescr);
	assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
	var expHits = [];
	assertHitsEqual(assert, expHits, caseDescr);
	assertPageButtonsAreOK(assert, 0, caseDescr);
		
	done();
});

QUnit.test("SearchController.Acceptance -- Press Return in Query field -- Runs the search", function( assert ) 
		{
			var done = assert.async();

			var caseDescr = "SearchController.Acceptance -- Press Return in Query field -- Runs the search";
			
		    var helpers = new TestHelpers();
		    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
		    helpers.pressEnter(srchControllerConfig.txtQuery);
		    
		    assertNoErrorDisplayed(assert, caseDescr);
			assertQueryEquals(assert, "ᓄᓇᕗᑦ");
			assertSearchButtonEnabled(assert, caseDescr);
			assertDisplayedTotalHitsIs(assert, "Found 12 hits", caseDescr);
			var expHits = mockRespPage1.hits;
			assertHitsEqual(assert, expHits, caseDescr);
			assertPageButtonsAreOK(assert, 2, caseDescr);
			
			done();
		});

QUnit.test("SearchController.Acceptance -- Web service returns errMessage -- Displays message", function( assert ) 
		{
			var done = assert.async();
		
			var caseDescr = "SearchController.Acceptance -- Web service returns errorMessage -- Displays message";
			
			mockResp = {"errorMessage": "There was an error in the web service"};
			attachMockAjaxResponse(srchController, mockResp);
			
		    var helpers = new TestHelpers();
		    helpers.typeText(srchControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
		    helpers.clickOn(srchControllerConfig.btnSearch);
		    
		    assertErrorDisplayed(assert, "There was an error in the web service", caseDescr);
			assertSearchButtonEnabled(assert, caseDescr);
			assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
			var expHits = [];
			assertHitsEqual(assert, expHits, caseDescr)
			assert.ok(true);
			
			done();
		});

QUnit.test("SearchController.generatePagesButtons -- HappyPath", function( assert ) 
{
	var done = assert.async();	
	
	var caseDescr = "SearchController.generatePagesButtons -- HappyPath";
	
	srchController.generatePagesButtons(143);
	assertPageButtonsAreOK(assert, 10, caseDescr)
	
	done();
});










