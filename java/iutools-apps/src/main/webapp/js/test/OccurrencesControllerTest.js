var occControllerConfig = {
		btnGet: "btn-occ",
		morpheme: "morpheme",
		exampleWord: "example-word",
		divResults:  "div-results",
		divMessage: "div-message",
		divExampleWord: "div-example-word",
		divError: "div-error",
		iconizer: "iconizer",
		divIconizedExampleWord: "div-example-word-iconized"
	};

var occController = null;


QUnit.module("OccurenceController Tests", {
	
	beforeEach: function(assert) {
	    
	    ///////////////////////////////////////
	    // DEFINE HELPER METHODS
	    ///////////////////////////////////////

	    getErrorMessage = function() {
	    	var errMessage = occController.elementForProp('divError').html();
	    	return errMessage;
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

	    assertGetButtonEnabled = function(assert, caseDescr) {
	    	var message = "Checking that 'Get' button is enabled.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	var isDisabled = $("#"+occControllerConfig.btnGet).prop('disabled')
	    	
	    	assert.ok(!isDisabled);
	    }
	    
	    assertMorphemeListEquals = function(assert, expectedList, caseDescr) {
	    	var message = "Checking the list of displayed morphemes.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	var gotList = [];
	    	$('div#list-of-morphemes a').each(function(index) {
	    		gotList.push($(this).text());
	    	});
	    	assert.deepEqual(gotList,expectedList,message)
	    }
	    
	    assertMorphemeDetailsEquals = function(assert, morphemeIndex, expectedDetails, caseDescr) {
	    	var message = "Checking the displayed morpheme details for morpheme "+morphemeIndex+".";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	var gotDetailsDiv = $('div.morpheme-details').eq(morphemeIndex);
	    	var gotDetails = gotDetailsDiv.text();
	    	new TestHelpers().assertStringEquals(assert, message, gotDetails, expectedDetails, true);
	    }

	    attachMockMorphemeResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successCallback", "failureCallback");		
	    }

	    attachMockWordResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successExampleWordCallback", "failureExampleWordCallback");		
	    }


	    // HashMap<String,Pair<String,Pair<String,Long>[]>>
	    mockMorphemeResp = {matchingWords: {"siuq/1nv":{meaning:"to go after; to search", words:["nanusiuqti","tuktusiulauqtut"], wordFrequencies:[132,45]}}};
	    mockWordResp = {exampleWord:{gist:{word:"blah",wordComponents:["blah","blah"]},alignments:["19990101:: abc@----@ aaa","19990202:: xyz@----@ xxx"]}};


		
		/*********************************************
		 * Setup HTML page and controller for testing
		 *********************************************/ 

		// Add HTML elements that are used by this occController
        var formHTML = "Morpheme: <input id=\""+occControllerConfig.morpheme+"\"><br>\n"+
        	"<button id=\""+occControllerConfig.btnGet+"\" type=\"button\" value=\"Occurrences\">Get words</button><br>\n"+
        	"<input type=\"hidden\" id=\""+occControllerConfig.exampleWord+"\" value=\"\"><br>\n"+
        	"<div id=\""+occControllerConfig.divMessage+"\" class=\"div-message\"></div>\n"+
        	"<div id=\""+occControllerConfig.divError+"\" class=\"div-error\"></div>\n"+
        	"<div id=\""+occControllerConfig.divResults+"\" class=\"div-results\"></div>\n"+
        	"<div id=\""+occControllerConfig.divExampleWord+"\" class=\"div-example-word\"><div id=\"word\"></div><div id=\""+occControllerConfig.iconizer+"\" title=\"Minimize\"><img src=\"imgs/minimize.png\" ></div><div id=\"contents\"></div></div>\n"+
        	"<div id=\""+occControllerConfig.divIconizedExampleWord+"\" title=\"Maximize\"><img src=\"imgs/maximize.png\" height=24 ></div>"
                ;
		$("#testMainDiv").html(formHTML);
		
		
		occController = new OccurrenceController(occControllerConfig);
	    attachMockMorphemeResponse(occController, mockMorphemeResp);	
	    attachMockWordResponse(occController, mockWordResp);	
	    
	    	    
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

QUnit.test("OccurenceController.Acceptance -- HappyPath", function( assert ) 
{
	var done = assert.async();
	
	var caseDescr = "OccurenceController.Acceptance -- HappyPath";
	
    var helpers = new TestHelpers();
    helpers.typeText(occControllerConfig.morpheme, "siuq");
    helpers.clickOn(occControllerConfig.btnGet);
    
    assertNoErrorDisplayed(assert, caseDescr);
	assertGetButtonEnabled(assert, caseDescr);
	
	var expectedList = ["siuq/1nv"];
	assertMorphemeListEquals(assert, expectedList, caseDescr);
	
	var expectedMorphemeDetails = "siuq/1nv   –   to go after; to searchnanusiuqti(132);    tuktusiulauqtut(45)";
	assertMorphemeDetailsEquals(assert, 0, expectedMorphemeDetails, caseDescr);
	
	helpers.clickOn("word-example-nanusiuqti");
	done();
});

//QUnit.test("OccurenceController.Acceptance -- Query field is empty -- Displays error", function( assert ) 
//{
//	var done = assert.async();
//	
//	var caseDescr = "OccurenceController.Acceptance -- Query field is empty -- Displays error";
//	
//    var helpers = new TestHelpers();
//    helpers.typeText(occControllerConfig.txtQuery, "");
//    helpers.clickOn(occControllerConfig.btnSearch);
//    
//    assertErrorDisplayed(assert, "You need to enter something in the query field", caseDescr);
//	assertSearchButtonEnabled(assert, caseDescr);
//	assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
//	var expHits = [];
//	assertHitsEqual(assert, expHits, caseDescr);
//	assertPageButtonsAreOK(assert, 0, caseDescr);
//		
//	done();
//});
//
//QUnit.test("OccurenceController.Acceptance -- Press Return in Query field -- Runs the search", function( assert ) 
//		{
//			var done = assert.async();
//
//			var caseDescr = "OccurenceController.Acceptance -- Press Return in Query field -- Runs the search";
//			
//		    var helpers = new TestHelpers();
//		    helpers.typeText(occControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
//		    helpers.pressEnter(occControllerConfig.txtQuery);
//		    
//		    assertNoErrorDisplayed(assert, caseDescr);
//			assertQueryEquals(assert, "ᓄᓇᕗᑦ");
//			assertSearchButtonEnabled(assert, caseDescr);
//			assertDisplayedTotalHitsIs(assert, "Found 12 hits", caseDescr);
//			var expHits = mockRespPage1.hits;
//			assertHitsEqual(assert, expHits, caseDescr);
//			assertPageButtonsAreOK(assert, 2, caseDescr);
//			
//			done();
//		});
//
//QUnit.test("OccurenceController.Acceptance -- Web service returns errMessage -- Displays message", function( assert ) 
//		{
//			var done = assert.async();
//		
//			var caseDescr = "OccurenceController.Acceptance -- Web service returns errorMessage -- Displays message";
//			
//			mockResp = {"errorMessage": "There was an error in the web service"};
//			attachMockAjaxResponse(occController, mockResp);
//			
//		    var helpers = new TestHelpers();
//		    helpers.typeText(occControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
//		    helpers.clickOn(occControllerConfig.btnSearch);
//		    
//		    assertErrorDisplayed(assert, "There was an error in the web service", caseDescr);
//			assertSearchButtonEnabled(assert, caseDescr);
//			assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
//			var expHits = [];
//			assertHitsEqual(assert, expHits, caseDescr)
//			assert.ok(true);
//			
//			done();
//		});
//
//QUnit.test("OccurenceController.generatePagesButtons -- HappyPath", function( assert ) 
//{
//	var done = assert.async();	
//	
//	var caseDescr = "OccurenceController.generatePagesButtons -- HappyPath";
//	
//	occController.generatePagesButtons(143);
//	assertPageButtonsAreOK(assert, 10, caseDescr)
//	
//	done();
//});










