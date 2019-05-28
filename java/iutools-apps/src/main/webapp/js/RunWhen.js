/*
 * This class allows you wait until a particular condition is met before calling
 * a function. For example, wait until the DOM is ready.
 */

class RunWhen {
	
	conditionMet(condition, toRun, maxMSecs, interval, timeSoFar) {
		if (maxMSecs == null) maxMSecs = 10*1000;
		if (interval == null) interval = 1000;
		if (timeSoFar == null) {
			timeSoFar = 0;
		} else {
			timeSoFar += interval;
		}
		console.log("-- RunWhen.conditionMet: interval="+interval+", timeSoFar="+timeSoFar);
		
		if (timeSoFar > maxMSecs) throw "Condition was not met even after more than "+maxMSecs+" seconds";
		
		if (!condition()) {
			console.log("-- RunWhen.conditionMet: spellController is null. Wait some more");
			window.setTimeout(this.runUponCondition, interval, condition, toRun, interval, timeSoFar);
		} else {
			toRun();
		}
	}
	
	domReady(toRun, maxMSecs, interval, timeSoFar) {
		
		var isReady = function() {
			var state = document.readyStat;
			return (state != null && state == 'complete')
		};
		this.conditionMet(isReady, maxMSecs, interval, timeSoFar);
	}
}