# Solace AsyncAPI Importer for Event Portal
This project provides tooling to import AsyncAPI specs into Event Portal. 

# Prerequisites
- Java 11+
- Maven 3.6+

# Steps to build the project
Build the project using maven, skipping JUnit tests:

`mvn clean install -DskipTests=true`

JUnit tests require access and configuration for an Event Portal account and Application Domain.

# Modules

## AsyncApi Accessor v2
This module provides the capability to parse and extract objects from an AsyncAPI specifications.
Convenience methods are provided to obtain objects and fields, including Event Portal Extensions. 
This module was created to handle AsyncAPI versions 2.X. The intent is for this module to be used in
applications where needed.

## AsyncApi Importer - Core
This module provides the core functionality of the importer. References the AsyncApi Accessor v2 module.

## AsyncApi Importer - CLI
This module provides a wrapper to call the AsyncApi Importer Core module from a command line. Building the project successfully will produce an executable jar in the module target/ directory.
Details for how to invoke the CLI can be found in the module's
[Readme.md](https://github.com/dennis-brinley/sol-ep-asyncapi-importer/blob/main/asyncapi-importer-cli/Readme.md) file.
