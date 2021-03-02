# Valheim Save Tools

This tool parses Valheim save files, and outputs them to JSON files. The following formats are supported:
* World data files (.db)
* World metadata files (.fwl)
* Character files (.fch)

## Building

Build the project with `gradlew shadowJar`, a jar with all dependencies included will be created.
Use the produced `build/libs/valheim-save-tools-x.x.x-all.jar`.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code. To fix compile errors
in your IDE, make sure you have a plugin installed for that.

## Running

`java -jar valheim-save-tools-1.0.0-all.jar <input> <output>`

input should specify a .db or .fwl file, output is where the json file will be written