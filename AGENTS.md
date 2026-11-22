# Agent guide for javacc-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

- One module with goals for four tools, in `org.codehaus.mojo.javacc`: `javacc` (`JavaCCMojo`),
  `jjtree` (`JJTreeMojo`), `jtb` (`JTBMojo`) and `jjdoc` (`JJDocMojo`), plus the combined
  `jjtree-javacc` (`JJTreeJavaCCMojo`) and `jtb-javacc` (`JTBJavaCCMojo`) goals. `JavaCC`, `JJTree`,
  `JTB` and `JJDoc` wrap the tools behind `ToolFacade`; `ForkedJvm` runs a tool in a separate JVM and
  `GrammarDirectoryScanner` finds the grammar files.
- The site is built from `src/site`: `index.md`, `usage.md` and `faq.md` in `src/site/markdown`,
  one page per tool under `src/site/markdown/examples`, and the menu in `src/site/site.xml`.

## Build gates

- Main code must use the Java 8 API: mojo-parent sets `maven.compiler.release` to 8.
- Spotless runs on JDK 17 and later only. It applies the formatting in `process-sources` on a local
  build and checks it when the `CI` environment variable is set. It formats Java with
  palantir-java-format and also reformats Markdown, so run `mvn spotless:apply` after editing the
  README or this file.
- The plugin compiles against the Maven 3.9.x release in `mavenVersion` and declares a literal
  `<prerequisites><maven>3.9.0</maven>`; keep the two apart, so raising `mavenVersion` does not
  raise the prerequisite. Building the plugin itself needs the Maven version in
  `minimalMavenBuildVersion`.
- `.github/workflows/maven.yml` calls the MojoHaus `maven-verify.yml` workflow in `mojohaus/.github`.
  The project has no `run-its` profile, which the workflow's optional `-P "?run-its" verify` goal
  tolerates, so the integration tests run in every build. Branches in this repository build under
  the push event.

## Unit tests

- Unit tests use JUnit 5, in `src/test/java`; `MainStub` stands in for a tool's `Main` class and the grammars
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

## Documentation updates

A change that users or contributors can notice ships with the documentation it affects, in the
same pull request. A refactor with no visible effect needs none.

- A new goal or parameter, or a changed default: the mojo Javadoc, which becomes the goal page, and
  the goal list in `src/site/markdown/index.md`.
- New or changed usage: `src/site/markdown/usage.md` or the page for the tool under
  `src/site/markdown/examples`; recurring questions go in `src/site/markdown/faq.md`. The site pages
  are plain Markdown; the first plugin declaration in `usage.md` writes the version as
  `<version><!-- latest release --></version>` and the examples leave it out, so they never go
  stale.
- A page added, removed or renamed: the menu in `src/site/site.xml`. Never rename a published page;
  its URL is linked from elsewhere.
- The Maven or Java requirement, or how to build and release: `README.md`.
- A new convention, build gate or test fixture: this file.

Check the result with `mvn site` and open `target/site/index.html`.

## Pull requests

- Pull requests are squash- or rebase-merged; merge commits are disabled. Release Drafter builds
  the release notes from the titles and labels, so the title should describe the change for a
  user.

