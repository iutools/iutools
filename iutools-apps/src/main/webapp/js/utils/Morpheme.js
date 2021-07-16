class Morpheme {

    constructor(attrs) {
        var tracer = Debug.getTraceLogger("Morpheme.constructor")
        tracer.trace("attrs="+JSON.stringify(attrs));
        this.id = null;
        if ("id" in attrs) {
            this.id = attrs.id
        }

        this.canonicalForm = null;
        if ("canonicalForm" in attrs) {
            this.canonicalForm = attrs.canonicalForm
        }

        this.meaning = null;
        if ("meaning" in attrs) {
            this.meaning = attrs.meaning
        }

        this.grammar = null;
        if ("grammar" in attrs) {
            this.grammar = attrs.grammar
        }
        tracer.trace("Upon exit, this="+JSON.stringify(this))
    }
}