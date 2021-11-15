/*
 * This class allows you wait until a particular condition is met before calling
 * a function. For example, wait until the DOM is ready.
 */

class RunWhen {
	
	constructor() {
		this.timerID = null;
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

	
	conditionMet(condition, toRun, maxMSecs, interval) {
		var tracer = Debug.getTraceLogger("RunWhen.conditionMet");
		tracer.trace("Invoked");
		if (maxMSecs == null) maxMSecs = 9999 * 1000;
		if (interval == null) interval = 1000;
		var timeSoFar = 0;

		tracer.trace("interval="+interval);
		tracer.trace("condition="+condition);
		tracer.trace("toRun="+toRun);
		
		var that = this;
		
		var runner = function() {
			var tracer = Debug.getTraceLogger("RunWhen.conditionMet");
			tracer.trace("condition function="+condition);
			timeSoFar += interval;
			if (timeSoFar > maxMSecs) {
				tracer.trace("max time of "+maxMSecs+" exceeded. Stopping timer and throwing error.");
				that.stopTimer();
				throw "Condition was not met even after more than "+maxMSecs+" seconds";
			}
			var stop = condition();
			tracer.trace("stop="+stop);
			if (!stop) {
				tracer.trace("Condition not met. Return and wait for the next interval to fire");
				window.setTimeout(this.conditionMet, interval, condition, toRun, maxMSecs, interval, timeSoFar);
			} else {
				tracer.trace("Condition IS MET. Stop the timer with timerID="+this.timerID);
				that.stopTimer();
				// Wait a bit more just to be safe
				tracer.trace("Sleeping for a bit more");
				that.timeElapsed(100, function() {
					tracer.trace("DONE Sleeping");
					toRun();
				});
			}
		};
		
		if (this.timerID == null) {
			this.timerID = window.setInterval(runner, interval);
			tracer.trace("Inteval set with timerID="+this.timerID);
		}
	}
	
	
	stopTimer() {
		clearInterval(this.timerID);
	}
	
	domReady(toRun, maxMSecs, interval, timeSoFar) {
	    if (maxMSecs == null) {
	        maxMSecs = 10 * 1000;
        }
	    if (interval == null) {
	        interval = 1000;
        }
		var isReady = function() {
			var tracer = Debug.getTraceLogger("RunWhen.domReady");
			var ready = false;
			var state = document.readyState;
			ready = (state != null && state === 'complete');
			tracer.trace("state="+state+", ready="+ready);
			return ready
		};
		this.conditionMet(isReady, toRun, maxMSecs, interval, timeSoFar);
	}
	
}