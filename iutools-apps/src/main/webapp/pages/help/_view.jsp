    <div id="div-help-content"></div>

    <script type="text/javascript">
        const params = new URLSearchParams(window.location.search);
        var topic = params.get("topic");
        var helpPage = "pages/help/"+topic+".html";
        $("#page_title").remove();
        $("#div-help-content").html("Loading help...")
            .load(helpPage)
        ;
    </script>

