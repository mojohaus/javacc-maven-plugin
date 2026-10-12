# Agent guide for javacc-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with goals for four tools, in `org.codehaus.mojo.javacc`: `javacc` (`JavaCCMojo`),
  `jjtree` (`JJTreeMojo`), `jtb` (`JTBMojo`) and `jjdoc` (`JJDocMojo`), plus the combined
  `jjtree-javacc` (`JJTreeJavaCCMojo`) and `jtb-javacc` (`JTBJavaCCMojo`) goals. `JavaCC`, `JJTree`,
  `JTB` and `JJDoc` wrap the tools behind `ToolFacade`; `ForkedJvm` runs a tool in a separate JVM and
  `GrammarDirectoryScanner` finds the grammar files.
- The site is built from `src/site`.

## Build gates

- Main code must use the Java 8 API: mojo-parent sets `maven.compiler.release` to 8.
- Spotless runs on JDK 17 and later only. It applies the formatting in `process-sources` on a local
  build and checks it when the `CI` environment variable is set. It formats Java with
  palantir-java-format and also reformats Markdown, so run `mvn spotless:apply` after editing the
  README or this file.
- The plugin compiles against Maven 3.9.16 (`mavenVersion`) and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite. Building the plugin itself needs Maven 3.9 (`minimalMavenBuildVersion`).
- `.github/workflows/maven.yml` calls the shared `apache/maven-gh-actions-shared`
  `maven-verify.yml@v5` workflow, with `verify` as its goal because the project has no `run-its`
  profile. Branches in this repository build under the push event.

## Unit tests

- JUnit 5 only, in `src/test/java`; `MainStub` stands in for a tool's `Main` class and the grammars
  are in `src/test/resources`.
- Prefer a unit test. Add an integration test only for what needs a real Maven build: reactor
  builds, source roots added to the project, or stale-file detection.

## Integration tests

- They live in `src/it`, one directory per test, and run in the normal `verify` build: there is no
  `run-its` profile, and `-DskipTests` skips them. The invoker plugin installs the plugin into
  `target/local-repo` and builds with `src/it/settings.xml`.
- Run a subset (the unit tests run first):

  ```shell
  mvn -B verify -Dinvoker.test=javacc-basic,jjdoc-basic-standalone
  ```
- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), so a test must not depend
  on another test's output.
- The forked builds get `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`, so every JVM
  prints a `Picked up JAVA_TOOL_OPTIONS` line on stderr.
- Write `verify.groovy` as `assert` statements. Invoker fails a script whose last expression is
  anything other than `true` or `null`, so a trailing `list.each { }` fails the test; use a `for`
  loop.

## Dependencies

- Deliberate pins in `.github/dependabot.yml`: Maven artifacts stay below 3.10.0, the Maven 3.9.x
  line being the plugin baseline, and plexus-xml below 4.0.0 because 4.x depends on the Maven 4
  XML API.

## Pull requests

- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

