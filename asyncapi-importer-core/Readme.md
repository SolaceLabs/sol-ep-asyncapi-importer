# AsyncApi Importer Core
This module provides the core functionality to import AsyncApi spec files into Event Portal.

## Build and Execute


## Design (to be completed)



## Field Mapping


### Standard AsyncAPI
In a *Standard* AsyncAPI specification, certain conventions are followed. There is a **components** section that contains **schemas** and **messages**. Message payloads reference schemas. And **channels** reference **messages**. See [AsyncApi](https://www.asyncapi.com/) for more details. The table below defines how fields are mapped from AsyncAPI specifications conforming to these practices.

|Event Portal Field|Source Field Description|AsyncAPI Spec Path|
|---|---|---|
|Enumeration Name|Parameter ID in Channel Defintion|$.channels.[channel_name].parameters.[paramId]|
|Enumeration Version Values|Values Array of `schema.enum` Field in Channel Defintion|$.channels.[channel_name].parameters.[paramId].schema.enum|
|Schema Name|Schema Component ID|$.components.schemas.[schemaId]|
|Schema Version Content|Serialized Schema Payload &#10687;|$.components.schemas.[schemaId].[object value]|
|Event Name|Message Component ID|$components.messages.[messageId]|
|Event Version Topic Address|Channel Name|$.channels.[channel_name]|
|Event Version Schema|Schema Payload &rarr; Message Component &rarr; Channel||


> &#10687; Both schemas found in Event Portal and in the AsyncAPI spec will be re-serialized for comparision. This step is performed to prevent non-material differences (whitespace, formatting) from resulting in differences between versions.


### Enumerations

Consider the following channel. The channel name is `importer/customer/updated/{customerId}/{IMP_regionId}`. The channel contains two parameters: `customerId` and `regionId`. **customerId** is an unbound parameter, meaning that it can effectively take on any value (there are some constraints). Whereas the **regionId** is bound to a set of discrete values: `CA-EAST`, `CA-WEST`, `USA-WEST`, etc. These values will be mapped into **Event Portal** as an Enumeration.

```yaml
channels:
  importer/customer/updated/{customerId}/{regionId}:
    subscribe:
      message:
        $ref: "#/components/messages/CustomerUpdated"
    parameters:
      customerId:
        schema:
          type: "string"
      regionId:
        schema:
          type: "string"
          enum:
          - "CA-EAST"
          - "CA-WEST"
          - "USA-WEST"
          - "USA-EAST"
          - "EU"
          - "SA"
          - "AP
```

Details:
- Upon import, an Enumeration with the parameter name will be created in Event Portal.
    - Example above: **regionId**
- Each distinct value set imported for an enumeration will result in a new version

### Schemas

