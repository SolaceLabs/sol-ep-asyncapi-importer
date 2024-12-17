# Solace AsyncAPI Importer for Event Portal
This project provides tooling to import AsyncAPI specs into Event Portal. 

# Prerequisites
- Java 11+
- Maven 3.6+

# Steps to build the project
Build the project using maven:

`mvn clean install`

Build the project usisng Maven, explicitly skipping JUnit tests:

`mvn clean install -DskipTests=true`

## JUnit tests
There are a number of JUnit tests designed to test code functionality. Many of these tests require live access to an Event Portal instance and a configured target Application Domain. Accordingly, these tests are not designed to run automatically.

JUnit tests require access and configuration for an Event Portal account and Application Domain.

# Modules

## AsyncApi Accessor v2
This module provides the capability to parse and extract objects from an AsyncAPI specifications.
This module was created to handle AsyncAPI versions 2.X. The intent is for this module to be used in
applications where needed. Convenience methods are provided to obtain objects and fields, including Event Portal Extensions. 

## AsyncApi Importer Core
This module provides the core functionality of the importer.
- References the AsyncApi Accessor v2 module.

## AsyncApi Importer CLI
This module provides a wrapper to call the AsyncApi Importer Core module from a command line. Building the project successfully will produce an executable jar in the module target/ directory.
Details for how to invoke the CLI can be found in the module's
[Readme.md](https://github.com/dennis-brinley/sol-ep-asyncapi-importer/blob/main/asyncapi-importer-cli/Readme.md) file.
- References the AsyncApi Importer Core module

# Issues and Enhancements
1. Add ability to specify new objects as Shared upon creation
