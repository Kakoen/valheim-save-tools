# Valheim Save Tools

This repository contains two projects.

* A command line interface tool that converts Valheim save files to and from JSON and processes them.
* A Java library that can be used in your own Java projects.

The following formats are supported:
* World data files (.db)
* World metadata files (.fwl)
* Character files (.fch)

## Running the Command Line Interface tool

There are three ways to get the CLI Tool:
* Download the [Latest release](https://github.com/Kakoen/valheim-save-tools/releases),
* Download a [snapshot build](https://github.com/Kakoen/valheim-save-tools/actions/workflows/build.yml) (click on a commit and find the artifact in the bottom)
* Build the project yourself (see further down)

When you've got your flavour of `valheim-save-tools.jar`, make sure you have Java 11 installed, and use it as follows:

```
usage: java -jar valheim-save-tools.jar <infile> [outfile] [--addGlobalKey
       <arg>] [--listGlobalKeys] [--removeGlobalKey <arg>] [--resetWorld]
       [--skipResolveNames]
    --addGlobalKey <arg>      Adds a global key (.db only)
    --listGlobalKeys          List global keys (.db only)
    --removeGlobalKey <arg>   Remove a global key, specify 'all' to remove
                              all (.db only)
    --resetWorld              Regenerates all zones that don't have
                              player-built structures in them
                              (experimental, .db only)
    --skipResolveNames        Do not resolve names of prefabs and property
                              keys (faster for processing, .db only)
<infile>: Input file of type .fch, .db, .fwl or .json
<outfile>: Output file of type .fch, .db, .fwl or .json (optional)
```

## Building

Build the project with `gradlew build`, a jar `build/libs/valheim-save-tools.jar` with
all dependencies included will be created.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code.
To fix compilation errors in your IDE (Eclipe, IntelliJ), make sure you have a plugin
installed for that.

## Using the library in your Java project

From version 1.0.4, the library is published as a Maven package to Github packages. See https://github.com/Kakoen?tab=packages&repo_name=valheim-save-tools

### Gradle

Include the following in your `build.gradle` file to use `valheim-save-tools-lib` as a dependency.

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/kakoen/valheim-save-tools")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_PACKAGES_READ_TOKEN")
        }
    }
}

dependencies {
    implementation group: 'net.kakoen.valheim', name: 'valheim-save-tools-lib', version: '1.0.4-SNAPSHOT'
}
```

This requires two environment variables:
* `GITHUB_ACTOR` Your Github username
* `PACKAGES_READ_TOKEN` A Github token with at least `read:packages` access. Generate one at https://github.com/settings/tokens

### Maven

See https://docs.github.com/en/packages/guides/configuring-apache-maven-for-use-with-github-packages