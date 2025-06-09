package com.imperionite.cp2c.dto;

/**
 * Generic DTO for simple API responses that only convey a message (e.g., success or error).
 */
public class MessageResponse {
    private String message;

    // Default constructor for Jackson
    public MessageResponse() {
    }

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
