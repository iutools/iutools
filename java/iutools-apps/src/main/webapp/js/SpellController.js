/*
 * Controller for the spell.html page.
 */

class SpellController extends WidgetController {

	constructor(config) {
		super(config);
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnSpell", "click", this.spellCheck);
		this.onReturnKey("txtToCheck", this.spellCheck);
	}

	spellCheck() {
			var isValid = this.validateInputs();
			if (isValid) {
				this.setBusy(true);
				this.invokeSpellService(this.getSpellRequestData(), 
						this.successCallback, this.failureCallback)
			}
	}
	
	invokeSpellService(jsonRequestData, _successCbk, _failureCbk) {
//			var controller = this;
//			var fctSuccess = 
//					function(resp) {
//						_successCbk.call(controller, resp);
//					};
//			var fctFailure = 
//					function(resp) {
//						_failureCbk.call(controller, resp);
//					};
//		
//			$.ajax({
//				type: 'POST',
//				url: 'srv/spell',
//				data: jsonRequestData,
//				dataType: 'json',
//				async: true,
//		        success: fctSuccess,
//		        error: fctFailure
//			});
		
		var request = {
				errorMessage: null,
				checkedText: [["Hello"], [" "], ["warld", "world"]]
		};
		this.successCallback(request);
		
	}

	validateInputs() {
		var isValid = true;
		var toSpell = this.elementForProp("txtToCheck").val();
		if (toSpell == null || toSpell === "") {
			isValid = false;
			this.error("You need to enter some text to spell check");
		}
		return isValid;
	}

	successCallback(resp) {
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
			console.log("-- SpellController.successCallback: INVOKED with resp="+JSON.stringify(resp));
			var divChecked = this.elementForProp('divChecked');
			divChecked.empty();
			for (var ii=0; ii < resp.checkedText.length; ii++) {
				var wordAlternatives = resp.checkedText[ii];
				var origWord = wordAlternatives.shift()
				var wordOutput = origWord+"\n"
				
				if (wordAlternatives.length != 0) {
					wordOutput = this.picklistFor(origWord, wordAlternatives)
				}
				divChecked.append(wordOutput);
			}
		}
		this.setBusy(false);
	}

	failureCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.setBusy(false);
	}
	
	picklistFor(origWord, alternatives) {
		var picklistHtml = "<select>\n  <option value=\""+origWord+"\">"+origWord+"</option>\n";
		for (var ii=0; ii < alternatives.length; ii++) {
			var anAlternative = alternatives[ii];
			picklistHtml += "  <option value=\""+anAlternative+"\">"+anAlternative+"</option>\n"
		}
		picklistHtml += "</select>\n";
		
		var picklistElt = $.parseHTML(picklistHtml);
		
		return picklistElt;
	}
	
	setBusy(flag) {
		if (flag) {
			this.disableSpellButton();		
			this.error("");
		} else {
			this.enableSpellButton();		
		}
	}
	
	
	getSpellRequestData() {
		
		var request = {
				toCheck: this.elementForProp("txtToCheck").val(),
		};
		
		var jsonInputs = JSON.stringify(request);
		
		return jsonInputs;
	}
	
	disableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", true);
	}
	
	enableSpellButton() {
		this.elementForProp('btnSpell').attr("disabled", false);

	}
	
	error(err) {
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
	}
	
	
}
