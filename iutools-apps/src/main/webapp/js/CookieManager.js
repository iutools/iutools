class CookieManager {
    getCookie(cname) {
        var b = document.cookie.match('(^|[^;]+)\\s*' + cname + '\\s*=\\s*([^;]+)');
        return b ? b.pop() : '';
    }

    setCookie(cname, cvalue) {
        document.cookie =
            cname + "=" + cvalue +
            ";path=/";
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
                    "href": "help.jsp?topic=about_cookies"
                },
            })
        });
    }
}