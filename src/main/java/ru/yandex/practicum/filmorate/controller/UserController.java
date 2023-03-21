package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
public class UserController {
    private final static Logger log = LoggerFactory.getLogger(UserController.class);
    private List<User> users = new ArrayList<>();
    private int id = 0; // Вообще в тз не прописано, чтоб мы что-то делали с логикой, однако для прохождения тестов
    // я решил сделать простенький генератор айди

    @GetMapping("/users")
    public List<User> getUsers() {
        return users;
    }

    @PostMapping("/users")
    public User postUser(@RequestBody User user) throws ValidationException {
        validationUser(user);
        user.setId(++id);
        users.add(user);
        return user;
    }

    @PutMapping("/users")
    public User putUsers(@RequestBody User user) throws ValidationException {
        validationUser(user);
        for (User user1 : users) {
            if (user1.getId() == user.getId()) {
                users.remove(user1);
                users.add(user);
                return user;
            }
        }
        throw new ValidationException("Такого пользователя нет");
    }

    public void validationUser(User user) throws ValidationException {
        if (user.getEmail().isEmpty() || !(user.getEmail().contains("@"))) {
            log.error("Ошибка почты");
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            log.error("Ошибка логина");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }
        if (user.getName()==null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Ошибка даты рождения");
            throw new ValidationException("дата рождения не может быть в будущем");
        }
    }
}
