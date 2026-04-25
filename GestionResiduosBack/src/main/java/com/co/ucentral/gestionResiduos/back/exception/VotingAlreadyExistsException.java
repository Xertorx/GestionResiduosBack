package com.co.ucentral.gestionResiduos.back.exception;

/**
 * Deprecated: previously used to signal duplicate voting. Kept for compatibility but no longer thrown.
 */
@Deprecated
public class VotingAlreadyExistsException extends RuntimeException {
    public VotingAlreadyExistsException(String message) {
        super(message);
    }
}

