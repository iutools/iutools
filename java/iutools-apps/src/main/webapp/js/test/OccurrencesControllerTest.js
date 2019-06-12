var occControllerConfig = {
		btnGet: "btn-occ",
		inpMorpheme: "morpheme",
		inpExampleWord: "example-word",
		divResults:  "div-results",
		divMessage: "div-message",
		divExampleWord: "div-example-word",
		divError: "div-error",
		iconizer: "iconizer",
		divIconizedExampleWord: "div-example-word-iconized",
		divMessageInExample: "message-in-example",
		divWordInExample: "word-in-example",
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
	    
	    assertTableContentsEquals = function(assert, tableID, expectedTableTxt, caseDescr) {
	    	var message = "Checking the contents of table#"+tableID+".";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	var gotTableTxt = $(document).find('table#'+tableID).text();
	    	new TestHelpers().assertStringEquals(assert, message, gotTableTxt, expectedTableTxt, true);
	    }
	    
	    assertWordInExampleEquals = function(assert, expectedText, caseDescr) {
	    	var message = "Checking the top line of the example window.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	var gotText = $("#"+occControllerConfig.divWordInExample).text();
	    	console.log('gotText= '+gotText);
	    	new TestHelpers().assertStringEquals(assert, message, gotText, expectedText, true);
	    }
	    
	    assertElementIsVisible = function(assert,elementID,caseDescr) {
	    	var message = "Checking whether the element '"+elementID+"' is visible.";
	    	if (caseDescr != null) message = caseDescr+"\n"+message;
	    	var element = $("#"+elementID);
	    	var isVisible = element.is(":visible");
	    	assert.ok(isVisible,message);
	    }

	    attachMockMorphemeResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successGetCallback", "failureGetCallback");		
	    }

	    attachMockWordResponse = function(controller, _mockResp) {
	    	new TestHelpers().attachMockAjaxResponse(controller, _mockResp, "invokeSearchService", "successExampleWordCallback", "failureExampleWordCallback");		
	    }


	    // HashMap<String,Pair<String,Pair<String,Long>[]>>
	    mockMorphemeResp = {matchingWords: {"siuq/1nv":{meaning:"to go after; to search", words:["nanusiuqti","tuktusiulauqtut"], wordFrequencies:[132,45]}}};
	    mockWordResp = {exampleWord:{gist:{word:"nanusiuqti",
	    	wordComponents:[{"fst":"nanu","snd":"seal"},{"fst":"siuq","snd":"to hunt"},{"fst":"ti","snd":"one who..."}]},
	    	alignments:["19990101:: blah blah nanusiuqti blah blah@----@ english1","19990202:: blah blah nanusiuqti takujara blah blah@----@ english2"]}};
	    mockWordResp2 = {exampleWord:{gist:{word:" tuktusiulauqtut",
	    	wordComponents:[{"fst":"tuktu","snd":"cariboo"},{"fst":"siu","snd":"to hunt"},{"fst":"lauq","snd":"in the past"},{"fst":"tut","snd":"they (many)"}]},
	    	alignments:["19990101:: blah blah tuktusiulauqtut blah blah@----@ english1","19990202:: blah blah nunavut tuktusiulauqtut blah blah@----@ english2"]}};


		
		/*********************************************
		 * Setup HTML page and controller for testing
		 *********************************************/ 

		// Add HTML elements that are used by this occController
        var formHTML = "Morpheme: <input id=\""+occControllerConfig.inpMorpheme+"\"><br>\n"+
        	"<button id=\""+occControllerConfig.btnGet+"\" type=\"button\" value=\"Occurrences\">Get words</button><br>\n"+
        	"<input type=\"hidden\" id=\""+occControllerConfig.inpExampleWord+"\" value=\"\"><br>\n"+
        	"<div id=\""+occControllerConfig.divMessage+"\" class=\"div-message\"></div>\n"+
        	"<div id=\""+occControllerConfig.divError+"\" class=\"div-error\"></div>\n"+
        	"<div id=\""+occControllerConfig.divResults+"\" class=\"div-results\"></div>\n"+
        	"<div id=\""+occControllerConfig.divExampleWord+"\" class=\"div-example-word\">"+
        		"<div id=\""+occControllerConfig.divMessageInExample+"\"></div><div id=\""+occControllerConfig.divWordInExample+"\"></div><div id=\""+occControllerConfig.iconizer+"\" title=\"Minimize\"><img src=\"imgs/minimize.png\" ></div><div id=\"contents\"></div></div>\n"+

        	"<div id=\""+occControllerConfig.divIconizedExampleWord+"\" title=\"Maximize\"><img src=\"imgs/maximize.png\" height=24 ></div>"
            ;
		$("#testMainDiv").html(formHTML);
		
		
		occController = new OccurrenceController(occControllerConfig);
		occurrenceController = occController;
	    attachMockMorphemeResponse(occController, mockMorphemeResp);	
	    attachMockWordResponse(occController, mockWordResp);	
	    attachMockWordResponse(occController, mockWordResp2);	
	    
	    	    
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
    helpers.typeText(occControllerConfig.inpMorpheme, "siuq");
    helpers.clickOn(occControllerConfig.btnGet);
    
    assertNoErrorDisplayed(assert, caseDescr);
	assertGetButtonEnabled(assert, caseDescr);
	
	var expectedList = ["siuq/1nv"];
	assertMorphemeListEquals(assert, expectedList, caseDescr);
	
	var expectedMorphemeDetails = "siuq/1nv   –   to go after; to searchnanusiuqti(132);    tuktusiulauqtut(45)";
	assertMorphemeDetailsEquals(assert, 0, expectedMorphemeDetails, caseDescr);
	
	helpers.clickOn("word-example-nanusiuqti");
	assertElementIsVisible(assert,"div-example-word",caseDescr+" (word-example-nanusiuqti)");
	assertWordInExampleEquals(assert,"Example word: nanusiuqti", caseDescr);
	assertTableContentsEquals(assert,"tbl-gist","MorphemeMeaningnanusealsiuqto hunttione who...", caseDescr);
	var expectedAlignmentsText = "InuktitutEnglish"+
		"blah blah nanusiuqti blah blahenglish1blah blah nanusiuqti takujara blah blahenglish2";
	assertTableContentsEquals(assert,"tbl-alignments",expectedAlignmentsText, caseDescr);
	
//	helpers.clickOn("word-example-tuktusiulauqtut");
//	assertElementIsVisible(assert,"div-example-word",caseDescr+" (word-example-tuktusiulauqtut)");
//	assertWordInExampleEquals(assert,"Example word: tuktusiulauqtut", caseDescr);
//	assertTableContentsEquals(assert,"tbl-gist","MorphemeMeaningtuktucariboosiuto huntlauqin the pasttutthey (many)", caseDescr);
//	expectedAlignmentsText = "InuktitutEnglish"+
//		"blah blah tuktusiulauqtut blah blahenglish1blah blah nunavut tuktusiulauqtut blah blahenglish2";
//	assertTableContentsEquals(assert,"tbl-alignments",expectedAlignmentsText, caseDescr);
	
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

function checkWordExampleWindowIsDisplayed(word) {
	var divWordInExample = occurrenceController.elementForProp('divWordInExample');
	var expected = "";
	var got = divWordInExample.text();
	assert.equals("",expected,got);
}










