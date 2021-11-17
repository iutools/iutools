class HelpController extends IUToolsController {
    constructor(config) {
        super(config);
    }

    show() {
        this.elementForProp("#div-help-content").html("HELLO");
    }
}