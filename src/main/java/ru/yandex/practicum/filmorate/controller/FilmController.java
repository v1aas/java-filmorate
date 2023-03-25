package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FilmController {
    final static int MAX_LENGTH_DESCRIPTION = 200;
    private Map<Integer,Film> films = new HashMap<>();
    private final LocalDate birthdayFilms = LocalDate.of(1895, 12, 28);
    private int id = 0;

    @GetMapping("/films")
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @PostMapping("/films")
    public Film postFilm(@RequestBody Film film) {
        validationFilm(film);
        film.setId(++id);
        films.put(id, film);
        return film;
    }

    @PutMapping("/films")
    public Film putFilm(@RequestBody Film film) {
        validationFilm(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        throw new ValidationException("Такого фильма нет");
    }

    private void validationFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Фильм должен иметь название");
        }
        if (film.getDescription().getBytes().length > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(birthdayFilms)) {
            throw new ValidationException("Дата релиза должна быть позже!");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
