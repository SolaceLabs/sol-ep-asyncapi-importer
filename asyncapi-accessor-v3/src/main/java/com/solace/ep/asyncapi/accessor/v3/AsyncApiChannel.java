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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncApiChannel extends AbstractAccessor {
    
    @Getter
    private String channelName;
    
    private Map<String, AsyncApiMessage> channelMessages;

    private Map<String, ParameterObject> parameterObjects;

    public static class ParameterObject
    {
        private List<String> enumValues;

        public ParameterObject() { }

        public List<String> getEnumValues() {
            if (enumValues == null) {
                enumValues = new ArrayList<>();
            }
            return enumValues;
        }
    }

    public AsyncApiChannel(final String channelName, final JsonObject rootSpec, final JsonObject leafSpec)
    {
        super(rootSpec, leafSpec);
        this.channelName = channelName;
    }

    public String getAddress() throws Exception
    {
        return getStringFieldByName("address");
    }

    public Map<String, AsyncApiMessage> getChannelMessages() throws Exception
    {
        if (channelMessages != null) {
            return channelMessages;
        }
        final Map<String, AsyncApiMessage> newChannelMessages = new HashMap<>();

        final JsonObject messages = getObjectFieldByReference("messages");
        final Map<String, JsonElement> messagesJsonObjMap = messages.asMap();
        for ( Map.Entry<String, JsonElement> entry : messagesJsonObjMap.entrySet() ) {
            final String msgName = entry.getKey();
            final JsonObject messageAsJsonObject = getResolvedJsonObject(entry.getValue().getAsJsonObject());
            final AsyncApiMessage message = new AsyncApiMessage(msgName, this.rootSpec, messageAsJsonObject);
            newChannelMessages.put(msgName, message);
        }

        this.channelMessages = newChannelMessages;
        return newChannelMessages;
    }

    public Map<String, ParameterObject> getParameterObjects() throws Exception
    {
        if (this.parameterObjects != null) {
            return this.parameterObjects;
        }
        this.parameterObjects = new HashMap<>();
        final JsonObject parameters = getObjectFieldByName("parameters");
        final Map<String, JsonElement> parametersMap = parameters.asMap();
        for (Map.Entry<String, JsonElement> param : parametersMap.entrySet()) {
            final String paramName = param.getKey();
            final JsonObject paramValue = param.getValue().getAsJsonObject();
            try {
                if (paramValue.has("schema") && paramValue.get("schema").getAsJsonObject().has("enum")) {
                    JsonArray enumList = paramValue.get("schema").getAsJsonObject().getAsJsonArray("enum");
                    if (enumList.size() > 0) {
                        ParameterObject po = new ParameterObject();
                        for (JsonElement enumValue : enumList) {
                            po.getEnumValues().add(enumValue.getAsString());
                        }
                        this.parameterObjects.put(paramName, po);
                        continue;
                    }
                }
            } catch (Exception exc) {
                log.warn("Caught error parsing parameters on channel [{}] -- continuing", this.channelName);
            }
            // Just add the parameter with null object
            this.parameterObjects.put(paramName, null);
        }
        return this.parameterObjects;
    }

    public List<String> getParametersList() throws Exception
    {
        return new ArrayList<String>( getParameterObjects().keySet() );
    }

    public Map<String, ParameterObject> getEnums() throws Exception
    {
        final Map<String, ParameterObject> paramsWithEnums = new HashMap<>();
        for (Map.Entry<String, ParameterObject> p : getParameterObjects().entrySet()) {
            if (p.getValue() != null) {
                paramsWithEnums.put(p.getKey(), p.getValue());
            }
        }
        return paramsWithEnums;
    }

    // TODO - Bindings

}
