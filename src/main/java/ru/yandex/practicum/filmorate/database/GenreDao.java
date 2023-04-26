package ru.yandex.practicum.filmorate.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class GenreDao {
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    public GenreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Genre> getGenres() {
        String sql = "select * from genre_name";
        return jdbcTemplate.query(sql, new RowMapper<Genre>() {
            public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("genre_name"));
                return genre;
            }
        });
    }

    public Genre getGenre(int id) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet("select * from genre_name where genre_id = ?", id);
        if (genreRows.next()) {
            log.info("Жанр найден: {}", genreRows.getString("genre_name"));
            Genre genre = new Genre(
                    genreRows.getInt("genre_id"),
                    genreRows.getString("genre_name"));
            return genre;
        } else {
            log.info("Жанр {} не найден ", id);
            throw new NullPointerException("Такого жанра нет");
        }
    }
}