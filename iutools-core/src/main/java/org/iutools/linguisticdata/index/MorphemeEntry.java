package org.iutools.linguisticdata.index;


import ca.nrc.dtrc.elasticsearch.Document;
import org.iutools.linguisticdata.MorphemeHumanReadableDescr;

/**
 * Decorates a morpheme for the purpose of indexing and retrieving with
 * ElasticSearch.
 */
public class MorphemeEntry extends Document {

	public MorphemeHumanReadableDescr descr = null;

	/**
	 * Canonical form expressed as a sequence of space delimited chars.
	 * This makes it possible to retrieve the MorphemeEntry based on a partial canonical form.
	 */
	public String canonicalFormChars = null;

	/** Empty constructor for Jackson serialization */
	public MorphemeEntry() {
		init_MorphemeEntry((MorphemeHumanReadableDescr)null);
	}

	public MorphemeEntry(MorphemeHumanReadableDescr _descr) {
		init_MorphemeEntry(_descr);
	}

	private void init_MorphemeEntry(MorphemeHumanReadableDescr _descr) {
		descr = _descr;
		this.type = this.getClass().getSimpleName();
		if (_descr != null) {
			this.canonicalFormChars = canonicalForm2SpaceDelimitedChars(_descr.canonicalForm);
		}
	}

	public MorphemeEntry setId(String _id) {
		super.setId(_id);
		return this;
	}

	@Override
	public String getId() {
		return descr.id;
	}

	/** Convert a canonical form into a sequence of space delimited chars */
	public static String canonicalForm2SpaceDelimitedChars(String canonicalForm) {
		String joined = null;
		if (canonicalForm != null) {
			joined = String.join(" ", canonicalForm.split(""));
		}
		return joined;
	}

	@Override
	public String toString() {
		return descr.id;
	}

}
