package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUsers();

    User getUser(int id);

    User postUser(User user);

    User putUsers(User user);

    void validationUser(User user);
}
