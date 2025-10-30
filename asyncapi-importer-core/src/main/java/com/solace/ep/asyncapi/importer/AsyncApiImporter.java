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

import com.solace.ep.asyncapi.accessor.v2.AsyncApiAccessor;
import com.solace.ep.asyncapi.importer.client.EventPortalClientApi;
import com.solace.ep.asyncapi.importer.mapper.AsyncApiV2ToDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;
import com.solace.ep.asyncapi.importer.util.EventPortalModelUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Class to choreograph and execute AsyncApi import into Event Portal.
 * Can instantiate the object and execute or invoke statically.
 */
@Slf4j
public class AsyncApiImporter {
    
    private String applicationDomainId;

    private String applicationDomainName;

    private String eventPortalBearerToken;
    
    private String asyncApiSpecToImport;

    private String eventPortalBaseUrl;

    private EpNewVersionStrategy versionStrategy;

    private boolean performCascadeUpdate;

    private boolean importApplication;

    private boolean importEventApi;

    /**
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented. Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @param performaCascadeUpdate - Set to TRUE to automatically create new versions of objects by cascade update (See Documentation)
     * @param createApplication - Set to TRUE to create a new application in Event Portal
     * @param createEventApi - Set to TRUE to create an Event API in Event Portal
     * @throws Exception
     */
    public AsyncApiImporter(
        final String applicationDomainId,
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy,
        final boolean performCascadeUpdate,
        final boolean importApplication,
        final boolean importEventApi
    ) throws Exception
    {
        this.applicationDomainId = applicationDomainId;
        this.applicationDomainName = applicationDomainName;
        this.eventPortalBearerToken = eventPortalBearerToken;
        this.asyncApiSpecToImport = asyncApiSpecToImport;
        this.eventPortalBaseUrl = eventPortalBaseUrl;
        if (newVersionStrategy != null && !newVersionStrategy.isEmpty()) {
            this.versionStrategy = EpNewVersionStrategy.valueOf(newVersionStrategy);
        } else {
            this.versionStrategy = EpNewVersionStrategy.MAJOR;
        }
        this.performCascadeUpdate = performCascadeUpdate;
        this.importApplication = importApplication;
        this.importEventApi = importEventApi;
    }

    /**
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented.
     * Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @throws Exception
     */
    public AsyncApiImporter(
        final String applicationDomainId,
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy
    ) throws Exception
    {
        this(applicationDomainId, applicationDomainName, eventPortalBearerToken, asyncApiSpecToImport, eventPortalBaseUrl, newVersionStrategy, true, true, false);
    }

    /**
     * Statically invoke AsyncApi import operation
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented. Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @param performaCascadeUpdate - Set to TRUE to automatically create new versions of objects by cascade update (See Documentation)
     * @param createApplication - Set to TRUE to create a new application in Event Portal
     * @param createEventApi - Set to TRUE to create an Event API in Event Portal
     * @throws Exception
     */
    public static void execImportOperation(
        final String applicationDomainId,
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy,
        final boolean performCascadeUpdate,
        final boolean importApplication,
        final boolean importEventApi
    ) throws Exception
    {
        final AsyncApiImporter importer = new AsyncApiImporter(
            applicationDomainId,
            applicationDomainName, 
            eventPortalBearerToken, 
            asyncApiSpecToImport, 
            eventPortalBaseUrl, 
            newVersionStrategy, 
            performCascadeUpdate,
            importApplication,
            importEventApi);
        importer.execImportOperation();
    }

    /**
     * Statically invoke AsyncApi import operation
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented. Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @param performaCascadeUpdate - Set to TRUE to automatically create new versions of objects by cascade update (See Documentation)
     * @param createApplication - Set to TRUE to create a new application in Event Portal
     * @param createEventApi - Set to TRUE to create an Event API in Event Portal
     * @throws Exception
     */
    public static void execImportOperation(
        final String applicationDomainId,
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy,
        final boolean performCascadeUpdate,
        final boolean importApplication,
        final boolean importEventApi,
        final int operationId
    ) throws Exception
    {
        final AsyncApiImporter importer = new AsyncApiImporter(
            applicationDomainId,
            applicationDomainName, 
            eventPortalBearerToken, 
            asyncApiSpecToImport, 
            eventPortalBaseUrl, 
            newVersionStrategy, 
            performCascadeUpdate,
            importApplication,
            importEventApi);
        importer.execImportOperation(operationId);
    }

    /**
     * Statically invoke AsyncApi import operation
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented.
     * @param disableCascadeUpdate - Set to TRUE to prevent new versions of objects from being created by cascade update (See Documentation)
     * Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @throws Exception
     */
    public static void execImportOperation(
        final String applicationDomainId,
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy
    ) throws Exception
    {
        final AsyncApiImporter importer = 
            new AsyncApiImporter(
                applicationDomainId, 
                applicationDomainName, 
                eventPortalBearerToken, 
                asyncApiSpecToImport, 
                eventPortalBaseUrl, 
                newVersionStrategy
            );
        importer.execImportOperation();
    }

    /**
     * Invoke AsyncApi import operation for this AsyncApiImporter object
     * @throws Exception
     */
    public void execImportOperation() throws Exception
    {
        execImportOperation(null);
    }

    /**
     * Invoke AsyncApi import operation for this AsyncApiImporter object
     * @param operationId - Unique identifier for the import operation; used to capture logs
     * @throws Exception
     */
    public void execImportOperation(Integer operationId) throws Exception
    {
        final AsyncApiAccessor asyncApiAccessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(asyncApiSpecToImport) );

        EventPortalClientApi importClient;
        if (this.applicationDomainId == null || this.applicationDomainId.isBlank()) {
            importClient = new EventPortalClientApi(
                this.eventPortalBearerToken, 
                this.applicationDomainName, 
                this.versionStrategy,
                this.eventPortalBaseUrl
            );
            this.applicationDomainId = importClient.getAppDomainId();
        } else {
            importClient = new EventPortalClientApi(
                this.eventPortalBearerToken, 
                versionStrategy,
                this.eventPortalBaseUrl,
                applicationDomainId
            );
            this.applicationDomainName = importClient.getAppDomainName();
        }

        final AsyncApiV2ToDto asyncApiToDtoMapper = new AsyncApiV2ToDto(
            asyncApiAccessor, 
            applicationDomainId, 
            applicationDomainName
        );

        final DtoResultSet mappedResults = asyncApiToDtoMapper.mapAsyncApiToDto();
        if (! EventPortalModelUtils.versionCountsValid(mappedResults)) {
            throw new Exception("Input from the AsyncApi spec was found to be invalid -- EXITING");
        }

        final EpImportOperator importOperator = new EpImportOperator(mappedResults, importClient, operationId);

        try {
            importOperator.matchEpEnums();

            importOperator.matchEpSchemas();

            importOperator.importEnums();

            importOperator.importSchemas();

            importOperator.matchEpEvents();

            importOperator.importEvents();

            if (importApplication) 
            {
                importOperator.matchEpApplications();

                importOperator.importApplications();
            }

            if (importEventApi) {
                importOperator.matchEpEventApis();

                importOperator.importEventApis();
            }

            if (performCascadeUpdate)
            {
                importOperator.cascadeUpdateEvents();

                if (importApplication) {
                    importOperator.cascadeUpdateApplications();
                }

                if (importEventApi) {
                    importOperator.cascadeUpdateEventApis();
                }
            }
        } finally {
            // Shutdown the thread pool to allow the JVM to exit
            importOperator.shutdown();
        }
    }
}
