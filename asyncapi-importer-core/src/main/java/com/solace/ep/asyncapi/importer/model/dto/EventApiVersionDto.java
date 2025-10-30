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
