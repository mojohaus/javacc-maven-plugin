# MojoHaus JavaCC Maven Plugin

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/javacc-maven-plugin.svg?label=License)](https://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/javacc-maven-plugin.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.codehaus.mojo/javacc-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/javacc-maven-plugin/actions/workflows/maven.yml/badge.svg?branch=master)](https://github.com/mojohaus/javacc-maven-plugin/actions/workflows/maven.yml?query=branch%3Amaster)

The [javacc-maven-plugin](https://www.mojohaus.org/javacc-maven-plugin/) generates parsers from
[JavaCC](https://javacc.github.io/javacc/) grammar files, optionally preprocessed with JJTree or
JTB, and can document a grammar with JJDoc. It requires Maven 3.9.0 or later and Java 8 or later.

## Quickstart

Put the grammars in `src/main/javacc` and bind the goal:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>javacc-maven-plugin</artifactId>
      <!--<version>INSERT LATEST VERSION HERE</version>-->
      <executions>
        <execution>
          <goals>
            <goal>javacc</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

The [usage page](https://www.mojohaus.org/javacc-maven-plugin/usage.html) covers JJTree, JTB and
JJDoc, and the [plugin site](https://www.mojohaus.org/javacc-maven-plugin/) lists every goal.

Contributor and agent notes are in [AGENTS.md](AGENTS.md).

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

