package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;
import java.util.HashSet;

public class FilmControllerTest {

    Film film;

    FilmController filmController = new FilmController(new InMemoryFilmStorage());

    @BeforeEach
    public void beforeEach() {
        film = new Film(1, "My film", "Descript",
                LocalDate.of(2021, 1, 12), 20, new HashSet<Integer>());
    }

    @Test
    public void twoFilmsInList() {
        filmController.postFilm(film);
        Film film2 = new Film(1, "My film", "Descript",
                LocalDate.of(2021, 1, 12), 20, new HashSet<Integer>());
        try {
            filmController.postFilm(film2);
            Assertions.assertEquals(2, filmController.getFilms().size());
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void longDescription() {
        film.setDescription("1234567891011121314151617181920212" +
                "2232425262728293031323334353637383940414243444546" +
                "4748495051525354555657585960616263646566676869707172737475767778798" +
                "081828384858687888990919293949596979899100101102103104105106107108109110111112" +
                "113114115116117118119120121122123124125126127128129130131132133134135136137138139140141" +
                "142143144145146147148149150151152153154155156157158159160161162163164165166167168169170171172173" +
                "174175176177178179180181182183184185186187188189190191192193194195196197198199200201");
        Assertions.assertThrows(ValidationException.class, () -> filmController.postFilm(film));
        Assertions.assertEquals(0, filmController.getFilms().size());
    }

    @Test
    public void noNameFilm() {
        film.setName("");
        Assertions.assertThrows(ValidationException.class, () -> filmController.postFilm(film));
        Assertions.assertEquals(0, filmController.getFilms().size());
    }

    @Test
    public void minusDurationFilm() {
        film.setDuration(-5);
        Assertions.assertThrows(ValidationException.class, () -> filmController.postFilm(film));
        Assertions.assertEquals(0, filmController.getFilms().size());
    }

    @Test
    public void oldReleaseDate() {
        film.setReleaseDate(LocalDate.of(1001, 1, 1));
        Assertions.assertThrows(ValidationException.class, () -> filmController.postFilm(film));
        Assertions.assertEquals(0, filmController.getFilms().size());
    }

    @Test
    public void putIncorrectFilm() {
        try {
            filmController.postFilm(film);
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
        Film film2 = new Film(3, "My film12", "Descrip12t",
                LocalDate.of(2021, 1, 12), 20, new HashSet<Integer>());
        Assertions.assertThrows(NullPointerException.class, () -> filmController.putFilm(film2));
        Assertions.assertEquals(1, filmController.getFilms().size());
    }
}
