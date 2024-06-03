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

When you've got your flavour of `valheim-save-tools.jar`, make sure you have Java 17 installed, and use it as follows:

```
usage: java -jar valheim-save-tools.jar <infile> [outfile] [--addGlobalKey
       <arg>] [--cleanStructures] [--cleanStructuresThreshold <arg>]
       [--failOnUnsupportedVersion] [--listGlobalKeys] [--removeGlobalKey
       <arg>] [--resetWorld] [--skipResolveNames] [-v]
    --addGlobalKey <arg>               Adds a global key (.db only)
    --cleanStructures                  Cleans up player built structures
                                       (.db only)
    --cleanStructuresThreshold <arg>   Minimum amount of structures to
                                       consider as a base (default 25)
    --failOnUnsupportedVersion         Fail when input archive version is
                                       newer than known supported
    --listGlobalKeys                   List global keys (.db only)
    --removeGlobalKey <arg>            Remove a global key, specify 'all'
                                       to remove all (.db only)
    --resetWorld                       Regenerates all zones that don't
                                       have player-built structures in
                                       them (experimental, .db only)
    --skipResolveNames                 Do not resolve names of prefabs and
                                       property keys (faster for
                                       processing, .db only)
 -v,--verbose                          Print debug output
<infile>: Input file of type .fch, .db, .fwl or .json
<outfile>: Output file of type .fch, .db, .fwl or .json (optional)
```

## Example usage

Decode a save file to a json file:
`java -jar valheim-save-tools.jar mysave.db mysave.json`

Encode a json file to a save file:
`java -jar valheim-save-tools.jar mysave.json mysave.db`

## Processors

A handful of built-in processors are included. They are executed in the order they are
documented.

### Global Keys

Use `--listGlobalKeys` to list all the global keys in a .db file. With `--removeGlobalKey <arg>`
global keys can be removed. Specify `--removeGlobalKey all` to clear all global keys.
`--addGlobalKey <arg>` can be used to add a global key.

### CleanStructuresProcessor

Usage: `--cleanStructures`

This will attempt to remove any bases with structures smaller than the threshold.
By default this threshold is `25` buildings, but it can be configured with
`--cleanStructuresThreshold <arg>`.

The processor first flags all chunks where:
* The chunk itself contains at least 25 building pieces
* The chunk and all neighbouring chunks contain at least 25 building pieces.

Only buildable pieces count towards this number. Excluded are:
* Dig / Raise actions
* Farms
* Paths and roads

Then it will find and flag any connecting chunks with player built changes in them.
This includes farms, paths between bases, etc.

Ships will be left alone.

Any chunks that are not flagged will be cleared of player built structures.

It works great as a preprocessor for `ResetWorldProcessor` to first remove smaller
bases (for example temporary beds / campfires outside a crypt).

### ResetWorldProcessor

Usage: `--resetWorld`

This will attempt to reset the generation state of all chunks and locations, except for:
* Zones with player-built structures
* Zones with boss stones

## Flags

### --failOnUnsupportedVersion
The program will exit with code 1 when a version is detected that is higher than 
the last known supported version. When you automated processing of save files, for
example on an auto-updated server, this might come in handy in order to not lose
any information / corrupt your save files.

### --verbose
Some processors listen to this flag to show more information about the process.

### --skipResolveNames
Property keys and prefab names are present in a hashed form in the save file.
By default, an attempt is made to look them up in a reverse lookup table of known
texts. For processing, this can be disabled in order to improve processing performance
and decrease memory usage.

## Building

Build the project with `gradlew build`, a jar `build/libs/valheim-save-tools.jar` with
all dependencies included will be created.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code.
To fix compilation errors in your IDE (Eclipe, IntelliJ), make sure you have a plugin
installed for that.

## Using the library in your Java project

From version 1.1.0, the library is published as a Maven package to Github packages. See https://github.com/Kakoen?tab=packages&repo_name=valheim-save-tools

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
    implementation group: 'net.kakoen.valheim', name: 'valheim-save-tools-lib', version: '1.1.3'
}
```

This requires two environment variables:
* `GITHUB_ACTOR` Your Github username
* `PACKAGES_READ_TOKEN` A Github token with at least `read:packages` access. Generate one at https://github.com/settings/tokens

### Maven

See https://docs.github.com/en/packages/guides/configuring-apache-maven-for-use-with-github-packages adding the Github
repository and setting up authentication for it.

You'll need to add repository url: `https://maven.pkg.github.com/kakoen/valheim-save-tools`

And then add the following dependency:

```xml
<dependency>
  <groupId>net.kakoen.valheim</groupId>
  <artifactId>valheim-save-tools-lib</artifactId>
  <version>1.1.3</version>
</dependency>
```
