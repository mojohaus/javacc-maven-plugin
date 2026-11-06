title: Introduction
author:
- <jruiz@exist.com>
- Jesse McConnell <jesse.mcconnell@gmail.com>
- Tim Pizey <timp At paneris.org>
- Paul Gier <pgier at redhat.com>
date: February 09 2008

# JavaCC Maven Plugin

This plugin uses the JavaCC parser generator to process JavaCC grammar files and generate the appropriate Java source files. For general information about JavaCC, see the [JavaCC](https://javacc.github.io/javacc/) web site.

JavaCC grammar files use the extension `.jj`. This plugin can be used to locate and process all grammar files within a specified directory. Java source files will be written to a common generated-sources output directory, typically located in the build output directory. By default, the plugin will run during the `generate-sources` phase, but it can be configured to run in other phases as well.

The plugin can also be be used to call the JJTree and JTB tools. JJTree is a preprocessor for JavaCC that inserts parse tree building actions at various places in the JavaCC source. More information about JJTree can be found on the [JJTree](https://javacc.dev.java.net/doc/JJTree.html) site.

JTB (Java Tree Builder) is another syntax tree builder. Just like JJTree, it works as a preprocessor for JavaCC grammar files and inserts code to build syntax trees from a parsed input stream. More information about JTB in general and a feature comparison with JJTree can be found on the [JTB](http://compilers.cs.ucla.edu/jtb/) web site.

Finally, this plugin contains a goal for running JJDoc. JJDoc is a simple tool that generates [BNF](http://en.wikipedia.org/wiki/Backus%E2%80%93Naur_form) documentation for the JavaCC grammar files. This can be used to generate a report for your project site using the Maven reporting tools.

<a id="Goals"></a>

## Goals Overview

The following goals are meant to participate in the default build lifecycle:

- [`javacc:javacc`](./javacc-mojo.html)  
  Runs JavaCC on grammar files (`*.jj`) to generate a Java parser.
- [`javacc:jjtree-javacc`](./jjtree-javacc-mojo.html)  
  Runs JJTree and JavaCC in a pipeline to process annotated grammar files (`*.jjt`).
- [`javacc:jjtree`](./jjtree-mojo.html)  
  **Deprecated.** Preprocesses annotated grammar files (`*.jjt`) with JJTree.
- [`javacc:jtb-javacc`](./jtb-javacc-mojo.html)  
  Runs JTB and JavaCC in a pipeline to process ordinary grammar files (`*.jtb`).
- [`javacc:jtb`](./jtb-mojo.html)  
  **Deprecated.** Preprocesses ordinary grammar files (`*.jtb`) with JTB.

This last goal is intended for usage with the site lifecycle:

- [`javacc:jjdoc`](./jjdoc-mojo.html)  
  Generates BNF documentation from the grammar files for usage as a site report.

## Usage

General instructions on how to use the JavaCC Plugin can be found on the [usage page](usage.html). Some more specific use cases are described in the examples given below. Last but not least, users occasionally contribute additional examples, tips or errata to the [plugin's wiki page](http://docs.codehaus.org/display/MAVENUSER/JavaCC+Plugin).

In case you still have questions regarding the plugin's usage, please have a look at the [FAQ](faq.html) and feel free to contact the [user mailing list](mail-lists.html). The posts to the mailing list are archived and could already contain the answer to your question as part of an older thread. Hence, it is also worth browsing/searching the [mail archive](mail-lists.html).

If you feel like the plugin is missing a feature or has a defect, you can fill a feature request or bug report in our [issue tracker](issue-tracking.html). When creating a new issue, please provide a comprehensive description of your concern. Especially for fixing bugs it is crucial that the developers can reproduce your problem. For this reason, entire debug logs, POMs or most preferably little demo projects attached to the issue are very much appreciated. Of course, patches are welcome, too. Contributors can check out the project from our [source repository](source-repository.html) and will find supplementary information in the [development guidelines](http://mojo.codehaus.org/development/guidelines.html).

## Examples

To provide you with better understanding of some usages of the JavaCC Plugin, you can take a look into the following examples:

- [Basic JavaCC Invocation](./examples/example-javacc.html)
- [Preprocessing Grammars with JJTree](./examples/example-jjtree.html)
- [Preprocessing Grammars with JTB](./examples/example-jtb.html)
- [Generating BNF Documentation using JJDoc](./examples/example-jjdoc.html)

