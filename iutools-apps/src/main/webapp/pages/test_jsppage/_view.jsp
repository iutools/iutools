
Click on the button below to try the code.
<p/>
<button onclick="issueMobileWarning()">Issue Mobile Warning</button>
<p/>

<script>
    function issueMobileWarning() {
        new MobileWarningController().possiblyWarnAgainstMobile();
    }
</script>