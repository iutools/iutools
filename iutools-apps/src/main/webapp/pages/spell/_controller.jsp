<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script src="js/controllers/spell/SpellService.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/spell/ChooseCorrectionController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/spell/SpellController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
	// Setup and configure the controller for this page
    var config = {
    		btnSpell: "btn-spell",
            btnCancelSpell: "btn-cancel-spell",
    		txtToCheck: "txt-to-check",
    		chkIncludePartials: "chk-include-partials",
    		divChecked:  "div-checked",
    		divError: "div-error",
    		divMessage: "div-message",
    		btnCopy: "btn-copy",
    		divResults: "div-results",

    		divChooseCorrectionDlg: "div-choose-correction-dlg",
    		divChooseCorrectionTitle: "div-choose-correction-title",
            txtChooseCorrection_FinalCorrection: "txt-finalized-correction",
            btnChooseCorrection_ApplyCorrection: "btn-choose-correction-apply",
            btnChooseCorrection_CancelCorrection: "btn-choose-correction-cancel",

    		divChooseCorrectionMessage: "div-choose-correction-message",
            divChooseCorrectionSuggestions: "div-choose-correction-suggestions",
            divChooseCorrectionError: "div-choose-correction-error",
        };

    var spellController = new SpellController(config);
    var chooseCorrectionController = spellController.correctWordController;
</script>
