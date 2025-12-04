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
        if (!users.containsKey(newUser.getId()))
            throw new NotFoundException("id не найден");
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

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId).orElseThrow();
        User friend = findById(friendId).orElseThrow();
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId).orElseThrow();
        User friend = findById(friendId).orElseThrow();
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public void loadFriends(User user) {

    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId).orElseThrow();
        User other = findById(otherId).orElseThrow();
        Set<Long> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());
        return common.stream()
                .map(id -> findById(id).orElseThrow())
                .toList();
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));

        return user.getFriends().stream()
                .map(friendId -> findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + friendId)))
                .toList();
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
