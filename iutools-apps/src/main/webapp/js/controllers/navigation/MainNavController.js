class MainNavController extends IUToolsController {
    constructor(config) {
        super(config);
        var defaultAlphabet = new SettingsController().iuAlphabet();
        this.selectAlphabet(defaultAlphabet);
    }

    selectAlphabet(alphabet) {
        this.elementForProp("#mnu-alphabet").html(alphabet);
        new SettingsController().iuAlphabet(alphabet);
    }
}