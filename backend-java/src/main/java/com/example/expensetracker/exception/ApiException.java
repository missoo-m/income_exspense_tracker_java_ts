package com.example.expensetracker.exception;

public abstract class ApiException extends RuntimeException {

    private final int status;

    protected ApiException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
