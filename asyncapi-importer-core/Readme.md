# AsyncAPI Importer Core

This module provides the core functionality to import AsyncAPI specification files into Solace Event Portal. It handles the mapping, matching, and creation of Event Portal objects including schemas, enums, events, applications, and event APIs.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
  - [Programmatic Usage](#programmatic-usage)
  - [Configuration Options](#configuration-options)
- [Architecture](#architecture)
- [Performance](#performance)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Features

- **AsyncAPI v2.x Support**: Parse and import AsyncAPI v2.x specification files
- **Complete Object Mapping**: Automatically maps AsyncAPI components to Event Portal objects:
  - Schemas and Schema Versions
  - Enums and Enum Versions  
  - Events and Event Versions
  - Applications and Application Versions
  - Event APIs and Event API Versions
- **Smart Matching**: Identifies existing objects in Event Portal to avoid duplicates
- **Versioning Strategy**: Supports MAJOR, MINOR, and PATCH semantic versioning strategies
- **Cascade Updates**: Automatically updates dependent objects when schemas or enums change
- **Parallel Processing**: Multi-threaded operations for improved performance
- **Thread Safety**: All operations are thread-safe with proper synchronization
- **Flexible Import Options**: Choose to import applications, event APIs, or both

## Requirements

- **Java**: 17 or higher
- **Maven**: 3.6+ (for building)
- **Event Portal Access**: Valid bearer token with read/write permissions
- **Application Domain**: Target application domain must exist in Event Portal

## Installation

### As a Maven Dependency

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>com.solace.ep.asyncapi</groupId>
    <artifactId>asyncapi-importer-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Build from Source

```bash
git clone <repository-url>
cd sol-ep-asyncapi-importer
mvn clean install
```

## Usage

### Programmatic Usage

#### Basic Import Operation

```java
import com.solace.ep.asyncapi.importer.AsyncApiImporter;

// Basic import with default settings
AsyncApiImporter.execImportOperation(
    null,                           // applicationDomainId (null to lookup by name)
    "MyApplicationDomain",          // applicationDomainName  
    "your-ep-bearer-token",         // eventPortalBearerToken
    asyncApiSpecContent,            // asyncApiSpecToImport (String)
    null,                          // eventPortalBaseUrl (null for default US/CAN)
    "MAJOR",                       // newVersionStrategy
    true,                          // performCascadeUpdate
    true,                          // importApplication
    false                          // importEventApi
);
```

#### Advanced Usage with Custom Configuration

```java
import com.solace.ep.asyncapi.importer.AsyncApiImporter;
import com.solace.ep.asyncapi.importer.EpNewVersionStrategy;

// Create importer instance for more control
AsyncApiImporter importer = new AsyncApiImporter(
    "app-domain-id-12345",         // applicationDomainId
    "Production Domain",           // applicationDomainName
    "your-ep-bearer-token",        // eventPortalBearerToken  
    asyncApiSpecContent,           // asyncApiSpecToImport
    "https://api.solacecloud.com", // eventPortalBaseUrl
    "MINOR",                       // newVersionStrategy
    true,                          // performCascadeUpdate
    true,                          // importApplication
    true                           // importEventApi
);

// Execute the import
importer.execImportOperation();
```

#### Working with the Core Components

```java
import com.solace.ep.asyncapi.importer.EpImportOperator;
import com.solace.ep.asyncapi.importer.client.EventPortalClientApi;
import com.solace.ep.asyncapi.importer.mapper.AsyncApiV2ToDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;

// Step 1: Create Event Portal client
EventPortalClientApi client = new EventPortalClientApi(
    bearerToken, 
    applicationDomainName, 
    EpNewVersionStrategy.MAJOR
);

// Step 2: Map AsyncAPI to internal DTOs
DtoResultSet mappedResults = AsyncApiV2ToDto.mapAsyncApiToDto(
    asyncApiContent,
    client.getAppDomainId(),
    client.getAppDomainName()
);

// Step 3: Execute import operations
EpImportOperator importOperator = new EpImportOperator(mappedResults, client);

try {
    // Match existing objects
    importOperator.matchEpSchemas();
    importOperator.matchEpEnums();
    importOperator.matchEpEvents();
    
    // Import new objects
    importOperator.importSchemas();
    importOperator.importEnums(); 
    importOperator.importEvents();
    
    // Import applications/APIs if needed
    importOperator.matchEpApplications();
    importOperator.importApplications();
    
    // Cascade updates to dependent objects
    importOperator.cascadeUpdateEvents();
    importOperator.cascadeUpdateApplications();
    
} finally {
    // Always shutdown the thread pool
    importOperator.shutdown();
}
```

### Configuration Options

| Parameter | Type | Description | Default |
|-----------|------|-------------|---------|
| `applicationDomainId` | String | Target app domain ID (optional if name provided) | null |
| `applicationDomainName` | String | Target app domain name | Required |
| `eventPortalBearerToken` | String | EP bearer token with read/write access | Required |
| `asyncApiSpecToImport` | String | AsyncAPI specification content | Required |
| `eventPortalBaseUrl` | String | EP base URL (for non-US/CAN regions) | https://api.solace.cloud |
| `newVersionStrategy` | String | Version increment strategy: MAJOR, MINOR, PATCH | MAJOR |
| `performCascadeUpdate` | boolean | Update dependent objects automatically | true |
| `importApplication` | boolean | Create application objects | true |
| `importEventApi` | boolean | Create event API objects | false |

## Architecture

The importer follows a multi-stage pipeline:

1. **Parsing**: AsyncAPI spec parsed using `AsyncApiAccessor`
2. **Mapping**: Spec components mapped to internal DTOs via `AsyncApiV2ToDto`
3. **Matching**: Existing EP objects identified to avoid duplicates
4. **Import**: New objects created in Event Portal
5. **Cascade Update**: Dependent objects updated with new references

### Key Components

- **`AsyncApiImporter`**: Main orchestration class
- **`EpImportOperator`**: Core import logic with parallel processing
- **`EventPortalClientApi`**: Event Portal REST API client
- **`AsyncApiV2ToDto`**: Mapping between AsyncAPI and internal DTOs
- **DTO Classes**: Internal representation of Event Portal objects

## Performance

The importer uses parallel processing for improved performance:

- **Thread Pool**: Fixed pool of 8 threads for concurrent operations
- **Parallel Operations**: Schema, enum, and event processing parallelized
- **Thread Safety**: `ConcurrentHashMap` and synchronized collections used
- **Custom Thread Names**: Threads named `ep-importer-N` for easier debugging

Performance optimizations include:
- Bulk matching operations
- Cached API responses
- Efficient object comparison
- Minimal API calls through smart caching

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

Integration tests require live Event Portal access:

```bash
# Configure test properties
cp src/test/resources/TEMPLATE_config.properties src/test/resources/config.properties
# Edit config.properties with your EP credentials

# Run integration tests
mvn test -Dtest=AsyncApiImporterTests
```

### Test AsyncAPI Files

Sample AsyncAPI files for testing are located in:
- `src/test/resources/asyncapi/eventapi/`

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow existing code style and patterns
- Add unit tests for new functionality
- Update documentation as needed
- Ensure thread safety for concurrent operations
- Use meaningful commit messages

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](../LICENSE) file for details.

---

For command-line usage, see the [AsyncAPI Importer CLI](../asyncapi-importer-cli/Readme.md) module.
