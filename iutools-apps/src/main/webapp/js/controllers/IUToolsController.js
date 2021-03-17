/*
 * Base class for all Controllers in iutools
 */

class IUToolsController extends WidgetController {

    constructor(config) {
        super(config);
    }

    error(err) {
        this.elementForProp('divError').html(err);
        this.elementForProp('divError').show();
    }

    logOnServer(action, data) {
        var logEntry = {
            action: action,
            taskData: data,
            taskID: data.taskID
        }
        this.invokeLogService(data,
            this.expandQuerySuccessCallback, this.expandQueryFailureCallback)
    }

    invokeLogService(data, cbkSuccess, cbkFailure) {
        $.ajax({
            method: 'POST',
            url: 'srv2/loh',
            data: data,
            dataType: 'json',
            async: false,
            success: cbkSuccess,
            error: cbkFailure
        });
        return;
    }
}