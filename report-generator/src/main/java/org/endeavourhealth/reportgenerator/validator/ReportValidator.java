package org.endeavourhealth.reportgenerator.validator;

import lombok.extern.slf4j.Slf4j;
import org.endeavourhealth.reportgenerator.model.Report;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;


@Slf4j
public class ReportValidator {

    private final Validator validator;

    public ReportValidator() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    public Set<ConstraintViolation<Report>> validate(Report report) {

        Set<ConstraintViolation<Report>> constraintViolations = validator.validate( report );

        report.setConstraintViolations( constraintViolations );

        if(!constraintViolations.isEmpty()) {
            log.warn("Report has failed validations {}", constraintViolations);
//            report.setIsValid( false );
        } else {
//            report.setIsValid( true );
        }

        return validator.validate( report );
    }
}
