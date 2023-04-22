package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.database.UserDbStorage;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserDbStorage userStorage;

    @Autowired
    public UserService(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getFriendList(int id) {
        List<User> userList = new ArrayList<>();
        for (Integer userId : userStorage.getUser(id).getFriendList()) {
            userList.add(userStorage.getUser(userId));
        }
        return userList;
    }

    public void addFriend(int id, int friendId) {
        if (userStorage.getUser(friendId) == null || userStorage.getUser(id) == null) {
            throw new NullPointerException("Такого пользователя нет");
        } else {
            userStorage.addFriend(id, friendId);
        }
    }

    public void deleteFriend(int id, int friendId) {
        if (userStorage.getUser(friendId) == null || userStorage.getUser(id) == null) {
            throw new NullPointerException("Такого пользователя нет");
        } else {
            userStorage.deleteFriend(id, friendId);
        }
    }

    public List<User> getCommonFriendList(int id, int friendId) {
        List<User> commonUserList = new ArrayList<>();
        Set<Integer> commonSet = new HashSet<>(userStorage.getUser(id).getFriendList());
        commonSet.retainAll(userStorage.getUser(friendId).getFriendList());
        for (Integer userId : commonSet) {
            commonUserList.add(userStorage.getUser(userId));
        }
        return commonUserList;
    }
}
