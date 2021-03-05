# Valheim Save Tools

This tool converts Valheim save files to and from JSON. The following formats are supported:
* World data files (.db)
* World metadata files (.fwl)
* Character files (.fch)

## Building

Build the project with `gradlew shadowJar`, a jar with all dependencies included will be created.
Use the produced `build/libs/valheim-save-tools.jar`.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code. To fix compile errors
in your IDE, make sure you have a plugin installed for that.

## Running

`java -jar valheim-save-tools.jar <input> <output>`

For reading game files:
Input should specify a .db, .fwl, or .fch file, output is where the json file will be written

For writing game files:
Input should specify a .json file, output should specify a .db, .fwl or .fch file.