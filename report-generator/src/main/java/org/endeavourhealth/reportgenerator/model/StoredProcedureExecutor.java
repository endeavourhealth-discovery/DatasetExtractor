package org.endeavourhealth.reportgenerator.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StoredProcedureExecutor extends AbstractEntity {

    @ElementCollection
    private List<String> preStoredProcedures;

    @ElementCollection
    private List<String> postStoredProcedures;

    @Enumerated(EnumType.STRING)
    @Column( name = "db")
    private Database database = Database.COMPASS;

    private Boolean switchedOn = true;

    public boolean requiresDatabase() {
        if(!switchedOn) return false;

        if(postStoredProcedures == null && preStoredProcedures == null) return false;

        return true;
    }
}
