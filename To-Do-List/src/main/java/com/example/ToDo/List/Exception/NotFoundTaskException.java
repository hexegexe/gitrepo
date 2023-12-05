package com.example.ToDo.List.Exception;

public class NotFoundTaskException extends RuntimeException {
    public NotFoundTaskException(String notFoundTaskInDb) {
    }
}
