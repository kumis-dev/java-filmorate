package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.rowmappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.Optional;

@Component
public class GenreDbStorage implements GenreStorage  {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper mapper;

    public GenreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    public Collection<Genre> findAll() {
        String query = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        try {
            String query = "SELECT * FROM genres WHERE genre_id = ?";
            Genre genre = jdbcTemplate.queryForObject(query, mapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}