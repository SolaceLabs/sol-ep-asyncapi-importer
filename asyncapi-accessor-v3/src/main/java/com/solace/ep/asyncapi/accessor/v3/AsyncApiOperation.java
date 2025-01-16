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

public class AsyncApiOperation extends AbstractAccessor {

    @Getter
    private String operationName;
    
    /**
     * Constructor for AsyncApiOperation
     * @param name - The message name
     * @param rootSpec - The root AsyncApi as JsonObject
     * @param leafSpec - The message as JsonObject
     */
    public AsyncApiOperation(final String name, final JsonObject rootSpec, final JsonObject leafSpec)
    {
        super(rootSpec, leafSpec);
        this.operationName = name;
    }

    /**
     * Check if Operation action is receive
     * @return 'True' if operation action is 'receive'
     * @throws Exception
     */
    public boolean isReceiveAction() throws Exception {
        if (getAction().equalsIgnoreCase("receive")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Check if Operation action is send
     * @return 'True' if operation action is 'send'
     * @throws Exception
     */
    public boolean isSendAction() throws Exception {
        if (getAction().equalsIgnoreCase("send")) {
            return true;
        } else {
            return false;
        }
    }

    public String getAction() throws Exception
    {
        return getStringFieldByName("action");
    }

    public String getSummary() throws Exception
    {
        return getStringFieldByName("summary");
    }

    /**
     * Get the channel object associated with this operation
     * @return Channel as AsyncApiChannel
     * @throws Exception
     */
    public AsyncApiChannel getChannel() throws Exception
    {
        final JsonObject channelElt = getObjectFieldByName("channel");
        if (channelElt == null) {
            throw new Exception(String.format("Channel for Operation [%s] is blank", operationName));
        }
        if (channelElt.has("$ref")) {
            final String refString = getStringFieldByName("$ref");
            final String channelName = AsyncApiUtils.getLastElementFromRefString(refString);
            final JsonObject channelObject = getResolvedJsonObject(channelElt);
            return new AsyncApiChannel(channelName, this.rootSpec, channelObject);
        }
        // TODO - Determine if channel can be returned in-line
        return null;
    }
}
