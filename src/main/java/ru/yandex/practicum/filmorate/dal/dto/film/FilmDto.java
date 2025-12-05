package ru.yandex.practicum.filmorate.dal.dto.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.yandex.practicum.filmorate.dal.dto.genre.GenreDto;
import ru.yandex.practicum.filmorate.dal.dto.mpa.MpaDto;
import java.time.LocalDate;
import java.util.Set;

@Data
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    @JsonProperty("mpa")
    private MpaDto mpa;

    private Set<GenreDto> genres;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer likes;
}

