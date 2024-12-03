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

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class SchemaDto {
  
    private String id;

    private String applicationDomainId;

    private String name;

    private Boolean shared;

    private String schemaType;

    private String contentType;

    private String type = "schema";

    private List<SchemaVersionDto> schemaVersions;

    private boolean matchFound = false;

    public SchemaDto( String name, String applicationDomainId ) {
        this.name = name;
        this.applicationDomainId = applicationDomainId;
    }
    
    public List<SchemaVersionDto> getSchemaVersions() {
        if (this.schemaVersions == null) {
            this.schemaVersions = new ArrayList<>();
        }
        return schemaVersions;
    }
}
