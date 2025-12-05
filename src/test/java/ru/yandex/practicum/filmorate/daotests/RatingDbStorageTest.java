package ru.yandex.practicum.filmorate.daotests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.RatingDbStorage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RatingDbStorageTest {

    private final RatingDbStorage ratingDbStorage;

    @Test
    void testFindRatingById() {
        Optional<MpaRating> ratingOptional = ratingDbStorage.findById(1);

        assertThat(ratingOptional)
                .isPresent()
                .hasValueSatisfying(rating ->
                        assertThat(rating.getId()).isEqualTo(1)
                );
    }

    @Test
    void testFindAllRatings() {
        var list = ratingDbStorage.findAll();

        assertThat(list).hasSize(5);
        assertThat(list.get(0).getId()).isEqualTo(1);
        assertThat(list.get(4).getId()).isEqualTo(5);
    }
}

