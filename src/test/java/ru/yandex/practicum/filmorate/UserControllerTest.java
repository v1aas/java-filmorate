package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

public class UserControllerTest {

    User user;

    UserController userController = new UserController();

    @BeforeEach
    public void BeforeEach() {
        user = new User(1, "Myfilm@mal.ru", "Descript", "Nick",
                LocalDate.of(2001, 12, 18));
    }

    @Test
    public void twoUsersInList() {
        userController.getUsers().add(user);
        User user2 = new User(2, "Myf2ilm@mal.ru", "Des2cript", "Ni2ck",
                LocalDate.of(2004, 12, 18));
        try {
            userController.postUser(user2);
            Assertions.assertEquals(2, userController.getUsers().size());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void IncorrectEmail() {
        user.setEmail("");
        Assertions.assertThrows(ValidationException.class, () -> userController.postUser(user));
        user.setEmail("MyFilm.ru");
        Assertions.assertThrows(ValidationException.class, () -> userController.postUser(user));
        Assertions.assertEquals(0, userController.getUsers().size());
    }

    @Test
    public void IncorrectLogin() {
        user.setLogin("");
        Assertions.assertThrows(ValidationException.class, () -> userController.postUser(user));
        user.setLogin("Nick NAme");
        Assertions.assertThrows(ValidationException.class, () -> userController.postUser(user));
        Assertions.assertEquals(0, userController.getUsers().size());
    }

    @Test
    public void NullName() {
        user.setName("");
        try {
            userController.postUser(user);
            Assertions.assertEquals(user.getLogin(), user.getName());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void OldReleaseDate() {
        user.setBirthday(LocalDate.of(2101, 1, 1));
        Assertions.assertThrows(ValidationException.class, () -> userController.postUser(user));
        Assertions.assertEquals(0, userController.getUsers().size());
    }

    @Test
    public void PutIncorrectUser() {
        try {
            userController.postUser(user);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        User user2 = new User(3, "Myf2ilm@mal.ru", "Des2cript", "Ni2ck",
                LocalDate.of(2004, 12, 18));
        Assertions.assertThrows(ValidationException.class, () -> userController.putUsers(user2));
        Assertions.assertEquals(1, userController.getUsers().size());
    }
}
