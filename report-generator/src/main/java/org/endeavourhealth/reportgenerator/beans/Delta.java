package org.endeavourhealth.reportgenerator.beans;

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
            if(o == null) row.add("");
            else {
                if(o instanceof byte[]) continue;
                row.add(o.toString());
            }
        }

        this.row = row;
    }
}
