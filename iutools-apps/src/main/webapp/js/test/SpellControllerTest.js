var spellControllerConfig = {
		btnSpell: "btn-spell",
		btnCancelSpell: "btn-cancel-spell",
		txtToCheck: "txt-to-check",
		divChecked:  "div-checked",
		divError: "div-error",
		divMessage: "div-message",
    	divResults: "div-results",
    	btnCopy: "btn-copy"
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
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSpellService", "successCallback", "failureCallback");		
	    }

	    clearMockAjaxResponses = function(controller, _mockResp) {
	    	new TestHelpers().clearMockAjaxResponsesFor(controller, "invokeSpellService");		
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
	    	var message = "Checking that 'Spell' button is enabled.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	
	    	var isDisabled = spellController.elementForProp('btnSpell').prop('disabled')
	    	
	    	assert.ok(!isDisabled);
	    }
	    
	    assertCorrectedTextIs = function(assert, expText, caseDescr) {
	    	var message = "Checking the corrected text";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;

	    	var gotText = spellController.getCheckedText();
	    	helpers.assertStringEquals(assert, message, gotText, expText, true);
	    }
	    
	    /***************************************
	     * Setup HTML page and Controller for testing
	     ***************************************/

		// Add HTML elements that are used by this spellController
        var formHTML =
            "<button id=\""+spellControllerConfig.btnSpell+"\"><br/>\n"
			"<button id=\""+spellControllerConfig.btnCancelSpell+"\"><br/>\n"
          + "<textarea id=\""+spellControllerConfig.txtToCheck+"\" name=\""+spellControllerConfig.txtToCheck+"\" rows=5 cols=40></textarea><p/>"
          + "<div id=\""+spellControllerConfig.divMessage+"\" class=\""+spellControllerConfig.divMessage+"\"></div><br/>\n"
          + "<div id=\""+spellControllerConfig.divError+"\" class=\""+spellControllerConfig.divError+"\"></div><br/>\n"
          + "<div id=\""+spellControllerConfig.divChecked+"\" class=\""+spellControllerConfig.divChecked+"\"></div>\n"
      	  + "<div id=\"div-results\" class=\"div-results\"></div>\n"
      	  + "<button id=\"btn-copy\" type=\"button\" name=\"btn-copy\">Copy to clipboard</button>\n"
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
	    
		var tracer = Debug.getTraceLogger("SpellControllerTest.beforeEach");
		tracer.trace("Launching the method that will wait for the DOM to be ready before setting the controller")
		
		new RunWhen().domReady(function () {
			tracer.trace("document is ready... setting up the controller")
			var controller = new SpellController(spellControllerConfig);
			$.mockjax.clear();
			$.mockjax([
				{url: 'srv/spell', responseText: mockSpellSrvResp},
			]);
			
		    spellController = controller;
			tracer.trace("DONE setting up the controller")	
		});
		
		tracer.trace("exiting beforeEach");
	},
	
	afterEach: function(assert) {
		$.mockjax.clear();
	}
	
});

function spellControllerReady() {
	var ready = (spellController != null);
	return ready;
}

/**********************************
 * DOCUMENTATION TESTS
 **********************************/



/**********************************
 * VERIFICATION TESTS
 **********************************/

function spellControllerNotBusy() {
	var busy = spellController.busy;
	return ! busy;
}

function spellControllerIsDefined() {
	var defined = (spellController != null);
	return defined;
}

QUnit.test("SpellController.Acceptance -- HappyPath", function( assert )
{
	var done = assert.async();
	
	new RunWhen().conditionMet(spellControllerIsDefined, function() {
		var caseDescr = "SpellController.Acceptance -- HappyPath";
	    helpers.typeText(spellControllerConfig.txtToCheck, "Inuktut, nunavutt inuksuk.");
	    helpers.clickOn(spellControllerConfig.btnSpell);
	    
	    new RunWhen().conditionMet(spellControllerNotBusy, function() {
		    assertNoErrorDisplayed(assert, caseDescr);
			assertSpellButtonEnabled(assert, caseDescr);
			var expText = "Spell checked contentInuktut, nunavuttt nunavut inuksuk."
			assertCorrectedTextIs(assert, expText, caseDescr);
		
			done();
	    });
	});	
});

QUnit.test("SpellController.Acceptance -- Text field is empty -- Displays error", function( assert ) 
{
	var caseDescr = "SpellController.Acceptance -- Text field is empty -- Displays error";
	var done = assert.async();

	new RunWhen().conditionMet(spellControllerIsDefined, function() {
	    helpers.typeText(spellControllerConfig.txtToCheck, "");
	    helpers.clickOn(spellControllerConfig.btnSpell);
	    
	    new RunWhen().conditionMet(spellControllerNotBusy, function() {
	    	assertErrorDisplayed(assert, "You need to enter some text to spell check", caseDescr);
			assertSpellButtonEnabled(assert, caseDescr);
			var expText = ""
			assertCorrectedTextIs(assert, expText, caseDescr);
			
			done();
	    });
	});	
});


QUnit.test("SpellController.Acceptance -- Web service returns errMessage -- Displays message", function( assert ) 
{
	var caseDescr = "SpellController.Acceptance -- Web service returns errorMessage -- Displays message";
	var done = assert.async();
	
	new RunWhen().conditionMet(spellControllerIsDefined, function() {
	
		mockResp = {"errorMessage": "There was an error in the web service"};
		$.mockjax.clear();
		$.mockjax([
			{url: 'srv/spell', responseText: mockResp},
		]);
		
	    var helpers = new TestHelpers();
	    helpers.typeText(spellControllerConfig.txtToCheck, "ᓄᓇᕗᑦ");
	    helpers.clickOn(spellControllerConfig.btnSpell);
	    
	    new RunWhen().conditionMet(spellControllerNotBusy, function() {
		    assertErrorDisplayed(assert, "There was an error in the web service", caseDescr);
			assertSpellButtonEnabled(assert, caseDescr);			
			done();
	    });
	});
});











