package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        validate(film);
        // формируем дополнительные данные
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан фильм id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка валидации фильма");
            throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        if (!films.containsKey(newFilm.getId()))
            throw new NotFoundException();
        validate(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        // если фильм найден и все условия соблюдены, обновляем его содержимое
        if (newFilm.getName() != null) oldFilm.setName(newFilm.getName());
        if (newFilm.getDescription() != null) oldFilm.setDescription(newFilm.getDescription());
        if (newFilm.getReleaseDate() != null) oldFilm.setReleaseDate(newFilm.getReleaseDate());
        if (newFilm.getDuration() != null) oldFilm.setDuration(newFilm.getDuration());
        log.info("Обновлён фильм id={}", newFilm.getId());
        return oldFilm;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validate(Film film) {
        // проверяем выполнение необходимых условий
        if (film.getDescription() != null && film.getDescription().length() > 200)
            throw new ValidationException("Максимальная длина описания — 200 символов");
        if (film.getName() == null || film.getName().isBlank())
            throw new ValidationException("Название не может быть пустым");
        if (film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28)))
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        if (film.getDuration() == null || film.getDuration() <= 0)
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
    }
}
