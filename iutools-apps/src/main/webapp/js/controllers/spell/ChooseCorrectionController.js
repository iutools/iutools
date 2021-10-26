/*
 * Controller for the Word Correction dialog.
 */

class ChooseCorrectionController extends IUToolsController {

    constructor(corrConfig) {
        var tracer = Debug.getTraceLogger('ChooseCorrectionController.constructor');
        tracer.trace("corrConfig=" + JSON.stringify(corrConfig));
        super(corrConfig);
        this.idOfWordBeingCorrected = null;
        this.busy = false;
        this.hideDialog();

        this.elementForProp("divChooseCorrectionDlg").draggable();
        tracer.trace("upon exit, this=" + JSON.stringify(this));
    }

    attachHtmlElements() {
        this.setEventHandler("btnChooseCorrection_ApplyCorrection", "click", this.applyCorrection);
        this.setEventHandler("btnChooseCorrection_CancelCorrection", "click", this.cancelCorrection);
    }

    showDialog(word) {
        this.clearDialog();
        this.diplayClickedWord(word)
        this.setBusy(true);
        this.divDialog().show();
    }

    hideDialog() {
        this.divDialog().hide();
    }

    clearDialog() {
        this.divTitle().html("");
        this.txtCorrection().val("");
        this.divSuggestions().html("");
    }

    diplayClickedWord(word) {
        this.divTitle().html("<h2>Choose correction forr: <em>"+word+"</em></h2>");
        this.hideEditCorrectionForm();
    }

    hideEditCorrectionForm() {
        this.txtCorrection().hide();
        this.btnApplyCorrection().hide();
        this.btnCancelCorrection().hide();
    }

    showEditCorrectionForm() {
        this.txtCorrection().show();
        this.btnApplyCorrection().show();
        this.btnCancelCorrection().show();
    }

    setBusy(flag) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.setBusy");
        tracer.trace("this.config.divMessage="+this.config.divMessage);
        this.busy = flag;
        if (flag) {
            this.showSpinningWheel('divChooseCorrectionMessage', "Looking for suggestions");
            this.error("");
        } else {
            this.hideSpinningWheel('divChooseCorrectionMessage');
        }

        return;
    }


    display(checkedWordID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.display");
        tracer.trace("checkedWordID="+checkedWordID);
        this.idOfWordBeingCorrected = checkedWordID
        var word = this.wordBeingCorrected();
        tracer.trace("word="+word);

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
        tracer.trace("this.idOfWordBeingCorrected="+this.idOfWordBeingCorrected+", resp="+JSON.stringify(resp));
        if (resp.errorMessage != null) {
            this.suggestCorrectionsFailure(resp);
        } else {
            this.showEditCorrectionForm();
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
                this.divSuggestions().html(html);
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
            // Note: If word !== this.wordBeingCorrected(), it means that
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
            var wordBeingCorrected = this.wordBeingCorrected();
            if (word != null && word === wordBeingCorrected) {
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
        tracer.trace("this="+(typeof this)+": "+JSON.stringify(this));
        tracer.trace("suggEltID="+suggEltID+", suggestion="+suggestion+", this.idOfWordBeingCorrected="+this.idOfWordBeingCorrected);
        this.txtCorrection().val(suggestion);
    }

    applyCorrection() {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.applyCorrection");
        tracer.trace("this="+JSON.stringify(this)+", $(this)="+JSON.stringify($(this)));
        var correction = this.txtCorrection().val();
        var wordBeingCorrected = this.wordBeingCorrected();
        tracer.trace("correction="+correction+", wordBeingCorrected="+wordBeingCorrected);
        $('.corrected-word').each(
            function(index, wordElt) {
                var word = $(this).text();
                tracer.trace("Looking at index="+index+", $(this)="+$(this)+", word='"+word+"', wordBeingCorrected='"+wordBeingCorrected+"'");
                if (word === wordBeingCorrected) {
                    tracer.trace("Changing text of the word");
                    $(this).text(correction);
                } else {

                }
            }
        );
        this.idOfWordBeingCorrected = null;
        this.clearDialog();
        this.hideDialog();
    }

    cancelCorrection() {
        this.idOfWordBeingCorrected = null;
        this.clearDialog();
        this.hideDialog();
    }

    divDialog() {
        return this.elementForProp("divChooseCorrectionDlg");
    }

    divTitle() {
        return this.elementForProp("divChooseCorrectionTitle");
    }

    divSuggestions() {
        return this.elementForProp("divChooseCorrectionSuggestions");
    }

    txtCorrection() {
        return this.elementForProp("txtChooseCorrection_FinalCorrection");
    }

    btnApplyCorrection() {
        return this.elementForProp("btnChooseCorrection_ApplyCorrection");
    }

    btnCancelCorrection() {
        return this.elementForProp("btnChooseCorrection_CancelCorrection");
    }

    show() {
        this.divDialog().show();
    }

    wordBeingCorrected() {
        var word = null;
        var parts = this.parseIDOfWordBeingCorrected();
        if (parts != null) {
            word = parts[0];
        }
        var parts = this.idOfWordBeingCorrected.split("_");
        return word;
    }

    parseIDOfWordBeingCorrected() {
        var parts = null;
        if (this.idOfWordBeingCorrected != null) {
            parts = this.idOfWordBeingCorrected.split("_");
            if (parts.length > 2) {
                var word = parts.slice(0, -1).join("");
                var tokenID = parts.slice(-1);
                parts = [word, tokenID];
            }
        }
        return parts;
    }
}
