package org.iutools.concordancer;

import java.util.*;

public class Alignment_ES extends Alignment {

	public Alignment_ES() {
		super();
		init__Alignment_ES();
	}

	public Alignment_ES(String _fromDoc, long _pairNum) {
		super(_fromDoc, _pairNum);
		init__Alignment_ES();
	}

	private void init__Alignment_ES() {
		this.type = "Alignment";
		return;
	}

	@Override
	public Alignment_ES setWebDomain(String _domain) {
		return (Alignment_ES) super.setWebDomain(_domain);
	}

	@Override
	public Alignment_ES setTopics(List<String> _topics) {
		return (Alignment_ES) super.setTopics(_topics);
	}

	@Override
	public String getIdWithoutType() {
		return from_doc+"-p"+pair_num;
	}
}
