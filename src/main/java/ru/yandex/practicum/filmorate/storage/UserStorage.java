package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {

    List<User> getUsers();

    User getUser(int id);

    User createUser(User user);

    User updateUsers(User user);

    void addFriend(int id, int friendId);

    void deleteFriend(int id, int friendId);

    void validateUser(User user);
}
