/*
 * Controller for the Word Correction dialog.
 */

class ChooseCorrectionController extends IUToolsController {

    constructor(corrConfig) {
        var tracer = Debug.getTraceLogger('CorrectWordController.constructor');
        tracer.trace("corrConfig=" + JSON.stringify(corrConfig));
        super(corrConfig);
        this.wordBeingCorrected = null;
        this.tokenBeingCorrected = null;
        this.busy = false;

        this.elementForProp("divChooseCorrectionDlg").draggable();
        tracer.trace("upon exit, this=" + JSON.stringify(this));
    }

    showDialog(word) {
        this.diplayClickedWord(word)
        this.setBusy(true);
        this.divDialog().show();
    }

    diplayClickedWord(word) {
        this.divDialog().html("<h2>Suggested corrections for: <em>"+word+"</em></h2>");
    }

    setBusy(flag) {
        this.busy = flag;
        if (flag) {
            this.showSpinningWheel('divMessage', "Looking for suggestions");
            this.error("");
        } else {
            this.hideSpinningWheel('divMessage');
        }

        return;
    }


    display(word, tokenID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.display");
        tracer.trace("word="+word+", tokenID="+tokenID);
        this.wordBeingCorrected = word;
        this.tokenBeingCorrected = tokenID;
        this.showDialog(word);

        var controller = this;
        var cbkSuccess = function(resp) {
            // controller.suggestCorrectionsSuccess.call(controller, resp);
            controller.suggestCorrectionsSuccess(resp);
        };
        var cbkFailure = function(resp) {
            // controller.suggestCorrectionsFailure.call(controller, resp);
            controller.suggestCorrectionsFailure(resp);
        };

        var request = {
            text: word,
            suggestCorrections: true
        };

        new SpellService().invokeSpellCheckWordService(
            request, cbkSuccess, cbkFailure)
    }

    suggestCorrectionsSuccess(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.suggestCorrectionsSuccess");
        tracer.trace("this.wordBeingCorrected="+this.wordBeingCorrected+", resp="+JSON.stringify(resp));
        if (resp.errorMessage != null) {
            this.suggestCorrectionsFailure(resp);
        } else {
            var html = this.htmlChooseCorrection(resp);
            if (html != null) {
                // html == null means that:
                //
                // - the user clicked on a word while the server was still
                //   processing
                // - this response is the one for the FIST word that was clicked
                //
                // Therefore we should ignore that response because the user
                // is clearly not interested in the first word anymore
                //
                this.divDialog().append(html);
                this.setBusy(false);
            }
        }
    }

    suggestCorrectionsFailure(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.suggestCorrectionsFailure");
        tracer.trace("resp="+JSON.stringify(resp));
        if (! resp.hasOwnProperty("errorMessage")) {
            // Error condition comes from tomcat itself, not from our servlet
            resp.errorMessage =
                "Server generated a "+resp.status+" error:\n\n" +
                resp.responseText;
        }
        this.error(resp.errorMessage);
    }

    htmlChooseCorrection(resp) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.htmlChooseCorrection");
        tracer.trace("resp="+JSON.stringify(resp));
        var html = null;
        var correction = resp.correction;
        var suggestions = correction.allSuggestions;
        if (correction != null && suggestions != null) {
            var word = correction.orig;
            // Note: If word !== this.wordBeingCorrected, it means that
            //
            // - the user clicked on a word while the server was still
            //   computing the suggestions for a previously clicked word
            //
            // - the server response we are handlign in this method corresponds
            //   FIST word that was clicked
            //
            // Therefore we should ignore that response because the user
            // is clearly not interested in the first word anymore
            //
            if (word != null && word === this.wordBeingCorrected) {
                html = "";
                if (suggestions.length == 0) {
                    html += "No suggestions for this word";
                } else {
                    for (var ii = 0; ii < suggestions.length; ii++) {
                        html += this.htmlSuggestion(suggestions[ii], ii);
                    }
                }
            }
        }
        return html;
    }

    htmlSuggestion(suggestion, suggIndex) {
        var eltName = "suggestion-"+suggIndex;
        var html =
            '</u><a class="spell-suggestion" id="suggestion-'+suggIndex+'" onclick="chooseCorrectionController.onClickSuggestion(\''+eltName+'\')">'+suggestion+'</a><br/>\n';
        return html;
    }

    onClickSuggestion(suggEltID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.onClickSuggestion");
        var suggestion = $("#"+suggEltID).text();
        var wordBeingCorrected = this.wordBeingCorrected;
        tracer.trace("this="+(typeof this)+": "+JSON.stringify(this));
        tracer.trace("** this.wordBeingCorrected="+this.wordBeingCorrected+", this['wordBeingCorrected']="+this['wordBeingCorrected']);
        tracer.trace("suggEltID="+suggEltID+", suggestion="+suggestion+", this.wordBeingCorrected="+this.wordBeingCorrected);
        $('.corrected-word').each(
            function(index, wordElt) {
                var word = $(this).text();
                tracer.trace("Looking at index="+index+", $(this)="+$(this)+", word='"+word+"', wordBeingCorrected='"+wordBeingCorrected+"'");
                if (word === wordBeingCorrected) {
                // if (word == this.wordBeingCorrected) {
                // if ("blah" === "blah") {
                    tracer.trace("Changing text of the word");
                    $(this).text(suggestion);
                } else {

                }
            }
        );
        this.wordBeingCorrected = suggestion;
    }

    divDialog() {
        return this.elementForProp("divChooseCorrectionDlg");
    }

    show() {
        this.divDialog().show();
    }
}
