package org.iutools.concordancer.nunhansearch;

import java.util.HashMap;

/*
# A TermDistribution object is a hash table representing the
# distribution of one term in the Hansard, with the following keys:
#     allVariantsIndices
#     distribution : hashtable(term's_variant => [frequency,indices])
#     web_language

# 'distribution' is a hash table the keys of which are words, variants of the query
# (if the query contained wildcards; otherwise only one variant: the term itself).
# The value of each key is an array of 2 elements:
# the frequency of the word, and a string of ':'-separated positions
# in the file of sentence alignments where the word is to be found.
*/

public class TermDistribution {
	
	public Long[] allVariantsIndices;
	public HashMap<String, Long[]> variantsDistributions;

	public TermDistribution(Long[] _allVariantsIndices, HashMap<String, Long[]> _variantsDistributions) {
		allVariantsIndices = _allVariantsIndices;
		variantsDistributions = _variantsDistributions;
	}

}
