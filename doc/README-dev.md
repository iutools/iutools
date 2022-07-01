# IUTools Developer Manual

This document contains information that can be useful for developers working on 
IUTools.

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
The file iutools/doc/test-plan.md provides a fairly. Carryng out the test 
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
traces, you must create a log4j.properties file. You can use the following file 
as a model:

    iutools/doc/log4j.properties.sample
    
Once you have a created a log4j.properties file, you must pass it as a VM option 
whenever you run iutools code (whether it be through a Main, a Tomcat app or 
a unit test). You do this with the following -D option:

    -Dlog4j.configuration=file:/path/to/your/log4j.properties

You can add your own traces to the code, by writing something like this:

    Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDict.someMethod");
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
