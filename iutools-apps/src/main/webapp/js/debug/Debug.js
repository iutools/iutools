/**
 * Various Debugging utilities
 * - printing traces
 * - monitoring presence and content of certain HTML elements
 */
class Debug {

	static trace(name, mess) {
		if (Debug.activeTraces.includes(name) &&
			Debug.browserDevtoolsActive()) {
			this.getLogger(name).trace(mess);
		}
	}

	static browserDevtoolsActive() {
		var active = false;

		var minimalUserResponseInMiliseconds = 100;
		var before = new Date().getTime();
		debugger;
		var after = new Date().getTime();
		if (after - before > minimalUserResponseInMiliseconds) { // user had to resume the script manually via opened dev tools
			active = true;
		}

		return active;
	}

	static traceElements(name, mess, wait = null) {
		if (Debug.activeTraces.includes(name) && Debug.elementsToTrack != null) {
			if (mess == null) {
				mess = "";
			}
			var logger = this.getLogger(name);
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

	static getLogger(name) {
		var logger = log4javascript.getLogger(name);
		logger.setLevel(log4javascript.Level.TRACE);

		if (Debug.popUpAppender == null){
			Debug.popUpAppender = new log4javascript.PopUpAppender();
			var popUpLayout = new log4javascript.PatternLayout("%d{HH:mm:ss} %-5p - %m%n");
			Debug.popUpAppender.setLayout(popUpLayout);
		}
		logger.addAppender(Debug.popUpAppender)

		return logger;
	}
}
