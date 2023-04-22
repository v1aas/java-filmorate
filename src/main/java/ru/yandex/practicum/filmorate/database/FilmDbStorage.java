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
import java.util.*;

import static ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage.BIRTHDAY_FILM;
import static ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage.MAX_LENGTH_DESCRIPTION;

@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);
    private Map<Integer, Film> films = new HashMap<>();
    private int id = 0;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getFilms() {
        HashMap<Integer, List<Genre>> genreMap = getGenreOnFilms();
        String sql = "select f.*,\n" +
                "r.RATING_ID,\n" +
                "rn.RATING_NAME,\n" +
                "g.GENRE_ID,\n" +
                "gn.GENRE_NAME\n" +
                "from films f\n" +
                "LEFT JOIN RATING r ON f.FILM_ID = r.FILM_ID\n" +
                "LEFT JOIN RATING_NAME rn ON r.RATING_ID = rn.RATING_ID\n" +
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
                "r.RATING_ID,\n" +
                "rn.RATING_NAME,\n" +
                "from films f\n" +
                "LEFT JOIN RATING r ON f.FILM_ID = r.FILM_ID\n" +
                "LEFT JOIN RATING_NAME rn ON r.RATING_ID = rn.RATING_ID\n" +
                "where f.film_id = ?;", id);
        if (filmRows.next()) {
            log.info("Фильм найден: {} {}", filmRows.getInt("film_id"),
                    filmRows.getString("film_name"));
            MPA mpa = new MPA(filmRows.getInt("rating_id"), filmRows.getString("rating_name"));
            TreeSet<Genre> setGenre = new TreeSet<>();
            Set<Integer> likes = new HashSet<>(getLikes(id));
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
                    likes,
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
        film.setId(++id);
        String sqlFilms = "insert into films (film_name, DESCRIPTION, RELEASE_DATE, DURATION) " +
                "values (?, ?, ?, ?)";
        String sqlMPA = "insert into rating (film_id, rating_id) " +
                "values (?, ?)";
        String sqlGenre = "insert into genre (film_id, genre_id) " +
                "values (?, ?)";
        jdbcTemplate.update(sqlFilms,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration());
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select film_id from films where film_name IN (?)",
                film.getName());
        if (filmRows.next()) {
            int filmId = filmRows.getInt("film_id");
            jdbcTemplate.update(sqlMPA, filmId, film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlGenre, id, genre.getId());
            }
        }
        films.put(id, film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validationFilm(film);
        if (!films.containsKey(film.getId())) {
            throw new NullPointerException("Такого фильма нет");
        }
        String sqlFilms = "update films set " + "film_name = ?, DESCRIPTION = ?, RELEASE_DATE = ?, DURATION = ? " +
                "where film_id = ?";
        String sqlMPA = "update rating set " + "rating_id = ? " +
                "where film_id = ?";
        jdbcTemplate.update(sqlFilms, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getId());
        jdbcTemplate.update(sqlMPA, film.getMpa().getId(), film.getId());
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

    public List<Integer> getLikes(int filmId) {
        String sql = "select user_id from likes " +
                "where film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("user_id"), filmId);
    }

    public int addLike(int filmId, int userId) {
        String sql = "INSERT INTO LIKES (USER_ID,FILM_ID)" +
                "VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, filmId);
        films.get(filmId).addLikes(userId);
        return films.get(filmId).getLikes().size();
    }

    public Integer deleteLike(int filmId, int userId) {
        String sql = "DELETE FROM LIKES " +
                "WHERE USER_ID = ? AND FILM_ID = ?";
        jdbcTemplate.update(sql, userId, filmId);
        films.get(filmId).deleteLikes(userId);
        return films.get(filmId).getLikes().size();
    }
}