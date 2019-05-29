/*
 * This class allows you wait until a particular condition is met before calling
 * a function. For example, wait until the DOM is ready.
 */

class RunWhen {
	
	constructor() {
		this.timerID = null;
	}
	
	sleep(msecs) {
		this.timeElapsed(msecs, function() {});
	}
	
	timeElapsed(milliseconds, fct) {
		const sleep = (msecs) => {
			  return new Promise(resolve => setTimeout(resolve, msecs))
		};
			
//		consoleLogTime("SpellController.Acceptance", "waiting for the controller to be defined");
		return sleep(milliseconds).then(() => {
//			console.log("-- sleep.callback: fct="+fct);
			fct()
		});	
	}
	
	conditionMet(condition, toRun, maxMSecs, interval, timeSoFar) {
		if (maxMSecs == null) maxMSecs = 9999 * 1000;
		if (interval == null) interval = 1000;
		if (timeSoFar == null) {
			timeSoFar = 0;
		} else {
			timeSoFar += interval;
		}
//		console.log("-- RunWhen.conditionMet: interval="+interval+", timeSoFar="+timeSoFar);
//		console.log("-- RunWhen.conditionMet: condition="+condition);

		if (timeSoFar > maxMSecs) {
//			console.log("-- RunWhen.conditionMet: max time of "+maxMSecs+" exceeded. Throwing error.");
			throw "Condition was not met even after more than "+maxMSecs+" seconds";
		}
		
		if (!condition()) {
//			console.log("-- RunWhen.conditionMet: Condition not met. Wait some more. interval="+interval);
			window.setTimeout(this.conditionMet, interval, condition, toRun, maxMSecs, interval, timeSoFar);
		} else {
			toRun();
		}
	}
	
	conditionMet2(condition, toRun, maxMSecs, interval) {
		if (maxMSecs == null) maxMSecs = 9999 * 1000;
		if (interval == null) interval = 1000;
		var timeSoFar = 0;
		
//		console.log("-- RunWhen.conditionMet: interval="+interval);
//		console.log("-- RunWhen.conditionMet: condition="+condition);
//		console.log("-- RunWhen.conditionMet: toRun="+toRun);
		
		var that = this;
		
		var runner = function() {
//			console.log("-- RunWhen.conditionMet.runner: condition function="+condition);			
			timeSoFar += interval;
			if (timeSoFar > maxMSecs) {
				console.log("-- RunWhen.conditionMet.runner: max time of "+maxMSecs+" exceeded. Stopping timer and throwing error.");
				that.stopTimer();
				throw "Condition was not met even after more than "+maxMSecs+" seconds";
			}
			var stop = condition();
//			console.log("-- RunWhen.conditionMet.runner: stop="+stop);
			if (!stop) {
//				console.log("-- RunWhen.conditionMet.runner: Condition not met. Return and wait for the next interval to fire");
				window.setTimeout(this.conditionMet, interval, condition, toRun, maxMSecs, interval, timeSoFar);
			} else {
				console.log("-- RunWhen.conditionMet.runner: Condition IS MET. Stop the timer with timerID="+this.timerID);
				that.stopTimer();
				toRun();
			}
		};
		
		if (this.timerID == null) {
			this.timerID = window.setInterval(runner, interval);
			console.log("-- RunWhen.conditionMet2: Inteval set with timerID="+this.timerID);
		}
	}
	
	stopTimer() {
		clearInterval(this.timerID);
	}
	
	domReady2(toRun, maxMSecs, interval, timeSoFar) {
		
		var isReady = function() {
			var ready = false;
			var state = document.readyState;
			ready = (state != null && state === 'complete');
			console.log("domReady.isReady: state="+state+", ready="+ready);
			return ready
		};
		this.conditionMet2(isReady, toRun, maxMSecs, interval, timeSoFar);
	}

	
	domReady(toRun, maxMSecs, interval, timeSoFar) {
		
		var isReady = function() {
			var state = document.readyStat;
			console.log("domReady.isReady: state="+state);
			return (state != null && state == 'complete')
		};
		this.conditionMet(isReady, toRun, maxMSecs, interval, timeSoFar);
	}
}