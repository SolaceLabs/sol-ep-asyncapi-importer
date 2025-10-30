# AsyncAPI Importer CLI

A command-line interface (CLI) tool for importing AsyncAPI specifications into Solace Event Portal. This module provides a wrapper around the [asyncapi-importer-core](../asyncapi-importer-core/Readme.md) functionality for easy command-line usage.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
  - [Command-Line Options](#command-line-options)
  - [Examples](#examples)
- [Regional Endpoints](#regional-endpoints)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features

- **AsyncAPI v2.x Support**: Import AsyncAPI v2.x specification files
- **Flexible Import Options**: 
  - Import Applications and their versions
  - Import Event APIs and their versions
  - Import Events, Schemas, and Enums
- **Versioning Control**: Choose between MAJOR, MINOR, or PATCH version increments
- **Cascade Updates**: Automatically update dependent objects
- **Multi-threaded Processing**: Optimized performance with parallel operations
- **Regional Support**: Works with all Solace Cloud regions
- **Cross-platform**: Runs on any system with Java 17+

## Requirements

### Event Portal Access
- **Event Portal Bearer Token**: Full read and write access required
- **Application Domain**: Target application domain must exist before import
- **Network Access**: HTTPS access to Solace Cloud API (port 443)

### System Requirements
- **Java**: 17 or higher
- **Memory**: Minimum 512MB RAM recommended
- **Disk Space**: ~50MB for the JAR file

## Installation

### Download Pre-built Release

Download the latest `asyncapi-import.jar` from the [GitHub Releases](https://github.com/SolaceLabs/sol-ep-asyncapi-importer/releases) page.

### Build from Source

Requires Maven 3.6+:

```bash
# Clone the repository
git clone <repository-url>
cd sol-ep-asyncapi-importer

# Build the project (skip tests for faster build)
mvn clean install -DskipTests=true

# Locate the executable JAR
ls asyncapi-importer-cli/target/asyncapi-import.jar
```

## Usage

### Basic Usage

```bash
java -jar asyncapi-import.jar \
  -a <asyncapi-file> \
  -d <app-domain-name> \
  -t <ep-bearer-token>
```

**Minimum Requirements:**
1. AsyncAPI specification file path
2. Existing Event Portal Application Domain name
3. Event Portal Bearer token with read/write permissions

### Command-Line Options

| Short | Long Form | Description | Required | Default |
|-------|-----------|-------------|:--------:|:-------:|
| `-a` | `--asyncapi` | Path to AsyncAPI spec file to import | **Yes** | N/A |
| `-d` | `--app-domain` | Target Application Domain name | **Yes** | N/A |
| `-t` | `--ep-token` | Event Portal Bearer token | **Yes** | N/A |
| `-u` | `--ep-base-url` | Event Portal API base URL | No | `https://api.solace.cloud` |
| `-m` | `--version-major` | Increment MAJOR version (default) | No | Selected |
| `-i` | `--version-minor` | Increment MINOR version | No | N/A |
| `-p` | `--version-patch` | Increment PATCH version | No | N/A |
| `-n` | `--import-application` | Create Application objects | No | Enabled by default |
| `-e` | `--import-eventapi` | Create Event API objects | No | Disabled by default |
| `-z` | `--cascade-update` | Enable cascade updates | No | Enabled by default |
| `-h` | `--help` | Display help message | No | N/A |

**Important Notes:**
- Version options (`-m`, `-i`, `-p`) are mutually exclusive
- If no version option is specified, MAJOR increment is used by default
- Applications are imported by default; use appropriate flags to control import behavior

### Examples

#### Basic Import with Applications
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token"
```

#### Import Event APIs Only
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token" \
  -e
```

#### Import with MINOR Version Increment
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token" \
  -i
```

#### Import for EU Region
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token" \
  -u "https://api.solacecloud.eu"
```

#### Import Both Applications and Event APIs
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token" \
  -n \
  -e
```

#### Disable Cascade Updates
```bash
java -jar asyncapi-import.jar \
  -a my-asyncapi-spec.yaml \
  -d "Production Domain" \
  -t "your-ep-bearer-token" \
  --no-cascade-update
```

## Regional Endpoints

Solace Cloud operates in multiple regions. Use the `-u` option to specify the appropriate endpoint:

| Region | Endpoint | Usage |
|--------|----------|-------|
| **US East** (Default) | `https://api.solace.cloud` | `-u https://api.solace.cloud` |
| **EU** | `https://api.solacecloud.eu` | `-u https://api.solacecloud.eu` |
| **Australia** | `https://api.solacecloud.com.au` | `-u https://api.solacecloud.com.au` |
| **Singapore** | `https://api.solacecloud.sg` | `-u https://api.solacecloud.sg` |

## Troubleshooting

### Common Issues

**"Application domain not found"**
- Verify the application domain exists in Event Portal
- Check spelling and case sensitivity
- Ensure your token has access to the domain

**"Authentication failed"**
- Verify your bearer token is valid and not expired
- Ensure the token has read and write permissions
- Check that you're using the correct regional endpoint

**"Connection timeout"**
- Verify network connectivity to Solace Cloud
- Check firewall settings for HTTPS (port 443)
- Try using a different regional endpoint if applicable

**"Out of memory errors"**
- Increase JVM memory: `java -Xmx1g -jar asyncapi-import.jar ...`
- For very large AsyncAPI files, consider splitting them

### Verbose Logging

For debugging, you can enable verbose logging using Log4j2 system properties:

```bash
# Override the entire Log4j2 configuration
java -Dlog4j.configurationFile=log4j2-debug.properties \
     -jar asyncapi-import.jar \
     -a my-spec.yaml -d "Domain" -t "token"
```

### Exit Codes

- `0`: Success
- `1`: General error (check error message)
- `2`: Invalid command-line arguments

## Performance Considerations

The CLI tool uses multi-threaded processing for optimal performance:

- **Parallel Processing**: Up to 8 concurrent threads for API operations
- **Memory Usage**: Typically 256-512MB depending on AsyncAPI size
- **Processing Time**: Varies based on:
  - AsyncAPI complexity
  - Number of existing objects in Event Portal
  - Network latency to Solace Cloud

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development

For programmatic usage and advanced features, see the [AsyncAPI Importer Core](../asyncapi-importer-core/Readme.md) module documentation.

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](../LICENSE) file for details.
