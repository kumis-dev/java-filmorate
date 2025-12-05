package ru.yandex.practicum.filmorate.daotests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.rowmappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testCreateAndFindById() {
        // Создаём фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setRatingId(1);

        Film created = filmStorage.create(film);

        // Проверяем что ID был присвоен
        assertThat(created.getId()).isNotNull();

        // Находим по ID
        Optional<Film> found = filmStorage.findById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Film");
    }

    @Test
    void testFindAll() {
        Collection<Film> films = filmStorage.findAll();
        assertThat(films).isNotNull();
    }

    @Test
    void testUpdate() {
        // Создаём фильм
        Film film = new Film();
        film.setName("Original Name");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setRatingId(1);

        Film created = filmStorage.create(film);

        // Обновляем
        created.setName("Updated Name");
        filmStorage.update(created);

        // Проверяем
        Optional<Film> updated = filmStorage.findById(created.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDelete() {
        // Создаём фильм
        Film film = new Film();
        film.setName("To Delete");
        film.setDescription("Will be deleted");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setRatingId(1);

        Film created = filmStorage.create(film);
        Long id = created.getId();

        // Удаляем
        filmStorage.delete(id);

        // Проверяем что больше не существует
        Optional<Film> deleted = filmStorage.findById(id);
        assertThat(deleted).isEmpty();
    }
}
