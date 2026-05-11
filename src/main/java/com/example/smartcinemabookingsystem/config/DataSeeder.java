package com.example.smartcinemabookingsystem.config;

import com.example.smartcinemabookingsystem.model.*;
import com.example.smartcinemabookingsystem.repository.*;
import com.example.smartcinemabookingsystem.service.ShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatRepository seatRepository;
    private final ShowtimeService showtimeService;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ chạy seedData nếu chưa có dữ liệu nào (ví dụ: genreRepository.count() == 0)
        // Điều này giúp tránh việc seed lại dữ liệu mỗi khi ứng dụng khởi động
        if (genreRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        // --- Cảnh báo: Các dòng dưới đây chỉ nên được uncomment trong môi trường phát triển
        // --- để xóa sạch dữ liệu cũ trước khi seed lại.
        // --- KHÔNG NÊN sử dụng trong môi trường sản phẩm mà không có chiến lược sao lưu.
        // showtimeRepository.deleteAll();
        // ticketRepository.deleteAll();
        // bookingRepository.deleteAll();
        // seatRepository.deleteAll();
        // roomRepository.deleteAll();
        // movieRepository.deleteAll();
        // genreRepository.deleteAll();
        // userRepository.deleteAll();


        // Genres
        Genre action = genreRepository.save(new Genre(null, "Hành động"));
        Genre comedy = genreRepository.save(new Genre(null, "Hài hước"));
        Genre drama = genreRepository.save(new Genre(null, "Chính kịch"));
        Genre horror = genreRepository.save(new Genre(null, "Kinh dị"));
        Genre animation = genreRepository.save(new Genre(null, "Hoạt hình"));
        Genre scifi = genreRepository.save(new Genre(null, "Khoa học viễn tưởng"));
        Genre romance = genreRepository.save(new Genre(null, "Lãng mạn"));
        Genre adventure = genreRepository.save(new Genre(null, "Phiêu lưu"));

        // Rooms
        Room room1 = roomRepository.save(new Room(null, "Phòng 01", 50, null));
        Room room2 = roomRepository.save(new Room(null, "Phòng 02", 30, null));
        Room room3 = roomRepository.save(new Room(null, "Phòng 03", 40, null));


        // Seats for Room 1 (50 seats: A1-A10, B1-B10, C1-C10, D1-D10, E1-E10)
        for (char row = 'A'; row <= 'E'; row++) { // 5 rows
            for (int i = 1; i <= 10; i++) { // 10 seats per row
                seatRepository.save(new Seat(null, row + String.valueOf(i), room1));
            }
        }
        // Seats for Room 2 (30 seats: A1-A6, B1-B6, C1-C6, D1-D6, E1-E6)
        for (char row = 'A'; row <= 'E'; row++) { // 5 rows
            for (int i = 1; i <= 6; i++) { // 6 seats per row
                seatRepository.save(new Seat(null, row + String.valueOf(i), room2));
            }
        }
        // Seats for Room 3 (40 seats: A1-A8, B1-B8, C1-C8, D1-D8, E1-E8)
        for (char row = 'A'; row <= 'E'; row++) { // 5 rows
            for (int i = 1; i <= 8; i++) { // 8 seats per row
                seatRepository.save(new Seat(null, row + String.valueOf(i), room3));
            }
        }


        // Movies
        Movie movie1 = createMovie("John Wick: Chapter 4", "John Wick discovers a path to defeating The High Table.", 169, "https://image.tmdb.org/t/p/original/h8gH9u9GTvS6SAt9T4uGvTSadjw.jpg", new HashSet<>(Arrays.asList(action)));
        Movie movie2 = createMovie("Doraemon: Nobita và vùng đất lý tưởng trên bầu trời", "Nobita và các bạn lên đường tìm kiếm Utopia.", 107, "https://m.media-amazon.com/images/M/MV5BMmM2N2ZlYjAtNjE3Ny00NDVmLTk0OTUtZTI2N2I2Nzk1ZDEyXkEyXkFqcGdeQXVyMTU2NTQxMTY3._V1_.jpg", new HashSet<>(Arrays.asList(comedy, animation)));
        Movie movie3 = createMovie("Oppenheimer", "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.", 180, "https://image.tmdb.org/t/p/original/ptM0QDQz0o6w12000000000000000000.jpg", new HashSet<>(Arrays.asList(drama, scifi)));
        Movie movie4 = createMovie("Barbie", "Barbie and Ken are having the time of their lives in the colorful and seemingly perfect world of Barbie Land.", 114, "https://image.tmdb.org/t/p/original/iuFNMS8U5s6Y82000000000000000000.jpg", new HashSet<>(Arrays.asList(comedy, romance)));
        Movie movie5 = createMovie("Spider-Man: Across the Spider-Verse", "Miles Morales catapults across the Multiverse, where he encounters a team of Spider-People charged with protecting its very existence.", 140, "https://image.tmdb.org/t/p/original/fC500000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action, animation, scifi)));
        Movie movie6 = createMovie("The Little Mermaid", "A young mermaid makes a deal with a sea witch to trade her beautiful voice for human legs so she can discover the world above water.", 135, "https://image.tmdb.org/t/p/original/w0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(romance, drama)));
        Movie movie7 = createMovie("Mission: Impossible - Dead Reckoning Part One", "Ethan Hunt and his IMF team embark on their most dangerous mission yet.", 163, "https://image.tmdb.org/t/p/original/z0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action)));
        Movie movie8 = createMovie("Guardians of the Galaxy Vol. 3", "Still reeling from the loss of Gamora, Peter Quill rallies his team to defend the universe and one of their own.", 150, "https://image.tmdb.org/t/p/original/r0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action, scifi, comedy)));
        Movie movie9 = createMovie("Elemental", "Follows Ember and Wade, in a city where fire, water, land and air residents live together.", 101, "https://image.tmdb.org/t/p/original/40000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(animation, comedy, romance)));
        Movie movie10 = createMovie("Fast X", "Dom Toretto and his family are targeted by the vengeful son of drug kingpin Hernan Reyes.", 141, "https://image.tmdb.org/t/p/original/fi0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action)));
        Movie movie11 = createMovie("The Flash", "Barry Allen uses his super speed to change the past, but his attempt to save his family creates a world without superheroes.", 144, "https://image.tmdb.org/t/p/original/q0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action, scifi)));
        Movie movie12 = createMovie("Indiana Jones and the Dial of Destiny", "Archaeologist Indiana Jones races against time to retrieve a legendary dial that can change the course of history.", 154, "https://image.tmdb.org/t/p/original/e0000000000000000000000000000000.jpg", new HashSet<>(Arrays.asList(action, adventure)));


        // Showtimes
        showtimeService.saveShowtime(new Showtime(null, movie1, room1,
                LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(5), 75000, null));

        showtimeService.saveShowtime(new Showtime(null, movie2, room2,
                LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), 60000, null));

        showtimeService.saveShowtime(new Showtime(null, movie3, room3,
                LocalDateTime.now().plusHours(3), LocalDateTime.now().plusHours(6), 80000, null));

        showtimeService.saveShowtime(new Showtime(null, movie4, room1,
                LocalDateTime.now().plusHours(5).plusMinutes(15), LocalDateTime.now().plusHours(6).plusMinutes(30), 70000, null));

        showtimeService.saveShowtime(new Showtime(null, movie5, room2,
                LocalDateTime.now().plusHours(5), LocalDateTime.now().plusHours(7).plusMinutes(30), 75000, null));

        showtimeService.saveShowtime(new Showtime(null, movie6, room3,
                LocalDateTime.now().plusHours(6).plusMinutes(30), LocalDateTime.now().plusHours(8).plusMinutes(30), 65000, null));

        showtimeService.saveShowtime(new Showtime(null, movie7, room1,
                LocalDateTime.now().plusHours(7).plusMinutes(30), LocalDateTime.now().plusHours(10), 85000, null));

        showtimeService.saveShowtime(new Showtime(null, movie8, room2,
                LocalDateTime.now().plusHours(8), LocalDateTime.now().plusHours(10).plusMinutes(30), 70000, null));

        showtimeService.saveShowtime(new Showtime(null, movie9, room3,
                LocalDateTime.now().plusHours(9), LocalDateTime.now().plusHours(11), 60000, null));

        showtimeService.saveShowtime(new Showtime(null, movie10, room1,
                LocalDateTime.now().plusHours(10).plusMinutes(45), LocalDateTime.now().plusHours(12).plusMinutes(30), 75000, null));

        showtimeService.saveShowtime(new Showtime(null, movie11, room2,
                LocalDateTime.now().plusHours(11), LocalDateTime.now().plusHours(13).plusMinutes(30), 80000, null));

        showtimeService.saveShowtime(new Showtime(null, movie12, room3,
                LocalDateTime.now().plusHours(12), LocalDateTime.now().plusHours(14).plusMinutes(30), 70000, null));


        // Admin User (Password is 'admin' - ideally should be hashed)
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setEmail("admin@smartcinema.com");
        admin.setFullName("System Admin");
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);
    }

    private Movie createMovie(String title, String description, int duration, String posterUrl, Set<Genre> genres) {
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDuration(duration);
        movie.setPosterUrl(posterUrl);
        movie.setGenres(genres);
        return movieRepository.save(movie);
    }
}
