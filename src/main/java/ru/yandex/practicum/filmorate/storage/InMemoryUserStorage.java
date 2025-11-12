package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        validate(user);
        if (user.getName() == null || user.getName().isBlank())
            user.setName(user.getLogin());
        // формируем дополнительные данные
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь id={}", user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null || newUser.getId() <= 0) {
            log.warn("Ошибка валидации пользователя: некорректный id={}", newUser.getId());
            throw new ValidationException("id должен быть > 0");
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

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
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
