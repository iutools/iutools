class CookieManager {
    getCookie(cname) {
        var b = document.cookie.match('(^|[^;]+)\\s*' + cname + '\\s*=\\s*([^;]+)');
        return b ? b.pop() : '';
    }

    setCookie(cname, cvalue) {
        const d = new Date();
        d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
        let expires = "expires="+d.toUTCString();
        document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
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