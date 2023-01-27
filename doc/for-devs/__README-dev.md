# IUTools Developer Manual

This document contains information that can be useful for developers working on 
IUTools.

## Managing linguistic resources (translation memory, dictionary etc...)

IUtools relies on many linguistic resources, namely:
- Parallel corpora in IU-EN (in particular, the hansard)
- Human generated glossaries

It is relatively straightforward to modify/expand these 
resources. See details in the [iutools/doc/for-devs/administering-linguistic-resources.md](./administering-linguistic-resources.md) file.


## Testing

### Junit tests

The code comes with a fairly extensive suite of unit tests. Make sure that all 
tests pass before you commit code to the Git repo.

That said, as of June 2022, there is a handful of broken tests:
- Various tests whose name starts with test__TODO*. These are just reminders of 
  something that needs to be done eventually.
- Aligner_MalignaTest (fails intermittently for some unknown reason)
- MorphologicalAnalyzer_R2LTest.test__decomposeWord__apiqsuqtaujuksaq

The suite takes about 20 mins to execute. 

But of course, when you work on a particular class, it's possible to run only 
tests that relate for that class. Each class X comes with a corresponding test case 
called XTest.

### Manual Interative tests

While the web app does come with some automated JUnit tests, those focus solely 
on the server end of things (Java). None of the client-side UI code (JavaScript) 
is covered by JUnit tests.

At the moment, the only way to test the client-side code is to manually test it. 
The file [iutools/doc/for-devs/test-plan.md](./test-plan.md) provides a fairly
extensive manual test plan. Carryng out the test 
plan takes about 30 mins.

### Stress tests

There is also a JMeter test suite that tests the performance of the web apps 
under simulated traffic of 20 simultaneous connections. The suite is defined in 
file:

    iutools/iutools-apps/src/test/jmeter/Test.jmx

Before you can run that suite, you must first install JMeter on your machine.

You can run the suite from either the JMeter UI or command line

#### Running from the JMeter UI

- Start the JMeter UI by typing 'jmeter' from a terminal. 
- File > Open, then navigate to file:
     iutools/iutools-apps/src/test/jmeter/Test.jmx
- Click the Play icon (green triangle) or Run > Start
- To view results of the run
  - Click on the View Result Tree listener
  - Note that the results of the various runs get appended to the same file
  - Yo- u can clear the results of the various runs by doing Run > Clear all
- If some of the requests failed because the server returned an error code, you 
  can get more info by inspecting the tomcat log (whose path is defined in the 
  log4j.properties file).
   
#### Running from the command line

From a terminal, invoke the following command

    cd iutools/iutools-apps
    jmeter -n --testfile src/test/jmeter/Test.jmx \
      -l src/test/jmeter/testresults.jtl --forceDeleteResultFile
      
export REPORTS_DIR=/path/to/reports
rm -rf $REPORTS_DIR; \
jmeter -g /path/to/testresults.jtl -o $REPORTS_DIR

## Debugging tips

### Debuggin the Java code

#### Tracing the Java code

The Java code contains lots of log4j traces that can be activated. To activate 
traces, you must create a log4j2.xml file. You can use the following file 
as a model:

    iutools/doc/log4j2.xml.sample
    
Once you have a created a log4j.properties file, you must pass it as a VM option 
whenever you run iutools code (whether it be through a Main, a Tomcat app or 
a unit test). You do this with the following -D option:

    -Dlog4j.configurationFile=file:/path/to/your/log4j2.xml

You can add your own traces to the code, by writing something like this:

    Logger tLogger = Logger.getLogger("org.iutools.worddict.MachineGeneratedDict.someMethod");
    tLogger.trace("Hello world");
    
    # If the trace message takes a long time to generate, you can check if the 
    # logger is trace enabled before generating it 
    if (tLogger.isTraceEnabled()) {
        tLogger.trace(someLongMethodCall());
    }
    
Note that in general, the iutools traces are named after the class and method 
that print them. This makes it easy to exert control over traces on a method by 
method level.    

### Debugging the JavaScript code

The javascript code in IUTools includes many useful traces that can be activated. 

By default, all traces are deactivated. To activate specific traces, you must create 
a file called 

    iutools-apps/src/main/webapp/js/debug/DebugConfig.js
    
In that file, you can provide the list of traces that you want to activate. For
example:

    Debug.activeTraces = [
        'WordEntryController.displayWordEntry',
        'WordEntryController.translationsInfo',
        'WordEntryController.htmlTranslations',
        'WordEntryController.htmlAlignmentsByTranslation',
    ];

This will activate all the traces whose names is mentioned in the _activeTraces_ 
list. Note however that the traces will only be displayed if the URL of the page 
has a _debug=1_ CGI argument. For example:

    http://localhost:8080/iutools/worddict.jsp?debug=1

By convention, all traces bear a nam in the format:

    ClassName.methodName
    
But this is just a convention and is not obligatory. You can add your own traces

You can create your own  traces in the code by writing something like this:

    var tracer = Debug.getTraceLogger("WordDictController.onSearch")
    tracer.trace("Hello world");
    tracer.trace("Greetings Universe");

## Changing the Skin of the web apps

If you want to change the look of the web apps in order to rebrand it, you can 
change the web app "skin" creating the following two files (both under 
iutools/iutools-apps/src/main/webapp_):

- Custom skin file: _pages/common/_customSkin.jsp_ 
  - Use _defaultSkin.jsp_ as a model
- Custom styles file: _css/_custom-styles.jsp_  
  - Just override styles defined in _design-styles.css_

## Testing improvements to the Portage IU-EN word aligner

The IUTools TranslationMemory and MachineGeneratedDictionary make heavy use of the word-level alignment produced 
by Portage and stored in the the various .tm.json files.

Below is a producedure which you can use to generate a test set that can be used to evaluate the peformance of a new 
version of the Portage aligner, in the context of IUTools.

### Overview of the workflow

You can evaluate Portage word alignments in two ways:

When trying to improve the Portage word alignments for IUTools, the workflow is as follows.
- _Step 1: Try various improvements, and evaluate them in a _Standalone_ fashion, i.e. in a way that does not worry about 
  how the alignments will be used in the context of IUTools
- Step 2: When the results of the _Standalone_ evaluation indicates that there has been significant improvements, 
  carry out a _Functional_ evaluation which takes into account how the alignemtns will be used in the context of IUTools.
- Go back to Step 1.

Sections below provide more details on how to carry out _Standalone_ versus _Functional_ evaluations

### Standalone Evaluation

The directory

    iutools-data/data/translation-memories/testdata

contains two files that can be used to carry out _Standalone Evaluation_ of the word alignemnts.

- _tmCases.json:_ A list of IU words along with known EN translations. These cases were extracted from the glossaries 
  (i.e. _gloss.json_ files). We only kept IU terms which:
  - Consist of a single word
  - Exist in at least one of the dialects supported by IUTools
  - Have at least one known EN equivalent.

- _sentencePairs.json:_ A list of IU-EN sentence pairs extracted from the NRC-Nunavut-Hansard. These are all the pairs 
  that contain at least one of the IU words listed in _tmCases.json_. 

If you add or modify a glossary, you can regenerate the above data files using the CLI command _generate_tm_eval_data_.

### Carrying out _Functional Evaluation_

To carry out _Functional Evalaution_:
- Create a word-alignment file for all sentences listed in the _sentencePairs.json:_ (in the _Standalone_ data). The 
  format of this file is currently not documented, but you can see some examples in directory

     _iutools-data/data/translation-memories/testdata_
  - They are the files that start with _class=org.iutools.concordancer.Alignment_ES_

Run the following JUnit TestCases:
- TMEvaluationTest
- DictEvaluationTest

Before you run them however, you must modify them to point them to the new version of the test alignments files.

Those tests will fail if the new alignments resulted in a significant change in performance (whether it be a failure or 
an improvement). If some of the tests "fail" because there has been an improvement, then you should change the test's 
expectations so it reflects this new and improved performance.

Note that as of Jan 2023, the above tests only evaluate terms that appear in the Wikipedia glossary. Eventually, we should 
modify them so the evaluate a sample of words taken from all glossaries.