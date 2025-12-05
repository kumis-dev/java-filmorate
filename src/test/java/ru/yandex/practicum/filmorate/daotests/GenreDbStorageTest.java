package ru.yandex.practicum.filmorate.daotests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.rowmappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void testFindAll() {
        Collection<Genre> genres = genreStorage.findAll();
        assertThat(genres).isNotNull();
        assertThat(genres).isNotEmpty();
    }

    @Test
    void testFindById() {
        Optional<Genre> genre = genreStorage.findById(1);
        assertThat(genre).isPresent();
    }

    @Test
    void testFindByIdNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);
        assertThat(genre).isEmpty();
    }
}
