# How to install the experimental SQL-based corpus

As of July 2022, we are experimenting with the use of a MySQL db as a data store 
the CompiledCorpus class (instead of ElasticSearch).

This file contains instructions on how to install and configure MySQL for this 
experimental corpus.

## New Requirements

- MySQL

## Installing MySQL

MacOS
    # This does not seem to work...
    brew install mysql
    
- Download the dmg file
     https://dev.mysql.com/downloads/mysql/
- Double click on the dmg file to mount it
- Double click on the pkg file to run the installer
- To start the MySQL server 
    cd /Library/LaunchDaemons
    sudo launchctl load -F com.oracle.oss.mysql.mysqld.plist
- To configure MySQL to automatically start at bootup, you can:
    sudo launchctl load -w com.oracle.oss.mysql.mysqld.plist


    
