package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmorateApplicationTests {

	FilmController controller;
	Film film;

	@BeforeEach
	public void setUp() {
		controller = new FilmController();
		film = new Film();
		film.setName("movie");
		film.setDescription("description");
		film.setReleaseDate(LocalDate.of(2002, 4, 14));
		film.setDuration(Duration.ofMinutes(125));
	}

	@Test
	void shouldThrowWhenNameBlank() {
		film.setName("");
		ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
		assertTrue(e.getMessage().contains("Название не может быть пустым"));
	}

	@Test
	void shouldReturnValidationExceptionWithReleaseDateTooEarly() {
		film.setReleaseDate(LocalDate.of(1895, 12, 28).minusDays(200));
		ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
		assertTrue(e.getMessage().contains("Дата релиза — не раньше 28 декабря 1895 года"));
	}

	@Test
	void shouldReleaseDateCinemaSuccessfully() {
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		assertDoesNotThrow(() -> controller.create(film));
	}

	@Test
	void shouldReturnValidationExceptionWithLongLengthDescription() {
		film.setDescription("f".repeat(300));
		ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
		assertTrue(e.getMessage().contains("Максимальная длина описания — 200 символов"));
	}

	@Test
	void shouldReturnValidationExceptionWith0Duration() {
		film.setDuration(Duration.ofMinutes(0));
		ValidationException e = assertThrows(ValidationException.class, () -> controller.create(film));
		assertTrue(e.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
	}

	@Test
	void shouldReturnValidationExceptionWithLimitLengthDescription() {
		film.setDescription("f".repeat(200));
		assertDoesNotThrow(() -> controller.create(film));
	}

	@Test
	void shouldReturnValidationExceptionWithNotFoundId() {
		film.setId(null);
		ValidationException e = assertThrows(ValidationException.class, () -> controller.update(film));
		assertTrue(e.getMessage().contains("не найден"));

		film.setId(11111L);
		e = assertThrows(ValidationException.class, () -> controller.update(film));
		assertTrue(e.getMessage().contains("не найден"));
	}

	@Test
	void shouldReturnValidationExceptionWithNullFields() {
		Film film = new Film();
		film.setDescription(null);
		film.setId(null);
		film.setReleaseDate(null);
		film.setDuration(null);
		assertThrows(ValidationException.class, () -> controller.create(film));
	}

	@Test
	void shouldSuccessfulCreateFilm() {
		Film film = controller.create(this.film);
		assertNotNull(film.getId());
		assertTrue(film.getId() > 0);
		assertEquals(film.getDescription(),"description");
		assertEquals(film.getName(), "movie");
		assertEquals(film.getDuration(), Duration.ofMinutes(125));
		assertEquals(film.getReleaseDate(), LocalDate.of(2002, 4, 14));
	}

	@Test
	void shouldSuccessfulUpdateChangeFields() {
		// здесь сделай правильный апдейт юзера через контроллер и его метод update
		Film film = controller.create(this.film);
		film.setName("movie2");
		film.setDescription("description2");
		film.setReleaseDate(LocalDate.of(2004, 2, 11));
		film.setDuration(Duration.ofMinutes(133));

		Film updateFilm = controller.update(film);
		// id не изменился
		assertEquals(film.getId(), updateFilm.getId());

		assertEquals(updateFilm.getName(), "movie2");
		assertEquals(updateFilm.getDescription(), "description2");
		assertEquals(updateFilm.getReleaseDate(), LocalDate.of(2004, 2, 11));
		assertEquals(updateFilm.getDuration(), Duration.ofMinutes(133));
	}

	@Test
	void shouldReturnValidationExceptionWithNegativeDuration() {
		film.setDuration(Duration.ofMinutes(-2));
		assertThrows(ValidationException.class, () -> controller.create(film));
	}
}
