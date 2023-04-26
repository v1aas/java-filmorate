package ru.yandex.practicum.filmorate.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class MPADao {
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    public MPADao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MPA> getRatings() {
        String sql = "select * from rating_name";
        return jdbcTemplate.query(sql, new RowMapper<MPA>() {
            public MPA mapRow(ResultSet rs, int rowNum) throws SQLException {
                MPA mpa = new MPA();
                mpa.setId(rs.getInt("rating_id"));
                mpa.setName(rs.getString("rating_name"));
                return mpa;
            }
        });
    }

    public MPA getRating(int id) {
        SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating_name where rating_id = ?", id);
        if (ratingRows.next()) {
            log.info("Рейтинг найден: {}", ratingRows.getString("rating_name"));
            MPA mpa = new MPA(
                    ratingRows.getInt("rating_id"),
                    ratingRows.getString("rating_name"));
            return mpa;
        } else {
            log.info("Рейтинг {} не найден ", id);
            throw new NullPointerException("Такого рейтинга нет");
        }
    }
}