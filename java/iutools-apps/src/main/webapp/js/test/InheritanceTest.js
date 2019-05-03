QUnit.module("Inheritance tests", {
	beforeEach: function(assert) {
		
//	    window.srchController = new SearchControllerMock(srchControllerConfig, mockResp);
//		
//		
//		// Add HTML elements that are used by this srchController
//        var formHTML =
//                  "Query: <input id=\""+srchControllerConfig.txtQuery+"\"><br/>\n"
//                + "<button id=\"btn-search\" \"onClick\"=\"srchController.onSearch()\">Search</button><br/>\n"
//                + "<p/>\n<br/>\n<p/>\n"
//                + "Error message: <div id=\""+srchControllerConfig.divError+"\"></div><br/>\n"
//                + "Total Hits: <div id=\""+srchControllerConfig.divTotalHits+"\"></div><br/>\n"
//                + "Results:<p/><div id=\""+srchControllerConfig.divResults+"\"></div><br/>\n"
//                ;
//        console.log("-- SearchControllerTest.setup: formHTML=\n"+formHTML);
//		$("#testMainDiv").html(formHTML);
//		
//		$("#"+srchControllerConfig.btnSearch).off('click').on("click", function() {srchController.onSearch();});
	},
	
	afterEach: function(assert) {
		
	}
});


QUnit.test("Basic tests", function( assert ) 
{
	var caseDescr = "Basic Test";
	
	let rabbit = new Rabbit("White Rabbit");

	rabbit.run(5); // White Rabbit runs with speed 5.
	rabbit.hide(); // White Rabbit hides!
	
	assert.ok(true);
});

function runNew(spped) {
	console.log("runNew: `${this.name} runs with speed ${this.speed}.");
}

function runNewBYNAME(spped) {
	console.log("runNewBYNAME: `${this.name} runs with speed ${this.speed}.");
}

QUnit.test("Attach new version of run", function( assert ) 
		{
			var caseDescr = "Basic Test";
		
			let rabbit = new Rabbit("White Rabbit");
			
			console.log("Before attaching method");
			rabbit.run(5); // White Rabbit runs with speed 5.
			
			rabbit.run = runNew;

			console.log("AFTER attaching method");
			rabbit.run(5); // White Rabbit runs with speed 5.
			
			console.log("AFTER attaching method by name");
			rabbit["run"] = runNewBYNAME;
			rabbit.run(5); // White Rabbit runs with speed 5.
			
			assert.ok(true);
		});

class MyController {
	
	
}
