/*
 * Word Entry data structure received from the werver.
 */
class WordEntryData {
    constructor(data) {
        var tracer = Debug.getTraceLogger('WordEntryData.constructor');
        tracer.trace("data=" + jsonStringifySafe(data));
        for (const [key, value] of Object.entries(data)) {
            this[key] = value;
        }
        this.compute_allHumanTranslations();
        tracer.trace("Upon exit, attrs=" + jsonStringifySafe(Object.getOwnPropertyNames(this)));
        return;
    }

    otherLang() {
        var other = (this.lang === "iu"? "en": "iu");
        return other;
    }

    compute_allHumanTranslations() {
        this.allHumanTranslations = [];
        var key, val;
        for (const [l1Word, translations] of Object.entries(this.humanTranslations)) {
            for (const aTranslation of translations) {
                if (!this.allHumanTranslations.includes(aTranslation)) {
                    this.allHumanTranslations.push(aTranslation);
                }
            }
        }
        return;
    }

    l1Words() {
        var words = Object.keys(this.translations4l1Word);
        return words;
    }

    translationsAreForRelatedWords() {
        var tracer = Debug.getTraceLogger('WordEntrydata.translationsAreForRelatedWords');
        tracer.trace("attrs=" + jsonStringifySafe(Object.getOwnPropertyNames(this)));

        var isForRelatedWords = true;
        for (var aTranslatedWord in this.translations4l1Word) {
            if (aTranslatedWord === this.word || aTranslatedWord === this.wordInOtherScript) {
                isForRelatedWords = false;
                break;
            }
        }
        return isForRelatedWords;
    }

    getTranslations4l1Word(word) {
        var translations = [];
        var rawTranslations = this.translations4l1Word[word]
        if (rawTranslations != null) {
            for (var ii=0; ii < rawTranslations.length; ii++) {
                var aTranslation = rawTranslations[ii];
                if (aTranslation === "ALL") {
                    // For some reason we end up with a bunch of "ALL" in the
                    // list of translations.
                    continue;
                }
                translations.push(aTranslation);
            }
        }
        return translations;
    }

    isHumanTranslation(term) {
        var normalizedTerm = this.getNormalizedTerm(term, this.otherLang());
        var answer = (this.allHumanTranslations.includes(normalizedTerm));
        return answer;
    }

    getNormalizedTerm(term, lang) {
        var normalized = term;
        // if (this.normalizedTerms[lang].hasOwnPropertythis(term)) {
        if (typeof this.normalizedTerms[lang][term] !== 'undefined') {
            normalized = this.normalizedTerms[lang][term];
        }
        return normalized;
    }

    glossarySources4word(word, lang, source) {
        word = this.getNormalizedTerm(word, lang);
        var sources = [];
        if (this.humanTranslationSources != null &&
            word in this.humanTranslationSources) {
            sources = this.humanTranslationSources[word];
        }
        return sources;

    }
}