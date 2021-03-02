# The iutools project

_iutools_ is an Open Source project that aims at developing basic language tools for Inuktut, the language of the Inuit people.

It includes user-facing web applications for _learning_ the language as well as _writing_, _translating_ and _searching_ in that language.
_Learning:_
- _[Morpheme Search](http://cyclosa.web.net:8080/iutools/occurrences.html)_: Given an Inuktut morpheme, provides examples of words that use that morpheme. English "gists" are also provided for the morpheme and each of the example.
- _[Gister](http://cyclosa.web.net:8080/iutools/gisttext.html):_ Given some Inuktut text, provides an english "gist" for each word. Each gist consists of: (a) The english meaning of each morpheme in the word and (b) a list of inuktut sentences that use the word, along with its equivalent sentence in english.
__Writing and Translating__
- _[Spell Checker](http://cyclosa.web.net:8080/iutools/spell.html):_ Inuktut spell checker.
__Searching__
- _[Web Search engine](http://cyclosa.web.net:8080/iutools/search.html):_ Special search engine for Inuktut which searches not only for the word you typed, but also closely related words. This is something that mainstream search engines like Google and Bing are not able to do for Inuktut.

_iutools_also provides a _Command Line Interface (CLI)_ for those webapps, as 
well as commands for other tasks currently not supported by web apps. 

Finally, it includes includes several _components_ that can be used by software developers who are building applications that need to process Inuktut text. Examples of components are:
- Morphological decomposer
- Transliterator
- Tokenizer
- and much more 

## Installing _iutools_

For more information about installing _iutools_, see the 
[_README-install.MD_](README-install.MD) file. 

## Using _iutools_

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
    
## The _iutools_ webapps

The _iutools_ webapps can be accessed through the url:

    http://host:8080/iutools/ 
    
where _host_ is the name of your host (may be _localhost_ for a local server).

If you do not wish to install the web apps on your own server, you can use them on the "official" _iutools_ server:

    http://cyclosa.web.net:8080/iutools/

The home page of the webapps server provides links to the various apps with a short description 
for each of them.

# Using the _iutools_ classes
More information to come.





