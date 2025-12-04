package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserRateApplicationTests {
    UserController controller;
    UserService userService;
    UserStorage userStorage;
    User user;

    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        controller = new UserController(userService);
        user = new User();
        user.setEmail("dxdkymis@mail.ru");
        user.setLogin("alpha");
        user.setName("Alpha");
        user.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldReturnValidationExceptionWithEmptyAndWithSpacesLogin() {
        user.setLogin("");
        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(e.getMessage().contains("Логин не может быть пустым"));

        user.setLogin("login name surname");
        e = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(e.getMessage().contains("содержать пробелы"));
    }

    @Test
    void shouldReturnValidationExceptionWithDateBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(500));
        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(e.getMessage().contains("Дата рождения не может быть в будущем."));
    }

    @Test
    void shouldReturnValidationExceptionWithWrongEmail() {
        user.setEmail(null);
        ValidationException e = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(e.getMessage().contains("Электронная почта не может быть пустой и должна содержать символ @"));

        // без @
        user.setEmail("123");
        e = assertThrows(ValidationException.class, () -> controller.create(user));
        assertTrue(e.getMessage().contains("Электронная почта не может быть пустой и должна содержать символ @"));
    }

    @Test
    void shouldReturnValidationAndNotFoundExceptionWithNotFoundId() {
        user.setId(null);
        ValidationException e = assertThrows(ValidationException.class, () -> controller.update(user));
        assertTrue(e.getMessage().contains("Некорректный"));

        user.setId(11111L);
        NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> controller.update(user));
        assertTrue(notFoundException.getMessage().contains("не найден"));
    }

    @Test
    void shouldReturnValidationExceptionWithNullFields() {
        User user = new User();
        user.setEmail(null);
        user.setId(null);
        user.setLogin(null);
        user.setName(null);
        user.setBirthday(null);
        assertThrows(ValidationException.class, () -> controller.create(user));
    }

    @Test
    void shouldSuccessfulCreateUser() {
        User user = controller.create(this.user);
        assertNotNull(user.getId());
        assertTrue(user.getId() > 0);
        assertEquals(user.getEmail(),"dxdkymis@mail.ru");
        assertEquals(user.getLogin(), "alpha");
        assertEquals(user.getName(), "Alpha");

        assertEquals(user.getBirthday(), LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldSuccessfulUpdateChangeFields() {
        // здесь сделай правильный апдейт юзера через контроллер и его метод update
        User user = controller.create(this.user);
        user.setEmail("123@mail.ru");
        user.setName("12345");
        user.setLogin("qwerty");
        user.setBirthday(LocalDate.of(1997, 12, 2));

        User updateUser = controller.update(user);
        // id не изменился
        assertEquals(user.getId(), updateUser.getId());

        assertEquals(updateUser.getEmail(), "123@mail.ru");
        assertEquals(updateUser.getBirthday(), LocalDate.of(1997, 12, 2));
        assertEquals(updateUser.getName(), "12345");
        assertEquals(updateUser.getLogin(), "qwerty");
    }

    @Test
    void shouldEmptyLoginWithEmptyName() {
        user.setName("");
        User user = controller.create(this.user);
        assertEquals(user.getName(), user.getLogin());
    }

    @Test
    void shouldSuccessfulWithInstantBirthday() {
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> controller.create(user));
    }
}
