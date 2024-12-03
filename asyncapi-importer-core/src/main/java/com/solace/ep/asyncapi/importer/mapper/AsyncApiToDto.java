/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.ep.asyncapi.importer.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.gson.JsonElement;
import com.solace.ep.asyncapi.accessor.v2.AsyncApiAccessor;
import com.solace.ep.asyncapi.accessor.v2.AsyncApiChannel;
import com.solace.ep.asyncapi.accessor.v2.AsyncApiMessage;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationDomainDto;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationDto;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;
import com.solace.ep.asyncapi.importer.model.dto.EnumDto;
import com.solace.ep.asyncapi.importer.model.dto.EnumVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.EventDto;
import com.solace.ep.asyncapi.importer.model.dto.EventVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.SchemaDto;
import com.solace.ep.asyncapi.importer.model.dto.SchemaVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.EnumVersionDto.EnumValue;
import com.solace.ep.asyncapi.importer.util.EventPortalModelUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncApiToDto {
    
    private AsyncApiAccessor asyncApiAccessor;

    private String applicationDomainId;

    private String applicationDomainName;

    private ApplicationDomainDto mapApplicationDomain;

    private Map<String, EnumDto> mapEnums = new HashMap<>();

    private Map<String, SchemaDto> mapSchemas = new HashMap<>();

    private Map<String, EventDto> mapEvents = new HashMap<>();

    private Map<String, ApplicationDto> mapApplications = new HashMap<>();

    public AsyncApiToDto( 
        final AsyncApiAccessor asyncApiAccessor, 
        final String applicationDomainId,
        final String applicationDomainName ) 
    {
        this.asyncApiAccessor = asyncApiAccessor;
        this.applicationDomainId = applicationDomainId;
        this.applicationDomainName = applicationDomainName;
    }

    public DtoResultSet mapAsyncApiToDto() throws Exception {

        final Map<String, AsyncApiChannel> importChannels = new HashMap<>();

        // Get the channels we care about
        for ( Map.Entry<String, JsonElement> channelEntry : asyncApiAccessor.getChannels().entrySet() ) {
            if ( channelEntry.getValue().isJsonObject() ) {
                AsyncApiChannel channel = new AsyncApiChannel(channelEntry.getValue().getAsJsonObject(), asyncApiAccessor);
                if (channel.hasSubscribeOperation()) {
                    importChannels.put(channelEntry.getKey(), channel);
                }
            } else {
                throw new Exception("Error parsing AsyncApi input file; channel content invalid");
            }
        }

        // Now map the channels
        for ( Map.Entry<String, AsyncApiChannel> entry : importChannels.entrySet() ) {
            final String channelName = entry.getKey();
            final AsyncApiChannel channel = entry.getValue();
            mapAsyncApiChannelToDto(channelName, channel);
        }

        mapApplicationDomain = new ApplicationDomainDto();
        mapApplicationDomain.setId(applicationDomainId);
        mapApplicationDomain.setName(applicationDomainName);

        ApplicationDto appDto = new ApplicationDto();
        appDto.setApplicationDomainId(applicationDomainId);
        appDto.setBrokerType("solace");
        appDto.setName(asyncApiAccessor.getInfo().getInfoTitle());
        mapApplications.put(asyncApiAccessor.getInfo().getInfoTitle(), appDto);

        ApplicationVersionDto appVersionDto = new ApplicationVersionDto();
        appDto.getApplicationVersions().add(appVersionDto);     // Add empty application version
        
        DtoResultSet resultSet = new DtoResultSet();
        resultSet.setApplicationDomainDto(mapApplicationDomain);
        resultSet.setMapApplications(mapApplications);
        resultSet.setMapEnums(mapEnums);
        resultSet.setMapSchemas(mapSchemas);
        resultSet.setMapEvents(mapEvents);

        log.info("Discovered objects in AsyncApi for import to EP App Domain: {}", applicationDomainName);
        log.info(
            "Counts -- Applications: {} -- Enums: {} -- Schemas: {} -- Events: {}",
            mapApplications.size(),
            mapEnums.size(),
            mapSchemas.size(),
            mapEvents.size()
        );

        return resultSet;
    }

    private void mapAsyncApiChannelToDto(
        final String channelName, 
        final AsyncApiChannel channel
    ) throws Exception
    {
        final Map<String, EnumVersionDto> enumVersionsInThisChannel = new HashMap<>();  // indexed by param/enum name - this works because only one version can be present per channel

        SchemaVersionDto schemaVersionInThisChannel = null;   // TODO - null or empty string?

        // Map Enums
        for ( Map.Entry<String, AsyncApiChannel.ParameterObject> entry : channel.getParameterObjects().entrySet()) {
            final String enumName = entry.getKey();
            final List<String> valueSet = entry.getValue().getEnumValues();
            EnumVersionDto ev = mapEnumDto(enumName, valueSet);
            enumVersionsInThisChannel.put(enumName, ev);
        }

        // Map Schemas and Events
        // There should only be one message in this list
        for ( AsyncApiMessage msg : channel.getSubscribeOpMessages() ) {
            // final String schemaFormat = msg.getSchemaFormat();
            final String contentType = msg.getContentType();
            final String payload = msg.getPayloadAsString();
            final String schemaName = msg.getSchemaName();
            final String messageName = msg.getMessageName();

            // Map the schema, return the object representing the schema version
            schemaVersionInThisChannel = mapSchemaDto(schemaName, contentType, payload);

            mapEventDto(channelName, messageName, schemaVersionInThisChannel, enumVersionsInThisChannel);
        }
    }

    private void mapEventDto(
        final String channelName,
        final String messageName, 
        final SchemaVersionDto schemaVersion, 
        final Map<String, EnumVersionDto> enumVersions) 
    {
        EventDto eventDto = mapEvents.get(messageName);
        if (eventDto == null) {
            eventDto = new EventDto(messageName, applicationDomainId);
            mapEvents.put(messageName, eventDto);
        }

        EventVersionDto eventVersion = new EventVersionDto();
        eventVersion.setSchemaVersionDto(schemaVersion);
        mapDeliveryDescriptor(eventVersion, channelName, enumVersions);
        eventDto.getEventVersions().add(eventVersion);
    }

    private void mapDeliveryDescriptor(
        final EventVersionDto eventVersion, 
        final String topicAddressPattern, 
        final Map<String, EnumVersionDto> enumVersions ) 
    {
        eventVersion.setTopicAddressPattern(topicAddressPattern);
        StringTokenizer tTopic = new StringTokenizer(topicAddressPattern, "/");
        EventVersionDto.TopicAddress address = eventVersion.getDeliveryDescriptor().getAddress();
        // TODO - Handle queue destinations
        address.setAddressType("topic");

        while ( tTopic.hasMoreTokens() ) {
            String elt = tTopic.nextToken();
            EventVersionDto.TopicAddressLevel topicAddressLevel = new EventVersionDto.TopicAddressLevel();
            if ( isTopicParameter(elt) ) {
                String parameterName = elt.substring(1, elt.length() - 1);
                topicAddressLevel.setName(parameterName);
                topicAddressLevel.setAddressLevelType("variable");
                EnumVersionDto enumVersion = enumVersions.get(parameterName);
                if (enumVersion != null) {
                    topicAddressLevel.setEnumVersionDto(enumVersion);
                    topicAddressLevel.setHasEnum(true);
                } else {
                    topicAddressLevel.setHasEnum(false);
                }
            } else {
                topicAddressLevel.setName(elt);
                topicAddressLevel.setAddressLevelType("literal");
                topicAddressLevel.setHasEnum(false);
            }
            address.getAddressLevels().add(topicAddressLevel);
        }
    }
    
    /**
     * Test whether a topic level is a variable (asyncapi paramter)
     * @param topicElt - Topic level between '/' delimiters
     * @return
     */
    private boolean isTopicParameter( String topicElt ) {
        return ( topicElt.startsWith("{") && topicElt.endsWith("}") );
    }

    private SchemaVersionDto mapSchemaDto(
        final String schemaName, 
        final String contentType, 
        final String payload ) 
    {
        SchemaVersionDto schemaVersionDtoToReturn = null;

        SchemaDto schemaDto = mapSchemas.get( schemaName );
        if (schemaDto == null) {
            schemaDto = new SchemaDto( schemaName, applicationDomainId );
            schemaDto.setApplicationDomainId(applicationDomainId);
            schemaDto.setContentType(contentType);
            mapSchemas.put(schemaName, schemaDto);
        }
        boolean foundSchemaVersion = false;
        for ( SchemaVersionDto sv : schemaDto.getSchemaVersions() ) {
            if ( sv.getContent().contentEquals(payload) ) {
                schemaVersionDtoToReturn = sv;
                foundSchemaVersion = true;
                break;
            }
        }
        if ( ! foundSchemaVersion ) {
            schemaVersionDtoToReturn = createSchemaVersionDto(payload);
            schemaDto.getSchemaVersions().add(schemaVersionDtoToReturn);
        }
        return schemaVersionDtoToReturn;
    }

    private SchemaVersionDto createSchemaVersionDto( final String schemaContent ) {

        SchemaVersionDto schemaVersionDto = new SchemaVersionDto();
        schemaVersionDto.setContent(schemaContent);
        return schemaVersionDto;
    }

    /**
     * If a new EnumVersionDto entry is added to this object, then returns that object.
     * If the ENUM Object does not exist, then an enumDto entry is added 
     * @param enumName
     * @param valueSet
     * @return
     */
    private EnumVersionDto mapEnumDto( String enumName, List<String> valueSet ) {

        EnumVersionDto enumVersionDtoToReturn = null;

        EnumDto enumDto = mapEnums.get( enumName );
        if (enumDto == null) {
            enumDto = new EnumDto( enumName, applicationDomainId );
            enumDto.setApplicationDomainId(this.applicationDomainId);
            mapEnums.put( enumName, enumDto );
        }
        boolean foundEnumVersion = false;
        for ( EnumVersionDto ev : enumDto.getEnumVersions() ) {
            if ( EventPortalModelUtils.valuesListsMatch( ev.getValuesAsStringList(), valueSet ) ) {
                enumVersionDtoToReturn = ev;
                foundEnumVersion = true;
                break;
            }
        }
        if ( ! foundEnumVersion ) {
            enumVersionDtoToReturn = createEnumVersionDto(valueSet);
            enumDto.getEnumVersions().add(enumVersionDtoToReturn);
        }
        return enumVersionDtoToReturn;
    }

    /**
     * Create an Enum for import in the internal DTO working format
     * @param valueSet - List of Enum Values
     * @return - The new EnumVersionDto object
     */
    private EnumVersionDto createEnumVersionDto( final List<String> valueSet ) 
    {
        final EnumVersionDto enumVersionDto = new EnumVersionDto();

        for ( String value : valueSet ) {
            enumVersionDto.getValues().add(new EnumValue(value));
        }
        return enumVersionDto;
    }

}
