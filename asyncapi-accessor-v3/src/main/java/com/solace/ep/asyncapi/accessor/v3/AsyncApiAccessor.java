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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncApiAccessor extends AbstractAccessor {
    
    private Map<String, AsyncApiOperation> operationsMap = null;

    private Map<String, AsyncApiChannel> channelsMap = null;

    private Map<String, AsyncApiSchema> schemasMap = null;

    private Map<String, AsyncApiMessage> messagesMap = null;

    /**
     * Constructor accepting AsyncApi parsed to JsonObject using Gson
     * @param asyncApiRoot
     * @throws IllegalArgumentException
     */
    public AsyncApiAccessor( JsonObject asyncApiRoot ) throws IllegalArgumentException
    {
        super(asyncApiRoot);
        if (asyncApiRoot == null) {
            throw new IllegalArgumentException("AsyncApi Input cannot be null");
        }
    }

    /**
     * Constructor accepting AsyncApi as String
     * @param asyncApiAsString
     * @throws IllegalArgumentException
     * @throws Exception
     */
    public AsyncApiAccessor( String asyncApiAsString ) throws IllegalArgumentException, Exception
    {
        this( parseAsyncApi(asyncApiAsString) );
    }

    /**
     * Get 'info' block of AsyncApi
     * @return
     * @throws Exception
     */
    public AsyncApiInfo getInfo() throws Exception
    {
        return new AsyncApiInfo( getObjectFieldByReference( AsyncApiFieldConstants.API_INFO ) );
    }

    /**
     * Get SemVer of this AsyncApi spec
     * @return
     * @throws Exception
     */
    public String getAsyncApiVersion() throws Exception
    {
        return getStringFieldByName(AsyncApiFieldConstants.API_ASYNCAPI);
    }

    /*
     * Get Default Content Type for schemas.
     * Returns Null if not present.
     */
    public String getDefaultContentType() throws Exception
    {
        return getStringFieldByName(AsyncApiFieldConstants.API_DEFAULT_CONTENT_TYPE);
    }

    /**
     * Parse AsyncApi using Gson
     * @param asyncApi - AsyncApi passed as a String
     * @return JsonObject representing parsed AsyncApi
     * @throws Exception
     */
    public static JsonObject parseAsyncApi( String asyncApi ) throws Exception {

        try {
            JsonElement jsonElement = JsonParser.parseString(asyncApi);
            if ( jsonElement != null && jsonElement.isJsonObject() ) {
                return jsonElement.getAsJsonObject();
            }
        } catch ( JsonSyntaxException jsexc ) {
            log.debug( "Failed to parse AsyncApi as JSON; re-trying as YAML" );
        } catch ( Exception exc ) {
            log.warn( "Caught exception parsing AsyncApi: {}", exc.getMessage() );
        }

        try {
            ObjectMapper yamlReader = new ObjectMapper( new YAMLFactory() );
            Object parsedYamlObj = yamlReader.readValue(asyncApi, Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            String jsonAsyncApi = jsonWriter.writeValueAsString(parsedYamlObj);

            JsonElement jsonElement = JsonParser.parseString(jsonAsyncApi);
            if ( jsonElement != null && jsonElement.isJsonObject() ) {
                return jsonElement.getAsJsonObject();
            }
        } catch ( JsonProcessingException jpexc ) {
            log.error( "Failed to parse AsyncApi as YAML: {}", jpexc.getMessage() );
            throw jpexc;
        } catch ( JsonSyntaxException jsexc ) {
            log.error( "Caught exception parsing AsyncApi: {}", jsexc.getMessage() );
            throw jsexc;
        } catch ( Exception exc ) {
            log.error( "Caught Exception parsing AsyncApi: {}", exc.getMessage() );
            throw exc;
        }
        return null;
    }

    /**
     * Get Operations from AsyncApi
     * @return Map of Operation IDs --> Operations
     * @throws Exception
     */
    public Map<String, AsyncApiOperation> getOperations() throws Exception
    {
        if (this.operationsMap != null) {
            return this.operationsMap;
        }
        final Map<String, AsyncApiOperation> ops = new HashMap<>();
        final JsonObject operations = getObjectFieldByReference(AsyncApiFieldConstants.API_OPERATIONS);
        final Map<String, JsonElement> mapOfOpsJsonObjects = operations.asMap();
        for ( Map.Entry<String, JsonElement> entry : mapOfOpsJsonObjects.entrySet() ) {
            final String opName = entry.getKey();
            final AsyncApiOperation op = new AsyncApiOperation(opName, this.rootSpec, entry.getValue().getAsJsonObject());
            ops.put(opName, op);
        }
        this.operationsMap = ops;
        return ops;
    }

    /**
     * Get Channels from AsyncApi
     * @return Map of Channel IDs --> Channels
     * @throws Exception
     */
    public Map<String, AsyncApiChannel> getChannels() throws Exception
    {
        if (this.channelsMap != null) {
            return this.channelsMap;
        }
        final Map<String, AsyncApiChannel> chs = new HashMap<>();
        final JsonObject channels = getObjectFieldByReference(AsyncApiFieldConstants.API_CHANNELS);
        final Map<String, JsonElement> mapOfChsJsonObjects = channels.asMap();
        for ( Map.Entry<String, JsonElement> entry : mapOfChsJsonObjects.entrySet() ) {
            final String chName = entry.getKey();
            final AsyncApiChannel channel = new AsyncApiChannel(chName, this.rootSpec, entry.getValue().getAsJsonObject());
            chs.put(chName, channel);
        }
        this.channelsMap = chs;
        return chs;
    }

    /**
     * Get Schemas from components section of AsyncApi
     * @return Map of schema names --> schemas
     * @throws Exception
     */
    public Map<String, AsyncApiSchema> getSchemas() throws Exception
    {
        if (this.schemasMap != null) {
            return this.schemasMap;
        }
        final Map<String, AsyncApiSchema> schs = new HashMap<>();
        final JsonObject schemas = getObjectFieldByReference(AsyncApiFieldConstants.API_SCHEMAS);
        final Map<String, JsonElement> mapOfSchsJsonObjects = schemas.asMap();
        for ( Map.Entry<String, JsonElement> entry : mapOfSchsJsonObjects.entrySet() ) {
            final String schemaName = entry.getKey();
            final AsyncApiSchema schema = new AsyncApiSchema(schemaName, this.rootSpec, entry.getValue().getAsJsonObject());
            schs.put(schemaName, schema);
        }
        this.schemasMap = schs;
        return schs;
    }

    /**
     * Get Messages from components section of AsyncApi
     * @return Map of message names --> messages
     * @throws Exception
     */
    public Map<String, AsyncApiMessage> getMessages() throws Exception
    {
        if (this.messagesMap != null) {
            return this.messagesMap;
        }
        final Map<String, AsyncApiMessage> newMessagesMap = new HashMap<>();
        final JsonObject messages = getObjectFieldByReference(AsyncApiFieldConstants.API_MESSAGES);
        final Map<String, JsonElement> mapOfMessageJsonObjects = messages.asMap();
        for ( Map.Entry<String, JsonElement> entry : mapOfMessageJsonObjects.entrySet() ) {
            final String messageName = entry.getKey();
            final AsyncApiMessage message = new AsyncApiMessage(messageName, this.rootSpec, entry.getValue().getAsJsonObject());
            newMessagesMap.put(messageName, message);
        }
        this.messagesMap = newMessagesMap;
        return newMessagesMap;
    }

}
