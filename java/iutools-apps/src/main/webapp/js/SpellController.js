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
			var controller = this;
			var fctSuccess = 
					function(resp) {
						_successCbk.call(controller, resp);
					};
			var fctFailure = 
					function(resp) {
						_failureCbk.call(controller, resp);
					};
		
			$.ajax({
				type: 'POST',
				url: 'srv/spell',
				data: jsonRequestData,
				dataType: 'json',
				async: false,
		        success: fctSuccess,
		        error: fctFailure
			});
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
			var divChecked = this.elementForProp('divChecked');
			divChecked.empty();
			divChecked.append("<h2>Spell checked content</h2>")
			for (var ii=0; ii < resp.correction.length; ii++) {
				var corrResult = resp.correction[ii];
				var wordOutput = ""
				if (! corrResult.wasMispelled) {
					wordOutput = this.htmlify(corrResult.orig)
				} else {
					wordOutput = this.picklistFor(corrResult);
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
	
	htmlify(text) {
		text = text.replace(/\n/g, "<br/>\n");
		return text;
	}
	
	picklistFor(corrResult) {
		var origWord = corrResult.orig;
		var alternatives = corrResult.possibleSpellings;
		var picklistHtml = "<select>\n  <option value=\""+origWord+"\">"+origWord+"</option>\n";
		for (var ii=0; ii < alternatives.length; ii++) {
			var anAlternative = alternatives[ii];
			picklistHtml += "  <option value=\""+anAlternative+"\">"+anAlternative+"</option>\n"
		}
		picklistHtml += "</select>\n";
		return picklistHtml;
	}
	
	setBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.elementForProp('divChecked').empty();
			this.disableSpellButton();	
			super.showSpinningWheel('divMessage', "Checking spelling");
			this.error("");
		} else {
			this.enableSpellButton();
			super.hideSpinningWheel('divMessage');
		}		
	}
	
	
	getSpellRequestData() {
		
		var request = {
				text: this.elementForProp("txtToCheck").val(),
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
	
	getCheckedText() {
		var divChecked = this.elementForProp('divChecked');
		var text = divChecked.text();
		return text;
	}
}
