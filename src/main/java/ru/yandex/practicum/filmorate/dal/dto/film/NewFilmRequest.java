package ru.yandex.practicum.filmorate.dal.dto.film;

import lombok.Data;
import ru.yandex.practicum.filmorate.dal.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dal.dto.mpa.MpaDto;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private Set<GenreDto> genres;
}

