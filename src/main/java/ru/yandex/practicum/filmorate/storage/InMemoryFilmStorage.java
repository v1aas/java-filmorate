package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private static final int MAX_LENGTH_DESCRIPTION = 200;
    private static final LocalDate BIRTHDAY_FILM = LocalDate.of(1895, 12, 28);
    private Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilm(int id) {
        if (films.get(id) == null) {
            throw new NullPointerException("Такого фильма нет");
        } else {
            return films.get(id);
        }
    }


    @Override
    public Film createFilm(Film film) {
        validationFilm(film);
        film.setId(++id);
        films.put(id, film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validationFilm(film);
        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            return film;
        }
        throw new NullPointerException("Такого фильма нет");
    }

    @Override
    public void validationFilm(Film film) {
        if (film.getName().isEmpty()) {
            throw new ValidationException("Фильм должен иметь название");
        }
        if (film.getDescription().getBytes().length > MAX_LENGTH_DESCRIPTION) {
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate().isBefore(BIRTHDAY_FILM)) {
            throw new ValidationException("Дата релиза должна быть позже!");
        }
        if (film.getDuration() < 0) {
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
