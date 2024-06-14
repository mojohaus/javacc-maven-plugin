# MojoHaus Javacc Maven Plugin

This is the [javacc-maven-plugin](http://www.mojohaus.org/javacc-maven-plugin/).

[![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/mojohaus/versions-maven-plugin.svg?label=License)](http://www.apache.org/licenses/)
[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/javacc-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/javacc-maven-plugin)
[![GitHub CI](https://github.com/mojohaus/javacc-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/javacc-maven-plugin/actions/workflows/maven.yml)

## Quickstart

```
<build>
  <plugins>
    <plugin>
      <groupId>org.codehaus.mojo</groupId>
      <artifactId>javacc-maven-plugin</artifactId>
      <!--<version>INSERT LATEST VERSION HERE</version>-->
      <executions>
        <execution>
          <goals>
            <goal>....</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <!-- See usage on maven site from link above for details -->
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Releasing

* Make sure `gpg-agent` is running.
* Execute `mvn -B release:prepare release:perform`

For publishing the site do the following:

```
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```

