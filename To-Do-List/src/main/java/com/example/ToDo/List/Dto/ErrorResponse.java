package com.example.ToDo.List.Dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class ErrorResponse {
    private String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
