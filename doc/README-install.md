# IUtools Installation Manual

This document provides instructions on how to install _iutools_. 

At the moment, the procedure is somewhat involved. In the future, 
we hope to automate most of the steps using something like _Ansible_ or _Docker_.

## Requirements

_iutools_ should work on most flavours of Unix, and in particular:
- Mac OSX
- CentOS

Because most of the components are written in Java, it should work also on 
Windows. However:
- The _iutools/admin_ script may not work and you may have to carry 
  out their operations manually

At the minimum, _iutools_ also requires the following components: 
- Java JDK 1.8
- Elastic Search 5.6

This will be sufficient if you only plan to use the _Command Line Interface_.

If you want to use the web apps, you also need:
- Tomcat

## Build the JAR and WAR files
As of this writing, there are no precompiled Maven artifacts for _iutools_. You 
therefore have to build the WAR and JAR files from sources.

You need to build two Maven projects:
- _java-utils_: A collection of general purpose java utilities developed at the 
National Council of Canada (NRC).
- _iutools_: The _iutools_ project itself

First build _java-utils_:

     git clone https://github.com/nrc-cnrc/java-utils.git
     cd java-utils
     mvn clean install -DskipTests=true

Then build _iutools_:

     git clone https://github.com/iutools/iutools.git
     cd iutools
     mvn clean install -DskipTests=true
     
 At this point, your maven repository (a directory called _.m2_) should contain 
 builds for both of those projects:
 
     /path/to/your/.m2/repository/ca/nrc/java-utils
     /path/to/your/.m2/repository/org/iutools

## Create an _iutools_cli_ alias

If you would like to use the Command Line Interface (CLI) we recommend that you 
you create an alias for it:

    # Note: we split the alias on different lines so it will display nicely 
    #   in this file, but the alias should be on a single line
    #
    alias iutools_cli='java -Xmx6g 
       -Dorg_iutools=/path/to/your/org_iutools.properties 
       -cp /path/to/your/.m2/repository/org/iutools/iutools-core/N.N.N/iutools-core-N.N.N-jar-with-dependencies.jar
       org.iutools.cli.CLI'
       
Where:
- _N.N.N_ is the version number of your _iutools_ installation
- _org_iutools.properties_ is an initially empty 
configuration file that you create (more details will be provided about 
the use of that file later in the present document).

At this point, you should be able to invoke the help info for the 
CLI by typing

    iutools_cli
    
without any argument. Note that some of the commands listed there will not be 
available until you carry out further installation steps.

But at this point, you should be able to use the following commands:
- _align_: Align content of two or more documents that are translations of each other.
- _segment_iu_: Decompose an Inuktut word into its morphemes.
- _transliterate_: Transliterate Legacy inuktitut to Unicode.

## Installing the Compiled Corpora

Most of the _iutols_ components require a _Compiled Corpus_. You can think of 
this as a kind of dictionary that provides information about all the 
Inuktut words contained in a series of documents.

The information stored about each word includes things like:
- Frequency in the corpus
- Top N decompositions of the word into morphemes

To install the corpora, you need to carry out two steps:
- Ensure that ElasticSearch is running on your local machine
- Install the _iutools-data_ project

Below are details about each of those steps.

### Installing ElasticSearch for use by _iutools_

At the moment, _iutools_ only works with version 5.6 of _ElasticSearch_.

Follow the standard installation instructions for _ElasticSearch_.
 
We recommend that you install it as a _service_ that will start automatically 
when the machine starts up. If you choose not to install ElasticSearch as a 
service then you will have to start it manually by typing the following in a 
dedicated terminal windows

    elasticsearch-N.N.N/bin/elasticsearch 
 
 The above command will monopolise the terminal window and you 
 will not be able to issue other commands in it. Leave that window open with the 
 _elasticsearch_ command running.
 - __IMPORTANT:__ Do NOT kill that window and Do NOT start ElasticSearch in 
 background (i.e. do not append & to the above command).
 
To ensure that _ElasticSearch_ was installed and started properly, issue the 
following command:

    curl http://localhost:9200
   
This should output something like this:

    {
      "name" : "abmAGwG",
      "cluster_name" : "elasticsearch",
      "cluster_uuid" : "InLdbP8_T5-Mmc48CJdWWQ",
      "version" : {
        "number" : "5.6.2",
        "build_hash" : "57e20f3",
        "build_date" : "2017-09-23T13:16:45.703Z",
        "build_snapshot" : false,
        "lucene_version" : "6.6.1"
      },
      "tagline" : "You Know, for Search"
    }   
    
### Load the corpus and Translation Memory data into ElasticSearch

First you must download the corpus data from its repository on DAGsHub:

     git clone https://dagshub.com/iutools/iutools-data.git
     
 Then you must point _iutools_ to this directory. Simply add the following line 
 to your _org_iutools.properties_ file: 

     org.iutools.datapath=/path/to/root/of/your/iutools-data
 
 Then issue a _load_corpus_ and _load_translation_memory_ commands to load the 
 default corpus and translation memory from the _iutools-data_ files:
 
     iutools_cli load_corpus -force
     iutool_cli load_translation_memory --force

Note that it may take a few hours for each of those commands to complete, but this 
overhead will only be encurred once. Likewise, if you ever issue a command that 
uses a different corpus or translation memory than the default one, a loading 
overhead will be encurred the first time you use that specific corpus or TM.

While loading the corpus or TM, if you see the following ElasticSearch error:

    TOO_MANY_REQUESTS/12/disk usage exceeded flood-stage watermark, index has read-only-allow-delete block    
     
you can fix the problem by change the ElasticSearch node settings. 
Simply issue the two _curl_ commands below, and 
then reissuing the above _load_corpus_ command.  

    curl -X PUT "localhost:9200/_cluster/settings?pretty" -H 'Content-Type: application/json' -d'
    {
      "persistent": {
        "cluster.routing.allocation.disk.watermark.low": "90%",
        "cluster.routing.allocation.disk.watermark.low.max_headroom": "100GB",
        "cluster.routing.allocation.disk.watermark.high": "95%",
        "cluster.routing.allocation.disk.watermark.high.max_headroom": "20GB",
        "cluster.routing.allocation.disk.watermark.flood_stage": "97%",
        "cluster.routing.allocation.disk.watermark.flood_stage.max_headroom": "5GB",
        "cluster.routing.allocation.disk.watermark.flood_stage.frozen": "97%",
        "cluster.routing.allocation.disk.watermark.flood_stage.frozen.max_headroom": "5GB"
      }
    }
    '
    curl -X PUT "localhost:9200/*/_settings?expand_wildcards=all&pretty" -H 'Content-Type: application/json' -d'
    {
      "index.blocks.read_only_allow_delete": null
    }
    '
     
Once you have successfully completing the _load_corpus_ and _load_translation_memory_ 
commands, you should be able to use the full range of Command Line 
Interface commands. 

### Install and Configure the web apps

If you don't plan to use the _iutools_ web apps, then you are done. 

If you do plan to use the web apps, then there are more steps involved.

- Install and configure Tomcat
- Set file permissions
- Deploy the _iutools_ web apps
- OPTIONAL: Enable the Inuktut Web Search app

### Install and configure Tomcat

Just follow the standard Tomcat installation procedure.

In what follows, we will assume that the location of Tomcat is $CATALINA_HOME.

Next, you must edit the file:

    $CATALINA_HOME/bin/setenv.sh
    
and edit (or create) the value of CATALINA_OPTS to include the _-Dca_nrc_ and 
_-Dorg_iutools_ JRE variables

    CATALINA_OPTS="$CATALINA_OPTS -Dorg_iutools=/path/to/your/org_iutools.properties"
    

### Set file permissions

At this point, you must set the permission of various files and directories so 
that they are accessible (read-only) to the user under which Tomcat runs.

These are:

- The _iutools_data_ directory and all its descendants
- The _org_iutools.properties_ file.  

### Deploy the _iutools_ web apps

To deploy (or redeploy) the _iutools_ web apps, issue the following commands:

    cd iutools/admin
    bash redeploy-webapps.bash
    
Note that this script requires that you define certain environment variables. If 
they are not defined, the script will notify you of the missing ones.    

