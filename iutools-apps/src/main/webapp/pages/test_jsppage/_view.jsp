
Click on the button below to try the code.
<p/>
<button onclick="tryCode()">Try Code</button>

<script>
    // Reads the value of a cookie by name or returns empty string
    function getCookie(name) {
        var b = document.cookie.match('(^|[^;]+)\\s*' + name + '\\s*=\\s*([^;]+)');
        return b ? b.pop() : '';
    }

    // Waiting for the load event
    window.addEventListener("load", function () {
        // Reading "cookieconsent_status" cookie
        const cookieConsent = getCookie('cookieconsent_status');

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
            // // Reload the page on user choice to make sure cookie is set
            // onStatusChange: function (status, chosenBefore) {
            //     location.reload();
            // }
        })
    });
</script>

<script>
    function tryCode() {
        window.cookieconsent.initialise(
            {
                // container: document.getElementById("content"),
                // palette:{
                //     popup: {background: "#fff"},
                //     button: {background: "#aa0000"},
                // },
                // revokable:true,
                // onStatusChange: function(status) {
                //     console.log(this.hasConsented() ?
                //         'enable cookies' : 'disable cookies');
                // },
                // law: {
                //     regionalLaw: false,
                // },
                // location: true,
            }
        );
    }


</script>

<script>
</script>