package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class FilmController {
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);
    private List<Film> films = new ArrayList<>();
    private LocalDate birthdayFilms = LocalDate.of(1895, 12, 28);
    private int id = 0; // Вообще в тз не прописано, чтоб мы что-то делали с логикой, однако для прохождения тестов
    // я решил сделать простенький генератор айди

    @GetMapping("/films")
    public List<Film> getFilms() {
        return films;
    }

    @PostMapping("/films")
    public Film postFilm(@RequestBody Film film) throws ValidationException {
        validationFilm(film);
        film.setId(++id);
        films.add(film);
        return film;
    }

    @PutMapping("/films")
    public Film putFilm(@RequestBody Film film) throws ValidationException {
        validationFilm(film);
        for (Film film1 : films) {
            if (film1.getId() == film.getId()) {
                films.remove(film1);
                films.add(film);
                return film;
            }
        }
        throw new ValidationException("Такого фильма нет");
    }

    public void validationFilm(Film film) throws ValidationException {
        if (film.getName().isEmpty()) {
            log.error("Ошибка названия");
            throw new ValidationException("Фильм должен иметь название");
        }
        if (film.getDescription().getBytes().length > 200) {
            log.error("Ошибка описания");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(birthdayFilms)) {
            log.error("Ошибка даты");
            throw new ValidationException("Дата релиза должна быть позже!");
        }
        if (film.getDuration() < 0) {
            log.error("Ошибка продолжительности");
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
