package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.dto.mpa.MpaDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.RatingDbStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingDbStorage ratingDbStorage;

    public Collection<MpaDto> findAll() {
        return ratingDbStorage.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public MpaDto findById(Integer id) {
        MpaRating rating = ratingDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA-рейтинг с id = " + id + " не найден"));
        return toDto(rating);
    }

    private MpaDto toDto(MpaRating rating) {
        MpaDto dto = new MpaDto();
        dto.setId(rating.getId());
        dto.setName(rating.getName());
        return dto;
    }
}