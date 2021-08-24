<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script>
	// Setup and configure the controller for this page
    var config = {
        divMessage: "div-message",
        divError: "div-error",


        // Floating div where we display the "gist" of a word
        // (after the user clicks on the example word)
        //
        divWordEntry: "div-wordentry",
        divWordEntry_iconizer: "div-wordentry-iconizer",
        divWordEntry_iconized: "div-wordentry-iconized",
        divWordEntry_message: "div-wordentry-message",
        divWordEntry_word: "div-wordentry-word",
    };

    var wordEntryController = new WordEntryController(config);
</script>


