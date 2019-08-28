package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Extension {

    private ExtensionType type;
    private Map<String, String> properties;

}
