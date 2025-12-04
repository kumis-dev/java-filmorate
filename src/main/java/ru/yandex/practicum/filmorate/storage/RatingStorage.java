package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

public interface RatingStorage {
    List<MpaRating> findAll();
    Optional<MpaRating> findById(int id);
}
