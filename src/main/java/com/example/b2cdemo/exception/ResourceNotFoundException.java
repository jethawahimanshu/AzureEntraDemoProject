package com.example.b2cdemo.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceName, String id) {
        super("%s with id '%s' was not found".formatted(resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        this(resourceName, id.toString());
    }
}
