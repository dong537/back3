package com.example.demo.exception;

public class McpApiException extends RuntimeException {
    public McpApiException(String message) {
        super(message);
    }

    public McpApiException(String message, Throwable cause) {
        super(message, cause);
    }
}