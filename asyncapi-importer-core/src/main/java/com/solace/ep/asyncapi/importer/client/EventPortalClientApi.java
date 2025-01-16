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

package com.solace.ep.asyncapi.importer.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.solace.cloud.ep.designer.ApiClient;
import com.solace.cloud.ep.designer.api.ApplicationDomainsApi;
import com.solace.cloud.ep.designer.api.ApplicationsApi;
import com.solace.cloud.ep.designer.api.EnumsApi;
import com.solace.cloud.ep.designer.api.EventsApi;
import com.solace.cloud.ep.designer.api.SchemasApi;
import com.solace.cloud.ep.designer.auth.HttpBearerAuth;
import com.solace.cloud.ep.designer.model.Application;
import com.solace.cloud.ep.designer.model.ApplicationDomain;
import com.solace.cloud.ep.designer.model.ApplicationDomainResponse;
import com.solace.cloud.ep.designer.model.ApplicationDomainsResponse;
import com.solace.cloud.ep.designer.model.ApplicationResponse;
import com.solace.cloud.ep.designer.model.ApplicationVersion;
import com.solace.cloud.ep.designer.model.ApplicationVersionResponse;
import com.solace.cloud.ep.designer.model.ApplicationVersionsResponse;
import com.solace.cloud.ep.designer.model.ApplicationsResponse;
import com.solace.cloud.ep.designer.model.DeliveryDescriptor;
import com.solace.cloud.ep.designer.model.Event;
import com.solace.cloud.ep.designer.model.EventResponse;
import com.solace.cloud.ep.designer.model.EventVersion;
import com.solace.cloud.ep.designer.model.EventVersionResponse;
import com.solace.cloud.ep.designer.model.EventVersionsResponse;
import com.solace.cloud.ep.designer.model.EventsResponse;
import com.solace.cloud.ep.designer.model.SchemaObject;
import com.solace.cloud.ep.designer.model.SchemaResponse;
import com.solace.cloud.ep.designer.model.SchemaVersion;
import com.solace.cloud.ep.designer.model.SchemaVersionResponse;
import com.solace.cloud.ep.designer.model.SchemaVersionsResponse;
import com.solace.cloud.ep.designer.model.SchemasResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnum;
import com.solace.cloud.ep.designer.model.TopicAddressEnumResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnumValue;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersionResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnumVersionsResponse;
import com.solace.cloud.ep.designer.model.TopicAddressEnumsResponse;
import com.solace.cloud.ep.designer.model.Application.BrokerTypeEnum;
import com.solace.ep.asyncapi.importer.EpNewVersionStrategy;
import com.solace.ep.asyncapi.importer.model.dto.EventVersionDto;
import com.solace.ep.asyncapi.importer.util.EventPortalModelUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventPortalClientApi {

    private static final String DEFAULT_BASE_URL_PATH = "https://api.solace.cloud";

    private static final int    PAGE_SZ_OBJECT = 10,
                                PAGE_SZ_VERSION = 20;

    private String baseUrlPath;

    private ApiClient apiClient;

    private String bearerToken;

    private EpNewVersionStrategy versionStrategy;

    @Getter
    @Setter
    private String appDomainName;

    @Getter
    private String appDomainId;

    @Getter
    private ApplicationDomain applicationDomain;

    @Getter
    private SchemaVersion cachedLatestSchemaVersion;

    @Getter
    private TopicAddressEnumVersion cachedLatestEnumVersion;

    @Getter
    private EventVersion cachedLatestEventVersion;

    @Getter
    private ApplicationVersion cachedLatestApplicationVersion;

    /**
     * Constructs a new EventPortalApiClientGet Object and retrieves ApplicationDomain for appDomain name parameter passed
     * Assumes that the Solace Cloud URL = https://api.solace.cloud. Use alternate constructor to specify Solace Cloud URL
     * @param bearerToken - EP Bearer token, should have full READ priviliges to Event Portal
     * @param appDomainName - Target application domain name
     * @param versionStrategy - Increment MAJOR, MINOR, or PATCH version when creating new versions
     * @throws Exception
     */
    public EventPortalClientApi(
        final String bearerToken, 
        final String appDomainName,
        final EpNewVersionStrategy versionStrategy
    ) throws Exception
    {
        this(bearerToken, appDomainName, versionStrategy, DEFAULT_BASE_URL_PATH);
    }

    /**
     * Constructs a new EventPortalApiClientGet Object and retrieves ApplicationDomain for appDomain name parameter passed
     * @param bearerToken - EP Bearer token, should have full READ priviliges to Event Portal
     * @param appDomainName - Target application domain name
     * @param versionStrategy - Increment MAJOR, MINOR, or PATCH version when creating new versions
     * @param baseUrlPath - Specify Solace Cloud API path (outside of the US/CAN region)
     * @throws Exception
     */
    public EventPortalClientApi(
        final String bearerToken, 
        final String appDomainName, 
        final EpNewVersionStrategy versionStrategy,
        final String baseUrlPath
    ) throws Exception 
    {
        this.bearerToken = bearerToken;
        this.appDomainName = appDomainName;
        this.baseUrlPath = baseUrlPath == null || baseUrlPath.isBlank() ? DEFAULT_BASE_URL_PATH : baseUrlPath;
        this.versionStrategy = versionStrategy == null ? EpNewVersionStrategy.MAJOR : versionStrategy;
        this.apiClient = getApiClient();
        ApplicationDomain localAppDomain = getApplicationDomainByName( appDomainName );
        this.applicationDomain = localAppDomain;
        this.appDomainId = localAppDomain.getId();
    }

    public EventPortalClientApi( 
        final ApiClient apiClient, 
        final String appDomainName,
        final EpNewVersionStrategy versionStrategy
    ) throws Exception
    {
        this.apiClient = apiClient;
        this.appDomainName = appDomainName;
        ApplicationDomain localAppDomain = getApplicationDomainByName(appDomainName);
        this.applicationDomain = localAppDomain;
        this.appDomainId = localAppDomain.getId();
        this.versionStrategy = versionStrategy == null ? EpNewVersionStrategy.MAJOR : versionStrategy;
    }

    /**
     * Start from Application Domain ID instead of Application Domain Name
     * @param bearerToken
     * @param versionStrategy
     * @param baseUrlPath
     * @param appDomainId
     * @throws Exception
     */
    public EventPortalClientApi(
        final String bearerToken, 
        final EpNewVersionStrategy versionStrategy,
        final String baseUrlPath,
        final String appDomainId
    ) throws Exception 
    {
        this.bearerToken = bearerToken;
        this.appDomainId = appDomainId;
        this.baseUrlPath = baseUrlPath == null || baseUrlPath.isBlank() ? DEFAULT_BASE_URL_PATH : baseUrlPath;
        this.versionStrategy = versionStrategy == null ? EpNewVersionStrategy.MAJOR : versionStrategy;
        this.apiClient = getApiClient();
        ApplicationDomain localAppDomain = getApplicationDomainById( appDomainId );
        this.applicationDomain = localAppDomain;
        this.appDomainName = localAppDomain.getName();
    }

    public EventPortalClientApi( 
        final ApiClient apiClient, 
        final EpNewVersionStrategy versionStrategy,
        final String appDomainId
    ) throws Exception
    {
        this.apiClient = apiClient;
        this.appDomainId = appDomainId;
        ApplicationDomain localAppDomain = getApplicationDomainById(appDomainId);
        this.applicationDomain = localAppDomain;
        this.appDomainName = localAppDomain.getName();
        this.versionStrategy = versionStrategy == null ? EpNewVersionStrategy.MAJOR : versionStrategy;
    }

    /**
     * Returns ApiClient object used by the instance of EventPortalClientApi
     * @return ApiClient object
     */
    public ApiClient getApiClient() 
    {
        if ( this.apiClient == null )
        {
            ApiClient localApiClient = new ApiClient();
            localApiClient.setBasePath(this.baseUrlPath);
            HttpBearerAuth apiToken = (HttpBearerAuth)localApiClient.getAuthentication("APIToken");
            apiToken.setBearerToken(this.bearerToken);
            this.apiClient = localApiClient;
        }
        return this.apiClient;
    }

    /**
     * Get the Application Domain by the ID (Key)
     * Calls Solace Cloud API
     * @param appDomainId
     * @return
     * @throws Exception
     */
    public ApplicationDomain getApplicationDomainById(final String appDomainId) throws Exception
    {
        ApplicationDomainsApi applicationDomainsApi = new ApplicationDomainsApi(apiClient);
        try {
            ApplicationDomainResponse applicationDomainResponse = applicationDomainsApi.getApplicationDomain(appDomainId, null);
            if (applicationDomainResponse.getData() != null) {
                return applicationDomainResponse.getData();
            } else {
                log.error("Application domain ID = [{}] not found", appDomainId);
                throw new Exception("Application domain ID = [" + appDomainId + "] not found");
            }
        } catch (Exception exc) {
            log.error("Error encountered in EventPortalClientApi.getApplicationDomainById", exc);
            throw exc;
        }
    }

    /**
     * Get the ApplicationDomain associated with the appDomainName. Must be exact match.
     * Calls Event Portal REST API
     * @param appDomainName
     * @return ApplicationDomain object
     * @throws Exception
     */
    public ApplicationDomain getApplicationDomainByName(final String appDomainName) throws Exception 
    {
        ApplicationDomainsApi applicationDomainsApi = new ApplicationDomainsApi(apiClient);
        
        try {
            ApplicationDomainsResponse applicationDomainsResponse = applicationDomainsApi.getApplicationDomains(PAGE_SZ_OBJECT, 1, appDomainName, null, null);

            if ( applicationDomainsResponse.getData().isEmpty()) {
                log.error("Application domain name = [{}] not found", appDomainName);
                throw new Exception("Application domain name = [" + appDomainName + "] not found");
            }
            if ( applicationDomainsResponse.getData().size() > 1 ) {
                // Shouldn't happen
                log.error("Application domain name = [{}] returned > 1 app domains", appDomainName);
                return null;
            }

            return applicationDomainsResponse.getData().get(0);

        } catch (Exception exc) {
            log.error("Error encountered in EventPortalClientApi.getApplicationDomainByName", exc);
            throw exc;
        }
    }

    public Application getApplicationByName(final String appName) throws Exception
    {
        ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);

        try {
            ApplicationsResponse applicationsResponse = applicationsApi.getApplications(PAGE_SZ_OBJECT, 1, appName, appDomainId, null, null, null, null );
            if (applicationsResponse.getData().isEmpty()) {
                log.debug("Application name = [{}] not found", appName);
                return null;
            }
            if (applicationsResponse.getData().size() > 1) {
                // Should not happen
                log.error("Application name = [{}] returned > 1 applications", appName);
                return null;
            }
            return applicationsResponse.getData().get(0);

        } catch (Exception exc) {
            log.error("Error retrieving application [{}] EventPortalClientApi.getApplicationByName", appName, exc);
            throw exc;
        }
    }

    /**
     * Lookup a version of an application by application ID and matching a list of
     * Event IDs produced by the application version.
     * Calls Event Portal REST API
     * @param appId
     * @param producedEventVersionIds
     * @return ApplicationVersion object
     * @throws Exception
     */
    public ApplicationVersion getApplicationVersionByProducedEvents(
        final String appId,
        final List<String> producedEventVersionIds
    ) throws Exception
    {
        ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);

        this.cachedLatestApplicationVersion = null;

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++) {
                ApplicationVersionsResponse response = applicationsApi.getApplicationVersions(PAGE_SZ_VERSION, page, Set.of(appId), null, null, null);  // EventPortalModelUtils.OPEN_OBJECT_STATES);

                if (response.getData().isEmpty()) {
                    break;
                }
                if (page == 1 && response.getMeta().getPagination().getNextPage() != null) {
                    maxPages = response.getMeta().getPagination().getTotalPages();
                }
                if (page == 1 && ! response.getData().isEmpty() ) {
                    this.cachedLatestApplicationVersion = response.getData().get(0);
                }

                for (ApplicationVersion appVersion : response.getData()) {
                    if (EventPortalModelUtils.valuesListsMatch(producedEventVersionIds, appVersion.getDeclaredProducedEventVersionIds())) {
                        return appVersion;
                    }
                }
            }
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getApplicationVersionByProducedEvents", exc);
            throw exc;
        }

        return null;
    }

    /**
     * Gets a map of all Event IDs --> Event Names in the application domain associated with this client instance.
     * Calls Event Portal REST API
     * @return
     * @throws Exception
     */
    public Map<String, String> getAllEventIds() throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);
        final Map<String, String> eventIds = new HashMap<>();

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++) {
                EventsResponse response = eventsApi.getEvents(PAGE_SZ_OBJECT, page, null, null, "solace", appDomainId, null, null, null, null);

                if (response.getData().isEmpty()) {
                    break;
                }
                if (page == 1 && response.getMeta().getPagination().getNextPage() != null) {
                    maxPages = response.getMeta().getPagination().getTotalPages();
                }
                response.getData().forEach( event -> {
                    eventIds.put(event.getId(), event.getName());
                } );
            }
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getAllEventIds", exc);
            throw exc;
        }
        return eventIds;
    }

    /**
     * Retrieve the last EventVersion object associated with an eventId
     * Calls Event Portal REST API
     * @param eventId
     * @return
     * @throws Exception
     */
    public EventVersion getLastEventVersion(
        final String eventId
    ) throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);

        try {
            EventVersionsResponse response = eventsApi.getEventVersions(1, 1, Set.of(eventId), null, null, null, null);
            if (response.getData().isEmpty()) {
                return null;
            } else {
                return response.getData().get(0);
            }
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getLastEventVersion", exc);
            throw exc;
        }
    }

    /**
     * Gets a map of all Application IDs --> Application Names in the application domain associated with this client instance.
     * Calls Event Portal REST API
     * @return
     * @throws Exception
     */
    public Map<String, String> getAllApplicationIds() throws Exception
    {
        final ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);
        final Map<String, String> appIds = new HashMap<>();

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++) {
                ApplicationsResponse response = applicationsApi.getApplications(PAGE_SZ_OBJECT, page, null, appDomainId, null, null, null, null);

                if (response.getData().isEmpty()) {
                    break;
                }
                if (page == 1 && response.getMeta().getPagination().getNextPage() != null) {
                    maxPages = response.getMeta().getPagination().getTotalPages();
                }
                response.getData().forEach( app -> {
                    appIds.put(app.getId(), app.getName());
                } );
            }
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getAllApplicationIds", exc);
            throw exc;
        }
        return appIds;
    }

    /**
     * Retrieve the last ApplicationVersion object associated with an applicationId
     * Calls Event Portal REST API
     * @param applicationId
     * @return
     * @throws Exception
     */
    public ApplicationVersion getLastApplicationVersion(
        final String applicationId
    ) throws Exception
    {
        final ApplicationsApi applicationsApi = new ApplicationsApi(apiClient);

        try {
            ApplicationVersionsResponse response = applicationsApi.getApplicationVersions(1, 1, Set.of(applicationId), null, null, null);
            if (response.getData().isEmpty()) {
                return null;
            } else {
                return response.getData().get(0);
            }
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getLastApplicationVersion", exc);
            throw exc;
        }
    }

    /**
     * Get the Event Portal Event Object associated with the eventName. Event name must be an exact match.
     * Calls Event Portal REST API
     * @param eventName
     * @return
     * @throws Exception
     */
    public Event getEventByName(final String eventName) throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);

        try {
            EventsResponse response = eventsApi.getEvents(PAGE_SZ_OBJECT, 1, eventName, null, null, appDomainId, null, null, null, null);
            if (response.getData().isEmpty()) {
                return null;
            }
            if (response.getData().size() > 1) {
                throw new Exception("Returned > 1 Events for eventName=" + eventName);
            }
            return response.getData().get(0);
        } catch (Exception exc) {
            log.error("EventPortalClientApi.getEventByName", exc);
            throw exc;
        }
    }

    public EventVersion getEventVersionBySchemaIdAndDeliveryDescriptor(
        final String eventId,
        final String schemaVersionId,
        final DeliveryDescriptor modelDeliveryDescriptor
    ) throws Exception
    {
        final EventVersionDto.DeliveryDescriptor ddDto = 
                EventPortalModelUtils.mapEpEventVersionToDtoDeliveryDescriptor(modelDeliveryDescriptor);
        return getEventVersionBySchemaIdAndDeliveryDescriptor(eventId, schemaVersionId, ddDto);
    }

    /**
     * Find an EventVersion object for an Event object (by eventId).
     * The event version must match the schemaVersionId and the delivery descriptor passed.
     * Calls Event Portal REST API
     * @param eventId - Event ID to search for event version
     * @param schemaVersionId
     * @param deliveryDescriptor - contains mapped topic with literal/variable attribute and
     * matching Enum IDs
     * @return
     * @throws Exception
     */
    public EventVersion getEventVersionBySchemaIdAndDeliveryDescriptor(
        final String eventId,
        final String schemaVersionId,
        final EventVersionDto.DeliveryDescriptor deliveryDescriptor
    ) throws Exception
    {
        EventsApi eventsApi = new EventsApi(apiClient);

        this.cachedLatestEventVersion = null;

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++) {
                EventVersionsResponse response = eventsApi.getEventVersions(PAGE_SZ_VERSION, page, Set.of(eventId), null, null, null, null);

                if (response.getData().isEmpty()) {
                    break;
                }
                if (page == 1 && response.getMeta().getPagination().getNextPage() != null) {
                    maxPages = response.getMeta().getPagination().getTotalPages();
                }
                if (page == 1 && ! response.getData().isEmpty() ) {
                    this.cachedLatestEventVersion = response.getData().get(0);
                }

                for ( EventVersion eventVersion : response.getData() ) {
                    if ( eventVersion.getEventId().contentEquals(eventId) &&
                        eventVersion.getSchemaVersionId().contentEquals(schemaVersionId) )
                    {
                        EventVersionDto.DeliveryDescriptor responseDeliveryDescriptor = EventPortalModelUtils.mapEpEventVersionToDtoDeliveryDescriptor(eventVersion.getDeliveryDescriptor());
                        if ( EventPortalModelUtils.deliveryDescriptorsMatch(responseDeliveryDescriptor, deliveryDescriptor)) {
                            return eventVersion;
                        }
                    }
                }
            }

        } catch (Exception exc) {
            log.error("EventPortalClientApi.getEventVersionBySchemaIdAndDeliveryDescriptor", exc);
            throw exc;
        }
        return null;
    }

    /**
     * Lookup schema object in application domain by schemaName. schemaName must be exact match.
     * Calls Event Portal REST API
     * @param schemaName
     * @return
     * @throws Exception
     */
    public SchemaObject getSchemaObjectByName(final String schemaName) throws Exception {
        SchemasApi schemasApi = new SchemasApi(apiClient);

        try {
            SchemasResponse schemasResponse = schemasApi.getSchemas(PAGE_SZ_OBJECT, 1, schemaName, null, appDomainId, null, null, null, null, null);
            if (schemasResponse.getData().isEmpty()) {
                // TODO - not an error
                log.debug("Schema name = [{}] not found in appDomainId = [{}]", schemaName, appDomainId);
                return null;
            }
            if (schemasResponse.getData().size() > 1) {
                // TODO - error condition
                throw new Exception("Returned > 1 Schemas for schemaName=" + schemaName);
            }

            return schemasResponse.getData().get(0);
        } catch (Exception exc) { 
            log.error("Error encountered in EventPortalClientApi.getSchemaObjectdByName", exc);
            throw exc;
        }
    }

    public SchemaVersion getSchemaVersionByContent(final String schemaId, final String schemaContent) throws Exception
    {
        SchemasApi schemasApi = new SchemasApi(apiClient);

        this.cachedLatestSchemaVersion = null;

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++ ) {
                log.debug("Preparing to execute call #{} to retrieve schema versions for schemaId = {}", page, schemaId);
                SchemaVersionsResponse schemaVersionsResponse = schemasApi.getSchemaVersions(PAGE_SZ_VERSION, page, Set.of(schemaId), null, null);
                log.debug("Retrieved {} versions for schemaId = {}", schemaVersionsResponse != null ? schemaVersionsResponse.getData().size() : "null", schemaId);

                if (schemaVersionsResponse.getData().isEmpty()) {
                    break;
                }
                if ( page == 1 && schemaVersionsResponse.getMeta().getPagination().getNextPage() != null ) {
                    maxPages = schemaVersionsResponse.getMeta().getPagination().getTotalPages();
                }
                if ( page == 1 && ! schemaVersionsResponse.getData().isEmpty()) {
                    this.cachedLatestSchemaVersion = schemaVersionsResponse.getData().get(0);
                }

                // TODO - Is whitespace in schemas content a problem?
                for ( SchemaVersion schemaVersion : schemaVersionsResponse.getData()) {
                    if (EventPortalModelUtils.reserializeJsonSchema(schemaVersion.getContent()).contentEquals(schemaContent)) {
                        return schemaVersion;
                    }
                }
            }
        } catch (Exception exc) {
            log.error("Error in EventPortalClientApi.getSchemaVersionByContent", exc);
            throw exc;
        }

        return null;
    }

    /**
     * Retrieve TopicAddressEnum by Name
     * - Application Domain ID is NOT required as Enum names are globally unique within an account
     * @param apiClient
     * @param topicAddressEnumName
     * @return
     */
    public TopicAddressEnum getTopicAddressEnumByName(final String topicAddressEnumName) throws Exception
    {
        EnumsApi enumsApi = new EnumsApi(apiClient);

        try {
            TopicAddressEnumsResponse topicAddressEnumsResponse = enumsApi.getEnums(PAGE_SZ_OBJECT, 1, null, null, null, List.of(topicAddressEnumName), null, null, null);
            if (topicAddressEnumsResponse.getData().isEmpty()) {
                return null;
            }
            if (topicAddressEnumsResponse.getData().size() > 1) {
                // Should never happen
                throw new Exception("More than one TopicAddressEnum object was returned for Enum name = " + topicAddressEnumName);
            }
            final String appDomainIdInReturnedEnum = topicAddressEnumsResponse.getData().get(0).getApplicationDomainId();
            if ( ! appDomainIdInReturnedEnum.contentEquals( this.appDomainId )) {
                throw new Exception( "Topic Parameter Enum [" + topicAddressEnumName + "] was found to exist outside of the target app domain for import [" + appDomainName + "]" );
            }
            return topicAddressEnumsResponse.getData().get(0);
        } catch (Exception exc) {
            log.error("Error in EventPortalClientApi.getTopicAddressByEnumName", exc);
            throw exc;
        }
    }

    public TopicAddressEnumVersion getTopicAddressEnumVersionByContent(
        final String topicAddressEnumId, 
        List<String> values) throws Exception
    {
        EnumsApi enumsApi = new EnumsApi(apiClient);

        this.cachedLatestEnumVersion = null;

        try {
            int maxPages = 1;
            for (int page = 1; page <= maxPages; page++) {
                log.debug("Preparing to execute call #{} to retrieve enum versions for enumId = {}", page, topicAddressEnumId);
                TopicAddressEnumVersionsResponse response = enumsApi.getEnumVersions(PAGE_SZ_VERSION, page, Set.of(topicAddressEnumId), null);
                log.debug("Retrieved {} versions for enumId = {}", response != null ? response.getData().size() : "null", topicAddressEnumId);

                if (response.getData().isEmpty()) {
                    break;
                }
                if ( page == 1 && response.getMeta().getPagination().getNextPage() != null ) {
                    maxPages = response.getMeta().getPagination().getTotalPages();
                }
                if ( page == 1 && ! response.getData().isEmpty()) {
                    this.cachedLatestEnumVersion = response.getData().get(0);
                }

                for ( TopicAddressEnumVersion enumVersion : response.getData() ) {
                    List<String> epVersionValues = EventPortalModelUtils.getValuesListFromTopicAddressEnumVersion(enumVersion);
                    if ( EventPortalModelUtils.valuesListsMatch(epVersionValues, values) ) {
                        return enumVersion;
                    }
                }

            }
        } catch (Exception exc) {
            log.error("Error in EventPortalClientApi.getTopicAddressEnumVersionByContent", exc);
            throw exc;
        }

        return null;
    }

    public SchemaObject createSchemaObject( 
        final String schemaName,
        final String schemaType ) throws Exception
    {
        SchemasApi schemasApi = new SchemasApi( apiClient );

        SchemaObject schemaObject = new SchemaObject();
        schemaObject.setApplicationDomainId(appDomainId);
        schemaObject.setName(schemaName);
        schemaObject.setSchemaType(schemaType);
        schemaObject.setShared(true);

        try {
            SchemaResponse response = schemasApi.createSchema(schemaObject);
            return response.getData();
        } catch (Exception exc) {
            log.error("Error in EventPortalClientApi.createSchemaObject", exc);
            throw exc;
        }
    }

    public SchemaVersion createSchemaVersion(
        final String schemaId,
        final String schemaContent,
        final String schemaName,
        final String lastSemVer
    ) throws Exception
    {
        SchemasApi schemasApi = new SchemasApi(apiClient);

        SchemaVersion schemaVersion = new SchemaVersion();
        schemaVersion.setSchemaId(schemaId);
        schemaVersion.setDescription("Schema Version created by AsyncApi Import " + schemaName);
        schemaVersion.setEndOfLifeDate(null);
        schemaVersion.setContent(EventPortalModelUtils.reserializeJsonAsPretty(schemaContent));
        schemaVersion.setVersion(incrementSemVer(lastSemVer));

        try {
            SchemaVersionResponse response = schemasApi.createSchemaVersion(schemaVersion);
            return response.getData();
        } catch (Exception exc) {
            final String msg = "EventPortalClientApi.createSchemaVersion";
            log.error(msg, exc);
            throw exc;
        }
    }

    /**
     * Update schema version with content - currently works with schema content type JSON
     * Updates content (schema) only
     * @param schemaVersionId
     * @param schemaContent
     * @return
     * @throws Exception
     */
    public SchemaVersion updateSchemaVersion(
        final String schemaVersionId,
        final String schemaContent
    ) throws Exception
    {
        SchemasApi schemasApi = new SchemasApi(apiClient);

        SchemaVersion schemaVersion = new SchemaVersion();
        schemaVersion.setContent(EventPortalModelUtils.reserializeJsonAsPretty(schemaContent));

        try {
            SchemaVersionResponse response = schemasApi.updateSchemaVersion(schemaVersionId, schemaVersion);
            return response.getData();
        } catch (Exception exc) {
            final String msg = "EventPortalClientApi.updateSchemaVersion";
            log.error(msg, exc);
            throw exc;
        }
    }

    public TopicAddressEnum createEnumObject(
        final String enumName
    ) throws Exception
    {
        EnumsApi enumsApi = new EnumsApi(apiClient);

        TopicAddressEnum topicAddressEnum = new TopicAddressEnum();
        topicAddressEnum.setName(enumName);
        topicAddressEnum.setShared(true);
        topicAddressEnum.setApplicationDomainId(appDomainId);
        
        try {
            TopicAddressEnumResponse response = enumsApi.createEnum(topicAddressEnum);
            return response.getData();
        } catch (Exception exc) {
            final String msg = "EventPortalClientApi.createEnumObject";
            log.error(msg, exc);
            throw exc;
        }
    }

    public TopicAddressEnumVersion createEnumVersion(
        final String enumId,
        final List<String> values,
        final String enumName,
        final String lastSemVer
    ) throws Exception
    {
        final EnumsApi enumsApi = new EnumsApi(apiClient);

        final List<TopicAddressEnumValue> enumValues = new ArrayList<>();
        for (String s : values) {
            final TopicAddressEnumValue ev = new TopicAddressEnumValue();
            // ev.setEnumVersionId(enumId);
            ev.setValue(s);
            enumValues.add(ev);
        }
        TopicAddressEnumVersion enumVersion = new TopicAddressEnumVersion();
        enumVersion.setEnumId(enumId);
        enumVersion.setValues(enumValues);
        enumVersion.setDescription("Enum version created by AsyncApi import " + enumName);
        enumVersion.setVersion(incrementSemVer(lastSemVer));

        try {
            TopicAddressEnumVersionResponse response = enumsApi.createEnumVersion(enumVersion);
            return response.getData();
        } catch (Exception exc) {
            final String msg = "EventPortalClientApi.createEnumVersion";
            log.error(msg, exc);
            throw exc;
        }
    }

    public TopicAddressEnumVersion updateEnumVersion(
        final String enumVersionId,
        final List<String> values
    ) throws Exception
    {
        final EnumsApi enumsApi = new EnumsApi(apiClient);

        final List<TopicAddressEnumValue> enumValues = new ArrayList<>();
        for (String s : values) {
            final TopicAddressEnumValue ev = new TopicAddressEnumValue();
            ev.setValue(s);
            enumValues.add(ev);
        }
        final TopicAddressEnumVersion enumVersionUpdate = new TopicAddressEnumVersion();
        enumVersionUpdate.setValues(enumValues);

        try {
            TopicAddressEnumVersionResponse response = enumsApi.updateEnumVersion(enumVersionId, enumVersionUpdate);
            return response.getData();
        } catch (Exception exc) {
            final String msg = "EventPortalClientApi.updateEnumVersion";
            log.error(msg, exc);
            throw exc;
        }
    }

    public Event createEventObject(
        final String eventName
    ) throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);

        final Event event = new Event();
        event.setApplicationDomainId(this.appDomainId);
        event.setName(eventName);
        event.setShared(true);

        try {
            EventResponse response = eventsApi.createEvent(event);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.createEventObject - Error creating event {}", eventName, exc);
            throw exc;
        }
    }

    public EventVersion createEventVersion(
        final String eventId,
        final String eventName,
        final String schemaVersionId,
        final EventVersionDto.DeliveryDescriptor dtoDeliveryDescriptor,
        final String lastSemVer
    ) throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);

        EventVersion eventVersion = new EventVersion();
        eventVersion.setEventId(eventId);
        eventVersion.setVersion(incrementSemVer(lastSemVer));
        eventVersion.setDescription("Schema Version created by AsyncApi import " + eventName);
        eventVersion.setSchemaVersionId(schemaVersionId);
        eventVersion.setDeliveryDescriptor(
            EventPortalModelUtils.mapDtoDeliveryDescriptorToEpEventDeliveryDescriptor(dtoDeliveryDescriptor)
        );
        
        try {
            EventVersionResponse response = eventsApi.createEventVersion(eventVersion);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.createEventVersion - Error creating event version for event {}", eventName, exc);
            throw exc;
        }
    }

    public EventVersion updateEventVersion(
        final String eventVersionId,
        final String schemaVersionId,
        final EventVersionDto.DeliveryDescriptor dtoDeliveryDescriptor
    ) throws Exception
    {
        final EventsApi eventsApi = new EventsApi(apiClient);

        EventVersion eventVersion = new EventVersion();
        eventVersion.setSchemaVersionId(schemaVersionId);
        eventVersion.setDeliveryDescriptor(
            EventPortalModelUtils.mapDtoDeliveryDescriptorToEpEventDeliveryDescriptor(dtoDeliveryDescriptor)
        );

        try {
            EventVersionResponse response = eventsApi.updateEventVersion(eventVersionId, eventVersion);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.updateEventVersion - Error updating event version {}", eventVersionId, exc);
            throw exc;
        }
    }

    public Application createApplicationObject(
        final String appName
    ) throws Exception
    {
        final ApplicationsApi appApi = new ApplicationsApi(apiClient);

        final Application app = new Application();
        app.setName(appName);
        app.setApplicationDomainId(this.appDomainId);
        app.setBrokerType(BrokerTypeEnum.SOLACE);
        app.setApplicationType("standard");

        try {
            ApplicationResponse response = appApi.createApplication(app);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.createApplicationObject - Error creating application: {}", appName, exc);
            throw exc;
        }
    }

    /**
     * Create a new application version
     * @param appId - appId where the application version child should be created
     * @param appName - appName of the parent application. Used for logging
     * @param declaredPublishedEventIds - List of published events
     * @param lastSemVer - SemVer of the last application version
     * @return
     * @throws Exception
     */
    public ApplicationVersion createApplicationVersion(
        final String appId,
        final String appName,
        final List<String> declaredPublishedEventIds,
        final String lastSemVer
    ) throws Exception
    {
        final ApplicationsApi appApi = new ApplicationsApi(apiClient);

        final ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setApplicationId(appId);
        appVersion.setDeclaredProducedEventVersionIds(declaredPublishedEventIds);
        appVersion.setDescription("Application version created by AsyncApi import for Application: " + appName);
        appVersion.setVersion(incrementSemVer(lastSemVer));
        appVersion.setEndOfLifeDate(null);

        try {
            ApplicationVersionResponse response = appApi.createApplicationVersion(appVersion);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.createApplicationVersion - Error creating application version for App: {}", appName, exc);
            throw exc;
        }
    }

    /**
     * Update the declaredPublishedEvents associated with an application version
     * @param appVersionId
     * @param declaredPublishedEvents
     * @return The update ApplicationVersion object
     * @throws Exception
     */
    public ApplicationVersion updateApplicationVersion(
        final String appVersionId,
        final List<String> declaredPublishedEvents
    ) throws Exception
    {
        final ApplicationsApi appApi = new ApplicationsApi(apiClient);

        final ApplicationVersion appVersion = new ApplicationVersion();
        declaredPublishedEvents.forEach( item -> {
            appVersion.addDeclaredProducedEventVersionIdsItem(item);
        });

        try {
            ApplicationVersionResponse response = appApi.updateApplicationVersion(appVersionId, appVersion, null, null);
            return response.getData();
        } catch (Exception exc) {
            log.error("EventPortalClientApi.updateApplicationVersion - Error updating application version", exc);
            throw exc;
        }
    }

    /**
     * Increment SemVer passed as argument based upon the strategy determined when this object
     * was constructed.
     * @param lastSemVer
     * @return
     * @throws Exception
     */
    private String incrementSemVer( final String lastSemVer ) throws Exception
    {
        final String SEMVER_REGEX = "^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*))?(?:\\+([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*))?$";
        final Pattern SEMVER_PATTERN = Pattern.compile(SEMVER_REGEX);

        if (lastSemVer == null || lastSemVer == "") {
            return "1.0.0";
        }
        final Matcher m = SEMVER_PATTERN.matcher(lastSemVer);
        if (!m.matches()) {
            final String msg = "EventPortalClientApi.incrementSemVer - Error parsing SemVer of Event Portal Object: [" + lastSemVer + "]";
            log.error(msg);
            throw new Exception(msg);
        }
        final String major = m.group(1);
        final String minor = m.group(2);
        final String patch = m.group(3);

        int iMajor = Integer.parseInt(major);
        int iMinor = Integer.parseInt(minor);
        int iPatch = Integer.parseInt(patch);

        switch (versionStrategy) {
            case MAJOR:
                iMajor++;
                iMinor = 0;
                iPatch = 0;
                break;
            case MINOR:
                iMinor++;
                iPatch = 0;
                break;
            case PATCH:
                iPatch++;
                break;
            default:
        }
        return String.format("%d.%d.%d", iMajor, iMinor, iPatch);
    }
}
