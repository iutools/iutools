class Tracer {
	

	constructor(name, withTime) {
		this.name = name;
		if (withTime == null) withTime = true;
		this.withTime = withTime
	}
	
	trace(message) {
		if (typeof activeTraces === 'undefined') activeTraces = [];
		if (activeTraces.includes(this.name)) {
			if (this.withTime != null && this.withTime) message += " (@"+this.now()+")";
			console.log("-- "+this.name+": "+message);
		}
	}
	
	now() {
		var currentdate = new Date();
		var datetime = currentdate.getHours() + ":" 
				+ currentdate.getMinutes() + ":" 
				+ currentdate.getSeconds() + ":"
				+ currentdate.getMilliseconds();
		return datetime
	}
}