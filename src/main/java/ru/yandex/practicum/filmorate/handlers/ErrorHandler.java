package ru.yandex.practicum.filmorate.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UpdateTargetNotFoundValidationException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleValidation(ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handleNotFound(RuntimeException e) {
        return new ErrorResponse("Объект не найден");
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorResponse handleThrowable(Throwable e) {
        return new ErrorResponse("Внутренняя ошибка");
    }

    @ExceptionHandler(UpdateTargetNotFoundValidationException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404 для апдейта несуществующего ресурса
    public ErrorResponse handleUpdateTargetMissing(UpdateTargetNotFoundValidationException e) {
        return new ErrorResponse (e.getMessage());
    }

}

