class Alignment {
    constructor(attrs) {
        this.langText = attrs.langText;
    }

    text4lang(lang) {
        var text = null;
        if (lang in this.langText) {
            text = this.langText[lang];
        }
        return text;
    }
}