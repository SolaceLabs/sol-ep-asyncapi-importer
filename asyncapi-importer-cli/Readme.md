# AsyncApi Importer CLI Tool
This project provides a Command Line Interface wrapper around the **asyncapi-importer-core** project. This tool can be used to import AsyncApi specifications into Event Portal from a command line.

## Requirements

### Access to Event Portal
- Event Portal token with full read and write access
- Event Portal access - Access to the UI is desirable but not required to run the import
- Application Domain to use as a target. The application domain must exist before attempting to import.

### System Requirements
- Java 11 Runtime
- HTTP/S access to Solace Cloud API on Port 443
    - https://api.solace.cloud - Solace PubSub+ Cloud US East Region - **DEFAULT**
    - https://api.solacecloud.com.au - Solace PubSub+ Cloud AU Region
    - https://api.solacecloud.eu - Solace PubSub+ Cloud EU Region
    - https://api.solacecloud.sg - Solace PubSub+ Cloud SG Region

### Limitations
- The tool will import published events only (those channels with **subscribe** operations).

## Obtaining Executable
You can download a releaseed version of the JAR file from the GitHub project.

### Build from Source
1. From project root, build using Maven. Recommend skipping unit tests. These require setup and access to an Event Portal account.<br>`mvn clean install -DskipTests=true`
2. Locate executable jar file in `asyncapi-importer-cli/target/`. Compiled jar: `asyncapi-import.jar`

## Running as a JAR from the command line

Command will have the form:

java -jar asyncapi-import.jar -a **ASYNCAPI_TO_IMPORT** -d **APP_DOMAIN** -t **EP_TOKEN** [-u BASE_URL] [-m | -i | -p]

**You will need a minimum of three things to execute the importer:**
1. AsyncApi spec file to import
2. An existing Event Portal Application Domain
3. An Event Portal Bearer token for An/Az

**-m, -i, -p** options define which SemVer element to increment when creating new objects: Major, Minor, or Patch respectively.

### Options
|Short<br>Form|Long Form|Description|Required|Default|
|---|---|---|:---:|:---:|
|`-a`|`--asyncapi`|AsyncApi Spec File to import|**Yes**|N/A|
|`-d`|`--app-domain`|Target Application Domain|**Yes**|N/A|
|`-t`|`--ep-token`|Event Portal Access Token|**Yes**|N/A|
|`-u`|`--ep-base-url`|Cloud API URL<br>(varies by region)|No|https://api.solace.cloud|
|`-m`|`--version-major`|Increment Major Version|No|`-m` / `--version-major`|
|`-i`|`--version-minor`|Increment Minor Version|No|`-i` / `--version-minor`|
|`-p`|`--version-patch`|Increment Patch Version|No|`-p` / `--version-patch`|
|`-e`|`--events-only`|Import Events, Enums, and<br>Schemas. Skip Applications|No|N/A|
|`-z`|`--no-cascade`|Disable cascade update of objects<br>Schemas. Skip Applications|No|N/A|
|`-h`|`--help`|Display Help|No|N/A|

> **Note:** Version options are mutually exclusive. Incremented versions are new SemVer versions when new object versions are created as a result of importing.

> **Note:** If **--events-only** option is specified, applications may still be cascade updated if an associated event has a new version created.

## Outstanding
- Check the EP token access before being import operation
