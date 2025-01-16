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

package com.solace.ep.asyncapi.accessor.v3;

import com.google.gson.JsonObject;

import lombok.Getter;

public class AsyncApiSchema extends AbstractAccessor {

    @Getter
    private String schemaName;

    // private String contentType = null;

    public AsyncApiSchema(final String schemaName, final JsonObject rootSpec, final JsonObject leafSpec)
    {
        super(rootSpec, leafSpec);
        this.schemaName = schemaName;
    }

    // public AsyncApiSchema(
    //     final String schemaName, 
    //     final JsonObject rootSpec, 
    //     final JsonObject leafSpec,
    //     final String contentType
    // )
    // {
    //     super(rootSpec, leafSpec);
    //     this.schemaName = schemaName;
    //     this.contentType = contentType;
    // }
    
    

    // public String getSchemaContentAsString() throws Exception
    // {
    //     // TODO - return schema based upon content type
        
    //     Gson gson = new Gson();
    //     if (contentType.equalsIgnoreCase("application/json")) {
    //         if (leafSpec.has("type")) {

    //         }
    //     }

    // }
}
