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

	rabbit.run(5); 
	rabbit.hide(); 
	rabbit.jump();
	rabbit.hopAlong();
	
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

