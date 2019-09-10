package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StoredProcedureExecutor {

    private List<String> preStoredProcedures;

    private List<String> postStoredProcedures;

    private Database database = Database.COMPASS;

    private Boolean switchedOn = true;

    public boolean requiresDatabase() {
        if(!switchedOn) return false;

        if(postStoredProcedures == null && preStoredProcedures == null) return false;

        if(postStoredProcedures.isEmpty() && preStoredProcedures.isEmpty()) return false;

        return true;
    }
}
