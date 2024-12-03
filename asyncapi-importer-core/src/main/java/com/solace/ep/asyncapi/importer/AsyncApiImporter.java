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
import com.solace.ep.asyncapi.importer.mapper.AsyncApiToDto;
import com.solace.ep.asyncapi.importer.model.dto.DtoResultSet;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncApiImporter {
    
    private String applicationDomainName;

    private String eventPortalBearerToken;
    
    private String asyncApiSpecToImport;

    private String eventPortalBaseUrl;

    private EpNewVersionStrategy versionStrategy;

    private String applicationDomainId;

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
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy
    ) throws Exception
    {
        this.applicationDomainName = applicationDomainName;
        this.eventPortalBearerToken = eventPortalBearerToken;
        this.asyncApiSpecToImport = asyncApiSpecToImport;
        this.eventPortalBaseUrl = eventPortalBaseUrl;
        if (newVersionStrategy != null && !newVersionStrategy.isEmpty()) {
            this.versionStrategy = EpNewVersionStrategy.valueOf(newVersionStrategy);
        } else {
            this.versionStrategy = EpNewVersionStrategy.MAJOR;
        }
    }

    /**
     * Statically invoke AsyncApi import operation
     * @param applicationDomainName - Name of Application Domain in Event Portal where objects represented in the AsyncApi spec will be imported.
     * @param eventPortalBearerToken - Event Portal Bearer Token, must have read and write privileges
     * @param asyncApiSpecToImport - String representation of full asyncapi spec to import
     * @param eventPortalBaseUrl - Can be NULL, will use default URL for US/Canada
     * @param newVersionStrategy - Used to indicate how semantic versions for created object are incremented.
     * Allowed values are MAJOR, MINOR, and PATCH.
     * Can be NULL, will default increment MAJOR version;
     * @throws Exception
     */
    public static void execImportOperation(
        final String applicationDomainName,
        final String eventPortalBearerToken,
        final String asyncApiSpecToImport,
        final String eventPortalBaseUrl,
        final String newVersionStrategy
    ) throws Exception
    {
        final AsyncApiImporter importer = new AsyncApiImporter(applicationDomainName, eventPortalBearerToken, asyncApiSpecToImport, eventPortalBaseUrl, newVersionStrategy);
        importer.execImportOperation();
    }

    /**
     * Invoke AsyncApi import operation for this AsyncApiImporter object
     * @throws Exception
     */
    public void execImportOperation() throws Exception
    {
        final AsyncApiAccessor asyncApiAccessor = new AsyncApiAccessor( AsyncApiAccessor.parseAsyncApi(asyncApiSpecToImport) );

        final EventPortalClientApi importClient = new EventPortalClientApi(
            this.eventPortalBearerToken, 
            this.applicationDomainName, 
            this.versionStrategy,
            this.eventPortalBaseUrl
        );
        this.applicationDomainId = importClient.getAppDomainId();

        final AsyncApiToDto asyncApiToDtoMapper = new AsyncApiToDto(
            asyncApiAccessor, 
            applicationDomainId, 
            applicationDomainName
        );

        final DtoResultSet mappedResults = asyncApiToDtoMapper.mapAsyncApiToDto();

        final EpImportOperator importOperator = new EpImportOperator(mappedResults, importClient);

        importOperator.matchEpEnums();

        importOperator.matchEpSchemas();

        importOperator.importEnums();

        importOperator.importSchemas();

        importOperator.matchEpEvents();

        importOperator.importEvents();

        importOperator.matchEpApplications();

        importOperator.importApplications();

        importOperator.cascadeUpdateEvents();

        importOperator.cascadeUpdateApplications();
    }
}
