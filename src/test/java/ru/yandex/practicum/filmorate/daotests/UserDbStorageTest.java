package ru.yandex.practicum.filmorate.daotests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.rowmappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Test
    void testCreateAndFindById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();

        Optional<User> found = userStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void testFindAll() {
        Collection<User> users = userStorage.findAll();
        assertThat(users).isNotNull();
    }

    @Test
    void testUpdate() {
        User user = new User();
        user.setEmail("original@test.com");
        user.setLogin("original");
        user.setName("Original Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);

        created.setName("Updated Name");
        userStorage.update(created);

        Optional<User> updated = userStorage.findById(created.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDelete() {
        User user = new User();
        user.setEmail("delete@test.com");
        user.setLogin("delete");
        user.setName("To Delete");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.create(user);
        Long id = created.getId();

        userStorage.delete(id);

        Optional<User> deleted = userStorage.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void testAddAndRemoveFriend() {
        // Создаём двух пользователей
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        // Добавляем в друзья
        userStorage.addFriend(created1.getId(), created2.getId());

        // Загружаем друзей
        userStorage.loadFriends(created1);
        assertThat(created1.getFriends()).contains(created2.getId());

        // Удаляем из друзей
        userStorage.removeFriend(created1.getId(), created2.getId());

        // Проверяем
        created1.getFriends().clear();
        userStorage.loadFriends(created1);
        assertThat(created1.getFriends()).doesNotContain(created2.getId());
    }
}
