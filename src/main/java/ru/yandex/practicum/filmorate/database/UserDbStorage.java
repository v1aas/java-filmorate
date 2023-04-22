package ru.yandex.practicum.filmorate.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Component
public class UserDbStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;
    private final JdbcTemplate jdbcTemplate;
    private final Logger log = LoggerFactory.getLogger(UserDbStorage.class);

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, new RowMapper<User>() {
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setEmail(rs.getString("email"));
                user.setLogin(rs.getString("login"));
                user.setName(rs.getString("user_name"));
                user.setBirthday(rs.getDate("birthday").toLocalDate());
                user.setFriendList(null);
                return user;
            }
        });
    }

    @Override
    public User getUser(int id) {
        Set<Integer> friends = getSetFriends(id);
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", id);
        if (userRows.next()) {
            log.info("Пользователь найден: {} {}", userRows.getInt("user_id"),
                    userRows.getString("email"));
            User user = new User(
                    userRows.getInt("user_id"),
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("user_name"),
                    userRows.getDate("birthday").toLocalDate(),
                    friends);
            return user;
        } else {
            log.info("Пользователь {} не найден ", id);
            throw new NullPointerException("Такого пользователя нет");
        }
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setId(++id);
        String sql = "insert into users (email, login, user_name, birthday) " +
                "values (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());
        users.put(id, user);
        return user;
    }

    @Override
    public User updateUsers(User user) {
        validateUser(user);
        if (!users.containsKey(user.getId())) {
            throw new NullPointerException("Такого пользователя нет");
        }
        String sql = "update users set " + "email = ?, login = ?, user_name = ?, birthday = ? " +
                "where user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    @Override
    public void addFriend(int id, int friendId) {
        if (!(users.containsKey(id) && users.containsKey(friendId))) {
            throw new ValidationException("Такого пользователя нет");
        }
        User user = users.get(id);
        User friend = users.get(friendId);
        int status = 1;
        if (friend.getFriendList().contains(id)) {
            status = 2;
            String sqlAddFriends = "INSERT INTO friendship (user_id,friend_id,status_id)" +
                    "VALUES (?, ?, ?)";
            jdbcTemplate.update(sqlAddFriends,
                    id,
                    friendId,
                    status);
            user.addFriend(friendId);
            String sqlUpdateFriendship = "UPDATE FRIENDSHIP SET STATUS_ID = ?" +
                    "WHERE USER_ID= ?";
            jdbcTemplate.update(sqlUpdateFriendship, status, id);
        } else {
            String sqlAddFriends = "INSERT INTO friendship (user_id,friend_id,status_id)" +
                    "VALUES (?, ?, ?)";
            jdbcTemplate.update(sqlAddFriends,
                    id,
                    friendId,
                    status);
            user.addFriend(friendId);
        }
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        if (!(users.containsKey(id) && users.containsKey(friendId))) {
            throw new ValidationException("Такого пользователя нет");
        }
        User user = users.get(id);
        User friend = users.get(friendId);
        if (user.getFriendList().contains(friendId) && friend.getFriendList().contains(id)) {
            user.deleteFriend(friendId);
            String sqlUpdateFriendship = "UPDATE FRIENDSHIP SET STATUS_ID = 1" +
                    "WHERE USER_ID= ?";
            jdbcTemplate.update(sqlUpdateFriendship, friendId);
            String sqlDeleteFriend = "DELETE FROM FRIENDSHIP\n" +
                    "WHERE user_id = ?";
            jdbcTemplate.update(sqlDeleteFriend, id);
        }
        String sqlDeleteFriend = "DELETE FROM FRIENDSHIP\n" +
                "WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sqlDeleteFriend, id, friendId);
    }

    @Override
    public void validateUser(User user) {
        if (user.getEmail().isEmpty() || !(user.getEmail().contains("@"))) {
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }

    public Set<Integer> getSetFriends(int id) {
        HashSet<Integer> friendSet = new HashSet<>();
        String sqlGetFriends = "select u.USER_ID,\n" +
                "f.FRIEND_ID \n" +
                "from users u\n" +
                "RIGHT JOIN FRIENDSHIP f ON u.USER_ID = f.USER_ID " +
                "WHERE u.user_id = " + id;
        jdbcTemplate.query(sqlGetFriends, new RowMapper<Integer>() {
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                int friendId = rs.getInt("friend_id");
                friendSet.add(friendId);
                return null;
            }
        });
        return friendSet;
    }
}
