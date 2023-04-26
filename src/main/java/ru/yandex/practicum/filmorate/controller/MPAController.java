package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.database.MPADao;
import ru.yandex.practicum.filmorate.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.MPA;

import java.util.List;

@RestController
public class MPAController {

    private final MPADao mpaDao;

    @Autowired
    public MPAController(MPADao mpaDao) {
        this.mpaDao = mpaDao;
    }

    @GetMapping("/mpa")
    public List<MPA> getRatings() {
        return mpaDao.getRatings();
    }

    @GetMapping("/mpa/{id}")
    public MPA getRating(@PathVariable int id) {
        return mpaDao.getRating(id);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationExp(final ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse nullExp(final NullPointerException e) {
        return new ErrorResponse(e.getMessage());
    }
}