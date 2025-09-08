package com.reliaquest.api.validator;

import java.util.Optional;
import java.util.UUID;

public class UUIDValidator {
    public static Optional<UUID> parseUUID(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
