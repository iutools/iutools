/* Use this class to invoke the spell web service */

class SpellService {
    /**
     * Invoke the spell check web service on a single word.
     */
    invokeSpellCheckWordService(jsonRequestData, _successCbk, _failureCbk) {
        var controller = this;
        var fctSuccess =
            function (resp) {
                _successCbk.call(controller, resp);
            };
        var fctFailure =
            function (resp) {
                _failureCbk.call(controller, resp);
            };

        if (typeof jsonRequestData !== 'string') {
            jsonRequestData = JSON.stringify(jsonRequestData);
        }

        $.ajax({
            type: 'POST',
            url: 'srv2/spell',
            data: jsonRequestData,
            dataType: 'json',
            async: true,
            success: fctSuccess,
            error: fctFailure
        });
    }

}