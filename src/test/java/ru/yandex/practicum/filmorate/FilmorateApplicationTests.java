package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmorateApplicationTests {

	InMemoryFilmStorage filmStorage;
	Film film;

	@BeforeEach
	public void setUp() {
		filmStorage = new InMemoryFilmStorage();
		film = new Film();
		film.setName("movie");
		film.setDescription("description");
		film.setReleaseDate(LocalDate.of(2002, 4, 14));
		film.setDuration(125);
	}

	private void validateFilm(Film film) {
		if (film.getName() == null || film.getName().isBlank()) {
			throw new ValidationException("Название не может быть пустым");
		}
		if (film.getDescription() != null && film.getDescription().length() > 200) {
			throw new ValidationException("Максимальная длина описания — 200 символов");
		}
		if (film.getReleaseDate() != null &&
				film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
			throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
		}
		if (film.getDuration() == null || film.getDuration() <= 0) {
			throw new ValidationException("Продолжительность фильма должна быть положительным числом");
		}
	}

	private Film createFilm(Film film) {
		validateFilm(film);
		return filmStorage.create(film);
	}

	private Film updateFilm(Film film) {
		if (film.getId() == null) {
			throw new ValidationException("Некорректный id");
		}
		if (filmStorage.findById(film.getId()).isEmpty()) {
			throw new ValidationException("Фильм с id " + film.getId() + " не найден");
		}
		validateFilm(film);
		return filmStorage.update(film);
	}

	@Test
	void shouldThrowWhenNameBlank() {
		film.setName("");
		ValidationException e = assertThrows(ValidationException.class, () -> createFilm(film));
		assertTrue(e.getMessage().contains("Название не может быть пустым"));
	}

	@Test
	void shouldReturnValidationExceptionWithReleaseDateTooEarly() {
		film.setReleaseDate(LocalDate.of(1895, 12, 28).minusDays(200));
		ValidationException e = assertThrows(ValidationException.class, () -> createFilm(film));
		assertTrue(e.getMessage().contains("Дата релиза — не раньше 28 декабря 1895 года"));
	}

	@Test
	void shouldReleaseDateCinemaSuccessfully() {
		film.setReleaseDate(LocalDate.of(1895, 12, 28));
		assertDoesNotThrow(() -> createFilm(film));
	}

	@Test
	void shouldReturnValidationExceptionWithLongLengthDescription() {
		film.setDescription("f".repeat(300));
		ValidationException e = assertThrows(ValidationException.class, () -> createFilm(film));
		assertTrue(e.getMessage().contains("Максимальная длина описания — 200 символов"));
	}

	@Test
	void shouldReturnValidationExceptionWith0Duration() {
		film.setDuration(0);
		ValidationException e = assertThrows(ValidationException.class, () -> createFilm(film));
		assertTrue(e.getMessage().contains("Продолжительность фильма должна быть положительным числом"));
	}

	@Test
	void shouldReturnValidationExceptionWithLimitLengthDescription() {
		film.setDescription("f".repeat(200));
		assertDoesNotThrow(() -> createFilm(film));
	}

	@Test
	void shouldReturnValidationAndNotFoundExceptionWithNotFoundId() {
		film.setId(null);
		ValidationException e = assertThrows(ValidationException.class, () -> updateFilm(film));
		assertTrue(e.getMessage().contains("Некорректный id"));

		film.setId(11111L);
		e = assertThrows(ValidationException.class, () -> updateFilm(film));
		assertTrue(e.getMessage().contains("не найден"));
	}

	@Test
	void shouldReturnValidationExceptionWithNullFields() {
		Film film = new Film();
		film.setDescription(null);
		film.setId(null);
		film.setReleaseDate(null);
		film.setDuration(null);
		assertThrows(ValidationException.class, () -> createFilm(film));
	}

	@Test
	void shouldSuccessfulCreateFilm() {
		Film created = createFilm(this.film);
		assertNotNull(created.getId());
		assertTrue(created.getId() > 0);
		assertEquals("description", created.getDescription());
		assertEquals("movie", created.getName());
		assertEquals(125, created.getDuration());
		assertEquals(LocalDate.of(2002, 4, 14), created.getReleaseDate());
	}

	@Test
	void shouldSuccessfulUpdateChangeFields() {
		Film created = createFilm(this.film);
		created.setName("movie2");
		created.setDescription("description2");
		created.setReleaseDate(LocalDate.of(2004, 2, 11));
		created.setDuration(133);

		Film updated = updateFilm(created);

		assertEquals(created.getId(), updated.getId());
		assertEquals("movie2", updated.getName());
		assertEquals("description2", updated.getDescription());
		assertEquals(LocalDate.of(2004, 2, 11), updated.getReleaseDate());
		assertEquals(133, updated.getDuration());
	}

	@Test
	void shouldReturnValidationExceptionWithNegativeDuration() {
		film.setDuration(-2);
		assertThrows(ValidationException.class, () -> createFilm(film));
	}
}