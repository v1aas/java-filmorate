package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.database.FilmDbStorage;
import ru.yandex.practicum.filmorate.database.UserDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTest {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final FilmService filmService;
    private final UserService userService;
    private User firstUser;
    private User secondUser;
    private User thirdUser;
    private Film firstFilm;
    private Film secondFilm;
    private Film thirdFilm;

    @BeforeEach
    public void beforeEach() {
        firstUser = new User();
        firstUser.setName("First");
        firstUser.setLogin("First");
        firstUser.setEmail("first@mail.ru");
        firstUser.setBirthday(LocalDate.of(2000, 11, 11));

        secondUser = new User();
        secondUser.setName("Second");
        secondUser.setLogin("Second");
        secondUser.setEmail("second@mail.ru");
        secondUser.setBirthday(LocalDate.of(2000, 11, 12));

        thirdUser = new User();
        thirdUser.setName("third");
        thirdUser.setLogin("third");
        thirdUser.setEmail("third@mail.ru");
        thirdUser.setBirthday(LocalDate.of(2000, 11, 13));

        firstFilm = new Film();
        firstFilm.setName("Первый");
        firstFilm.setDescription("описание");
        firstFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        firstFilm.setDuration(111);
        firstFilm.setMpa(new MPA(1, "G"));
        firstFilm.setRate(0);
        firstFilm.setGenres(new TreeSet<>(Arrays.asList(new Genre(2, "Драма"),
                new Genre(1, "Комедия"))));

        secondFilm = new Film();
        secondFilm.setName("Второй");
        secondFilm.setDescription("описание");
        secondFilm.setReleaseDate(LocalDate.of(2000, 2, 10));
        secondFilm.setDuration(100);
        secondFilm.setMpa(new MPA(3, "PG-13"));
        secondFilm.setRate(0);
        secondFilm.setGenres(new TreeSet<>(Arrays.asList(new Genre(6, "Боевик"))));

        thirdFilm = new Film();
        thirdFilm.setName("Третий");
        thirdFilm.setDescription("описание");
        thirdFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        thirdFilm.setDuration(105);
        thirdFilm.setMpa(new MPA(4, "R"));
        thirdFilm.setRate(0);
        thirdFilm.setGenres(new TreeSet<>(Arrays.asList(new Genre(2, "Драма"))));
    }

    @Test
    public void shouldCreateUserAndGetUserById() {
        firstUser = userStorage.createUser(firstUser);
        Optional<User> userOptional = Optional.ofNullable(userStorage.getUser(firstUser.getId()));
        assertThat(userOptional)
                .hasValueSatisfying(user ->
                        assertThat(user)
                                .hasFieldOrPropertyWithValue("id", firstUser.getId())
                                .hasFieldOrPropertyWithValue("name", "First"));
    }

    @Test
    public void shouldGetUsers() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        List<User> listUsers = userStorage.getUsers();
        assertEquals(2, listUsers.size());
    }

    @Test
    public void shouldUpdateUser() {
        firstUser = userStorage.createUser(firstUser);
        User updateUser = new User();
        updateUser.setId(firstUser.getId());
        updateUser.setName("UpdateMisterFirst");
        updateUser.setLogin("First");
        updateUser.setEmail("1@ya.ru");
        updateUser.setBirthday(LocalDate.of(1980, 12, 23));
        Optional<User> testUpdateUser = Optional.ofNullable(userStorage.updateUsers(updateUser));
        assertThat(testUpdateUser)
                .hasValueSatisfying(user -> assertThat(user)
                        .hasFieldOrPropertyWithValue("name", "UpdateMisterFirst")
                );
    }


    @Test
    public void shouldCreateFilmAndGetFilmById() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilm(firstFilm.getId()));
        assertThat(filmOptional)
                .hasValueSatisfying(film -> assertThat(film)
                        .hasFieldOrPropertyWithValue("id", firstFilm.getId())
                        .hasFieldOrPropertyWithValue("name", "Первый")
                );
    }

    @Test
    public void shouldGetFilms() {
        firstFilm = filmStorage.createFilm(firstFilm);
        secondFilm = filmStorage.createFilm(secondFilm);
        thirdFilm = filmStorage.createFilm(thirdFilm);
        List<Film> listFilms = filmStorage.getFilms();
        assertThat(listFilms).contains(firstFilm);
        assertThat(listFilms).contains(secondFilm);
        assertThat(listFilms).contains(thirdFilm);
    }

    @Test
    public void shouldUpdateFilm() {
        firstFilm = filmStorage.createFilm(firstFilm);
        Film updateFilm = new Film();
        updateFilm.setId(firstFilm.getId());
        updateFilm.setName("Новый первый");
        updateFilm.setDescription("Новое описание");
        updateFilm.setReleaseDate(LocalDate.of(1975, 11, 19));
        updateFilm.setDuration(133);
        updateFilm.setMpa(new MPA(1, "G"));
        Optional<Film> testUpdateFilm = Optional.ofNullable(filmStorage.updateFilm(updateFilm));
        assertThat(testUpdateFilm).hasValueSatisfying(film ->
                        assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Новый первый")
                                .hasFieldOrPropertyWithValue("description", "Новое описание")
                );
    }

    @Test
    public void shouldAddLike() {
        firstUser = userStorage.createUser(firstUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilm(firstFilm.getId());
        assertEquals(1, firstFilm.getRate());
    }

    @Test
    public void shouldDeleteLike() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());
        filmService.addLike(firstFilm.getId(), secondUser.getId());
        filmService.deleteLike(firstFilm.getId(), firstUser.getId());
        firstFilm = filmStorage.getFilm(firstFilm.getId());
        assertEquals(1, firstFilm.getRate());
    }

    @Test
    public void shouldGetPopularFilms() {

        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);

        firstFilm = filmStorage.createFilm(firstFilm);
        filmService.addLike(firstFilm.getId(), firstUser.getId());

        secondFilm = filmStorage.createFilm(secondFilm);
        filmService.addLike(secondFilm.getId(), firstUser.getId());
        filmService.addLike(secondFilm.getId(), secondUser.getId());
        filmService.addLike(secondFilm.getId(), thirdUser.getId());

        thirdFilm = filmStorage.createFilm(thirdFilm);
        filmService.addLike(thirdFilm.getId(), firstUser.getId());
        filmService.addLike(thirdFilm.getId(), secondUser.getId());

        List<Film> listFilms = filmService.getPopularFilms(3);

        assertThat(listFilms).hasSize(3);

        assertThat(Optional.of(listFilms.get(0)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Третий"));

        assertThat(Optional.of(listFilms.get(1)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Второй"));

        assertThat(Optional.of(listFilms.get(2)))
                .hasValueSatisfying(film ->
                        AssertionsForClassTypes.assertThat(film)
                                .hasFieldOrPropertyWithValue("name", "Первый"));
    }

    @Test
    public void shouldAddFriend() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getFriendList(firstUser.getId())).hasSize(1);
        assertThat(userService.getFriendList(firstUser.getId())).contains(secondUser);
    }

    @Test
    public void shouldDeleteFriend() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.deleteFriend(firstUser.getId(), secondUser.getId());
        assertThat(userService.getFriendList(firstUser.getId())).hasSize(1);
        assertThat(userService.getFriendList(firstUser.getId())).contains(thirdUser);
    }

    @Test
    public void shouldGetFriends() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        assertThat(userService.getFriendList(firstUser.getId())).hasSize(2);
        assertThat(userService.getFriendList(firstUser.getId())).contains(secondUser, thirdUser);
    }

    @Test
    public void shouldGetCommonFriends() {
        firstUser = userStorage.createUser(firstUser);
        secondUser = userStorage.createUser(secondUser);
        thirdUser = userStorage.createUser(thirdUser);
        userService.addFriend(firstUser.getId(), secondUser.getId());
        userService.addFriend(firstUser.getId(), thirdUser.getId());
        userService.addFriend(secondUser.getId(), firstUser.getId());
        userService.addFriend(secondUser.getId(), thirdUser.getId());
        assertThat(userService.getCommonFriendList(firstUser.getId(), secondUser.getId())).hasSize(1);
        assertThat(userService.getCommonFriendList(firstUser.getId(), secondUser.getId()))
                .contains(thirdUser);
    }
}