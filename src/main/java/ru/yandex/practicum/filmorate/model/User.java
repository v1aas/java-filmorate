package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private int id;
    @NotNull
    @Email
    private String email;
    @NotNull
    private String login;
    private String name;
    private LocalDate birthday;

    public User(int id, String email, String login, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.birthday = birthday;
    }
}
