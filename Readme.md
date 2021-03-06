# Valheim Save Tools

This tool converts Valheim save files to and from JSON. The following formats are supported:
* World data files (.db)
* World metadata files (.fwl)
* Character files (.fch)

Additionally, some support for basic processing has been added.

## Building

You'll need to have Java SDK 11 or higher. Build the project with `gradlew shadowJar`, 
a jar with all dependencies included will be created.
Use the produced `build/libs/valheim-save-tools.jar`.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code. 
To fix compilation errors in your IDE (Eclipe, IntelliJ), make sure you have a plugin 
installed for that.

## Running

```
usage: java -jar valheim-save-tools.jar <infile> [outfile]
       [--listGlobalKeys] [--removeGlobalKey <arg>] [--skipResolveNames]
    --listGlobalKeys          List global keys (.db only)
    --removeGlobalKey <arg>   Remove a global key (.db only)
    --skipResolveNames        Do not resolve names of prefabs and property
                              keys (.db only)
<infile>: Input file of type .fch, .db, .fwl or .json
<outfile>: Output file of type .fch, .db, .fwl or .json (optional)
```