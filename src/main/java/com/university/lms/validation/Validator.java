package com.university.lms.validation;

/** Contract implemented by every form/DTO validator in the system. */
public interface Validator<T> {

    ValidationResult validate(T input);
}
