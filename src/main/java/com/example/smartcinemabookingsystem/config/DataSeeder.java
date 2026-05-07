package com.example.smartcinemabookingsystem.config;

import com.example.smartcinemabookingsystem.model.*;
import com.example.smartcinemabookingsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeRepository showtimeRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (genreRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // Genres
        Genre action = genreRepository.save(new Genre(null, "Hành động"));
        Genre comedy = genreRepository.save(new Genre(null, "Hài hước"));
        Genre drama = genreRepository.save(new Genre(null, "Chính kịch"));
        Genre horror = genreRepository.save(new Genre(null, "Kinh dị"));

        // Rooms
        Room room1 = roomRepository.save(new Room(null, "Phòng 01", 50, null));
        Room room2 = roomRepository.save(new Room(null, "Phòng 02", 30, null));

        // Seats for Room 1
        for (int i = 1; i <= 5; i++) {
            for (char row = 'A'; row <= 'J'; row++) {
                seatRepository.save(new Seat(null, row + String.valueOf(i), room1));
            }
        }

        // Movies
        Movie movie1 = new Movie();
        movie1.setTitle("John Wick: Chapter 4");
        movie1.setDescription("John Wick discovers a path to defeating The High Table.");
        movie1.setDuration(169);
        movie1.setPosterUrl("https://image.tmdb.org/t/p/original/h8gH9u7GTvS6SAt9T4uGvTSadjw.jpg");
        movie1.setGenres(new HashSet<>(Arrays.asList(action)));
        movieRepository.save(movie1);

        Movie movie2 = new Movie();
        movie2.setTitle("Doraemon: Nobita và vùng đất lý tưởng trên bầu trời");
        movie2.setDescription("Nobita và các bạn lên đường tìm kiếm Utopia.");
        movie2.setDuration(107);
        movie2.setPosterUrl("https://m.media-amazon.com/images/M/MV5BMmM2N2ZlYjAtNjE3Ny00NDVmLTk0OTUtZTI2N2I2Nzk1ZDEyXkEyXkFqcGdeQXVyMTU2NTQxMTY3._V1_.jpg");
        movie2.setGenres(new HashSet<>(Arrays.asList(comedy, drama)));
        movieRepository.save(movie2);

        // Showtimes
        showtimeRepository.save(new Showtime(null, movie1, room1, 
            LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(5), 75000));
        
        showtimeRepository.save(new Showtime(null, movie2, room2, 
            LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), 60000));

        // Admin User (Password is 'admin' - ideally should be hashed)
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setEmail("admin@smartcinema.com");
        admin.setFullName("System Admin");
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
    }
}
