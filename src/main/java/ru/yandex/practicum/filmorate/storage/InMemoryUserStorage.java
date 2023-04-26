/*
package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 0;

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUser(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            throw new NullPointerException("Такого пользователя нет");
        }
    }

    @Override
    public void addFriend(int id, int friendId) {
        users.get(id).addFriend(friendId);
        users.get(friendId).addFriend(id);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        users.get(id).deleteFriend(friendId);
        users.get(friendId).deleteFriend(id);
    }

    @Override
    public User createUser(User user) {
        validateUser(user);
        user.setId(++id);
        users.put(id, user);
        return user;
    }

    @Override
    public User updateUsers(User user) {
        validateUser(user);
        if (users.containsKey(user.getId())) {
            users.put(user.getId(), user);
            return user;
        }
        throw new NullPointerException("Такого пользователя нет");
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
}
*/
