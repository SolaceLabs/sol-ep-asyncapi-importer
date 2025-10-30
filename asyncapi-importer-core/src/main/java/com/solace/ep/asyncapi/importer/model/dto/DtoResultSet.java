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

package com.solace.ep.asyncapi.importer.model.dto;

import java.util.Map;

import lombok.Data;

/**
 * Format representing import structure into Event Portal
 * There can be only one Application Domain and one Application + Application Version
 * represented in the input for import into Event Portal.
 */
@Data
public class DtoResultSet {
    
    private ApplicationDomainDto applicationDomainDto;

    private Map<String, ApplicationDto> mapApplications;

    private Map<String, EnumDto> mapEnums;

    private Map<String, SchemaDto> mapSchemas;

    private Map<String, EventDto> mapEvents;

    private Map<String, EventApiDto> mapEventApis;

}
