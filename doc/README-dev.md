# IUTools Developer Manual

This document contains information that can be useful for developers working on 
IUTools.

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
