package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {

    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность не может быть null")
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private Set<Long> likes = new HashSet<>();

    // LinkedHashSet сохраняет порядок добавления
    private Set<Integer> genres = new LinkedHashSet<>();

    private Integer ratingId;
}