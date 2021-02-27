# Valheim Save Tools

This tool is a basic prototype for parsing save files (.db) and metadata files (.fwl) from Valheim, and outputs them to JSON files.

## Building

Build the project with `gradlew shadowJar`. Then use the produced `build/libs/valheim-save-tools-x.x.x-all.jar`.

This project uses Lombok (https://projectlombok.org/) to prevent boilerplate code. To fix compile errors
in your IDE, make sure you have a plugin installed for that.

## Running

`java -jar valheim-save-tools-1.0.0-all.jar <input> <output>`

input should specify a .db or .fwl file, output is where the json file will be written