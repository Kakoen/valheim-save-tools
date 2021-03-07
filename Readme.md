# Valheim Save Tools

This tool converts Valheim save files to and from JSON. The following formats are supported:
* World data files (.db)
* World metadata files (.fwl)
* Character files (.fch)

Additionally, some support for basic processing has been added.

## Building

Build the project with `gradlew build`, a jar `build/libs/valheim-save-tools.jar` with
all dependencies included will be created.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code. 
To fix compilation errors in your IDE (Eclipe, IntelliJ), make sure you have a plugin 
installed for that.

## Running

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

## Using the library in your Java project

Maven packages are published to Github packages. See https://github.com/Kakoen?tab=packages&repo_name=valheim-save-tools

### Gradle

Include the following in your `build.gradle` file to use `valheim-save-tools-lib` as a dependency.

```groovy
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/kakoen/valheim-save-tools")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("PACKAGES_READ_TOKEN")
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