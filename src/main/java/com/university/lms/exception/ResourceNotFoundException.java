package com.university.lms.exception;

/** Raised when a requested entity (by id) does not exist. */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String entityType, Long id) {
        super(entityType + " not found: " + id);
    }
}
