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

import com.solace.cloud.ep.designer.model.TopicAddressEnum;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 
 * Internal Format for Event Portal TopicAddressEnum Objects
 * TopicAddressEnum objects may contain versions
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class EnumDto extends AbstractDtoObject {
    
    private Boolean shared;

    private String type = "enum";

    private List<EnumVersionDto> enumVersions;

    private TopicAddressEnum epTopicAddressEnum;

    public EnumDto( String name, String applicationDomainId ) {
        this.setName(name);
        this.setApplicationDomainId(applicationDomainId);
    }

    public List<EnumVersionDto> getEnumVersions() {
        if (enumVersions == null) {
            enumVersions = new ArrayList<>();
        }
        return this.enumVersions;
    }

    @Override
    public int getNumberOfVersions()
    {
        return ( this.getEnumVersions() == null ) ? 0 : this.getEnumVersions().size();
    }
    
}
