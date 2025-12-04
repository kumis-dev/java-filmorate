package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.rowmappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RatingDbStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper mapper;

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query("SELECT * FROM rating ORDER BY rating_id", mapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        List<MpaRating> result = jdbcTemplate.query(
                "SELECT * FROM rating WHERE rating_id = ?",
                mapper,
                id
        );
        return result.stream().findFirst();
    }
}
