class CookieManager {
    getCookie(name) {
        var b = document.cookie.match('(^|[^;]+)\\s*' + name + '\\s*=\\s*([^;]+)');
        return b ? b.pop() : '';
    }

    displayCookieConsent() {
        var cookieManager = this;
        window.addEventListener("load", function () {
            const cookieConsent = cookieManager.getCookie('cookieconsent_status');

            // Initialise cookie consent banner
            window.cookieconsent.initialise({
                "palette": {
                    "popup": {
                        "background": "#efefef",
                        "text": "#404040"
                    },
                    "button": {
                        "background": "#8ec760",
                        "text": "#ffffff"
                    }
                },
                "type": "informational",
                "content": {
                    "dismiss": "Got it",
                },
            })
        });
    }
}