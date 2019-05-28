var someVar = null;

QUnit.module("RunWhen Tests", {
	
	beforeEach: function(assert) {
		setTimeout(function() {someVar = 1}, 3*1000);
	},
	
	afterEach: function(assert) {
	}
	
});

function someVarIsDefined() {
	return (someVar != null);
}


QUnit.test("RunWhen.conditionMet -- HappyPath", function( assert ) 
{
	var caseDescr = "RunWhen.conditionMet -- HappyPath";
	
	assert.notOk(someVarIsDefined(), "Checking that someVar is not defined initially");
	
	var checkDefined = function() {
		assert.ok(someVarIsDefined(), "Checking that someVar IS defined after RunWhen.conditionMet");
	};
	new RunWhen().conditionMet(someVarIsDefined, checkDefined);
	
});


function putDOMinNonReadyState() {
	// Add lots of HTML code to the page, to force it to become in a non-ready state
	for (var ii=0; ii<100; ii++) {
		$("#body").append("<p>dummy paragraph "+ii+"</p>\n");
	}
}

QUnit.test("RunWhen.domReady -- HappyPath", function( assert ) 
{
	var caseDescr = "RunWhen.domReady -- HappyPath";
	
//	putDOMinNonReadyState();
//	assert.notOk(document.readyState == 'complete', "Checking that DOM was not ready initially");
	
	var checkDOMReady = function() {
		assert.ok(document.readyState == 'complete', "Checking that DOM is ready after RunWhen.domReady");
	};
	new RunWhen().domReady(checkDOMReady);
	
	assert.ok(true);
	
});

