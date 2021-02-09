/**
 * Various Debugging utilities
 * - printing traces
 * - monitoring presence and content of certain HTML elements
 */
class Debug {

    static activeTraces = [];
    
	static trace(name, mess) {
		this.getTraceLogger(name).trace(mess);
	}

	static debugModeIsOn() {
		if (Debug.debugMode == null) {
			Debug.debugMode = false;
			const params = new URLSearchParams(window.location.search)
			if (params.has("debug")) {
				Debug.debugMode = true;
			}
		}
		return Debug.debugMode;
	}

	static traceElements(name, mess, wait = null) {
		if (Debug.activeTraces.includes(name) && Debug.elementsToTrack != null) {
			if (mess == null) {
				mess = "";
			}
			var logger = this.getTraceLogger(name);
			for (var ii=0; ii < Debug.elementsToTrack.length; ii++) {
				var eltSelector = Debug.elementsToTrack[ii];
				var elt = $(eltSelector);
				if (elt == null || elt.length == 0 || elt.val() == null) {
					elt = null;
				}
				var eltText = null;
				if (elt != null) {
					eltText = elt.text();
				}

				// var cbkPrintTrace = function() {
					logger.trace(mess+"Element "+eltSelector+": "+eltText);
				// }
				// if (wait == null) {
				// 	cbkPrintTrace();
				// } else {
				// 	new RunWhen().timeElapsed(wait, cbkPrintTrace);
				// }

			}
		}
	}

	static getTraceLogger(name) {
		Debug.initTraceAppenders();
		var logger = log4javascript.getLogger(name);
		var logLevel = log4javascript.Level.ERROR;
		if (Debug.activeTraces.includes(name)) {
			logLevel = log4javascript.Level.TRACE;
		}
		logger.setLevel(logLevel);
		if (this.debugModeIsOn()) {
			logger.addAppender(Debug.popUpAppender)
		}

		return logger;
	}

	static initTraceAppenders() {
		if (Debug.popUpAppender == null){
			Debug.popUpAppender = new log4javascript.PopUpAppender();
			// Debug.popUpAppender = new log4javascript.InPageAppender();
			var popUpLayout = new log4javascript.PatternLayout("%d{HH:mm:ss} %-5p - %m%n");
			Debug.popUpAppender.setLayout(popUpLayout);
		}
	}
}
