package com.university.lms.exception;

/** Raised when a scanned/typed member identifier does not resolve to an active membership. */
public class NoActiveMembershipException extends BusinessException {

    public NoActiveMembershipException(String memberIdentifier) {
        super("No active membership found for '" + memberIdentifier + "'.");
    }
}
