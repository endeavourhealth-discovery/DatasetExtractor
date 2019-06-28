package org.endeavourhealth.datasetextractor.beans;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Delta {

	private List<String> row;

	private DeltaType type;
}
