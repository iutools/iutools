
Click on the button below to try the code.
<p/>
<button onclick="winboxDirect()">Winbox directly</button>
<p/>
<button onclick="winboxFloatingWindowController()">Winbox through FloatingWindowController</button>
<p/>
<button onclick="winboxWordEntryController()">Winbox through WordEntryController</button>

<script>
    function winboxDirect() {
        new WinBox("Default Dialog", {
            title: "Testing Winbox",
            html: "",
        });
    }

    function winboxFloatingWindowController() {
        var floatController = new FloatingWindowController();
        floatController.setTitle("FloatingWindowController");
        floatController.show();
    }

    function winboxWordEntryController() {
        var wentryController = new WordEntryController();
        wentryController.displayWordBeingLookedUp("roman", "syllabic");
        wentryController.show();
    }
</script>