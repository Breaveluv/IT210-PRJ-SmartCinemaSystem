package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.Movie;
import com.example.smartcinemabookingsystem.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Import Optional

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    // Changed return type to Optional<Movie>
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public Page<Movie> getPaginatedMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(pageable);
    }

    public Page<Movie> getPaginatedMoviesWithFutureShowtimes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findMoviesWithFutureShowtimes(LocalDateTime.now(), pageable);
    }

    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
}
