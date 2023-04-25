package ru.yandex.practicum.filmorate.database;

import lombok.extern.slf4j.Slf4j;
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
import java.time.LocalDate;
import java.util.*;


@Slf4j
@Component
public class FilmDbStorage implements FilmStorage {
    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final LocalDate BIRTHDAY_FILM = LocalDate.of(1895, 12, 28);
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        HashMap<Integer, LinkedList<Genre>> genreMap = getGenreOnFilms();
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
                film.setRate(rs.getInt("rate"));
                film.setMpa(new MPA(rs.getInt("rating_id"), rs.getString("rating_name")));
                if (genreMap.containsKey(film.getId()) && genreMap != null) {
                    LinkedHashSet<Genre> genres = new LinkedHashSet<>(genreMap.get(film.getId()));
                    film.setGenres(genres);
                } else {
                    film.setGenres(new LinkedHashSet<>());
                }
                return film;
            }
        });
    }

    @Override
    public Film getFilm(int id) {
        HashMap<Integer, LinkedList<Genre>> genres = getGenreOnFilms();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select f.*,\n" +
                "rn.RATING_NAME,\n" +
                "from films f\n" +
                "LEFT JOIN RATING_NAME rn ON f.RATING_ID = rn.RATING_ID\n" +
                "where f.film_id = ?;", id);
        if (filmRows.next()) {
            log.info("Фильм найден: {} {}", filmRows.getInt("film_id"),
                    filmRows.getString("film_name"));
            MPA mpa = new MPA(filmRows.getInt("rating_id"), filmRows.getString("rating_name"));
            LinkedHashSet<Genre> setGenre = new LinkedHashSet<Genre>();
            if (genres.containsKey(filmRows.getInt("film_id"))) {
                setGenre.addAll(genres.get(filmRows.getInt("film_id")));
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

    private HashMap<Integer, LinkedList<Genre>> getGenreOnFilms() {
        String sqlFilmsOnGenre = "select f.FILM_ID,\n" +
                "g.GENRE_ID,\n" +
                "gn.GENRE_NAME\n" +
                "from films f\n" +
                "LEFT JOIN GENRE g ON f.FILM_ID = g.FILM_ID\n" +
                "LEFT JOIN GENRE_NAME gn ON g.GENRE_ID = gn.GENRE_ID\n" +
                "ORDER BY f.FILM_ID;";
        HashMap<Integer, LinkedList<Genre>> genreFilms = new HashMap<>();
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
                    LinkedList<Genre> genres = new LinkedList<>();
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

    public void addLike(int filmId, int userId) {
        if (filmId < 0 || userId < 0) {
            throw new NullPointerException("ID не может быть отрицательным");
        }
        SqlRowSet sqlGetLikes = jdbcTemplate.queryForRowSet("select * from likes where film_id = " + filmId);
        if (sqlGetLikes.next()) {
            if (sqlGetLikes.getInt("user_id") == userId) {
                return;
            }
        }
        String sql = "MERGE INTO LIKES (USER_ID, FILM_ID) KEY(USER_ID, FILM_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
        String sqlGetRate = "select rate from films " +
                "where film_id = ?";
        int rate = jdbcTemplate.queryForObject(sqlGetRate, int.class, filmId);
        String sqlRate = "update films set "
                + "rate = ? " +
                "where film_id = ?";
        jdbcTemplate.update(sqlRate, rate + 1, filmId);
    }

    public void deleteLike(int filmId, int userId) {
        if (filmId < 0 || userId < 0) {
            throw new NullPointerException("ID не может быть отрицательным");
        }
        SqlRowSet sqlGetLikes = jdbcTemplate.queryForRowSet("select * from likes where film_id = " + filmId);
        if (sqlGetLikes.next()) {
            if (sqlGetLikes.getInt("user_id") != userId) {
                return;
            }
        }
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
    }
}