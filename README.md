# The iutools project

THIS DOCUMENTATION IS A WORK IN PROGRESS

## Description

_iutools_ is a suite of Java classes, webapps, command line interfaces  and web 
services that provides basic tools for the Inuktut languages (Inuktut is a family of languages that are 
spoken by the Inuit people of areas like Northern Canada, Alaska and Greenland).

In particular, _iutools_ provides the following web apps:
- _Spell Checking_: 
- _Gisting_:  

The _Command Line Interface_ provides some CLI-equivalents for those webapps, as 
well as commands for other tasks currently not supported by web apps. 
 

## Installing _iutools_

For more information about installing _iutools_, see the 
[_README-install.MD_](README-install.MD) file. 

##Using _iutools_

There are three ways to use the tools contained in _iutools_:
- Through a _Command Line Interface (CLI)_
- Using web aplications
- Invoking the Java classes within other applications

Sections below provide information on those three types of use.

##The _iutools_ Command Line Interface (CLI)

To invoke the _iutools CLI_, simply type:

    iutools_cli command options
    
where _iutools_cli_ is an alias for:

    java -Xmx6g \
      iutools-core-0.0.3-SNAPSHOT-jar-with-dependencies.jar \
      Console
      
To get an overview of all the supported commands, simply type:

    iutools_cli
    
To get a more detailed description of a given command, type:

    iutools_cli command
    
##The _iutools_ webapps

The _iutools_ webapps can be accessed through the url:

    http://host:8080/iutools/ 
    
where _host_ is the name of your host (may be _localhost_ for a local server).

This home page provides links to the various webapps with a short description 
for each of them.

#Using the _iutools_ classes




