package ru.netology.graduatework.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorInResponse {
    private final String message;
    private final int id;
}