package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Extension {

    @NotNull(message = "Extension type cannot be null")
    private ExtensionType type;

    private Map<String, String> properties;

    private Boolean switchedOn = true;

    public boolean requiresDatabase() {
        if(!switchedOn) return false;

        if(type == ExtensionType.DEANONYMISE_ELGH || type == ExtensionType.DEANONYMISE_WF || type == ExtensionType.DEANONYMISE_WF) return true;

        return false;
    }
}
