class Tracer {
	
	constructor(name, withTime) {
		this.activeTraces = [
				'SpellController.Acceptance',
				'SpellControllerTest.beforeEach',
				'RunWhen.conditionMet'
			];
		this.name = name;
		this.withTime = withTime
	}
	
	trace(message) {
		if (this.activeTraces.includes(this.name)) {
			if (this.withTime != null && this.withTime) message += " (@"+this.now()+")";
			console.log("-- "+this.name+": "+message);
		}
	}
	
	now() {
		var currentdate = new Date();
		var datetime = currentdate.getHours() + ":" 
				+ currentdate.getMinutes() + ":" + currentdate.getSeconds();
		return datetime
	}
}