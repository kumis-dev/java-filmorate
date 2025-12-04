package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserRateApplicationTests {

    InMemoryUserStorage userStorage;
    User user;

    @BeforeEach
    public void setUp() {
        userStorage = new InMemoryUserStorage();
        user = new User();
        user.setEmail("dxdkymis@mail.ru");
        user.setLogin("alpha");
        user.setName("Alpha");
        user.setBirthday(LocalDate.of(2000, 1, 1));
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User createUser(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    private User updateUser(User user) {
        if (user.getId() == null) {
            throw new ValidationException("Пользователь не найден");
        }
        if (userStorage.findById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        validateUser(user);
        return userStorage.update(user);
    }

    @Test
    void shouldReturnValidationExceptionWithEmptyAndWithSpacesLogin() {
        user.setLogin("");
        ValidationException e = assertThrows(ValidationException.class, () -> createUser(user));
        assertTrue(e.getMessage().contains("Логин не может быть пустым"));

        user.setLogin("login name surname");
        e = assertThrows(ValidationException.class, () -> createUser(user));
        assertTrue(e.getMessage().contains("содержать пробелы"));
    }

    @Test
    void shouldReturnValidationExceptionWithDateBirthdayInFuture() {
        user.setBirthday(LocalDate.now().plusDays(500));
        ValidationException e = assertThrows(ValidationException.class, () -> createUser(user));
        assertTrue(e.getMessage().contains("Дата рождения не может быть в будущем."));
    }

    @Test
    void shouldReturnValidationExceptionWithWrongEmail() {
        user.setEmail(null);
        ValidationException e = assertThrows(ValidationException.class, () -> createUser(user));
        assertTrue(e.getMessage().contains("Электронная почта не может быть пустой и должна содержать символ @"));

        user.setEmail("123");
        e = assertThrows(ValidationException.class, () -> createUser(user));
        assertTrue(e.getMessage().contains("Электронная почта не может быть пустой и должна содержать символ @"));
    }

    @Test
    void shouldReturnValidationAndNotFoundExceptionWithNotFoundId() {
        user.setId(null);
        ValidationException e = assertThrows(ValidationException.class, () -> updateUser(user));
        assertTrue(e.getMessage().contains("не найден"));

        user.setId(11111L);
        NotFoundException e2 = assertThrows(NotFoundException.class, () -> updateUser(user));
        assertTrue(e2.getMessage().contains("не найден"));
    }

    @Test
    void shouldReturnValidationExceptionWithNullFields() {
        User user = new User();
        user.setEmail(null);
        user.setId(null);
        user.setLogin(null);
        user.setName(null);
        user.setBirthday(null);
        assertThrows(ValidationException.class, () -> createUser(user));
    }

    @Test
    void shouldSuccessfulCreateUser() {
        User created = createUser(this.user);
        assertNotNull(created.getId());
        assertTrue(created.getId() > 0);
        assertEquals("dxdkymis@mail.ru", created.getEmail());
        assertEquals("alpha", created.getLogin());
        assertEquals("Alpha", created.getName());
        assertEquals(LocalDate.of(2000, 1, 1), created.getBirthday());
    }

    @Test
    void shouldSuccessfulUpdateChangeFields() {
        User created = createUser(this.user);
        created.setEmail("123@mail.ru");
        created.setName("12345");
        created.setLogin("qwerty");
        created.setBirthday(LocalDate.of(1997, 12, 2));

        User updated = updateUser(created);

        assertEquals(created.getId(), updated.getId());
        assertEquals("123@mail.ru", updated.getEmail());
        assertEquals(LocalDate.of(1997, 12, 2), updated.getBirthday());
        assertEquals("12345", updated.getName());
        assertEquals("qwerty", updated.getLogin());
    }

    @Test
    void shouldEmptyLoginWithEmptyName() {
        user.setName("");
        User created = createUser(this.user);
        assertEquals(created.getName(), created.getLogin());
    }

    @Test
    void shouldSuccessfulWithInstantBirthday() {
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> createUser(user));
    }
}