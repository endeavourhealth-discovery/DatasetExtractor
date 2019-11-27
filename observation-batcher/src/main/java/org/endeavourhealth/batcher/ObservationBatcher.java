package org.endeavourhealth.batcher;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.batcher.bean.Database;
import org.endeavourhealth.batcher.model.LocalObservation;
import org.endeavourhealth.batcher.model.RemoteObservation;
import org.endeavourhealth.batcher.repository.JpaRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;


@Slf4j
public class ObservationBatcher implements AutoCloseable{

    private JpaRepository jpaRepository;

    public ObservationBatcher(Properties properties) throws SQLException {
        jpaRepository = new JpaRepository( properties, Database.COMPASS );
    }

    protected void batch() {

        Integer offset = 0;

        List<RemoteObservation> observationsFromRemote = jpaRepository.getObservationsFromRemote( 0 );


        while(!observationsFromRemote.isEmpty()) {

            List<LocalObservation> localObservations = observationsFromRemote.stream().map(LocalObservation::new).collect(Collectors.toList());

            jpaRepository.insertObservations( observationsFromRemote );

            observationsFromRemote = jpaRepository.getObservationsFromRemote(offset);

            offset += observationsFromRemote.size();
        }
    }

    @Override
    public void close() throws Exception {
        jpaRepository.close();
    }
}
