package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    public List<Film> getFilms();

    public Film getFilm(int id);

    public Film postFilm(Film film);

    public Film putFilm(Film film);

    public void validationFilm(Film film);
}
