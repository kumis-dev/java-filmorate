package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UpdateTargetNotFoundValidationException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> findAll() {
        Collection<User> users = userStorage.findAll();
        log.debug("Найден список пользователей {}: ", users);
        log.info("Всего пользователей: {}", users.size());
        return users;
    }

    public User create(User user) {
        User createUser = userStorage.create(user);
        log.info("Создан пользователь с id {}: ", createUser.getId());
        return createUser;
    }

    public User update(User user) {
        validateId(user.getId());
        if (userStorage.findById(user.getId()).isEmpty()) {
            log.warn("Обновление пользователя: не существует id={}", user.getId());
            throw new UpdateTargetNotFoundValidationException("Пользователь с таким id не найден");
        }
        User updateUser = userStorage.update(user);
        log.info("Обновлен пользователь с id {}: ", updateUser.getId());
        return updateUser;
    }


    public User findById(Long id) {
        validateId(id);
        User findUserById = userStorage.findById(id).orElseThrow(() -> notFoundUser(id));
        log.debug("Найден пользователь: {}", findUserById);
        return findUserById;
    }

    public void delete(Long id) {
        validateId(id);
        userStorage.delete(id);
        log.info("Пользователь успешно удален");
    }

    // PUT /users/{id}/friends/{friendId}
    public void addFriend(Long userId, Long friendId) {
        validateId(userId);
        validateId(friendId);
        if (userId.equals(friendId))
            throw new ValidationException("Нельзя добавить себя в друзья");

        User user = userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        User friend = userStorage.findById(friendId).orElseThrow(() ->
                notFoundFriend(friendId));
        user.getFriends().add(friendId);
        friend.getFriends().add(userId); // и пользователь, и его друг должны быть добавлены
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriend(Long userId, Long friendId) {
        validateId(userId);
        validateId(friendId);
        User user = userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        User friend = userStorage.findById(friendId).orElseThrow(() ->
                notFoundFriend(friendId));
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getAllFriends(Long userId) {
        validateId(userId);
        // сначала находим юзера по айди
        User user = userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        // затем проходимся стримом по его друзьям и собираем их в список
        return user.getFriends()
                .stream().map(id -> userStorage.findById(id)
                        .orElseThrow(() -> notFoundUser(id))).toList();
    }

    // здесь будет выводиться общий список друзей
    public List<User> getCommonFriends(Long userId, Long otherId) {
        validateId(userId);
        validateId(otherId);
        User user = userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        User friend = userStorage.findById(otherId).orElseThrow(() ->
                notFoundFriend(otherId));

        HashSet<Long> commonFriends = new HashSet<>(user.getFriends());
        // retainAll - изменяет исходное множество
        commonFriends.retainAll(friend.getFriends());

        return commonFriends.stream().map(id -> userStorage
                .findById(id).orElseThrow(() -> notFoundUser(id))).toList();
    }

    private NotFoundException notFoundUser(Long userId) {
        log.warn("Пользователь не найден с id: {}", userId);
        return new NotFoundException();
    }

    private NotFoundException notFoundFriend(Long friendId) {
        log.warn("Друг пользователя не найден, его id: {}", friendId);
        return new NotFoundException();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            log.warn("Некорректный id: {}", id);
            throw new ValidationException("id должен быть > 0");
        }
    }
}
