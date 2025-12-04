package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.dto.user.NewUserRequest;
import ru.yandex.practicum.filmorate.dal.dto.user.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dal.dto.user.UserDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDb") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<UserDto> findAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    public UserDto create(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);
        validateUser(user);
        User saved = userStorage.create(user);
        return UserMapper.mapToUserDto(saved);
    }

    public UserDto update(UpdateUserRequest request) {
        User existing = userStorage.findById(request.getId())
                .orElseThrow(NotFoundException::new);

        User updated = UserMapper.updateUserFields(existing, request);
        validateUser(updated);

        updated = userStorage.update(updated);
        return UserMapper.mapToUserDto(updated);
    }

    public UserDto findById(Long id) {
        User user = userStorage.findById(id)
                .orElseThrow(NotFoundException::new);

        userStorage.loadFriends(user);

        return UserMapper.mapToUserDto(user);
    }


    public void delete(Long id) {
        userStorage.delete(id);
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId))
            throw new ValidationException("Нельзя добавить себя в друзья");

        userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        userStorage.findById(friendId).orElseThrow(() -> notFoundFriend(friendId));

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        userStorage.findById(friendId).orElseThrow(() -> notFoundFriend(friendId));

        userStorage.removeFriend(userId, friendId);
    }

    public List<UserDto> getAllFriends(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> notFoundUser(userId));

        userStorage.loadFriends(user);

        return user.getFriends().stream()
                .map(id -> {
                    User friend = userStorage.findById(id)
                            .orElseThrow(() -> notFoundUser(id));
                    userStorage.loadFriends(friend);
                    return UserMapper.mapToUserDto(friend);
                })
                .toList();

    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        // сначала проверяем, что оба пользователя существуют
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        userStorage.findById(otherUserId).orElseThrow(() -> new NotFoundException("User not found"));

        Collection<User> commonFriends = userStorage.getCommonFriends(userId, otherUserId);

        if (commonFriends.isEmpty()) {
            throw new InternalError("No common friends"); // или любое RuntimeException
        }

        return commonFriends;
    }



    private NotFoundException notFoundUser(Long id) {
        return new NotFoundException("Пользователь не найден: " + id);
    }

    private NotFoundException notFoundFriend(Long id) {
        return new NotFoundException("Друг не найден: " + id);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Некорректный логин");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Некорректная дата рождения");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

