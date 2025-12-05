package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.rowmappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Qualifier("userDb")
@Component
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;

    public UserDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public User create(User user) {
        // для того чтобы достать сгенерированный ключ
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String insertTableQuery = "INSERT INTO users(name, login, birthday, email)" +
                " VALUES (?, ?, ?, ?) ";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    insertTableQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setDate(3, Date.valueOf(user.getBirthday()));
            ps.setString(4, user.getEmail());
            return ps;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    @Override
    public User update(User newUser) {
        String updateTableQuery = "UPDATE users SET name = ?, login = ?, birthday = ?, email = ?" +
                " WHERE user_id = ?";
        jdbcTemplate.update(updateTableQuery,
                newUser.getName(),
                newUser.getLogin(),
                Date.valueOf(newUser.getBirthday()),
                newUser.getEmail(),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public void delete(Long id) {
        String deleteTableQuery = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(deleteTableQuery, id);
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users", mapper);
        users.forEach(this::loadFriends);
        return users;
    }


    @Override
    public Optional<User> findById(Long id) {
        try {
            User result = jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE user_id = ?",
                    mapper,
                    id
            );

            loadFriends(result);

            return Optional.of(result);

        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }


    public void addFriend(Long userId, Long friendId) {
        String insertGenreQuery = "INSERT INTO friends(user_id, friend_id, friendship_status) VALUES(?, ?, ?)";
        // добавляем друга юзеру (односторонне)
        jdbcTemplate.update(insertGenreQuery, userId, friendId, "UNCONFIRMED");
    }

    public void removeFriend(Long userId, Long friendId) {
        String deleteGenreQuery = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(deleteGenreQuery, userId, friendId);
    }

    public void loadFriends(User user) {
        String query = "SELECT friend_id FROM friends WHERE user_id = ?";
        Set<Long> friends = new HashSet<>(
                jdbcTemplate.queryForList(query, Long.class, user.getId())
        );
        user.setFriends(friends);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String query = "SELECT u.* FROM users u " +
                "INNER JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(query, mapper, userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        String query = "SELECT u.* FROM users u " +
                "WHERE u.user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)" +
                " AND u.user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
        Collection<User> commonFriends = jdbcTemplate.query(query, mapper, userId, friendId);
        return commonFriends;
    }
}
