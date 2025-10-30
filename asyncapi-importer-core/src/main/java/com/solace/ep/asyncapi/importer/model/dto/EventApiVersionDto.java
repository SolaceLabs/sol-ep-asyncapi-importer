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

import com.solace.cloud.ep.designer.model.EventApiVersion;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EventApiVersionDto extends AbstractVersionDto {

    private String eventApiId;

    private String description;

    private List<String> producedEventVersionIds;

    private List<String> consumedEventVersionIds;

    // Matched EP EventApiVersion or latest EP EventApiVersion
    private EventApiVersion epEventApiVersion;

    public List<String> getProducedEventVersionIds() {
        if ( producedEventVersionIds == null ) {
            producedEventVersionIds = new ArrayList<>();
        }
        return producedEventVersionIds;
    }

    public List<String> getConsumedEventVersionIds() {
        if ( consumedEventVersionIds == null ) {
            consumedEventVersionIds = new ArrayList<>();
        }
        return consumedEventVersionIds;
    }
    
}
