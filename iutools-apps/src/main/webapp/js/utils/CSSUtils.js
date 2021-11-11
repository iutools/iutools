class CSSUtils {

    constructor() {

    }

    stylesForID(elem) {
        if (typeof elem === 'string') {
            elem = document.getElementById(elem);
        };
        var cssProps = getComputedStyle(elem)
        return cssProps;
    }
}