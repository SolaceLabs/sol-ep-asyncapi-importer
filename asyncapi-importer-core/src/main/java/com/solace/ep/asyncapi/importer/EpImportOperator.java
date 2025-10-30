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

package com.solace.ep.asyncapi.importer;

import com.solace.ep.asyncapi.importer.client.EventPortalClientApi;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationDto;
import com.solace.ep.asyncapi.importer.model.dto.ApplicationVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;
import com.solace.ep.asyncapi.importer.model.dto.EnumDto;
import com.solace.ep.asyncapi.importer.model.dto.EnumVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.EventApiDto;
import com.solace.ep.asyncapi.importer.model.dto.EventApiVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.EventDto;
import com.solace.ep.asyncapi.importer.model.dto.EventVersionDto;
import com.solace.ep.asyncapi.importer.model.dto.SchemaDto;
import com.solace.ep.asyncapi.importer.model.dto.SchemaVersionDto;
import com.solace.ep.asyncapi.importer.util.EventPortalModelUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.solace.cloud.ep.designer.model.Application;
import com.solace.cloud.ep.designer.model.ApplicationVersion;
import com.solace.cloud.ep.designer.model.Event;
import com.solace.cloud.ep.designer.model.EventApi;
import com.solace.cloud.ep.designer.model.EventApiVersion;
import com.solace.cloud.ep.designer.model.EventVersion;
import com.solace.cloud.ep.designer.model.SchemaObject;
import com.solace.cloud.ep.designer.model.SchemaVersion;
import com.solace.cloud.ep.designer.model.TopicAddressEnum;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;

@Slf4j
public class EpImportOperator {

    private static final int DEFAULT_IMPORTER_THREADPOOL_SZ = 8;

    public static final String OPERATOR_ID_PREFIX = "ImportOp";

    private static int defaultOperationIdCounter = 1;
    
    @Getter
    private DtoResultSet dtoResultSet;

    private EventPortalClientApi epClient;

    private Map<String, String> incrementedEnumVersions = new ConcurrentHashMap<>();

    private Map<String, String> incrementedSchemaVersions = new ConcurrentHashMap<>();

    private Map<String, String> incrementedEventVersions = new ConcurrentHashMap<>();

    private final ExecutorService executor;

    private final int operationId;

    public static String getOperatorIdPrefix(int operationId) {
        return OPERATOR_ID_PREFIX + operationId;
    }

    public EpImportOperator(
        DtoResultSet dtoResultSetToUpdate,
        EventPortalClientApi client,
        final Integer operationId) throws Exception
    {
        this.epClient = client;
        this.dtoResultSet = dtoResultSetToUpdate;
        this.operationId = operationId != null ? operationId : defaultOperationIdCounter++;
        this.executor = Executors.newFixedThreadPool(
            DEFAULT_IMPORTER_THREADPOOL_SZ, 
            r -> {
                Thread t = new Thread(r);
                t.setName(OPERATOR_ID_PREFIX + this.operationId + "-" + t.getId());
                t.setDaemon(false);
                return t;
            }
        );
    }

    public EpImportOperator(
        DtoResultSet dtoResultSetToUpdate,
        EventPortalClientApi client) throws Exception
    {
        this(dtoResultSetToUpdate, client, null);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(12, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void matchEpApplications() throws Exception
    {
        updateApplicationIdentifiers();

        for (Map.Entry<String, ApplicationDto> appEntry : dtoResultSet.getMapApplications().entrySet()) {
            matchToEpApplicationObject(appEntry.getKey(), appEntry.getValue());
        }
    }

    /**
     * This function will set the event version Ids in the application version
     * to the event version Ids discovered in the import procedure. This method
     * works under the assumption that there is only one application version in
     * the import operation.
     */
    private void updateApplicationIdentifiers() {
        dtoResultSet.getMapApplications().forEach( ( appName, appDto ) -> {
            appDto.getApplicationVersions().forEach( appVersionDto -> {
                appVersionDto.setDeclaredProducedEventVersionIds(getPublishedEventIdsFromResults());
                appVersionDto.setDeclaredConsumedEventVersionIds(getConsumedEventIdsFromResults());
            } );
        } );
    }

    private void updateEventApiIdentifiers() {
        dtoResultSet.getMapEventApis().forEach( ( apiName, apiDto ) -> {
            apiDto.getEventApiVersions().forEach( apiVersionDto -> {
                apiVersionDto.setProducedEventVersionIds(getConsumedEventIdsFromResults());
                apiVersionDto.setConsumedEventVersionIds(getPublishedEventIdsFromResults());
            } );
        } );
    }

    private void matchToEpApplicationObject(
        final String appName,
        final ApplicationDto appDto
    ) throws Exception
    {
        final Application epApplication = epClient.getApplicationByName( appName );
        if ( epApplication == null ) {
            appDto.setMatchFound(false);
            log.info("Application [{}] not found in Event Portal", appName);
            return;
        }
        appDto.setMatchFound(true);
        log.info("Application [{}] was found to exist", appName);
        final String appId = epApplication.getId();
        appDto.setId(appId);
        appDto.setApplicationDomainId(epClient.getAppDomainId());
        appDto.setApplicationType(epApplication.getApplicationType());
        appDto.setBrokerType(epApplication.getBrokerType().getValue());

        for ( ApplicationVersionDto appVersionDto : appDto.getApplicationVersions()) {
            matchToEpApplicationVersion(appVersionDto, appId);
        }
    }

    private void matchToEpApplicationVersion(
        final ApplicationVersionDto appVersionDto,
        final String appId
    ) throws Exception
    {
        // final ApplicationVersion epAppVersion = epClient.getApplicationVersionByProducedEvents(
        final ApplicationVersion epAppVersion = epClient.getApplicationVersionByAllEvents(
            appId, 
            appVersionDto.getDeclaredProducedEventVersionIds(),
            appVersionDto.getDeclaredConsumedEventVersionIds()
        );
        if (epAppVersion != null) {
            appVersionDto.setMatchFound(true);
            appVersionDto.setEpApplicationVersion(epAppVersion);
            log.info("Application version [{}] found in Event Portal", epAppVersion.getVersion());
        } else {
            log.info("Matching Application version not found in Event Portal");
            if (epClient.getCachedLatestApplicationVersion() != null) {
                appVersionDto.setEpApplicationVersion(epClient.getCachedLatestApplicationVersion());
                appVersionDto.setLastestVersionFound(true);
                if (appVersionDto.getEpApplicationVersion().getStateId().contentEquals("1")) {
                    appVersionDto.setLatestVersionFoundInDraftState(true);
                }
            }
        }
    }

    /**
     * Returns a unique list of Event IDs in the result set assuming that all previous matching
     * operations have been completed: Schemas, Enums, Events -- including import.
     * This function is only valid to use for an applicationif there is exactly ONE APPLICATION
     * represented in the result set!
     * @return List of String values representing all published events in the result set
     */
    private List<String> getPublishedEventIdsFromResults() {
        final Set<String> publishedEventIds = new HashSet<>();
        dtoResultSet.getMapEvents().forEach( ( eventName, eventValue ) -> {
            if (eventValue.getPublishedEvent()) {
                eventValue.getEventVersions().forEach( eventVersion -> {
                    publishedEventIds.add(eventVersion.getId());
                } );
            }
        } );
        return new ArrayList<>(publishedEventIds);
    }

    private List<String> getConsumedEventIdsFromResults() {
        final Set<String> consumedEventIds = new HashSet<>();
        dtoResultSet.getMapEvents().forEach( ( eventName, eventValue ) -> {
            if (eventValue.getConsumedEvent()) {
                eventValue.getEventVersions().forEach( eventVersion -> {
                    consumedEventIds.add(eventVersion.getId());
                } );
            }
        } );
        return new ArrayList<>(consumedEventIds);
    }

    public void matchEpEventApis() throws Exception {
        updateEventApiIdentifiers();

        for (Map.Entry<String, EventApiDto> apiEntry : dtoResultSet.getMapEventApis().entrySet()) {
            matchToEpEventApiObject(apiEntry.getKey(), apiEntry.getValue());
        }
    }

    private void matchToEpEventApiObject(
        final String eventApiName,
        final EventApiDto apiDto
    ) throws Exception
    {
        final EventApi epEventApi = epClient.getEventApiByName(eventApiName);
        if ( epEventApi == null ) {
            apiDto.setMatchFound(false);
            log.info("Event API [{}] not found in Event Portal", eventApiName);
            return;
        }
        apiDto.setMatchFound(true);
        log.info("Event API [{}] was found to exist", eventApiName);
        final String eventApiId = epEventApi.getId();
        apiDto.setId(eventApiId);
        apiDto.setApplicationDomainId(epClient.getAppDomainId());
        // apiDto.setApplicationType(epEventApi.getType());
        apiDto.setBrokerType(epEventApi.getBrokerType().getValue());

        for ( EventApiVersionDto apiVersionDto : apiDto.getEventApiVersions()) {
            matchToEpEventApiVersion(apiVersionDto, eventApiId);
        }
    }

    private void matchToEpEventApiVersion(
        final EventApiVersionDto apiVersionDto,
        final String eventApiId
    ) throws Exception
    {
        // final ApplicationVersion epAppVersion = epClient.getApplicationVersionByProducedEvents(
        final EventApiVersion epEventApiVersion = epClient.getEventApiVersionByAllEvents(
            eventApiId,
            apiVersionDto.getProducedEventVersionIds(),
            apiVersionDto.getConsumedEventVersionIds()
        );
        if (epEventApiVersion != null) {
            apiVersionDto.setMatchFound(true);
            apiVersionDto.setEpEventApiVersion(epEventApiVersion);
            log.info("Event API version [{}] found in Event Portal", epEventApiVersion.getVersion());
        } else {
            log.info("Matching Event API version not found in Event Portal");
            if (epClient.getCachedLatestEventApiVersion() != null) {
                apiVersionDto.setEpEventApiVersion(epClient.getCachedLatestEventApiVersion());
                apiVersionDto.setLastestVersionFound(true);
                if (apiVersionDto.getEpEventApiVersion().getStateId().contentEquals("1")) {
                    apiVersionDto.setLatestVersionFoundInDraftState(true);
                }
            }
        }
    }

    public void matchEpSchemas() throws Exception
    {
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, SchemaDto> schemaEntry : dtoResultSet.getMapSchemas().entrySet()) {
            futures.add(executor.submit(() -> {
                try {
                    matchToEpSchemaObject(schemaEntry.getKey(), schemaEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to match schema {}", schemaEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void matchToEpSchemaObject(
        final String schemaName, 
        final SchemaDto schemaDto
    ) throws Exception
    {
        final SchemaObject schemaObject = epClient.getSchemaObjectByName(schemaName);
        if (schemaObject == null) {
            schemaDto.setMatchFound(false);
            log.info("Schema Object Name [{}] not found in Event Portal", schemaName);
            return;
        } 
        schemaDto.setMatchFound(true);
        log.info("Schema Object Name [{}] was found to exist", schemaName);
        final String schemaId = schemaObject.getId();
        schemaDto.setId(schemaId);
        schemaDto.setShared(schemaObject.getShared());

        for ( SchemaVersionDto dtoVersion : schemaDto.getSchemaVersions() ) {
            matchToEpSchemaVersion(dtoVersion, schemaId);
        }
    }

    private void matchToEpSchemaVersion(
        final SchemaVersionDto schemaVersionDto,
        final String schemaId
    ) throws Exception
    {
        SchemaVersion sv = epClient.getSchemaVersionByContent(schemaId, schemaVersionDto.getContent());
        if (sv != null) {
            schemaVersionDto.setMatchFound(true);
            schemaVersionDto.setEpSchemaVersion(sv);
            log.info("Schema version [{}] matching import content found to exist", sv.getVersion());
        } else {
            log.info("Schema version matching import content not found in Event Portal");
            if (epClient.getCachedLatestSchemaVersion() != null) {
                schemaVersionDto.setEpSchemaVersion(epClient.getCachedLatestSchemaVersion());
                schemaVersionDto.setLastestVersionFound(true);
                if (epClient.getCachedLatestSchemaVersion().getStateId().contentEquals("1")) {
                    schemaVersionDto.setLatestVersionFoundInDraftState(true);
                }
            }
        }
    }

    public void matchEpEnums() throws Exception
    {
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, EnumDto> enumEntry : dtoResultSet.getMapEnums().entrySet()) {
            futures.add(executor.submit(() -> {
                try {
                    matchToEpEnumObject(enumEntry.getKey(), enumEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to match enum {}", enumEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void matchToEpEnumObject(
        final String enumName, 
        final EnumDto enumDto ) throws Exception
    {
        final TopicAddressEnum topicAddressEnum = epClient.getTopicAddressEnumByName(enumName);
        if (topicAddressEnum == null) {
            enumDto.setMatchFound(false);
            log.info("Enum Object Name [{}] not found in Event Portal", enumName);
            return;
        }
        enumDto.setMatchFound(true);
        log.info("Enum Object Name [{}] was found to exist", enumName);
        final String enumId = topicAddressEnum.getId();
        enumDto.setId(enumId);
        enumDto.setShared(topicAddressEnum.getShared());

        for ( EnumVersionDto dtoEnumVersion : enumDto.getEnumVersions() ) {
            matchToEpEnumVersion(dtoEnumVersion, enumId);
        }
    }

    private void matchToEpEnumVersion(
        final EnumVersionDto enumVersionDto,
        final String enumId
    ) throws Exception
    {
        TopicAddressEnumVersion ev = epClient.getTopicAddressEnumVersionByContent(enumId, enumVersionDto.getValuesAsStringList());
        if (ev != null) {
            enumVersionDto.setMatchFound(true);
            enumVersionDto.setEpEnumVersion(ev);
            log.info("Enum version [{}] matching import content found to exist", ev.getVersion());
        } else {
            log.info("Enum version matching import content not found in Event Portal");
            if (epClient.getCachedLatestEnumVersion() != null) {
                enumVersionDto.setEpEnumVersion(epClient.getCachedLatestEnumVersion());
                enumVersionDto.setLastestVersionFound(true);
                if (epClient.getCachedLatestEnumVersion().getStateId().contentEquals("1")) {
                    enumVersionDto.setLatestVersionFoundInDraftState(true);
                }
            }
        }       
    }

    public void matchEpEvents() throws Exception
    {
        updateEventIdentifiers();   // Must update events + event versions with schemaVersionIds
                                    // and enumVersionIds discovered or created earlier

        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, EventDto> eventEntry : dtoResultSet.getMapEvents().entrySet() ) {
            futures.add(executor.submit(() -> {
                try {
                    matchToEpEventObject(eventEntry.getKey(), eventEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to match event {}", eventEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void updateEventIdentifiers()
    {
        dtoResultSet.getMapEvents().forEach( ( eventName, eventDto ) -> {
            eventDto.getEventVersions().forEach( eventVersion -> {
                final String schemaVersionId = eventVersion.getSchemaVersionDto().getId();
                if (schemaVersionId != null && !schemaVersionId.isBlank()) {
                    eventVersion.setSchemaVersionId(schemaVersionId);
                }
                eventVersion.getDeliveryDescriptor().getAddress().getAddressLevels().forEach( level -> {
                    if ( level.getHasEnum() ) {
                        final String enumVersionId = level.getEnumVersionDto().getId();
                        level.setEnumVersionId(enumVersionId);
                    }
                });
            });
        });
    }

    private void matchToEpEventObject(
        final String eventName,
        final EventDto eventDto
    ) throws Exception
    {
        final Event epEvent = epClient.getEventByName(eventName);
        if (epEvent == null) {
            eventDto.setMatchFound(false);
            log.info("Event Object Name [{}] not found in Event Portal", eventName);
            return;
        }
        eventDto.setMatchFound(true);
        log.info("Event Object Name [{}] was found to exist", eventName);
        final String eventId = epEvent.getId();
        eventDto.setId(eventId);
        eventDto.setShared(epEvent.getShared());

        for ( EventVersionDto eventVersionDto : eventDto.getEventVersions() ) {
            matchToEpEventVersion(eventVersionDto, eventId);
        }
    }

    private void matchToEpEventVersion(
        final EventVersionDto eventVersionDto,
        final String eventId
    ) throws Exception
    {
        final String schemaVersionId = eventVersionDto.getSchemaVersionId();
        final EventVersionDto.DeliveryDescriptor deliveryDescriptor = eventVersionDto.getDeliveryDescriptor();
        EventVersion epEventVersion = epClient.getEventVersionBySchemaIdAndDeliveryDescriptor(eventId, schemaVersionId, deliveryDescriptor);
        if (epEventVersion != null) {
            eventVersionDto.setMatchFound(true);
            eventVersionDto.setEpEventVersion(epEventVersion);
            log.info("Event version [{}] matching import content found to exist", epEventVersion.getVersion());
        } else {
            log.info("Event version matching import content not found in Event Portal");
            if (epClient.getCachedLatestEventVersion() != null) {
                eventVersionDto.setEpEventVersion(epClient.getCachedLatestEventVersion());
                eventVersionDto.setLastestVersionFound(true);
                if (eventVersionDto.getEpEventVersion().getStateId().contentEquals("1")) {
                    eventVersionDto.setLatestVersionFoundInDraftState(true);
                }
            }
        }
    }

    public void importApplications() throws Exception
    {
        for (Map.Entry<String, ApplicationDto> appEntry : dtoResultSet.getMapApplications().entrySet()) {
            importApplicationObject(appEntry.getKey(), appEntry.getValue());
        }
    }

    private void importApplicationObject(
        final String appName,
        final ApplicationDto appDto
    ) throws Exception
    {
        if (!appDto.isMatchFound()) {
            Application epApp = epClient.createApplicationObject(appName);
            log.info("CREATED Application [{}] in Event Portal", appName);
            appDto.setId(epApp.getId());
            appDto.setMatchFound(true);
            appDto.setApplicationType(epApp.getApplicationType());
            appDto.setBrokerType(epApp.getBrokerType().getValue());
        }
        for (ApplicationVersionDto appVersionDto : appDto.getApplicationVersions()) {
            if (appVersionDto.isLastestVersionFound()) {
                appDto.setLatestSemVer(appVersionDto.getEpApplicationVersion().getVersion());
                break;
            }
        }
        for (ApplicationVersionDto appVersionDto : appDto.getApplicationVersions()) {
            appVersionDto.setApplicationId(appDto.getId());
            importApplicationVersion(appVersionDto, appName, appDto);
        }
    }

    private void importApplicationVersion(
        final ApplicationVersionDto appVersionDto,
        final String appName,
        final ApplicationDto appDto
    ) throws Exception
    {
        ApplicationVersion epAppVersion;
        if (appVersionDto.isMatchFound()) {
            epAppVersion = appVersionDto.getEpApplicationVersion();
        } else {
            if (!appVersionDto.isLastestVersionFound() || (appVersionDto.isLastestVersionFound() && !appVersionDto.isLatestVersionFoundInDraftState())) {
                epAppVersion = epClient.createApplicationVersion(
                    appVersionDto.getApplicationId(), 
                    appName, 
                    appVersionDto.getDeclaredProducedEventVersionIds(), 
                    appVersionDto.getDeclaredConsumedEventVersionIds(),
                    appVersionDto.isLastestVersionFound() ? appDto.getLatestSemVer() : null
                );
                appDto.setLatestSemVer(epAppVersion.getVersion());
                log.info("CREATED New Application Version: [{}] v{} in Event Portal", appName, epAppVersion.getVersion());
            } else {
                epAppVersion = epClient.updateApplicationVersion(
                    appVersionDto.getEpApplicationVersion().getId(),
                    appVersionDto.getDeclaredProducedEventVersionIds(),
                    appVersionDto.getDeclaredConsumedEventVersionIds()
                );
                log.info("UPDATED Draft Application Version: [{}] v{} in Event Portal", appName, epAppVersion.getVersion());
            }
            appVersionDto.setMatchFound(true);
        }
        appVersionDto.setId(epAppVersion.getId());
        appVersionDto.setStateId(epAppVersion.getStateId());
        appVersionDto.setVersion(epAppVersion.getVersion());
        appVersionDto.setDescription(epAppVersion.getDescription());
    }

    public void importEventApis() throws Exception
    {
        for (Map.Entry<String, EventApiDto> apiEntry : dtoResultSet.getMapEventApis().entrySet()) {
            importEventApiObject(apiEntry.getKey(), apiEntry.getValue());
        }
    }
    
    private void importEventApiObject(
        final String eventApiName,
        final EventApiDto eventApiDto
    ) throws Exception
    {
        if (!eventApiDto.isMatchFound()) {
            EventApi epEventApi = epClient.createEventApiObject(eventApiName);
            log.info("CREATED Event API [{}] in Event Portal", eventApiName);
            eventApiDto.setId(epEventApi.getId());
            eventApiDto.setMatchFound(true);
            // eventApiDto.setApplicationType(epEventApi.getApplicationType());
            eventApiDto.setBrokerType(epEventApi.getBrokerType().getValue());
        }
        for (EventApiVersionDto eventApiVersionDto : eventApiDto.getEventApiVersions()) {
            if (eventApiVersionDto.isLastestVersionFound()) {
                eventApiDto.setLatestSemVer(eventApiVersionDto.getEpEventApiVersion().getVersion());
                break;
            }
        }
        for (EventApiVersionDto eventApiVersionDto : eventApiDto.getEventApiVersions()) {
            eventApiVersionDto.setEventApiId(eventApiDto.getId());
            importEventApiVersion(eventApiVersionDto, eventApiName, eventApiDto);
        }
    }

    private void importEventApiVersion(
        final EventApiVersionDto eventApiVersionDto,
        final String eventApiName,
        final EventApiDto eventApiDto
    ) throws Exception
    {
        EventApiVersion epEventApiVersion;
        if (eventApiVersionDto.isMatchFound()) {
            epEventApiVersion = eventApiVersionDto.getEpEventApiVersion();
        } else {
            if (!eventApiVersionDto.isLastestVersionFound() || (eventApiVersionDto.isLastestVersionFound() && !eventApiVersionDto.isLatestVersionFoundInDraftState())) {
                epEventApiVersion = epClient.createEventApiVersion(
                    eventApiVersionDto.getEventApiId(), 
                    eventApiName, 
                    eventApiVersionDto.getProducedEventVersionIds(), 
                    eventApiVersionDto.getConsumedEventVersionIds(),
                    eventApiVersionDto.isLastestVersionFound() ? eventApiDto.getLatestSemVer() : null
                );
                eventApiDto.setLatestSemVer(epEventApiVersion.getVersion());
                log.info("CREATED New Event API Version: [{}] v{} in Event Portal", eventApiName, epEventApiVersion.getVersion());
            } else {
                epEventApiVersion = epClient.updateEventApiVersion(
                    eventApiVersionDto.getEpEventApiVersion().getId(),
                    eventApiVersionDto.getProducedEventVersionIds(),
                    eventApiVersionDto.getConsumedEventVersionIds()
                );
                log.info("UPDATED Draft Event API Version: [{}] v{} in Event Portal", eventApiName, epEventApiVersion.getVersion());
            }
            eventApiVersionDto.setMatchFound(true);
        }
        eventApiVersionDto.setId(epEventApiVersion.getId());
        eventApiVersionDto.setStateId(epEventApiVersion.getStateId());
        eventApiVersionDto.setVersion(epEventApiVersion.getVersion());
        eventApiVersionDto.setDescription(epEventApiVersion.getDescription());
    }

    public void importSchemas() throws Exception
    {
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, SchemaDto> schemaEntry : dtoResultSet.getMapSchemas().entrySet()) {
            futures.add(executor.submit(() -> {
                try {
                    importSchemaObject(schemaEntry.getKey(), schemaEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to import schema {}", schemaEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void importSchemaObject(
        final String schemaName,
        final SchemaDto schemaDto
    ) throws Exception
    {
        if (!schemaDto.isMatchFound()) {
            // TODO - Detect schemaType
            SchemaObject schemaObject = epClient.createSchemaObject(schemaName, "jsonSchema");
            log.info("CREATED Schema Object [{}] in Event Portal", schemaName);
            schemaDto.setId(schemaObject.getId());
            schemaDto.setMatchFound(true);
            schemaDto.setShared(schemaObject.getShared());
        }
        for (SchemaVersionDto svDto : schemaDto.getSchemaVersions()) {
            if (svDto.isLastestVersionFound()) {
                schemaDto.setLatestSemVer(svDto.getEpSchemaVersion().getVersion());
                break;
            }
        }
        for (SchemaVersionDto svDto : schemaDto.getSchemaVersions()) {
            svDto.setSchemaId(schemaDto.getId());
            importSchemaVersion(svDto, schemaName, schemaDto);
        };
    }

    private void importSchemaVersion(
        final SchemaVersionDto svDto,
        final String schemaName,
        final SchemaDto schemaDto
    ) throws Exception
    {
        SchemaVersion sv;
        if (svDto.isMatchFound()) {
            sv = svDto.getEpSchemaVersion();
        } else {
            if (!svDto.isLastestVersionFound() || (svDto.isLastestVersionFound() && ! svDto.isLatestVersionFoundInDraftState())) {
                sv = epClient.createSchemaVersion(
                    svDto.getSchemaId(), 
                    svDto.getContent(), 
                    schemaName, 
                    svDto.isLastestVersionFound() ? schemaDto.getLatestSemVer() : null
                );
                schemaDto.setLatestSemVer(sv.getVersion());
                // For cascade object creation
                // If creating new schemaVersion to update an existing version, then
                // store the old and new version for retrieval
                if ( svDto.isLastestVersionFound() && ! svDto.isLatestVersionFoundInDraftState() ) {
                    incrementedSchemaVersions.put( svDto.getEpSchemaVersion().getId(), sv.getId() );
                }
                log.info("CREATED Schema Version: [{}] v{} in Event Portal", schemaName, sv.getVersion());
            } else {
                sv = epClient.updateSchemaVersion(
                    svDto.getEpSchemaVersion().getId(), 
                    svDto.getContent()
                );
                log.info("UPDATED Draft Schema Version: [{}] v{} in Event Portal", schemaName, sv.getVersion());
            }
            svDto.setMatchFound(true);
        }
        svDto.setId(sv.getId());
        svDto.setSchemaId(sv.getSchemaId());
        svDto.setStateId(sv.getStateId());
        svDto.setVersion(sv.getVersion());
    }

    public void importEnums() throws Exception
    {
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, EnumDto> enumEntry : dtoResultSet.getMapEnums().entrySet()) {
            futures.add(executor.submit(() -> {
                try {
                    importEnumObject(enumEntry.getKey(), enumEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to import enum {}", enumEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void importEnumObject(
        final String enumName,
        final EnumDto enumDto
    ) throws Exception
    {
        if (!enumDto.isMatchFound()) {
            TopicAddressEnum topicAddressEnum = epClient.createEnumObject(enumName);
            log.info("CREATED Enum Object [{}] in Event Portal", enumName);
            enumDto.setId(topicAddressEnum.getId());
            enumDto.setMatchFound(true);
            enumDto.setShared(topicAddressEnum.getShared());
        }

        for (EnumVersionDto evDto : enumDto.getEnumVersions()) {
            if (evDto.isLastestVersionFound()) {
                enumDto.setLatestSemVer(evDto.getEpEnumVersion().getVersion());
                break;
            }
        }

        for (EnumVersionDto evDto : enumDto.getEnumVersions()) {
            evDto.setEnumId(enumDto.getId());
            importEnumVersion(evDto, enumName, enumDto);
        }
    }

    private void importEnumVersion(
        final EnumVersionDto evDto,
        final String enumName,
        final EnumDto enumDto
    ) throws Exception
    {
        TopicAddressEnumVersion ev;
        if (evDto.isMatchFound()) {
            ev = evDto.getEpEnumVersion();
        } else {
            if (!evDto.isLastestVersionFound() || (evDto.isLastestVersionFound() && !evDto.isLatestVersionFoundInDraftState())) {

                ev = epClient.createEnumVersion(
                    evDto.getEnumId(), 
                    evDto.getValuesAsStringList(), 
                    enumName, 
                    evDto.isLastestVersionFound() ? enumDto.getLatestSemVer() : null
                );
                enumDto.setLatestSemVer(ev.getVersion());

                // For cascade object creation
                // If creating new enumVersion to update an existing version, then
                // store the old and new version for retrieval
                if ( evDto.isLastestVersionFound() && ! evDto.isLatestVersionFoundInDraftState() ) {
                    incrementedEnumVersions.put( evDto.getEpEnumVersion().getId(), ev.getId() );
                }
                log.info("CREATED Enum Version: [{}] v{} in Event Portal", enumName, ev.getVersion());

            } else {
                ev = epClient.updateEnumVersion(
                    evDto.getEpEnumVersion().getId(),
                    evDto.getValuesAsStringList()
                );
                log.info("UPDATED Draft Enum Version: [{}] v{} in Event Portal", enumName, ev.getVersion());
            }
            evDto.setMatchFound(true);
        }
        evDto.setId(ev.getId());
        evDto.setEnumId(ev.getEnumId());
        evDto.setStateId(ev.getStateId());
        evDto.setVersion(ev.getVersion());
    }

    public void importEvents() throws Exception
    {
        List<Future<?>> futures = new ArrayList<>();
        for (Map.Entry<String, EventDto> eventEntry : dtoResultSet.getMapEvents().entrySet()) {
            futures.add(executor.submit(() -> {
                try {
                    importEventObject(eventEntry.getKey(), eventEntry.getValue());
                } catch (Exception e) {
                    log.error("Failed to import event {}", eventEntry.getKey(), e);
                }
            }));
        }

        for (Future<?> future : futures) {
            future.get(); // wait for each task to complete
        }
    }

    private void importEventObject(
        final String eventName,
        final EventDto eventDto
    ) throws Exception
    {
        if (!eventDto.isMatchFound()) {
            Event epEvent = epClient.createEventObject(eventName);
            log.info("CREATED Event Object [{}] in Event Portal", eventName);
            eventDto.setId(epEvent.getId());
            eventDto.setMatchFound(true);
            eventDto.setShared(epEvent.getShared());
        }
        for (EventVersionDto eventVersionDto : eventDto.getEventVersions()) {
            if (eventVersionDto.isLastestVersionFound()) {
                // eventDto.setLatestSemVer(eventVersionDto.getEpEventVersion().getVersion());
                eventDto.setLatestSemVer(eventVersionDto.getEpEventVersion().getVersion());
                break;
            }
        }
        for (EventVersionDto eventVersionDto : eventDto.getEventVersions()) {
            eventVersionDto.setEventId(eventDto.getId());
            importEventVersion(eventVersionDto, eventName, eventDto);
        }
    }

    private void importEventVersion(
        final EventVersionDto eventVersionDto,
        final String eventName,
        final EventDto eventDto
    ) throws Exception
    {
        EventVersion epEventVersion;
        if (eventVersionDto.isMatchFound()) {
            epEventVersion = eventVersionDto.getEpEventVersion();
        } else {
            if (!eventVersionDto.isLastestVersionFound() || (eventVersionDto.isLastestVersionFound() && !eventVersionDto.isLatestVersionFoundInDraftState())) {
                epEventVersion = epClient.createEventVersion(
                    eventVersionDto.getEventId(),
                    eventName,
                    eventVersionDto.getSchemaVersionId(),
                    eventVersionDto.getDeliveryDescriptor(),
                    eventVersionDto.isLastestVersionFound() ? eventDto.getLatestSemVer() : null
                );
                eventDto.setLatestSemVer(epEventVersion.getVersion());
                // For cascade object creation
                // If creating new event version to update an existing version, then
                // store the old and new version for retrieval
                if ( eventVersionDto.isLastestVersionFound() && ! eventVersionDto.isLatestVersionFoundInDraftState() ) {
                    incrementedEventVersions.put( eventVersionDto.getEpEventVersion().getId(), epEventVersion.getId() );
                }
                log.info("CREATED Event Version: [{}] v{} in Event Portal", eventName, epEventVersion.getVersion());
            } else {
                epEventVersion = epClient.updateEventVersion(
                    eventVersionDto.getEpEventVersion().getId(),
                    eventVersionDto.getSchemaVersionId(),
                    eventVersionDto.getDeliveryDescriptor()
                );
                log.info("UPDATED Draft Event Version: [{}] v{} in Event Portal", eventName, epEventVersion.getVersion());
            }
            eventVersionDto.setMatchFound(true);
        }
        eventVersionDto.setId(epEventVersion.getId());
        eventVersionDto.setEventId(epEventVersion.getEventId());
        eventVersionDto.setStateId(epEventVersion.getStateId());
        eventVersionDto.setVersion(epEventVersion.getVersion());
    }

    public void cascadeUpdateEvents() throws Exception
    {
        final Map<String, String> eventIds = epClient.getAllEventIds();
        final List<EventVersion> latestEventVersions = Collections.synchronizedList(new ArrayList<>());

        List<Future<?>> lookupVersionFutures = new ArrayList<>();
        for (Map.Entry<String, String> entry : eventIds.entrySet()) {
            lookupVersionFutures.add(executor.submit(() -> {
                try {
                    final EventVersion eventVersion = epClient.getLastEventVersion(entry.getKey());
                    if (eventVersion != null && ! incrementedEventVersions.containsKey(eventVersion.getId())) {
                        latestEventVersions.add(eventVersion);
                    }
                } catch (Exception e) {
                    log.error("Failed to get last event version for event {}", entry.getValue(), e);
                }
            }));
        }

        for (Future<?> future : lookupVersionFutures) {
            future.get(); // wait for each task to complete
        }

        List<Future<?>> updateVersionFutures = new ArrayList<>();
        for (EventVersion ev : latestEventVersions) {
            updateVersionFutures.add(executor.submit(() -> {
                try {
                    boolean changeFound = false;
                    EventVersionDto.DeliveryDescriptor ddDto = EventPortalModelUtils.mapEpEventVersionToDtoDeliveryDescriptor(ev.getDeliveryDescriptor());
                    String schemaVersionId = ev.getSchemaVersionId();
                    final String semVer = ev.getVersion();
                    if ( incrementedSchemaVersions.containsKey(schemaVersionId) ) {
                        changeFound = true;
                        schemaVersionId = incrementedSchemaVersions.get(schemaVersionId);
                    }
                    for ( EventVersionDto.TopicAddressLevel level : ddDto.getAddress().getAddressLevels() ) {
                        if (level.getHasEnum() && incrementedEnumVersions.containsKey(level.getEnumVersionId())) {
                            changeFound = true;
                            level.setEnumVersionId(incrementedEnumVersions.get(level.getEnumVersionId()));
                        }
                    }
                    if (changeFound) {
                        EventVersion updatedEventVersion;
                        String op = "";
                        if (ev.getStateId().contentEquals("1")) {
                            updatedEventVersion = epClient.updateEventVersion(
                                ev.getId(), 
                                schemaVersionId, 
                                ddDto
                            );
                            op = "UPDATED existing";
                        } else {
                            updatedEventVersion = epClient.createEventVersion(
                                ev.getEventId(), eventIds.get(ev.getEventId()), schemaVersionId, ddDto, semVer);
                            if (updatedEventVersion != null) {
                                incrementedEventVersions.put( ev.getId(), updatedEventVersion.getId() );
                            }
                            op = "CREATED new";
                        }
                        log.info(
                            "CASCADE UPDATE -- {} event version [{}] for Event: [{}]",
                            op,
                            updatedEventVersion.getVersion(),
                            eventIds.get(ev.getEventId())
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to cascade update event {}", ev.getId(), e);
                }
            }));
        }

        for (Future<?> future : updateVersionFutures) {
            future.get(); // wait for each task to complete
        }
    }

    public void cascadeUpdateEventApis() throws Exception
    {
        final Map<String, String> eventApiIds = epClient.getAllEventApiIds();
        final List<EventApiVersion> latestEventApiVersions = new ArrayList<>();
        String thisEventApiVersionId = "";

        for (Map.Entry<String, EventApiDto> entry : dtoResultSet.getMapEventApis().entrySet()) {
            EventApiVersionDto eventApiVersionDto = entry.getValue().getEventApiVersions().get(0);
            if (eventApiVersionDto != null) {
                thisEventApiVersionId = eventApiVersionDto.getId();
            }
            break;
        }

        for (Map.Entry<String, String> entry : eventApiIds.entrySet()) {
            final EventApiVersion eventApiVersion = epClient.getLastEventApiVersion(entry.getKey());
            if (eventApiVersion != null && ! thisEventApiVersionId.contentEquals(eventApiVersion.getId())) {
                latestEventApiVersions.add(eventApiVersion);
            }
        }

        for (EventApiVersion eav : latestEventApiVersions) {
            boolean changeFound = false;
            final List<String> eavProducedEventVersions = eav.getProducedEventVersionIds();
            final List<String> eavConsumedEventVersions = eav.getConsumedEventVersionIds();
            final String semVer = eav.getVersion();
            final List<String> updatedEavProducedEventVersions = new ArrayList<>();
            final List<String> updatedEavConsumedEventVersions = new ArrayList<>();
            for (String eavEventVersion : eavProducedEventVersions) {
                if (incrementedEventVersions.containsKey(eavEventVersion)) {
                    changeFound = true;
                    updatedEavProducedEventVersions.add(incrementedEventVersions.get(eavEventVersion));
                } else {
                    updatedEavProducedEventVersions.add(eavEventVersion);
                }
            }
            for (String eavEventVersion : eavConsumedEventVersions) {
                if (incrementedEventVersions.containsKey(eavEventVersion)) {
                    changeFound = true;
                    updatedEavConsumedEventVersions.add(incrementedEventVersions.get(eavEventVersion));
                } else {
                    updatedEavConsumedEventVersions.add(eavEventVersion);
                }
            }
            if (changeFound) {
                EventApiVersion updatedEventApiVersion;
                String op = "";
                if (eav.getStateId().contentEquals("1")) {
                    updatedEventApiVersion = epClient.updateEventApiVersion(
                        eav.getId(), updatedEavProducedEventVersions, updatedEavConsumedEventVersions
                    );
                    op = "UPDATED existing";
                } else {
                    updatedEventApiVersion = epClient.createEventApiVersion(
                        eav.getEventApiId(),
                        eventApiIds.get(eav.getEventApiId()),
                        updatedEavProducedEventVersions, updatedEavConsumedEventVersions,
                        semVer
                    );
                    op = "CREATED new";
                }
                log.info(
                    "CASCADE UPDATE -- {} event api version [{}] for Event API: [{}]", 
                    op, 
                    updatedEventApiVersion.getVersion(), 
                    eventApiIds.get(eav.getEventApiId())
                );
            }
        }
    }

    /**
     * This method will update existing application versions (stateId == "1") or
     * Create new application versions (stateId > 1)
     * of the last application version if any eventId referenced by the app version
     * was updated as a result of the import operation.
     * @throws Exception
     */
    public void cascadeUpdateApplications() throws Exception
    {
        final Map<String, String> applicationIds = epClient.getAllApplicationIds();
        final List<ApplicationVersion> latestApplicationVersions = new ArrayList<>();
        String thisApplicationVersionId = "";
        // String thisApplicationName = "UNKNOWN";

        for (Map.Entry<String, ApplicationDto> entry : dtoResultSet.getMapApplications().entrySet()) {
            // thisApplicationName = entry.getKey();
            ApplicationVersionDto appVersionDto = entry.getValue().getApplicationVersions().get(0);
            if (appVersionDto != null) {
                thisApplicationVersionId = appVersionDto.getId();
            }
            break;
        }

        for (Map.Entry<String, String> entry : applicationIds.entrySet()) {
            final ApplicationVersion applicationVersion = epClient.getLastApplicationVersion(entry.getKey());
            if (applicationVersion != null && ! thisApplicationVersionId.contentEquals(applicationVersion.getId())) {
                latestApplicationVersions.add(applicationVersion);
            }
        }

        for (ApplicationVersion av : latestApplicationVersions) {
            boolean changeFound = false;
            final List<String> avProducedEventVersions = av.getDeclaredProducedEventVersionIds();
            final List<String> avConsumedEventVersions = av.getDeclaredConsumedEventVersionIds();
            final String semVer = av.getVersion();
            final List<String> updatedAvProducedEventVersions = new ArrayList<>();
            final List<String> updatedAvConsumedEventVersions = new ArrayList<>();
            for (String avEventVersion : avProducedEventVersions) {
                if (incrementedEventVersions.containsKey(avEventVersion)) {
                    changeFound = true;
                    updatedAvProducedEventVersions.add(incrementedEventVersions.get(avEventVersion));
                } else {
                    updatedAvProducedEventVersions.add(avEventVersion);
                }
            }
            for (String avEventVersion : avConsumedEventVersions) {
                if (incrementedEventVersions.containsKey(avEventVersion)) {
                    changeFound = true;
                    updatedAvConsumedEventVersions.add(incrementedEventVersions.get(avEventVersion));
                } else {
                    updatedAvConsumedEventVersions.add(avEventVersion);
                }
            }
            if (changeFound) {
                ApplicationVersion updatedApplicationVersion;
                String op = "";
                if (av.getStateId().contentEquals("1")) {
                    updatedApplicationVersion = epClient.updateApplicationVersion(
                        av.getId(), updatedAvProducedEventVersions, updatedAvConsumedEventVersions
                    );
                    op = "UPDATED existing";
                } else {
                    updatedApplicationVersion = epClient.createApplicationVersion(
                        av.getApplicationId(),
                        applicationIds.get(av.getApplicationId()),
                        updatedAvProducedEventVersions, updatedAvConsumedEventVersions,
                        semVer
                    );
                    op = "CREATED new";
                }
                log.info(
                    "CASCADE UPDATE -- {} application version [{}] for Application: [{}]", 
                    op, 
                    updatedApplicationVersion.getVersion(), 
                    applicationIds.get(av.getApplicationId())
                );
            }
        }
    }
}
