package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dal.dto.film.FilmDto;
import ru.yandex.practicum.filmorate.dal.dto.film.NewFilmRequest;
import ru.yandex.practicum.filmorate.dal.dto.film.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.dal.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dal.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());

        if (request.getMpa() != null && request.getMpa().getId() != null) {
            film.setRatingId(request.getMpa().getId());
        }

        if (request.getGenres() != null) {
            Set<Integer> genreIds = request.getGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toSet());
            film.setGenres(genreIds);
        }

        return film;
    }

    public static FilmDto mapToFilmDto(Film film,
                                       Collection<MpaRating> allMpa,
                                       Collection<Genre> allGenres) {
        FilmDto dto = new FilmDto();

        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());

        // MPA с именем
        if (film.getRatingId() != null) {
            MpaDto mpaDto = new MpaDto();
            mpaDto.setId(film.getRatingId());

            // Ищем имя по id
            allMpa.stream()
                    .filter(m -> m.getId() == film.getRatingId())
                    .findFirst()
                    .ifPresent(m -> mpaDto.setName(m.getName()));

            dto.setMpa(mpaDto);
        }

        // В методе mapToFilmDto, секция с жанрами:
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<GenreDto> genres = film.getGenres().stream()
                    .map(genreId -> {
                        GenreDto g = new GenreDto();
                        g.setId(genreId);

                        allGenres.stream()
                                .filter(genre -> genre.getId().equals(genreId))
                                .findFirst()
                                .ifPresent(genre -> g.setName(genre.getName()));

                        return g;
                    })
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            dto.setGenres(genres);
        }

        dto.setLikes(film.getLikes() != null ? film.getLikes().size() : 0);

        return dto;
    }

    public static Film updateFilmFields(Film film, UpdateFilmRequest request) {
        if (request.hasName()) {
            film.setName(request.getName());
        }
        if (request.hasDescription()) {
            film.setDescription(request.getDescription());
        }
        if (request.hasDuration()) {
            film.setDuration(request.getDuration());
        }
        if (request.hasReleaseDate()) {
            film.setReleaseDate(request.getReleaseDate());
        }
        if (request.hasMpa()) {
            film.setRatingId(request.getMpa().getId());
        }
        if (request.hasGenres()) {
            Set<Integer> genreIds = request.getGenres().stream()
                    .map(GenreDto::getId)
                    .collect(Collectors.toSet());
            film.setGenres(genreIds);
        }
        return film;
    }
}