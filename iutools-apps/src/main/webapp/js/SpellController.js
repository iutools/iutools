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

	copyToClipboard() {
		// Create new element
		var el = document.createElement('textarea');
		// Set value (string to be copied)
		el.value = this.getSpellCheckedText();
		// Set non-editable to avoid focus and move outside of view
		el.setAttribute('readonly', '');
		el.style = {position: 'absolute', left: '-9999px'};
		document.body.appendChild(el);
		// Select text inside element
		el.select();
		// Copy text to clipboard
		document.execCommand('copy');
		// Remove temporary element
		document.body.removeChild(el);
		window.getSelection().removeAllRanges();
	}

	getSpellCheckedText() {
		var wholeTextElements = $('div#div-results').contents();
		var allText = '';
		wholeTextElements.each(function (index, item) {
			var text = "";
			if ($(item).is('.corrections')) {
				text = $(item).find('.selected').text();
				console.log('item.select text= "' + text + '"');
			} else if ($(item).is('span')) {
				text = $(item).text();
				console.log('item text= "' + text + '"');
			}
			allText += text;
			console.log('allText= "' + allText + '"');
		});
		return allText;
	}

	spellCheck() {
		var isValid = this.validateInputs();
		if (isValid) {
			this.clearResults();
			this.setBusy(true);
			this.tokenizeAndSpellCheck();
		}
	}

	tokenizeAndSpellCheck() {

		this.invokeTokenizeService(
			this.getTokenizeRequestData(),
			this.cbkTokenizeSuccess, this.cbkTokenizeFailure
		);

		// this.invokeSpellServiceOnWholeText(
		// 	this.getSpellRequestData(),
		// 	this.cbkSpellSuccess, this.cbkSpellFailure)
	}

	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
	}

	invokeSpellServiceOnWholeText(jsonRequestData, _successCbk, _failureCbk) {
		var controller = this;
		var fctSuccess =
			function (resp) {
				_successCbk.call(controller, resp);
			};
		var fctFailure =
			function (resp) {
				_failureCbk.call(controller, resp);
			};

		$.ajax({
			type: 'POST',
			url: 'srv/spell',
			data: jsonRequestData,
			dataType: 'json',
			async: true,
			success: fctSuccess,
			error: fctFailure
		});
	}

	invokeTokenizeService(jsonRequestData, _successCbk, _failureCbk) {
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
			url: 'srv/tokenize',
			data: jsonRequestData,
			dataType: 'json',
			async: true,
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
	
	onClickOnCorrections(ev) {
			var target = $(ev.target);
			var divParent = target.closest('div');
			console.log('divParent: '+divParent.length+"; word= "+divParent.attr('word'));
			$('span',divParent).css('display','block');
	}
	
	setCorrectionsHandlers() {
		$(document).find('div.corrections').on('mouseleave',function(ev){
			var target = $(ev.target);
			var divParent = target.closest('div');
			$('span',divParent).css('display','none');
			$('.selected',divParent).css('display','block');
			$('.additional input',divParent).val('');
			});
		$(document).find('span.suggestion.selected')
			.on('click',this.onClickOnCorrections);
		$(document).find('span.suggestion:not(.selected)')
			.on('mouseover',function(ev){
				$(ev.target).css({'color':'red'});
				})
			.on('mouseleave',function(ev){
				$(ev.target).css('color','black')
				})
			.on('click',function(ev){
				console.log('click');
				var target = $(ev.target);
				var divParent = target.closest('div');
				$('span',divParent).css('display','none');
				$('span.selected',divParent).removeClass('selected');
				target.addClass('selected');
				$('.additional input',divParent).val('');
				$('.selected',divParent).css('display','block');
				console.log('out of click');
				spellController.setCorrectionsHandlers();
				});
		$(document).find('span.additional input')
			.on('mouseleave',function(ev){
				var target = $(ev.target);
				var divParent = target.closest('div');
				//$('span',divParent).css('display','none');
				$('.selected',divParent).css('display','block');
				$('.additional input',divParent).val('');
				})
			.on('keyup',function(ev){
				if(ev.keyCode == 13) {
					var target = $(ev.target);
					var divParent = target.closest('div');
					var newSuggestionValue = target.val().trim();
					if (newSuggestionValue != '') {
						$('span',divParent).css('display','none');
						$('span.selected',divParent).removeClass('selected');
						var newSuggestionElement = $('<span class="suggestion selected">'+newSuggestionValue+'</span>');
						newSuggestionElement.insertBefore($('.original',divParent));
						spellController.setCorrectionsHandlers();
						$('.additional input',divParent).val('');
						$('.selected',divParent).css('display','block');
					}
					else {
						
					}
				}
			});
	}

	cbkTokenizeSuccess(resp) {
		console.log("-- SpellController.cbkTokenizeSuccess: got resp="+
			JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.cbkTokenizeFailure(resp);
		} else {
			// Retrive the tokens from the tokenize response
			var respTokens = resp['tokens'];
			var tokens = [];
			for (var ii=0; ii < respTokens.length; ii++) {
				var respToken = respTokens[ii];
				tokens.push({text: respToken.first, isWord:respToken.second})
			}

			// Display the original words
			this.displayTokens(tokens);

			// For now, we invoke the call the "old" invokeSpellServiceOnWholeText() 
			// method which spell checks the complete text before displaying 
			// anything
			//
			this.invokeSpellServiceOnWholeText(
				this.getSpellRequestData(),
				this.cbkSpellSuccess, this.cbkSpellFailure)
			
			// Eventually, we will call the spell service on each token
			// individually.
			//

			// for (ii=0; ii < tokens.length; ii++) {
			// 	invokeSpellCheckWordService(tokens[ii]);
			// }



		}
	}

	cbkTokenizeFailure(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp.errorMessage);
		this.setBusy(false);
	}

	cbkSpellSuccess(resp) {
		if (resp.errorMessage != null) {
			this.cbkSpellFailure(resp);
		} else {
			var divChecked = this.elementForProp('divChecked');
			var divCheckedResults = divChecked.find('div#div-results');
			var divCheckedTitle = divChecked.find('div#title-and-copy');
			var btnCopy = this.elementForProp('btnCopy');
			divCheckedResults.empty();
			divCheckedTitle.css('display','block');
			divCheckedResults.css('display','block');
			for (var ii=0; ii < resp.correction.length; ii++) {
				var corrResult = resp.correction[ii];
				var wordOutput = ""
				if (! corrResult.wasMispelled) {
					wordOutput = this.htmlify(corrResult.orig)
				} else {
					wordOutput = this.picklistFor(corrResult);
				}
				divCheckedResults.append(wordOutput);
			}
			spellController.setCorrectionsHandlers();
		}

		btnCopy.show();
		this.setEventHandler("btnCopy", "click", this.copyToClipboard);

		this.setBusy(false);
	}

	cbkSpellFailure(resp) {
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
		return '<span>'+text+'</span>';

	}
	
	picklistFor(corrResult) {
		console.log("corrResult= "+JSON.stringify(corrResult));
		var origWord = corrResult.orig;
		var alternatives = corrResult.allSuggestions;
		var picklistHtml = "<div class='corrections' word='"+corrResult.orig+"'>\n";
		picklistHtml += "<span class=\"suggestion original selected\">"+origWord+"</span>\n";
		var inputLength = origWord.length;
		for (var ii=0; ii < alternatives.length; ii++) {
			var anAlternative = alternatives[ii];
			picklistHtml += "<span class=\"suggestion";
			picklistHtml += "\">"+anAlternative+"</span>\n";
			if (anAlternative.length > inputLength)
				inputLength = anAlternative.length;
		}
		picklistHtml += "<span class=\"additional\">"+"<input type=\"text\" />"+"</span>\n";
		picklistHtml += "</div>\n";
		return picklistHtml;
	}
	
	setBusy(flag) {
		console.log('setBusy: '+flag);
		this.busy = flag;
		if (flag) {
			this.disableSpellButton();	
			this.showSpinningWheel('divMessage', "Checking");
			this.error("");
			this.elementForProp('btnCopy').hide();
		} else {
			this.enableSpellButton();
			this.hideSpinningWheel('divMessage');
			this.elementForProp('btnCopy').show();
		}		
	}
	
	getSpellRequestData() {
		
		var includePartials = 
			this.elementForProp("chkIncludePartials").is(':checked')
		
		var request = {
				text: this.elementForProp("txtToCheck").val(),
				includePartiallyCorrect: includePartials
		};
		
		return JSON.stringify(request);
	}

	getTokenizeRequestData() {

		var request = {
			textOrUrl: this.elementForProp("txtToCheck").val(),
		};

		return JSON.stringify(request);
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

		return divChecked.text();
	}

	displayTokens(tokens) {
		var divChecked = this.elementForProp('divCheckedNew');
		var divCheckedResults = divChecked.find('div#div-results-new');
		// var divCheckedTitle = divChecked.find('div#title-and-copy');
		// var btnCopy = this.elementForProp('btnCopy');
		divCheckedResults.empty();
		// divCheckedTitle.css('display','block');
		divCheckedResults.css('display','block');
		for (var ii=0; ii < tokens.length; ii++) {
			var aToken = tokens[ii];

			var wordOutput = this.span4token(aToken);
			divCheckedResults.append(wordOutput);
		}
		divCheckedResults.show();
		spellController.setCorrectionsHandlers();
	}

	span4token(token) {
		var text = this.htmlify(token.text);
		var span =
			"<span \"class\"=\"token\" \"isWord\"=\"";
		if (token.isWord) {
			span += "true";
		} else {
			span += "false";
		}
		span += "\">"+text+"</span>";

		return span;
	}
}

