package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dal.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dal.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.RatingDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmService(@Qualifier("filmsDb") FilmStorage filmStorage,
                       @Qualifier("userDb") UserStorage userStorage,
                       RatingDbStorage mpaStorage,
                       GenreDbStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    // Вспомогательный метод для маппинга с именами
    private FilmDto toDto(Film film) {
        List<MpaRating> allMpa = mpaStorage.findAll();
        Collection<Genre> allGenres = genreStorage.findAll();
        return FilmMapper.mapToFilmDto(film, allMpa, allGenres);
    }

    public Collection<FilmDto> findAll() {
        List<MpaRating> allMpa = mpaStorage.findAll();
        Collection<Genre> allGenres = genreStorage.findAll();

        return filmStorage.findAll().stream()
                .map(film -> FilmMapper.mapToFilmDto(film, allMpa, allGenres))
                .toList();
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        validateFilm(film);
        Film saved = filmStorage.create(film);
        return toDto(saved);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film film = filmStorage.findById(request.getId())
                .orElseThrow(NotFoundException::new);

        Film updated = FilmMapper.updateFilmFields(film, request);
        validateFilm(updated);
        updated = filmStorage.update(updated);

        return toDto(updated);
    }

    public FilmDto findById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(NotFoundException::new);
        return toDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId).orElseThrow(NotFoundException::new);
        userStorage.findById(userId).orElseThrow(NotFoundException::new);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findById(filmId).orElseThrow(NotFoundException::new);
        userStorage.findById(userId).orElseThrow(NotFoundException::new);
        filmStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopular(int count) {
        List<MpaRating> allMpa = mpaStorage.findAll();
        Collection<Genre> allGenres = genreStorage.findAll();

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .map(film -> FilmMapper.mapToFilmDto(film, allMpa, allGenres))
                .toList();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Описание фильма не может превышать 200 символов");
        }

        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() != null && film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной");
        }

        // MPA — бросаем NotFoundException если не найден
        if (film.getRatingId() != null) {
            mpaStorage.findById(film.getRatingId())
                    .orElseThrow(() -> new NotFoundException("MPA rating not found: " + film.getRatingId()));
        }

        // Жанры — бросаем NotFoundException если не найден
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Integer genreId : film.getGenres()) {
                genreStorage.findById(genreId)
                        .orElseThrow(() -> new NotFoundException("Genre not found: " + genreId));
            }
        }
    }
}
