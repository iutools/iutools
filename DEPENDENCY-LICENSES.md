#Analysis of licenses for all iutools dependencies

When doing a maven build of _iutools_, the _licensescan-maven-plugin_ 
provides the license information for all dependancies (direct or transitive). 
Below is the list of all known licenses, as of 2020-10-26:

- Apache 2.0
- BSD
- Bouncy Castle License
- CDDL or GPLv2 with classpath exception
- Eclipse Public License - Version 1.0
- Java HTML Tidy License
- LGPL, version 2.1
- MIT License
- Mozilla Public License, Version 2.0
- Similar to Apache License but with the acknowledgment clause removed
- The JSON License
- The SAX License
- The W3C License

There are also some components for which the maven plugin was not able to 
automatically find the license information. These are:

- org.apache.commons:commons-math3:3.6
- cglib:cglib-nodep:2.1_3
- net.loomchild:maligna-ui:3.0.1-SNAPSHOT
- org.unix4j:unix4j-command:0.5
- bouncycastle:bcmail-jdk14:136
- org.unix4j:unix4j-base:0.5