package ca.pirurvik.iutools.corpus;

import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.dtrc.elasticsearch.*;
import ca.nrc.dtrc.elasticsearch.request.*;
import ca.nrc.dtrc.elasticsearch.request._Source;
import ca.nrc.ui.commandline.UserIO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.CREATE_IF_NOT_EXISTS;
import static ca.nrc.dtrc.elasticsearch.StreamlinedClient.ESOptions.UPDATES_WAIT_FOR_REFRESH;

/**
 * CompiledCorpus that uses an ElasticSearch index to store information about
 * the words.
 */
public class CompiledCorpus_ES extends CompiledCorpus {

    public CompiledCorpus_ES(String _indexName) throws CompiledCorpusException {
        super(_indexName);
    }
}

