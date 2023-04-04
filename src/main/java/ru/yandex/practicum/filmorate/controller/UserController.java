package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.error.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.util.List;
import java.util.Set;

@RestController
public class UserController {
    private InMemoryUserStorage userStorage;
    private UserService userService;

    @Autowired
    public UserController(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
        this.userService = new UserService(userStorage);
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable int id) {
        return userStorage.getUser(id);
    }

    @GetMapping("/users/{id}/friends")
    public List<User> getFriendList(@PathVariable int id) {
        return userService.getFriendList(id);
    }

    @GetMapping("/users/{id}/friends/common/{friendId}")
    public List<User> getCommonFriendList(@PathVariable int id, @PathVariable int friendId) {
        return userService.getCommonFriendList(id, friendId);
    }

    @PostMapping("/users")
    public User postUser(@RequestBody User user) {
        userStorage.postUser(user);
        return user;
    }

    @PutMapping("/users")
    public User putUsers(@RequestBody User user) {
        userStorage.putUsers(user);
        return user;
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public Set<Integer> addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
        return userStorage.getUser(id).getFriendList();
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public Set<Integer> deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFriend(id, friendId);
        return userStorage.getUsers().get(id).getFriendList();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse validationExp(final ValidationException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse nullExp(final NullPointerException e) {
        return new ErrorResponse(e.getMessage());
    }
}
