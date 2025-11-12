package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    // каждый пользователь может поставить лайк фильму только один раз
    // реализуется благодаря тому что лайки хранятся в множестве Set

    public Collection<Film> findAll() {
        Collection<Film> films = filmStorage.findAll();
        log.debug("Найден список фильмов: {}", films);
        log.info("Всего фильмов: {}", films.size());
        return films;
    }

    public Film create(Film film) {
        Film createFilm = filmStorage.create(film);
        log.info("Создан фильм: {}", createFilm.getId());
        return createFilm;
    }

    public Film update(Film film) {
        // Проверяем null отдельно с нужным текстом
        if (film.getId() == null) {
            log.warn("Некорректный id: null");
            throw new ValidationException("Некорректный id: id должен быть указан");
        }

        // Проверяем существование
        if (filmStorage.findById(film.getId()).isEmpty()) {
            log.warn("Обновление фильма: не существует id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        Film updateFilm = filmStorage.update(film);
        log.info("Обновлен фильм: {}", updateFilm.getId());
        return updateFilm;
    }


    public Film findById(Long id) {
        validateId(id);
        Film findFilmById = filmStorage.findById(id).orElseThrow(() -> notFoundFilm(id));
        log.debug("Найден фильм: {}", findFilmById);
        return findFilmById;
    }

    public void delete(Long id) {
        validateId(id);
        filmStorage.delete(id);
        log.info("Фильм успешно удален");
    }

    // PUT /films/{id}/like/{userId}
    public void addLike(Long filmId, Long userId) {
        validateId(filmId);
        validateId(userId);
        Film film = filmStorage.findById(filmId).orElseThrow(() -> notFoundFilm(filmId));
        userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        // когда меняем состояние фильма (добавляем лайк),
        // это считается модификацией объекта,
        // и корректно зафиксировать её через метод модификации хранилища —
        filmStorage.update(film);
    }

    public void removeLike(Long filmId, Long userId) {
        validateId(filmId);
        validateId(userId);
        Film film = filmStorage.findById(filmId).orElseThrow(() -> notFoundFilm(filmId));
        userStorage.findById(userId).orElseThrow(() -> notFoundUser(userId));
        film.getLikes().remove(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
        filmStorage.update(film);
    }

    // GET /films/popular?count={count}
    public List<Film> getPopular(int size) {
        if (size <= 0) throw new ValidationException("count должен быть > 0");
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(
                        (Film film) -> film.getLikes().size()).reversed())
                .limit(size).toList();
    }

    private NotFoundException notFoundFilm(Long filmId) {
        log.warn("Фильм не найден с id: {}", filmId);
        return new NotFoundException();
    }

    private NotFoundException notFoundUser(Long userId) {
        log.warn("Пользователь не найден с id: {}", userId);
        return new NotFoundException();
    }

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            log.warn("Некорректный id: {}", id);
            throw new ValidationException("id должен быть > 0");
        }
    }
}
