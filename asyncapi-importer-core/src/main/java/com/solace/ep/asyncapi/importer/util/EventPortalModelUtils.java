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

package com.solace.ep.asyncapi.importer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.solace.cloud.ep.designer.model.Address;
import com.solace.cloud.ep.designer.model.AddressLevel;
import com.solace.cloud.ep.designer.model.DeliveryDescriptor;
import com.solace.cloud.ep.designer.model.TopicAddressEnumValue;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;
import com.solace.ep.asyncapi.importer.model.dto.EnumDto;
import com.solace.ep.asyncapi.importer.model.dto.EventDto;
import com.solace.ep.asyncapi.importer.model.dto.EventVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.EventVersionDto.TopicAddressLevel;
import com.solace.ep.asyncapi.importer.model.dto.SchemaDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventPortalModelUtils {
    
    /**
     * The purpose of this function is to reserialize JSON schemas in a consistent manner so that they
     * may be compared -- without various whitespace characters.
     * @param jsonSchemaToReserialize
     * @return JSON schema as string without whitespace
     * @throws Exception
     */
    public static String reserializeJsonSchema(
        final String jsonSchemaToReserialize
    ) throws Exception
    {
        final JsonElement jsonElement = JsonParser.parseString(jsonSchemaToReserialize);
        final Gson gson = new Gson();
        return gson.toJson(jsonElement);
    }

    /**
     * The purpose of this function is to reserialize JSON schemas with formatting
     * for presentation in Event Portal
     * @param jsonSchemaToReserialize
     * @return JSON schema as string without whitespace
     * @throws Exception
     */
    public static String reserializeJsonAsPretty(
        final String jsonToReserialize
    ) throws Exception
    {
        final JsonElement jsonElement = JsonParser.parseString(jsonToReserialize);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(jsonElement);
    }

    /**
     * Returns enum values from Event Portal object type TopicAddressEnumVersion.
     * @param topicAddressEnumVersion
     * @return List<String> enum values
     */
    public static List<String> getValuesListFromTopicAddressEnumVersion(TopicAddressEnumVersion topicAddressEnumVersion) {
        List<String> values = new ArrayList<>();
        for (TopicAddressEnumValue enumValue : topicAddressEnumVersion.getValues() ) {
            values.add(enumValue.getValue());
        }
        return values;
    }

    /**
     * Compares two lists of string values. Returns `true` if the values lists are an exact match.
     * Returns `false` if either list object is null.
     * @param valueList1
     * @param valueList2
     * @return
     */
    public static boolean valuesListsMatch( List<String> valueList1, List<String> valueList2 ) {
        if (valueList1 == null || valueList2 == null) {
            return false;
        }
        return (valueList1.size() == valueList2.size()) && valueList1.containsAll(valueList2);
    }

    /**
     * Determine if two EventVersionDto objects match based upon comparison of:
     * - EventId
     * - SchemaId
     * - Delivery descriptor (topic address) and embedded Enum versions
     * @param ev1
     * @param ev2
     * @return
     */
    public static boolean eventVersionsMatch( final EventVersionDto ev1, final EventVersionDto ev2 ) {

        if (!ev1.getEventId().contentEquals(ev2.getEventId()) ||
            !ev1.getSchemaVersionId().contentEquals(ev2.getSchemaVersionId())) {
            return false;
        }
        return deliveryDescriptorsMatch(ev1.getDeliveryDescriptor(), ev2.getDeliveryDescriptor());
    }

    /**
     * Determine if two EventVersionDto Delivery Descriptors match. Includes
     * - Topic Address structure
     * - Embedded Enum versions
     * @param d1
     * @param d2
     * @return
     */
    public static boolean deliveryDescriptorsMatch( final EventVersionDto.DeliveryDescriptor d1, final EventVersionDto.DeliveryDescriptor d2 ) {

        if ( d1.getClass() != d2.getClass() ) {
            return false;
        }
        if ( !d1.getBrokerType().contentEquals(d2.getBrokerType()) ) {
            return false;
        }
        return topicAddressesMatch(d1.getAddress(), d2.getAddress());
    }

    /**
     * Determine if Topic address portion of EventVersionDto delivery descriptors match
     * @param a1
     * @param a2
     * @return
     */
    public static boolean topicAddressesMatch(final EventVersionDto.TopicAddress a1, final EventVersionDto.TopicAddress a2 ) {

        if (!a1.getAddressType().contentEquals(a2.getAddressType()) ||
            !a1.getType().contentEquals(a2.getType())) {
                return false;
        }
        if ( a1.getAddressLevels().size() != a2.getAddressLevels().size() ) {
            return false;
        }
        for ( int idx = 0; idx < a1.getAddressLevels().size(); idx++ ) {
            if ( !topicAddressLevelsMatch(a1.getAddressLevels().get(idx), a2.getAddressLevels().get(idx)) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determines if one topic address level matches the other:
     * - Level name
     * - Variable or literal
     * - Enum version if exists
     * @param l1
     * @param l2
     * @return
     */
    public static boolean topicAddressLevelsMatch( final EventVersionDto.TopicAddressLevel l1, final EventVersionDto.TopicAddressLevel l2 ) {

        if (l1.getClass() != l2.getClass()) {
            return false;
        }
        if (!l1.getName().contentEquals(l2.getName()) ||
            !l1.getAddressLevelType().contentEquals(l2.getAddressLevelType()) ||
            l1.getHasEnum() != l2.getHasEnum()) {
                return false;
        }
        if (l1.getHasEnum()) {
            if (!l1.getEnumVersionId().contentEquals(l2.getEnumVersionId())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Maps Event Portal Delivery Descriptor to DTO delivery descriptor
     * @param deliveryDescriptor
     * @return
     */
    public static EventVersionDto.DeliveryDescriptor mapEpEventVersionToDtoDeliveryDescriptor( DeliveryDescriptor deliveryDescriptor ) {
        final EventVersionDto.DeliveryDescriptor dd = new EventVersionDto.DeliveryDescriptor();
        
        final Address evAddress = deliveryDescriptor.getAddress();
        final EventVersionDto.TopicAddress dtoAddress = dd.getAddress();
        dtoAddress.setId(evAddress.getId());
        dtoAddress.setAddressType(evAddress.getAddressType().getValue());
        evAddress.getAddressLevels().forEach( al -> {
            EventVersionDto.TopicAddressLevel dtoLevel = new TopicAddressLevel();
            dtoLevel.setName(al.getName());
            dtoLevel.setAddressLevelType(al.getAddressLevelType().getValue());
            if (al.getEnumVersionId() != null) {
                dtoLevel.setHasEnum(true);
                dtoLevel.setEnumVersionId(al.getEnumVersionId());
            }
            dd.getAddress().getAddressLevels().add(dtoLevel);
        });
        return dd;
    }

    /**
     * Maps DTO Delivery descriptor to Event Portal Delivery Descriptor
     * @param dtoDeliveryDescriptor
     * @return
     */
    public static DeliveryDescriptor mapDtoDeliveryDescriptorToEpEventDeliveryDescriptor( 
        EventVersionDto.DeliveryDescriptor dtoDeliveryDescriptor 
    )
    {
        final DeliveryDescriptor dd = new DeliveryDescriptor();
        final Address address = new Address();
        // TODO - Handle Queue addresses
        address.setType("topic");
        address.setAddressLevels(new ArrayList<>());

        dtoDeliveryDescriptor.getAddress().getAddressLevels().forEach( dtoLevel -> {
            final AddressLevel level = new AddressLevel();
            level.setName(dtoLevel.getName());
            level.setAddressLevelType(AddressLevel.AddressLevelTypeEnum.fromValue(dtoLevel.getAddressLevelType()));
            if (dtoLevel.getHasEnum()) {
                level.setEnumVersionId(dtoLevel.getEnumVersionId());
            }
            address.addAddressLevelsItem(level);
        });
        dd.setBrokerType("solace");
        dd.setAddress(address);

        return dd;
    }

    /**
     * Tests DtoResultSet object to ensure that there is exactly one version of each named object for import
     * DtoResultSet object may contain multiple versions of each object when exported from Event Portal.
     * But an AsyncApi spec can only have one version of each object - which is what is being tested here.
     * @param resultSet
     * @return True if counts of Versions of EP Objects for import are correct
     */
    public static boolean versionCountsValid(final DtoResultSet resultSet) throws Exception
    {
        final String ERROR_MSG = "Found [{}] versions for {} [{}]; each named object must have exactly one unique version for import";

        boolean isValid = true;
        try {
            if (resultSet.getMapApplications().size() > 1) {
                log.error("More than 1 applications found in result set; not valid for import to EP");
                isValid = false;
            }
            for (Map.Entry<String, ApplicationDto> entry : resultSet.getMapApplications().entrySet()) {
                if (entry.getValue().getNumberOfVersions() != 1) {
                    log.error(ERROR_MSG, entry.getValue().getNumberOfVersions(), "APPLICATION", entry.getKey());
                    isValid = false;
                }
            }
            for (Map.Entry<String, EnumDto> entry : resultSet.getMapEnums().entrySet()) {
                if (entry.getValue().getNumberOfVersions() != 1) {
                    log.error(ERROR_MSG, entry.getValue().getNumberOfVersions(), "Channel Parameter/Event Portal ENUM", entry.getKey());
                    isValid = false;
                }
            }
            for (Map.Entry<String, SchemaDto> entry : resultSet.getMapSchemas().entrySet()) {
                if (entry.getValue().getNumberOfVersions() != 1) {
                    log.error(ERROR_MSG, entry.getValue().getNumberOfVersions(), "SCHEMA", entry.getKey());
                    isValid = false;
                }
            }
            for (Map.Entry<String, EventDto> entry : resultSet.getMapEvents().entrySet()) {
                if (entry.getValue().getNumberOfVersions() != 1) {
                    log.error(ERROR_MSG, entry.getValue().getNumberOfVersions(), "Event Portal EVENT", entry.getKey());
                    isValid = false;
                }
            }
        } catch (Exception exc) {
            log.error("EventPortalModelUtils.versionCountsValid -- Caught unexpected error: [{}] mapping AsyncApi spec", exc.getLocalizedMessage());
            throw exc;
        } 
        return isValid;
    }
}
