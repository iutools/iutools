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
//		this.setEventHandler("btnCopy", "click", this.copyToClipboard);
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
		wholeTextElements.each(function(index,item) {
			var text = "";
			if ($(item).is('.corrections')) {
				text = $(item).find('.selected').text();
//				if (text || 0 !== text.length) {
//					// Remove the \n at the end of the selected text
//					text = text.substring(0, text.length - 1);
//				}
			console.log('item.select text= "'+text+'"');
			}
			else if ($(item).is('span')) {
				text = $(item).text();
				console.log('item text= "'+text+'"');
			}
			allText += text;
			console.log('allText= "'+allText+'"');
		});
		return allText;
	}

	spellCheck() {
			var isValid = this.validateInputs();
			if (isValid) {
				this.clearResults();
				this.setBusy(true);
				this.elementForProp('btnCopy').hide();
				this.invokeSpellService(this.getSpellRequestData(), 
						this.successCallback, this.failureCallback)
			}
	}
	
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
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

	successCallback(resp) {
		if (resp.errorMessage != null) {
			this.failureCallback(resp);
		} else {
			var divChecked = this.elementForProp('divChecked');
			var divCheckedResults = divChecked.find('div#div-results');
			var divCheckedTitle = divChecked.find('div#title-and-copy');
			var btnCopy = this.elementForProp('btnCopy');
			divCheckedResults.empty();
			divCheckedTitle.css('display','block');
			divCheckedResults.css('display','block');
//			divChecked.append("<h2>Spell-checked content</h2>")
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
//		text = text.replace(/\n/g, "<br/>\n");
		var html = '<span>'+text+'</span>';
		return html;
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
		} else {
			this.enableSpellButton();
			this.hideSpinningWheel('divMessage');
		}		
	}
	
	
	getSpellRequestData() {
		
		var includePartials = 
			this.elementForProp("chkIncludePartials").is(':checked')
		
		var request = {
				text: this.elementForProp("txtToCheck").val(),
				includePartiallyCorrect: includePartials
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

