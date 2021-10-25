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

        this.elementForProp("divChooseCorrectionDlg").draggable();
        tracer.trace("upon exit, this=" + JSON.stringify(this));
    }

    display(word, tokenID) {
        var tracer = Debug.getTraceLogger("ChooseCorrectionController.display");
        tracer.trace("word="+word+", tokenID="+tokenID);
        this.wordBeingCorrected = word;
        this.tokenBeingCorrected = tokenID;

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
        tracer.trace("resp="+JSON.stringify(resp));
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
                this.divDialog().html(html);
                this.show();
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
                html =
                    "<h2>Correct word: <em>"+word+"</em></h2>\n\n"+
                    "<h3>Suggested spellings</h3>";
                if (suggestions.length == 0) {
                    html += "No suggestions for this word";
                } else {
                    for (var ii = 0; ii < suggestions.length; ii++) {
                        html += suggestions[ii]+"<br/>\n";
                    }
                }
            }
        }
        return html;
    }

    divDialog() {
        return this.elementForProp("divChooseCorrectionDlg");
    }

    show() {
        this.divDialog().show();
    }
}
