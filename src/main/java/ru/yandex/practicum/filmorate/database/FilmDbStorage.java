package ru.yandex.practicum.filmorate.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage.BIRTHDAY_FILM;
import static ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage.MAX_LENGTH_DESCRIPTION;

@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        HashMap<Integer, List<Genre>> genreMap = getGenreOnFilms();
        String sql = "select f.*,\n" +
                "rn.RATING_NAME,\n" +
                "g.GENRE_ID,\n" +
                "gn.GENRE_NAME\n" +
                "from films f\n" +
                "LEFT JOIN RATING_NAME rn ON f.RATING_ID = rn.RATING_ID\n" +
                "LEFT JOIN GENRE g ON f.FILM_ID = g.FILM_ID\n" +
                "LEFT JOIN GENRE_NAME gn ON g.GENRE_ID = gn.GENRE_ID;";
        return jdbcTemplate.query(sql, new RowMapper<Film>() {
            public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
                Film film = new Film();
                film.setId(rs.getInt("film_id"));
                film.setName(rs.getString("film_name"));
                film.setDescription(rs.getString("DESCRIPTION"));
                film.setReleaseDate(rs.getDate("release_date").toLocalDate());
                film.setDuration(rs.getInt("duration"));
                film.setMpa(new MPA(rs.getInt("rating_id"), rs.getString("rating_name")));
                if (genreMap.containsKey(film.getId()) && genreMap != null) {
                    TreeSet<Genre> genres = new TreeSet<>(genreMap.get(film.getId()));
                    film.setGenres(genres);
                } else {
                    film.setGenres(new TreeSet<>());
                }
                return film;
            }
        });
    }

    @Override
    public Film getFilm(int id) {
        HashMap<Integer, List<Genre>> genres = getGenreOnFilms();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select f.*,\n" +
                "rn.RATING_NAME,\n" +
                "from films f\n" +
                "LEFT JOIN RATING_NAME rn ON f.RATING_ID = rn.RATING_ID\n" +
                "where f.film_id = ?;", id);
        if (filmRows.next()) {
            log.info("Фильм найден: {} {}", filmRows.getInt("film_id"),
                    filmRows.getString("film_name"));
            MPA mpa = new MPA(filmRows.getInt("rating_id"), filmRows.getString("rating_name"));
            TreeSet<Genre> setGenre = new TreeSet<>();
            if (genres.containsKey(filmRows.getInt("film_id"))) {
                setGenre = new TreeSet<>(genres.get(filmRows.getInt("film_id")));
            }

            Film film = new Film(
                    filmRows.getInt("film_id"),
                    filmRows.getString("film_name"),
                    filmRows.getString("DESCRIPTION"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"),
                    mpa,
                    filmRows.getInt("rate"),
                    setGenre);
            return film;
        } else {
            log.info("Фильм {} не найден ", id);
            throw new NullPointerException("Такого фильма нет");
        }
    }

    @Override
    public Film createFilm(Film film) {
        validationFilm(film);
        String sqlFilms = "insert into films (film_name, DESCRIPTION, RELEASE_DATE, DURATION, rating_id, rate) " +
                "values (?, ?, ?, ?, ?, ?)";
        String sqlGenre = "insert into genre (film_id, genre_id) " +
                "values (?, ?)";

        jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), film.getRate());
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select film_id from films where film_name IN (?)",
                film.getName());
        int filmId = 0;
        if (filmRows.next()) {
            filmId = filmRows.getInt("film_id");
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlGenre, filmId, genre.getId());
            }
        }
        film.setId(filmId);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validationFilm(film);
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select film_id from films where film_id = ?",
                film.getId());
        if (!filmRows.next()) {
            throw new NullPointerException("Такого фильма нет");
        }
        String sqlFilms = "update films set " + "film_name = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ?, " +
                "rating_id = ?" +
                "where film_id = ?";
        jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        String sqlDeleteGenre = "DELETE FROM genre WHERE film_id = ?";
        jdbcTemplate.update(sqlDeleteGenre, film.getId());
        if (film.getGenres().size() > 0) {
            String sqlGenre = "insert into genre (film_id, genre_id) values (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlGenre, film.getId(), genre.getId());
            }
        }
        return film;
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

    private HashMap<Integer, List<Genre>> getGenreOnFilms() {
        String sqlFilmsOnGenre = "select f.FILM_ID,\n" +
                "g.GENRE_ID,\n" +
                "gn.GENRE_NAME\n" +
                "from films f\n" +
                "LEFT JOIN GENRE g ON f.FILM_ID = g.FILM_ID\n" +
                "LEFT JOIN GENRE_NAME gn ON g.GENRE_ID = gn.GENRE_ID\n" +
                "ORDER BY f.FILM_ID;";
        HashMap<Integer, List<Genre>> genreFilms = new HashMap<>();
        jdbcTemplate.query(sqlFilmsOnGenre, new RowMapper<Genre>() {
            public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
                int filmId = rs.getInt("film_id");
                if (rs.getInt("genre_id") == 0) {
                    return null;
                }
                if (genreFilms.containsKey(filmId)) {
                    genreFilms.get(filmId).add(new Genre(rs.getInt("genre_id"),
                            rs.getString("genre_name")));
                } else {
                    List<Genre> genres = new ArrayList<>();
                    genres.add(new Genre(rs.getInt("genre_id"), rs.getString("genre_name")));
                    genreFilms.put(filmId, genres);
                }
                return null;
            }
        });
        return genreFilms;
    }

    public Integer getLikes(int filmId) {
        String sql = "select rate from films " +
                "where film_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, filmId);
    }

    public int addLike(int filmId, int userId) {
        String sql = "MERGE INTO LIKES (USER_ID, FILM_ID) KEY(USER_ID, FILM_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
        String sqlGetRate = "select rate from films " +
                "where film_id = ?";
        int rate = jdbcTemplate.queryForObject(sqlGetRate, int.class, filmId);
        String sqlRate = "update films set "
                + "rate = ? " +
                "where film_id = ?";
        jdbcTemplate.update(sqlRate, rate + 1, filmId);
        return getFilm(filmId).getRate();
    }

    public Integer deleteLike(int filmId, int userId) {
        String sql = "DELETE FROM LIKES " +
                "WHERE USER_ID = ? AND FILM_ID = ?";
        jdbcTemplate.update(sql, userId, filmId);
        String sqlGetRate = "select rate from films " +
                "where film_id = ?";
        int rate = jdbcTemplate.queryForObject(sqlGetRate, int.class, filmId);
        String sqlRate = "update films set "
                + "rate = ? " +
                "where film_id = ?";
        jdbcTemplate.update(sqlRate, rate - 1, filmId);
        return getFilm(filmId).getRate();
    }
}