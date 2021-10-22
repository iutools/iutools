<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

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
    		divChooseCorrectionDlg: "div-choose-correction-dlg"
        };
    var spellController = new SpellController(config);
</script>
