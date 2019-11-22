package org.endeavourhealth.reportgenerator.beans;

import lombok.Data;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectScheduleEntity;

import java.util.List;

@Data
public class DSMConfiguration {

    private ProjectScheduleEntity projectScheduleEntity;

    private List<OrganisationEntity> organisations;

    private  ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity;

}
