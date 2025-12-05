package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User newUser);

    void delete(Long id);

    Collection<User> findAll();

    Optional<User> findById(Long id);

    void addFriend(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    void loadFriends(User user);

    Collection<User> getFriends(Long userId);

    Collection<User> getCommonFriends(Long userId, Long otherId);
}
