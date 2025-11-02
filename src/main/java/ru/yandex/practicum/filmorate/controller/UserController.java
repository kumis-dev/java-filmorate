package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank())
            user.setName(user.getLogin());
        // формируем дополнительные данные
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь id={}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null || !users.containsKey(newUser.getId())) {
            log.warn("Ошибка валидации пользователя");
            throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        validate(newUser);
        User oldUser = users.get(newUser.getId());
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank())
            oldUser.setEmail(newUser.getEmail());
        // если юзер найден и все условия соблюдены, обновляем его содержимое
        // обновляем поля по отдельности чтобы ничего не ломалось
        if (newUser.getLogin() != null)
            oldUser.setLogin(newUser.getLogin());
        if (newUser.getName() != null) {
            if (newUser.getName().isBlank()) {
                oldUser.setName(oldUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }
        }
        if (newUser.getBirthday() != null)
            oldUser.setBirthday(newUser.getBirthday());
        log.info("Обновлён пользователь id={}", newUser.getId());
        return oldUser;
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(User user) {
        // проверяем выполнение необходимых условий
        if (user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@"))
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank())
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now()))
            throw new ValidationException("Дата рождения не может быть в будущем.");
    }
}
