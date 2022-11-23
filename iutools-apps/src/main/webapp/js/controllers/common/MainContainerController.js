/**
 * Controller for the main container, i.e. the container that provides
 * elements that are common to all pages (ex: feedback link)
 */
class MainContainerController extends IUToolsController {

    constructor(config) {
        super(config);
        this.setupFeedbackLink(config.linkFeedback);
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
    }

    setupFeedbackLink(linkFeedback) {
        var requestData = {
            propertyNames: ["org.iutools.apps.feedkback_emails"]
        }
        this.invokeWebService("srv2/config", requestData, this.cbkFeedbackLinkSuccess, this.cbkFeedbackLinkFailure);
    }

    cbkFeedbackLinkSuccess(response) {
		var tracer = Debug.getTraceLogger('MainContainerController.cbkFeedbackLinkSuccess');
		tracer.trace("invoked");
    }

    cbkFeedbackLinkFailure(response) {
        var tracer = Debug.getTraceLogger('MainContainerController.cbkFeedbackLinkFailure');
        tracer.trace("invoked");
    }

}