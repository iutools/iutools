class HelpController extends IUToolsController {
    constructor(config) {
        super(config);
    }

    show() {
        var html =
            "<div id='"+this.id+"' class='div-info' align='right'>\n" +
            "  <a href='help.jsp?topic="+this.topic+"' target='#iutools_help'></a>\n" +
            "</div>";
        this.elementForProp("#div-help-content").html(html);
    }
}