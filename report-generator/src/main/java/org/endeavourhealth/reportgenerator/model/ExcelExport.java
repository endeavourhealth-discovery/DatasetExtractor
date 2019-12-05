package org.endeavourhealth.reportgenerator.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import javax.persistence.Transient;

@Entity
@Data
public class ExcelExport extends Export {

    @Transient
    private String password;
}
