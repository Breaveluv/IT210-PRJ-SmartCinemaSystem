package com.example.smartcinemabookingsystem.service;

import com.example.smartcinemabookingsystem.model.Showtime;
import com.example.smartcinemabookingsystem.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;

    public List<Showtime> getShowtimesByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }
}
