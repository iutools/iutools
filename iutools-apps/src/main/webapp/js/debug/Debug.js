/**
 * Various Debugging utilities
 * - printing traces
 * - monitoring prensence and content of certain HTML elements
 */
class Debug {

	static trace(name, mess) {
		if (Debug.activeTraces.includes(name)) {
			this.getLogger(name).trace(mess);
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
