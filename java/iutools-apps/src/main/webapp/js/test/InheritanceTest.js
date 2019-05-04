QUnit.module("Inheritance tests", {
	beforeEach: function(assert) {
		

	},
	
	afterEach: function(assert) {
		
	}
});


QUnit.test("Basic tests", function( assert ) 
{
	var caseDescr = "Basic Test";
	console.log("==== "+caseDescr);
	
	
	let rabbit = new Rabbit("White Rabbit");

	rabbit.run(5); // White Rabbit runs with speed 5.
	rabbit.hide(); // White Rabbit hides!
	
	assert.ok(true);
});

QUnit.test("Override method by attaching function", function( assert ) 
{
	var caseDescr = "Override method by attaching function";
	console.log("==== "+caseDescr);

	let rabbit = new Rabbit("White Rabbit");
	
	console.log("Before attaching method");
	rabbit.run(5); // White Rabbit runs with speed 5.
	
	rabbit.run = function(speed) {
		console.log("run (attached Method): "+this.name+" runs with speed "+this.speed);
	};

	console.log("AFTER attaching method");
	rabbit.run(5); // White Rabbit runs with speed 5.
		
	assert.ok(true);
});
	
QUnit.test("Attach mock response to a controller", function( assert ) 
{
	var caseDescr = "Attach mock response to a controller";
	console.log("==== "+caseDescr);
	
	var controller = null;
	var resp = {errorMessage: null};
	
	console.log("\n\n ** Attaching mock response through TestHelpers");
	controller = new MyController({txtQuery: "txt-query"});
	new TestHelpers().attachMockAjaxResponse_DUMMY(controller, resp, "invokeWebService", "successCallback", "failureCallback");
	controller.onSearch();

	
	assert.ok(true);
});


class MyController {
	
	constructor(config) {
		this.txtQuery = config.txtQuery;
	}
	
	
	onSearch() {
		console.log("-- onSearch: Invoking invokeWebService, txtQuery="+this.txtQuery);
		this.invokeWebService();
	}
	
	invokeWebService() {
		console.log("-- invokeWebService.ORIGINAL: Invoking successCallback, this.txtQuery="+this.txtQuery);
		var resp = "REAL service response";
		this.successCallback(resp);
		
	}
	
	successCallback(resp) {
		console.log("-- successCallback: resp="+JSON.stringify(resp)+"\n   this.txtQuery="+this.txtQuery);
	}
	
}

