package ru.yandex.practicum.filmorate.exceptions;

public class UpdateTargetNotFoundValidationException extends RuntimeException {
    public UpdateTargetNotFoundValidationException(String message) {
        super(message);
    }
}
