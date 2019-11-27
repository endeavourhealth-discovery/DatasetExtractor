package org.endeavourhealth.batcher.model;

import lombok.Data;

import javax.persistence.Entity;
import java.time.LocalDate;

@Data
@Entity
public class RemoteObservation {

    private Long patientId;
    private Long observationId;
    private LocalDate effectiveDate;

}
