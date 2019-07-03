package org.endeavourhealth.datasetextractor.beans;

import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString
public class Delta {

	private List<String> row;

	private DeltaType type;

    public void populateRow(Object[] data) {

        List<String> row = new ArrayList<>();

        for(Object o : data) {
            if(o == null) row.add(""); else row.add(o.toString());
        }
    }
}
