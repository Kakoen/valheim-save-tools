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