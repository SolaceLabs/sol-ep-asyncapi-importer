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

import com.solace.cloud.ep.designer.model.TopicAddressEnumVersion;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EnumVersionDto extends AbstractVersionDto {
    
    private String id;

    private String enumId;

    private String description;

    private String version;

    private String displayName;

    private List<EnumValue> values;

    private String stateId;

    private String type = "enumVersion";        // enumVersion

    private TopicAddressEnumVersion epEnumVersion;

    @Data
    public static class EnumValue {

        private String id;

        private String enumVersionId;

        private String value;

        private String type = "enumValue";

        public EnumValue( String value ) {
            this.value = value;
        }

    }

    public List<EnumValue> getValues() {
        if (this.values == null) {
            this.values = new ArrayList<>();
        }
        return this.values;
    }

    public List<String> getValuesAsStringList() {
        List<String> stringList = new ArrayList<>();
        for ( EnumValue ev : values ) {
            stringList.add(ev.getValue());
        }
        return stringList;
    }
}
