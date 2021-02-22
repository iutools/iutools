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
}