package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.rowmappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Qualifier("filmsDb")
@Component
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    protected final FilmRowMapper mapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public Film create(Film film) {
        // для того чтобы достать сгенерированный ключ
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        String insertTableQuery = "INSERT INTO films(name, description, release_date, duration, rating_id)" +
                " VALUES (?, ?, ?, ?, ?) ";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    insertTableQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getRatingId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        String updateTableQuery = "UPDATE films SET name = ?, description = ?, release_date = ?," +
                " duration = ?, rating_id = ? WHERE film_id = ?";
        jdbcTemplate.update(updateTableQuery,
            newFilm.getName(),
            newFilm.getDescription(),
            Date.valueOf(newFilm.getReleaseDate()),
            newFilm.getDuration(),
            newFilm.getRatingId(),
            newFilm.getId()
        );
        // удаляем старые жанры и добавляем новые, т.к. UPDATE сложнее (нужно сравнивать списки)
        deleteGenres(newFilm.getId());
        saveGenres(newFilm);
        return newFilm;
    }

    @Override
    public void delete(Long filmId) {
        deleteGenres(filmId);
        deleteAllLikes(filmId);
        String deleteTableQuery = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(deleteTableQuery, filmId);
    }

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbcTemplate.query("SELECT * FROM films", mapper);
        films.forEach(this::loadGenres);
        films.forEach(this::loadLikes);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film result = jdbcTemplate.queryForObject(
                    "SELECT * FROM films WHERE film_id = ?", mapper, id);
            if (result != null) {
                loadLikes(result);
                loadGenres(result); // отдаем и жанры вместе с фильмом, если фильм нашелся успешно
            }
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    public void addLike(Long filmId, Long userId) {
        String insertGenreQuery = "INSERT INTO likes(film_id, user_id) VALUES(?, ?)";
        // сохраняем у фильма лайк пользователя
        jdbcTemplate.update(insertGenreQuery, filmId, userId);
    }

    // Для удаления конкретного лайка (из контроллера)
    public void removeLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        // удаляем конкретный лайк у пользователя
        jdbcTemplate.update(query, filmId, userId);
    }

    // Для удаления всех лайков фильма (приватный, для delete)
    private void deleteAllLikes(Long filmId) {
        String query = "DELETE FROM likes WHERE film_id = ?";
        jdbcTemplate.update(query, filmId);
    }

    private void loadLikes(Film film) {
        String query = "SELECT user_id FROM likes WHERE film_id = ?";
        Set<Long> likes = new HashSet<>(
                jdbcTemplate.queryForList(query, Long.class, film.getId())
        );
        film.setLikes(likes);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty())
            return;
        String insertGenreQuery = "INSERT INTO film_genres(film_id, genre_id) VALUES(?, ?)";
        // сохраняем у каждого id жанры
        film.getGenres().forEach(genreId ->
                jdbcTemplate.update(insertGenreQuery, film.getId(), genreId));
    }

    private void loadGenres(Film film) {
        String loadQuery = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        Set<Integer> genres = new HashSet<>(
                jdbcTemplate.queryForList(loadQuery, Integer.class, film.getId())
        );
        film.setGenres(genres);
    }

    private void deleteGenres(long filmId) {
        String deleteQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteQuery, filmId);
    }
}
