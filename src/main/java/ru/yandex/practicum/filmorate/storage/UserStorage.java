package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    public List<User> getUsers();

    public User getUser(int id);

    public User postUser(User user);

    public User putUsers(User user);

    public void validationUser(User user);
}
