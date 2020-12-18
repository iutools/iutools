# IUtools Installation Manual

This document provides instructions on how to install _iutools_. 

At the moment, the procedure is somewhat involved. In the future, 
we hope to automate most of the steps using something like _Ansible_ or _Docker_.

## Requirements

## Requirements

At the minimum, _iutools_ require 
- Java JDK 1.8
- Elastic Search 5.6 or higher

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
    alias iutools_cli='iutools_console='java -Xmx6g 
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

Follow the standard installation instructions for _ElasticSearch_ and make sure 
that there is an instance of ElasticSearch running as a local service on port 9200.

To ensure that this was done properly, issue the following command:

   wget -O - http://localhost:9200/_cat/indices?v

and make sure the output looks something like this:

    --2020-12-17 07:37:51--  http://localhost:9200/_cat/indices?v
    Resolving localhost (localhost)... ::1, 127.0.0.1
    Connecting to localhost (localhost)|::1|:9200... connected.
    HTTP request sent, awaiting response... 200 OK
    Length: 83 [text/plain]
    Saving to: ‘indices?v.3’

         0K                                                       100% 19.1M=0s
    etc...

    
### Load the corpus data into ElasticSearch

First you must download the corpus data from its repository on DAGsHub:

     git clone https://dagshub.com/alain_desilets/iutools-data.git
     
 Then you must point _iutools_ to this directory. Simply add the following line 
 to your _org_iutools.properties_ file: 

     org.iutools.datapath=/path/to/root/of/your/iutools-data
 
At this point, you should be able to use the full range of Command Line 
Interface commands. Before you start using the CLI, we recommend that you issue 
a CLI command that will force loading of the default corpus file into 
ElasticSearch. For example:

    iutools_cli check_spelling
    
Note that it may take a few minutes for the corpus to loaded, but this 
overhead will only be encurred once. Likewise, if you ever issue a command that 
uses a different corpus than the default one, a loading overhead will be encurred 
the first time you use that specific corpus.

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

    # Delete the old WAR file and iutools directories on Tomcat
    rm $CATALINA_HOME/webapps/iutools.war
    
    # Copy new N.N.N version of the WAR file to tomcat
    rm -r $CATALINA_HOME/Tomcat/webapps/iutools
      cp /path/to/your/.m2/repository/org/iutools/iutools-apps/0.0.3-SNAPSHOT/iutools-apps-0.0.3-SNAPSHOT.war \
          $CATALINA_HOME/webapps/iutools.war
    
    # Restart Tomcat
    sh $CATALINA_HOME/bin/shutdown.sh
    sleep 2
    sh $CATALINA_HOME/bin/catalina.sh jpda start

### OPTIONAL: Enable the Inuktut Web Search app

One of the _iutools_ web app is a web search engine developed specifically for 
Inuktut. This particular app is not enabled by default because it requires a 
paying subscription to the Microsoft Bing Web Search API:

https://www.microsoft.com/en-us/bing/apis/bing-web-search-api

If you want your installation to include the Inuktut web search app, you must:

- Obtain a Bing API key from the Microsoft Azure web site
- Specify that key by entering it in the _ca_nrc.properties_ file:

     org.iutools.search.bingKey=yourBingAPIKey
  