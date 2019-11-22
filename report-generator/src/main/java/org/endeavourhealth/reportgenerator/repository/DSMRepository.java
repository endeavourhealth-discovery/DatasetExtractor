package org.endeavourhealth.reportgenerator.repository;

import lombok.NoArgsConstructor;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.DAL.SecurityProjectDAL;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ExtractTechnicalDetailsEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.OrganisationEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.database.ProjectScheduleEntity;
import org.endeavourhealth.common.security.datasharingmanagermodel.models.enums.MapType;

import java.util.List;

@NoArgsConstructor
public class DSMRepository {

    public ProjectScheduleEntity getSchedule(String projectId) throws Exception {

        ProjectScheduleEntity scheduleEntity = new SecurityProjectDAL().getLinkedSchedule(projectId, MapType.SCHEDULE.getMapType());

        return scheduleEntity;
    }

    public List<OrganisationEntity> getOrganisations(String projectId) throws Exception {

        List<OrganisationEntity> linkedOrganisations = new SecurityProjectDAL().getLinkedOrganisations(projectId, MapType.PUBLISHER.getMapType());

        return linkedOrganisations;
    }

    public ExtractTechnicalDetailsEntity getExtractTechnicalDetailsEntity(String projectId) throws Exception {

        ExtractTechnicalDetailsEntity extractTechnicalDetailsEntity = new SecurityProjectDAL().getLinkedExtractTechnicalDetails(projectId, MapType.PUBLISHER.getMapType());

        return extractTechnicalDetailsEntity;
    }
}
