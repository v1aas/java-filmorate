package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        log.error(message);
    }
    public String getMessage() {
        return "Некорректно введены данные!";
    }
}
