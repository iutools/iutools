Click on the button below to try the code.
<p/>
<button onclick="tryCode()">Try Code</button>

<script>
    function tryCode() {
        new WinBox("Default Dialog", {
            title: "Testing Winbox",
            html: "",
        });
    }

    var config = {
        divChooseCorrectionDlg: "div-choose-correction-dlg",
        divChooseCorrectionTitle: "div-choose-correction-title",
        txtChooseCorrection_FinalCorrection: "txt-finalized-correction",
        btnChooseCorrection_ApplyCorrection: "btn-choose-correction-apply",
        btnChooseCorrection_CancelCorrection: "btn-choose-correction-cancel",

        divChooseCorrectionMessage: "div-choose-correction-message",
        divChooseCorrectionSuggestions: "div-choose-correction-suggestions",
        divChooseCorrectionError: "div-choose-correction-error",
    }
    var errorDlg = new ChooseCorrectionController(config);
    // var errorDlg = new WordEntryController(config);
    errorDlg.hideDialog();

    errorDlg.showDialog("someword");
</script>