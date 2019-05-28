var spellControllerConfig = {
		btnSpell: "btn-spell",
		txtToCheck: "txt-to-check",
		divChecked:  "div-checked",
		divError: "div-error",
    };

var helpers = new TestHelpers();

var spellController = null;
var mockSpellSrvResp = null;


QUnit.module("SpellController Tests", {
	beforeEach: function(assert) {
		
	    /***************************************
	     * DEFINE HELPER METHODS
	     ***************************************/
	    
	    attachMockAjaxResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successCallback", "failureCallback");		
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

	    getErrorMessage = function() {
	    	var errMessage = spellController.elementForProp('divError').html();
	    	return errMessage;
	    }

	    assertSpellButtonEnabled = function(assert, caseDescr) {
	    	var message = "Checking that 'Search' button is enabled.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	var isDisabled = $("#"+spellControllerConfig.btnSearch).prop('disabled')
	    	
	    	assert.ok(!isDisabled);
	    }
	    
	    /***************************************
	     * Setup HTML page and Controller for testing
	     ***************************************/

		// Add HTML elements that are used by this spellController
        var formHTML =
            "<button id=\""+spellControllerConfig.btnSpell+"\"><br/>\n"
          + "<textarea id=\""+spellControllerConfig.txtToCheck+"\" name=\""+spellControllerConfig.txtToCheck+"\" rows=5 cols=40></textarea><p/>" 
          + "<div id=\""+spellControllerConfig.divError+"\" class=\""+spellControllerConfig.divError+"\"></div><br/>\n"
          + "<div id=\""+spellControllerConfig.divChecked+"\" class=\""+spellControllerConfig.divChecked+"\"></div>"
          
          ;
        $("#testMainDiv").html(formHTML);	
	
	
		mockSpellSrvResp = {
			status: null,
			errorMessage: null,
			stackTrace: null,
			correction: [
				{orig: 'Inuktut', 'wasMispelled': false, 'possibleSpellings': []},
				{orig: ', ', 'wasMispelled': false, 'possibleSpellings': []},
				{orig: 'nunavuttt', 'wasMispelled': true, 'possibleSpellings': ['nunavut']},
				{orig: 'inuksuk', 'wasMispelled': false, 'possibleSpellings': []},
				{orig: '.', 'wasMispelled': false, 'possibleSpellings': []},
			]
		};	 
	    
		console.log("-- beforeEach: document is ready... setting up the controller")
	    spellController = new SpellController(spellControllerConfig);
	    attachMockAjaxResponse(spellController, mockSpellSrvResp);				
		console.log("-- beforeEach: DONE setting up the controller")		
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

QUnit.test("SpellController.Acceptance -- HappyPath", function( assert )
{
	
	var caseDescr = "SpellController.Acceptance -- HappyPath";
	console.log("-- "+caseDescr+": STARTED, spellController="+spellController);
    helpers.typeText(spellControllerConfig.txtToCheck, "Inuktut, nunavutt inuksuk.");
    helpers.clickOn(spellControllerConfig.btnSpell);
    	
    assertNoErrorDisplayed(assert, caseDescr);
	assertSpellButtonEnabled(assert, caseDescr);
	assert.ok(true);
	
});

//QUnit.test("SpellController.Acceptance -- Query field is empty -- Displays error", function( assert ) 
//{
//	var caseDescr = "SpellController.Acceptance -- Query field is empty -- Displays error";
//	
//    var helpers = new TestHelpers();
//    helpers.typeText(spellControllerConfig.txtQuery, "");
//    helpers.clickOn(spellControllerConfig.btnSearch);
//    
//    assertErrorDisplayed(assert, "You need to enter something in the query field", caseDescr);
//	assertSpellButtonEnabled(assert, caseDescr);
//	assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
//	var expHits = [];
//	assertHitsEqual(assert, expHits, caseDescr)
//	assertPageButtonsAreOK(assert, 0, caseDescr)			
//});
//
//QUnit.test("SpellController.Acceptance -- Press Return in Query field -- Runs the search", function( assert ) 
//		{
//			var caseDescr = "SpellController.Acceptance -- Press Return in Query field -- Runs the search";
//			
//		    var helpers = new TestHelpers();
//		    helpers.typeText(spellControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
//		    helpers.pressEnter(spellControllerConfig.txtQuery);
//		    
//		    assertNoErrorDisplayed(assert, caseDescr);
//			assertQueryEquals(assert, "ᓄᓇᕗᑦ");
//			assertSpellButtonEnabled(assert, caseDescr);
//			assertDisplayedTotalHitsIs(assert, "Found 12 hits", caseDescr);
//			var expHits = mockRespPage1.hits;
//			assertHitsEqual(assert, expHits, caseDescr)
//			assertPageButtonsAreOK(assert, 2, caseDescr)			
//		});
//
//QUnit.test("SpellController.Acceptance -- Web service returns errMessage -- Displays message", function( assert ) 
//		{
//			var caseDescr = "SpellController.Acceptance -- Web service returns errorMessage -- Displays message";
//			
//			mockResp = {"errorMessage": "There was an error in the web service"};
//			attachMockAjaxResponse(spellController, mockResp);
//			
//		    var helpers = new TestHelpers();
//		    helpers.typeText(spellControllerConfig.txtQuery, "ᓄᓇᕗᑦ");
//		    helpers.clickOn(spellControllerConfig.btnSearch);
//		    
//		    assertErrorDisplayed(assert, "There was an error in the web service", caseDescr);
//			assertSpellButtonEnabled(assert, caseDescr);
//			assertDisplayedTotalHitsIs(assert, "No hits found", caseDescr);
//			var expHits = [];
//			assertHitsEqual(assert, expHits, caseDescr)
//			assert.ok(true);
//		});
//
//QUnit.test("SpellController.generatePagesButtons -- HappyPath", function( assert ) 
//{
//	var caseDescr = "SpellController.generatePagesButtons -- HappyPath";
//	
//	spellController.generatePagesButtons(143);
//	assertPageButtonsAreOK(assert, 10, caseDescr)
//});










